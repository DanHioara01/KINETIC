require('dotenv').config();

const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
const admin = require('firebase-admin');
const rateLimit = require('express-rate-limit');

// int8 (bigint) → Number și numeric → Number, ca să serializăm identic cu
// vechea bază SQLite (fără stringuri pentru createdAt/lastSeen/id-uri).
require('pg').types.setTypeParser(20, (v) => (v == null ? null : parseInt(v, 10)));
require('pg').types.setTypeParser(1700, (v) => (v == null ? null : parseFloat(v)));

const app = express();
const PORT = process.env.PORT || 4242;

// =============================================
// CORS
// =============================================
const ALLOWED_ORIGINS = [
  'https://kinetic-backend-3ff6.onrender.com',
  'https://ai-server-7tqx.onrender.com',
];

app.use(cors({
  origin: function (origin, callback) {
    if (!origin || ALLOWED_ORIGINS.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  },
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
}));
app.use(express.json({ limit: '512kb' }));

// =============================================
// RATE LIMITING
// =============================================
const globalLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 120,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again later' },
});
app.use(globalLimiter);

const authLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests from this IP, please try again later' },
});

const postLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 30,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many posts, slow down' },
});

// =============================================
// INPUT SANITIZATION
// =============================================
const MAX_STRING_LEN = 500;
const MAX_ID_LEN = 64;

function sanitizeString(str) {
  if (typeof str !== 'string') return '';
  return str.trim().substring(0, MAX_STRING_LEN)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;');
}

function sanitizeId(str) {
  if (typeof str !== 'string') return '';
  return str.trim().substring(0, MAX_ID_LEN).replace(/[^a-zA-Z0-9_\-:]/g, '');
}

function sanitizeInt(val, fallback = 0, min = 0, max = 10000) {
  const n = parseInt(val, 10);
  if (isNaN(n)) return fallback;
  return Math.max(min, Math.min(max, n));
}

function sanitizeFloat(val, fallback = 0, min = 0, max = 1e12) {
  const n = parseFloat(val);
  if (isNaN(n)) return fallback;
  return Math.max(min, Math.min(max, n));
}

// =============================================
// FIREBASE ADMIN
// =============================================
try {
  const serviceAccountEnv = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (serviceAccountEnv) {
    const serviceAccount = JSON.parse(serviceAccountEnv);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });
    console.log('Firebase Admin initialized from env');
  } else {
    try {
      const serviceAccount = require('./serviceAccountKey.json');
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
      });
      console.log('Firebase Admin initialized from file');
    } catch (e) {
      console.log('No service account file found, push notifications disabled');
    }
  }
} catch (e) {
  console.log('Firebase Admin not initialized - push notifications disabled:', e.message);
}

// =============================================
// DATABASE (PostgreSQL)
// =============================================
const DATABASE_URL =
  process.env.DATABASE_URL ||
  process.env.POSTGRES_URL ||
  'postgres://postgres:postgres@localhost:5432/kinetic';

// Render's managed Postgres requires SSL for external connections; local Postgres doesn't.
function sslConfig(url) {
  if (/sslmode=disable/i.test(url)) return false;
  if (/sslmode=(require|verify-ca|verify-full|no-verify)/i.test(url)) return { rejectUnauthorized: false };
  try {
    const host = new URL(url).hostname;
    if (['localhost', '127.0.0.1', '::1'].includes(host)) return false;
  } catch (_) {}
  return { rejectUnauthorized: false };
}

const pool = new Pool({
  connectionString: DATABASE_URL,
  ssl: sslConfig(DATABASE_URL),
  max: 10,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 10000,
});

// Query helpers
const getRow = async (text, params) => (await pool.query(text, params)).rows[0];
const allRows = async (text, params) => (await pool.query(text, params)).rows;
const runQuery = (text, params) => pool.query(text, params);

// Express 4 doesn't catch rejected promises from async handlers automatically.
const asyncRoute = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

async function waitForDb(retries = 30, delayMs = 3000) {
  for (let i = 1; i <= retries; i++) {
    try {
      await pool.query('SELECT 1');
      console.log('PostgreSQL connection established');
      return;
    } catch (e) {
      if (i === 1) {
        console.log('=== DATABASE DIAGNOSTIC ===');
        if (process.env.DATABASE_URL) {
          console.log('DATABASE_URL: SET');
          try {
            console.log(`DB host: ${new URL(process.env.DATABASE_URL).host}`);
          } catch (_) {}
        } else {
          console.log('DATABASE_URL: NOT SET — falling back to localhost:5432 (fails on Render).');
          console.log('Create a Render Postgres and add its connection string as the DATABASE_URL env var on this service.');
        }
        console.log('=== END DIAGNOSTIC ===');
      }
      console.log(`Waiting for PostgreSQL (attempt ${i}/${retries}): ${e.message}`);
      await new Promise((r) => setTimeout(r, delayMs));
    }
  }
  throw new Error('Could not connect to PostgreSQL after retries. Check the DATABASE_URL env var on Render.');
}

// =============================================
// AUTH MIDDLEWARE
// =============================================
async function verifyToken(req, res, next) {
  req.authUserId = null;
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next();
  }
  const idToken = authHeader.split('Bearer ')[1];
  try {
    const decoded = await admin.auth().verifyIdToken(idToken);
    req.authUserId = decoded.uid;
  } catch (e) {
    console.log('Token verification failed:', e.message);
  }
  next();
}

function enforceAuth(req, res, next) {
  if (!req.authUserId) {
    return res.status(401).json({ error: 'Authentication required' });
  }
  next();
}

// Verifică că userId-ul din cerere aparține utilizatorului autentificat.
function requireOwnership(req, res, userId) {
  if (!req.authUserId) {
    res.status(401).json({ error: 'Authentication required' });
    return false;
  }
  if (req.authUserId !== userId) {
    res.status(403).json({ error: 'Forbidden: not your data' });
    return false;
  }
  return true;
}

app.use(verifyToken);

// =============================================
// SCHEMA
// =============================================
const SCHEMA_SQL = `
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    photoUri TEXT NOT NULL DEFAULT '',
    fcmToken TEXT NOT NULL DEFAULT '',
    totalVolume DOUBLE PRECISION NOT NULL DEFAULT 0,
    workoutCount BIGINT NOT NULL DEFAULT 0,
    lastSeen BIGINT NOT NULL DEFAULT 0,
    createdAt BIGINT NOT NULL DEFAULT 0,
    isActive INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

  CREATE TABLE IF NOT EXISTS friendships (
    id SERIAL PRIMARY KEY,
    userId TEXT NOT NULL,
    friendId TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    createdAt BIGINT NOT NULL DEFAULT 0,
    UNIQUE(userId, friendId)
  );
  CREATE INDEX IF NOT EXISTS idx_friends_user ON friendships(userId);
  CREATE INDEX IF NOT EXISTS idx_friends_friend ON friendships(friendId);

  CREATE TABLE IF NOT EXISTS feed_posts (
    id SERIAL PRIMARY KEY,
    authorId TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    activityType TEXT NOT NULL DEFAULT 'post',
    createdAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_posts_author ON feed_posts(authorId);
  CREATE INDEX IF NOT EXISTS idx_posts_created ON feed_posts(createdAt DESC);

  CREATE TABLE IF NOT EXISTS comments (
    id SERIAL PRIMARY KEY,
    postId BIGINT NOT NULL,
    authorId TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    createdAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(postId);

  CREATE TABLE IF NOT EXISTS likes (
    id SERIAL PRIMARY KEY,
    postId BIGINT NOT NULL,
    userId TEXT NOT NULL,
    createdAt BIGINT NOT NULL DEFAULT 0,
    UNIQUE(postId, userId)
  );
  CREATE INDEX IF NOT EXISTS idx_likes_post ON likes(postId);

  CREATE TABLE IF NOT EXISTS leaderboard_entries (
    id SERIAL PRIMARY KEY,
    userId TEXT NOT NULL,
    metric TEXT NOT NULL,
    value DOUBLE PRECISION NOT NULL DEFAULT 0,
    periodStart BIGINT NOT NULL DEFAULT 0,
    periodEnd BIGINT NOT NULL DEFAULT 0,
    UNIQUE(userId, metric, periodStart)
  );
  CREATE INDEX IF NOT EXISTS idx_lb_metric ON leaderboard_entries(metric, value DESC);

  CREATE TABLE IF NOT EXISTS badges (
    key TEXT PRIMARY KEY,
    title TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    icon TEXT NOT NULL DEFAULT ''
  );

  CREATE TABLE IF NOT EXISTS user_badges (
    id SERIAL PRIMARY KEY,
    userId TEXT NOT NULL,
    badgeKey TEXT NOT NULL,
    awardedAt BIGINT NOT NULL DEFAULT 0,
    UNIQUE(userId, badgeKey)
  );
  CREATE INDEX IF NOT EXISTS idx_ub_user ON user_badges(userId);

  CREATE TABLE IF NOT EXISTS streaks (
    userId TEXT PRIMARY KEY,
    currentStreak BIGINT NOT NULL DEFAULT 0,
    bestStreak BIGINT NOT NULL DEFAULT 0,
    lastDate BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_antrenamente (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    grupaMusculara TEXT NOT NULL DEFAULT '',
    data BIGINT NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    totalWeight DOUBLE PRECISION NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_antren_user ON sync_antrenamente(userId);

  CREATE TABLE IF NOT EXISTS sync_exercitii (
    uuid TEXT PRIMARY KEY,
    antrenamentUuid TEXT NOT NULL DEFAULT '',
    numeExercitiu TEXT NOT NULL DEFAULT '',
    setIndex BIGINT NOT NULL DEFAULT 0,
    greutateKg DOUBLE PRECISION NOT NULL DEFAULT 0,
    repetari BIGINT NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_exerc_user ON sync_exercitii(antrenamentUuid);

  CREATE TABLE IF NOT EXISTS sync_exercises (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    groupName TEXT NOT NULL DEFAULT '',
    equipment TEXT NOT NULL DEFAULT '',
    isDefault INTEGER NOT NULL DEFAULT 1,
    isFavorite INTEGER NOT NULL DEFAULT 0,
    usageCount BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_templates (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_user ON sync_templates(userId);

  CREATE TABLE IF NOT EXISTS sync_template_exercises (
    uuid TEXT PRIMARY KEY,
    templateUuid TEXT NOT NULL DEFAULT '',
    exerciseName TEXT NOT NULL DEFAULT '',
    groupName TEXT NOT NULL DEFAULT '',
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_ex ON sync_template_exercises(templateUuid);

  CREATE TABLE IF NOT EXISTS sync_personal_records (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    exerciseName TEXT NOT NULL DEFAULT '',
    weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    reps BIGINT NOT NULL DEFAULT 0,
    volume DOUBLE PRECISION NOT NULL DEFAULT 0,
    date BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_pr_user ON sync_personal_records(userId);

  CREATE TABLE IF NOT EXISTS sync_muscle_recovery (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL DEFAULT '',
    grupaMusculara TEXT NOT NULL DEFAULT '',
    level DOUBLE PRECISION NOT NULL DEFAULT 0,
    lastUpdated BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_exercise_metadata (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL DEFAULT '',
    exerciseName TEXT NOT NULL DEFAULT '',
    grupaMusculara TEXT NOT NULL DEFAULT '',
    isFavorite INTEGER NOT NULL DEFAULT 0,
    isCustom INTEGER NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_biometric_entries (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT 0,
    weightKg DOUBLE PRECISION NOT NULL DEFAULT 0,
    bodyFatPercent DOUBLE PRECISION NOT NULL DEFAULT 0,
    waistCm DOUBLE PRECISION NOT NULL DEFAULT 0,
    hipsCm DOUBLE PRECISION NOT NULL DEFAULT 0,
    thighsCm DOUBLE PRECISION NOT NULL DEFAULT 0,
    chestCm DOUBLE PRECISION NOT NULL DEFAULT 0,
    armsCm DOUBLE PRECISION NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_bio_user ON sync_biometric_entries(userId);

  CREATE TABLE IF NOT EXISTS sync_food_entries (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    barcode TEXT NOT NULL DEFAULT '',
    name TEXT NOT NULL DEFAULT '',
    brand TEXT NOT NULL DEFAULT '',
    mealType TEXT NOT NULL DEFAULT 'snack',
    servingSize DOUBLE PRECISION NOT NULL DEFAULT 100,
    servingUnit TEXT NOT NULL DEFAULT 'g',
    calories DOUBLE PRECISION NOT NULL DEFAULT 0,
    proteinG DOUBLE PRECISION NOT NULL DEFAULT 0,
    carbsG DOUBLE PRECISION NOT NULL DEFAULT 0,
    fatG DOUBLE PRECISION NOT NULL DEFAULT 0,
    fiberG DOUBLE PRECISION NOT NULL DEFAULT 0,
    timestamp BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_food_user ON sync_food_entries(userId);

  CREATE TABLE IF NOT EXISTS sync_cardio_routes (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    routePoints TEXT NOT NULL DEFAULT '',
    distanceKm DOUBLE PRECISION NOT NULL DEFAULT 0,
    durationMs BIGINT NOT NULL DEFAULT 0,
    avgSpeedKmh DOUBLE PRECISION NOT NULL DEFAULT 0,
    avgPaceMinKm DOUBLE PRECISION NOT NULL DEFAULT 0,
    caloriesBurned DOUBLE PRECISION NOT NULL DEFAULT 0,
    startTime BIGINT NOT NULL DEFAULT 0,
    endTime BIGINT NOT NULL DEFAULT 0,
    activityType TEXT NOT NULL DEFAULT 'running',
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_cardio_user ON sync_cardio_routes(userId);

  CREATE TABLE IF NOT EXISTS sync_rest_days (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    date BIGINT NOT NULL DEFAULT 0,
    type TEXT NOT NULL DEFAULT 'rest',
    notes TEXT NOT NULL DEFAULT '',
    activities TEXT NOT NULL DEFAULT '',
    completed INTEGER NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_rest_user ON sync_rest_days(userId);

  CREATE TABLE IF NOT EXISTS sync_ai_chat_history (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    sessionId BIGINT NOT NULL DEFAULT 0,
    role TEXT NOT NULL DEFAULT '',
    message TEXT NOT NULL DEFAULT '',
    timestamp BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_chat_user ON sync_ai_chat_history(userId);

  CREATE TABLE IF NOT EXISTS sync_subscriptions (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    provider TEXT NOT NULL DEFAULT 'stripe',
    subscriptionId TEXT NOT NULL DEFAULT '',
    planId TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'inactive',
    currentPeriodEnd BIGINT NOT NULL DEFAULT 0,
    createdAt BIGINT NOT NULL DEFAULT 0,
    updatedAt BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_sub_user ON sync_subscriptions(userId);
`;

// Versiunea de schemă. La prima rulare (sau dacă schema e veche/incompatibilă — de ex.
// coloane cu ghilimele dintr-o migrare anterioară), tabelele Kinetic se reconstruiesc
// curat. După prima migrare reușită, guard-ul schema_meta previne orice DROP ulterior,
// deci datele reale nu se pierd niciodată la restarts/deploy-uri.
const SCHEMA_VERSION = 'kinetic-schema-v1';
const SCHEMA_TABLES = [...SCHEMA_SQL.matchAll(/CREATE TABLE IF NOT EXISTS (\w+)/g)].map((m) => m[1]);

async function migrateSchema() {
  await pool.query('CREATE TABLE IF NOT EXISTS schema_meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)');
  const existing = await getRow('SELECT value FROM schema_meta WHERE key = $1', ['schema_version']);
  if (existing && existing.value === SCHEMA_VERSION) return;

  for (const t of SCHEMA_TABLES) {
    await pool.query('DROP TABLE IF EXISTS "' + t + '" CASCADE');
  }
  await pool.query(SCHEMA_SQL);
  await pool.query(
    'INSERT INTO schema_meta (key, value) VALUES ($1, $2) ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value',
    ['schema_version', SCHEMA_VERSION]
  );
  console.log(`Kinetic schema migrated to ${SCHEMA_VERSION}`);
}

const SEED_BADGES = [
  { key: 'first_workout', title: 'First Workout', description: 'Completed your first workout', icon: '🏋️' },
  { key: '7day_streak', title: '7-Day Streak', description: 'Trained 7 days in a row', icon: '🔥' },
  { key: '30day_streak', title: '30-Day Streak', description: 'Trained 30 days in a row', icon: '🔥' },
  { key: 'pr_machine', title: 'PR Machine', description: 'Set 10 personal records', icon: '🏆' },
  { key: 'century_club', title: 'Century Club', description: 'Logged 100 workouts', icon: '💯' },
  { key: 'social_butterfly', title: 'Social Butterfly', description: 'Added 10 friends', icon: '🦋' },
  { key: 'helping_hand', title: 'Helping Hand', description: 'Commented on 10 posts', icon: '🤝' },
  { key: '1000kg_club', title: '1000kg Club', description: 'Lifted 1000kg total in one session', icon: '💪' },
];

// Columns that store numbers — used to coerce missing sync values to 0 instead of ''.
let numericColumns = new Set();

async function loadNumericColumns() {
  const { rows } = await pool.query(`
    SELECT table_name, column_name FROM information_schema.columns
    WHERE table_schema = 'public'
      AND data_type IN ('smallint','integer','bigint','real','double precision','numeric','decimal')
  `);
  numericColumns = new Set(rows.map((r) => `${r.table_name}.${r.column_name}`));
}

async function seedBadges() {
  for (const b of SEED_BADGES) {
    await runQuery(
      'INSERT INTO badges (key, title, description, icon) VALUES ($1, $2, $3, $4) ON CONFLICT (key) DO NOTHING',
      [b.key, b.title, b.description, b.icon]
    );
  }
}

// =============================================
// HEALTH CHECK
// =============================================
app.get('/health', asyncRoute(async (_req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'healthy', uptime: process.uptime() });
  } catch (e) {
    res.status(503).json({ status: 'unhealthy', error: e.message });
  }
}));

// =============================================
// USERS
// =============================================

app.post('/users', enforceAuth, asyncRoute(async (req, res) => {
  const { id, name, photoUri, fcmToken, totalVolume, workoutCount } = req.body;
  const userId = sanitizeId(id);
  if (!userId) return res.status(400).json({ error: 'id required' });
  if (requireOwnership(req, res, userId) !== true) return;
  const now = Date.now();
  await runQuery(
    `INSERT INTO users (id, name, photoUri, fcmToken, totalVolume, workoutCount, lastSeen, createdAt, isActive)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 1)
     ON CONFLICT(id) DO UPDATE SET
       name=CASE WHEN excluded.name != '' THEN excluded.name ELSE users.name END,
       photoUri=CASE WHEN excluded.photoUri != '' THEN excluded.photoUri ELSE users.photoUri END,
       fcmToken=CASE WHEN excluded.fcmToken != '' THEN excluded.fcmToken ELSE users.fcmToken END,
       totalVolume=CASE WHEN excluded.totalVolume > 0 THEN excluded.totalVolume ELSE users.totalVolume END,
       workoutCount=CASE WHEN excluded.workoutCount > 0 THEN excluded.workoutCount ELSE users.workoutCount END,
       lastSeen=excluded.lastSeen,
       isActive=1`,
    [userId, sanitizeString(name), sanitizeString(photoUri), sanitizeString(fcmToken), sanitizeFloat(totalVolume), sanitizeInt(workoutCount), now, now]
  );
  res.json({ id: userId, name: sanitizeString(name), photoUri: sanitizeString(photoUri), isActive: 1 });
}));

app.get('/users/search', asyncRoute(async (req, res) => {
  const q = sanitizeString(req.query.q || '');
  if (!q) return res.json([]);

  const normalize = (s) => s.toLowerCase()
    .replace(/ș/g, 's').replace(/ț/g, 't').replace(/ă/g, 'a').replace(/â/g, 'a').replace(/î/g, 'i')
    .replace(/ş/g, 's').replace(/ţ/g, 't');

  const normalizedQ = normalize(q);
  const likePattern = `%${normalizedQ}%`;
  const rows = await allRows(
    `SELECT * FROM users
     WHERE LOWER(name) LIKE $1 OR LOWER(id) LIKE $1
     LIMIT 20`,
    [likePattern]
  );

  res.json(rows);
}));

app.get('/users/:id', asyncRoute(async (req, res) => {
  const userId = sanitizeId(req.params.id);
  if (!userId) return res.status(400).json({ error: 'invalid id' });
  const user = await getRow('SELECT * FROM users WHERE id = $1', [userId]);
  if (!user) return res.status(404).json({ error: 'not found' });
  res.json(user);
}));

app.delete('/users/:id', enforceAuth, asyncRoute(async (req, res) => {
  const targetId = sanitizeId(req.params.id);
  if (!targetId) return res.status(400).json({ error: 'invalid id' });
  if (req.authUserId !== targetId) {
    return res.status(403).json({ error: 'can only delete your own account' });
  }
  await runQuery('DELETE FROM users WHERE id = $1', [targetId]);
  res.json({ success: true });
}));

// =============================================
// FRIENDSHIPS
// =============================================

app.post('/friends/request', enforceAuth, asyncRoute(async (req, res) => {
  const { fromUserId, toUserId } = req.body;
  const from = sanitizeId(fromUserId);
  const to = sanitizeId(toUserId);
  if (!from || !to) return res.status(400).json({ error: 'fromUserId and toUserId required' });
  if (from === to) return res.status(400).json({ error: 'cannot send friend request to yourself' });
  if (requireOwnership(req, res, from) !== true) return;
  const now = Date.now();
  await runQuery(
    'INSERT INTO friendships (userId, friendId, status, createdAt) VALUES ($1, $2, $3, $4) ON CONFLICT (userId, friendId) DO NOTHING',
    [from, to, 'pending', now]
  );

  const sender = await getRow('SELECT name FROM users WHERE id = $1', [from]);
  const recipient = await getRow('SELECT fcmToken FROM users WHERE id = $1', [to]);
  if (recipient && recipient.fcmtoken && admin.apps.length > 0) {
    admin.messaging().send({
      token: recipient.fcmtoken,
      notification: {
        title: 'Friend Request',
        body: `${sender?.name || 'Someone'} sent you a friend request!`,
      },
      data: { type: 'friend_request', fromUserId: from, fromUserName: sender?.name || '' },
      android: { priority: 'high', notification: { channelId: 'friend_requests', priority: 'max' } },
    }).catch(e => console.log('FCM error:', e.message));
  }

  res.json({ success: true });
}));

app.get('/friends/incoming/:userId', asyncRoute(async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await allRows(
    "SELECT * FROM friendships WHERE friendId = $1 AND status = 'pending' ORDER BY createdAt DESC",
    [userId]
  );
  res.json(rows);
}));

app.post('/friends/accept', enforceAuth, asyncRoute(async (req, res) => {
  const { userId, friendId } = req.body;
  // userId = utilizatorul autentificat (acceptorul), friendId = expeditorul cererii.
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  // Cererea pending e stocată (expeditor → acceptor): o acceptăm + creăm rândul invers.
  await runQuery("UPDATE friendships SET status = 'accepted' WHERE userId = $1 AND friendId = $2 AND status = 'pending'", [fid, uid]);
  await runQuery(
    "INSERT INTO friendships (userId, friendId, status, createdAt) VALUES ($1, $2, 'accepted', $3) ON CONFLICT (userId, friendId) DO NOTHING",
    [uid, fid, Date.now()]
  );

  const acceptor = await getRow('SELECT name FROM users WHERE id = $1', [uid]);
  const recipient = await getRow('SELECT fcmToken FROM users WHERE id = $1', [fid]);
  if (recipient && recipient.fcmtoken && admin.apps.length > 0) {
    admin.messaging().send({
      token: recipient.fcmtoken,
      notification: { title: 'Friend Request Accepted', body: `${acceptor?.name || 'Someone'} accepted your friend request!` },
      data: { type: 'friend_accepted', fromUserName: acceptor?.name || '' },
      android: { priority: 'high', notification: { channelId: 'kinetic_notifications', priority: 'max' } },
    }).catch(e => console.log('FCM error:', e.message));
  }

  res.json({ success: true });
}));

app.post('/friends/reject', enforceAuth, asyncRoute(async (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await runQuery('DELETE FROM friendships WHERE userId = $1 AND friendId = $2', [uid, fid]);
  await runQuery('DELETE FROM friendships WHERE userId = $1 AND friendId = $2', [fid, uid]);
  res.json({ success: true });
}));

app.post('/friends/remove', enforceAuth, asyncRoute(async (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await runQuery("DELETE FROM friendships WHERE userId = $1 AND friendId = $2 AND status = 'accepted'", [uid, fid]);
  await runQuery("DELETE FROM friendships WHERE userId = $1 AND friendId = $2 AND status = 'accepted'", [fid, uid]);
  res.json({ success: true });
}));

app.get('/friends/:userId', asyncRoute(async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await allRows(
    "SELECT * FROM friendships WHERE (userId = $1 OR friendId = $1) AND status = 'accepted' ORDER BY createdAt DESC",
    [userId]
  );
  res.json(rows);
}));

// =============================================
// FEED & POSTS (with pagination)
// =============================================

app.post('/posts', postLimiter, enforceAuth, asyncRoute(async (req, res) => {
  const { authorId, content, activityType } = req.body;
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!author || !text) return res.status(400).json({ error: 'authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = await runQuery(
    'INSERT INTO feed_posts (authorId, content, activityType, createdAt) VALUES ($1, $2, $3, $4) RETURNING id',
    [author, text, sanitizeString(activityType) || 'post', now]
  );
  res.json({ postId: result.rows[0].id });
}));

app.get('/feed', asyncRoute(async (req, res) => {
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = await allRows('SELECT * FROM feed_posts ORDER BY createdAt DESC LIMIT $1 OFFSET $2', [limit, offset]);
  const totalRow = await getRow('SELECT COUNT(*)::int as c FROM feed_posts');
  const total = totalRow.c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
}));

app.get('/posts/author/:authorId', asyncRoute(async (req, res) => {
  const authorId = sanitizeId(req.params.authorId);
  if (!authorId) return res.status(400).json({ error: 'invalid authorId' });
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = await allRows(
    'SELECT * FROM feed_posts WHERE authorId = $1 ORDER BY createdAt DESC LIMIT $2 OFFSET $3',
    [authorId, limit, offset]
  );
  const totalRow = await getRow('SELECT COUNT(*)::int as c FROM feed_posts WHERE authorId = $1', [authorId]);
  const total = totalRow.c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
}));

// =============================================
// COMMENTS & LIKES
// =============================================

app.post('/comments', postLimiter, enforceAuth, asyncRoute(async (req, res) => {
  const { postId, authorId, content } = req.body;
  const pid = sanitizeInt(postId, 0, 1, 1e9);
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!pid || !author || !text) return res.status(400).json({ error: 'postId, authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = await runQuery(
    'INSERT INTO comments (postId, authorId, content, createdAt) VALUES ($1, $2, $3, $4) RETURNING id',
    [pid, author, text, now]
  );
  res.json({ commentId: result.rows[0].id });
}));

app.get('/comments/:postId', asyncRoute(async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  if (!postId) return res.status(400).json({ error: 'invalid postId' });
  const rows = await allRows('SELECT * FROM comments WHERE postId = $1 ORDER BY createdAt ASC', [postId]);
  res.json(rows);
}));

app.post('/posts/:postId/like', enforceAuth, asyncRoute(async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await runQuery(
    'INSERT INTO likes (postId, userId, createdAt) VALUES ($1, $2, $3) ON CONFLICT (postId, userId) DO NOTHING',
    [postId, uid, Date.now()]
  );
  res.json({ success: true });
}));

app.delete('/posts/:postId/like', enforceAuth, asyncRoute(async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.query.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, userId) !== true) return;
  await runQuery('DELETE FROM likes WHERE postId = $1 AND userId = $2', [postId, userId]);
  res.json({ success: true });
}));

app.get('/posts/:postId/likes/count', asyncRoute(async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const row = await getRow('SELECT COUNT(*)::int as count FROM likes WHERE postId = $1', [postId]);
  res.json({ count: row.count });
}));

app.get('/posts/:postId/liked/:userId', asyncRoute(async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  const liked = await getRow('SELECT 1 FROM likes WHERE postId = $1 AND userId = $2', [postId, userId]);
  res.json({ liked: !!liked });
}));

// =============================================
// LEADERBOARD (with pagination)
// =============================================

app.post('/leaderboard', enforceAuth, asyncRoute(async (req, res) => {
  const { userId, metric, value, periodStart, periodEnd } = req.body;
  const uid = sanitizeId(userId);
  const met = sanitizeString(metric);
  if (!uid || !met) return res.status(400).json({ error: 'userId and metric required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();
  await runQuery(
    `INSERT INTO leaderboard_entries (userId, metric, value, periodStart, periodEnd)
     VALUES ($1, $2, $3, $4, $5)
     ON CONFLICT (userId, metric, periodStart) DO UPDATE SET value=excluded.value`,
    [uid, met, sanitizeFloat(value), sanitizeInt(periodStart, now), sanitizeInt(periodEnd, now)]
  );
  res.json({ success: true });
}));

app.get('/leaderboard', asyncRoute(async (req, res) => {
  const metric = sanitizeString(req.query.metric) || 'workouts';
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);

  if (metric === 'volume') {
    const rows = await allRows(
      'SELECT id as userId, name, photoUri, totalVolume as value, workoutCount FROM users WHERE totalVolume > 0 ORDER BY totalVolume DESC LIMIT $1 OFFSET $2',
      [limit, offset]
    );
    const totalRow = await getRow('SELECT COUNT(*)::int as c FROM users WHERE totalVolume > 0');
    const total = totalRow.c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  } else {
    const rows = await allRows(
      'SELECT * FROM leaderboard_entries WHERE metric = $1 ORDER BY value DESC LIMIT $2 OFFSET $3',
      [metric, limit, offset]
    );
    const totalRow = await getRow('SELECT COUNT(*)::int as c FROM leaderboard_entries WHERE metric = $1', [metric]);
    const total = totalRow.c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  }
}));

// =============================================
// WORKOUTS / STREAKS / BADGES
// =============================================

app.post('/workouts/log', enforceAuth, asyncRoute(async (req, res) => {
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();

  const streak = await getRow('SELECT * FROM streaks WHERE userId = $1', [uid]);
  let currentStreak = 1;
  let bestStreak = 1;
  let lastDate = now;

  if (streak) {
    const last = streak.lastdate;
    const diffHours = (now - last) / (1000 * 60 * 60);
    if (diffHours >= 24 && diffHours <= 48) {
      currentStreak = streak.currentstreak + 1;
    } else if (diffHours > 48) {
      currentStreak = 1;
    } else {
      currentStreak = streak.currentstreak;
    }
    bestStreak = Math.max(currentStreak, streak.beststreak);
    lastDate = now;
  }

  await runQuery(
    `INSERT INTO streaks (userId, currentStreak, bestStreak, lastDate)
     VALUES ($1, $2, $3, $4)
     ON CONFLICT (userId) DO UPDATE SET currentStreak=excluded.currentStreak, bestStreak=excluded.bestStreak, lastDate=excluded.lastDate`,
    [uid, currentStreak, bestStreak, lastDate]
  );

  const workoutCountRow = await getRow("SELECT COUNT(*)::int as c FROM feed_posts WHERE authorId = $1 AND activityType = 'workout'", [uid]);
  const commentCountRow = await getRow('SELECT COUNT(*)::int as c FROM comments WHERE authorId = $1', [uid]);
  const friendCountRow = await getRow("SELECT COUNT(*)::int as c FROM friendships WHERE userId = $1 AND status = 'accepted'", [uid]);
  const workoutCount = workoutCountRow.c;
  const commentCount = commentCountRow.c;
  const friendCount = friendCountRow.c;

  const newlyAwardedBadges = [];
  const hasBadge = async (key) => !!(await getRow('SELECT 1 FROM user_badges WHERE userId = $1 AND badgeKey = $2', [uid, key]));
  const awardBadge = async (key) => {
    await runQuery(
      'INSERT INTO user_badges (userId, badgeKey, awardedAt) VALUES ($1, $2, $3) ON CONFLICT (userId, badgeKey) DO NOTHING',
      [uid, key, now]
    );
    newlyAwardedBadges.push(key);
  };

  if (workoutCount >= 1 && !(await hasBadge('first_workout'))) await awardBadge('first_workout');
  if (currentStreak >= 7 && !(await hasBadge('7day_streak'))) await awardBadge('7day_streak');
  if (currentStreak >= 30 && !(await hasBadge('30day_streak'))) await awardBadge('30day_streak');
  if (workoutCount >= 100 && !(await hasBadge('century_club'))) await awardBadge('century_club');
  if (friendCount >= 10 && !(await hasBadge('social_butterfly'))) await awardBadge('social_butterfly');
  if (commentCount >= 10 && !(await hasBadge('helping_hand'))) await awardBadge('helping_hand');

  const stats = { workoutCount, commentCount, friendCount };

  res.json({
    success: true,
    stats,
    streak: { currentStreak, bestStreak, lastDate },
    newlyAwardedBadges,
  });
}));

app.get('/streaks/:userId', asyncRoute(async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const streak = await getRow('SELECT * FROM streaks WHERE userId = $1', [userId]);
  res.json(streak || { userId, currentStreak: 0, bestStreak: 0, lastDate: 0 });
}));

app.get('/badges', asyncRoute(async (_req, res) => {
  const rows = await allRows('SELECT * FROM badges');
  res.json(rows);
}));

app.get('/badges/user/:userId', asyncRoute(async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await allRows('SELECT * FROM user_badges WHERE userId = $1 ORDER BY awardedAt DESC', [userId]);
  res.json(rows);
}));

app.post('/badges/award', enforceAuth, asyncRoute(async (req, res) => {
  const { userId, badgeKey } = req.body;
  const uid = sanitizeId(userId);
  const key = sanitizeString(badgeKey);
  if (!uid || !key) return res.status(400).json({ error: 'userId and badgeKey required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const existing = await getRow('SELECT 1 FROM user_badges WHERE userId = $1 AND badgeKey = $2', [uid, key]);
  if (existing) return res.json({ success: false, alreadyAwarded: true });
  await runQuery('INSERT INTO user_badges (userId, badgeKey, awardedAt) VALUES ($1, $2, $3)', [uid, key, Date.now()]);
  res.json({ success: true });
}));

// =============================================
// DATA SYNC (Android <-> Backend)
// =============================================

const SYNC_TABLES = {
  antrenamente: {
    columns: ['uuid', 'userId', 'grupaMusculara', 'data', 'notes', 'totalWeight', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'grupaMusculara', 'data', 'notes', 'totalWeight', 'updatedAt']
  },
  exercitii: {
    columns: ['uuid', 'antrenamentUuid', 'numeExercitiu', 'setIndex', 'greutateKg', 'repetari', 'notes', 'updatedAt'],
    userCol: null,
    upsertCols: ['uuid', 'antrenamentUuid', 'numeExercitiu', 'setIndex', 'greutateKg', 'repetari', 'notes', 'updatedAt']
  },
  exercises: {
    columns: ['uuid', 'name', 'groupName', 'equipment', 'isDefault', 'isFavorite', 'usageCount', 'updatedAt'],
    userCol: null,
    upsertCols: ['uuid', 'name', 'groupName', 'equipment', 'isDefault', 'isFavorite', 'usageCount', 'updatedAt']
  },
  templates: {
    columns: ['uuid', 'userId', 'name', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'name', 'updatedAt']
  },
  template_exercises: {
    columns: ['uuid', 'templateUuid', 'exerciseName', 'groupName', 'updatedAt'],
    userCol: null,
    upsertCols: ['uuid', 'templateUuid', 'exerciseName', 'groupName', 'updatedAt']
  },
  personal_records: {
    columns: ['uuid', 'userId', 'exerciseName', 'weight', 'reps', 'volume', 'date', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'exerciseName', 'weight', 'reps', 'volume', 'date', 'updatedAt']
  },
  muscle_recovery: {
    columns: ['uuid', 'userId', 'grupaMusculara', 'level', 'lastUpdated', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'grupaMusculara', 'level', 'lastUpdated', 'updatedAt']
  },
  exercise_metadata: {
    columns: ['uuid', 'userId', 'exerciseName', 'grupaMusculara', 'isFavorite', 'isCustom', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'exerciseName', 'grupaMusculara', 'isFavorite', 'isCustom', 'updatedAt']
  },
  biometric_entries: {
    columns: ['uuid', 'userId', 'timestamp', 'weightKg', 'bodyFatPercent', 'waistCm', 'hipsCm', 'thighsCm', 'chestCm', 'armsCm', 'notes', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'timestamp', 'weightKg', 'bodyFatPercent', 'waistCm', 'hipsCm', 'thighsCm', 'chestCm', 'armsCm', 'notes', 'updatedAt']
  },
  food_entries: {
    columns: ['uuid', 'userId', 'barcode', 'name', 'brand', 'mealType', 'servingSize', 'servingUnit', 'calories', 'proteinG', 'carbsG', 'fatG', 'fiberG', 'timestamp', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'barcode', 'name', 'brand', 'mealType', 'servingSize', 'servingUnit', 'calories', 'proteinG', 'carbsG', 'fatG', 'fiberG', 'timestamp', 'updatedAt']
  },
  cardio_routes: {
    columns: ['uuid', 'userId', 'name', 'routePoints', 'distanceKm', 'durationMs', 'avgSpeedKmh', 'avgPaceMinKm', 'caloriesBurned', 'startTime', 'endTime', 'activityType', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'name', 'routePoints', 'distanceKm', 'durationMs', 'avgSpeedKmh', 'avgPaceMinKm', 'caloriesBurned', 'startTime', 'endTime', 'activityType', 'updatedAt']
  },
  rest_days: {
    columns: ['uuid', 'userId', 'date', 'type', 'notes', 'activities', 'completed', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'date', 'type', 'notes', 'activities', 'completed', 'updatedAt']
  },
  ai_chat_history: {
    columns: ['uuid', 'userId', 'sessionId', 'role', 'message', 'timestamp', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'sessionId', 'role', 'message', 'timestamp', 'updatedAt']
  },
  subscriptions: {
    columns: ['uuid', 'userId', 'provider', 'subscriptionId', 'planId', 'status', 'currentPeriodEnd', 'createdAt', 'updatedAt'],
    userCol: 'userId',
    upsertCols: ['uuid', 'userId', 'provider', 'subscriptionId', 'planId', 'status', 'currentPeriodEnd', 'createdAt', 'updatedAt']
  }
};

function mapSyncValue(tableName, col, val) {
  if (col === 'uuid') return sanitizeId(val);
  if (val === undefined || val === null) {
    return numericColumns.has(`${tableName}.${col}`) ? 0 : '';
  }
  if (typeof val === 'string') return sanitizeString(val);
  if (typeof val === 'boolean') return val ? 1 : 0;
  if (typeof val === 'number') return val;
  return val ?? '';
}

Object.entries(SYNC_TABLES).forEach(([table, config]) => {
  const tableName = `sync_${table}`;
  const placeholders = config.upsertCols.map((_, i) => `$${i + 1}`).join(',');
  const updateClauses = config.upsertCols.filter((c) => c !== 'uuid').map((c) => `${c}=excluded.${c}`).join(', ');
  const upsertSql = `INSERT INTO ${tableName} (${config.upsertCols.join(',')}) VALUES (${placeholders}) ON CONFLICT (uuid) DO UPDATE SET ${updateClauses}`;

  app.get(`/sync/${table}/:userId`, asyncRoute(async (req, res) => {
    const userId = sanitizeId(req.params.userId);
    if (!userId) return res.status(400).json({ error: 'invalid userId' });
    const since = parseInt(req.query.since) || 0;
    let rows;
    if (config.userCol) {
      rows = since > 0
        ? await allRows(`SELECT * FROM ${tableName} WHERE ${config.userCol} = $1 AND updatedAt > $2`, [userId, since])
        : await allRows(`SELECT * FROM ${tableName} WHERE ${config.userCol} = $1`, [userId]);
    } else {
      rows = since > 0
        ? await allRows(`SELECT * FROM ${tableName} WHERE updatedAt > $1`, [since])
        : await allRows(`SELECT * FROM ${tableName}`);
    }
    res.json(rows);
  }));

  app.post(`/sync/${table}/upsert`, enforceAuth, asyncRoute(async (req, res) => {
    const item = req.body;
    if (!item || !item.uuid) return res.status(400).json({ error: 'uuid required' });
    // Tabelele cu coloană de user trebuie să aparțină utilizatorului autentificat.
    if (config.userCol) {
      const owner = sanitizeId(item[config.userCol]);
      if (requireOwnership(req, res, owner) !== true) return;
    }
    const values = config.upsertCols.map((c) => mapSyncValue(tableName, c, item[c]));
    await runQuery(upsertSql, values);
    res.json({ success: true });
  }));

  app.post(`/sync/${table}/bulk`, enforceAuth, asyncRoute(async (req, res) => {
    const items = req.body.items;
    if (!Array.isArray(items)) return res.status(400).json({ error: 'items array required' });
    // Fiecare element cu userCol trebuie să aparțină utilizatorului autentificat.
    if (config.userCol) {
      for (const item of items) {
        if (!item.uuid) continue;
        const owner = sanitizeId(item[config.userCol]);
        if (requireOwnership(req, res, owner) !== true) return;
      }
    }
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      for (const item of items) {
        if (!item.uuid) continue;
        const values = config.upsertCols.map((c) => mapSyncValue(tableName, c, item[c]));
        await client.query(upsertSql, values);
      }
      await client.query('COMMIT');
      res.json({ success: true, count: items.length });
    } catch (e) {
      await client.query('ROLLBACK');
      throw e;
    } finally {
      client.release();
    }
  }));

  app.delete(`/sync/${table}/:uuid`, enforceAuth, asyncRoute(async (req, res) => {
    const uuid = sanitizeId(req.params.uuid);
    if (!uuid) return res.status(400).json({ error: 'invalid uuid' });
    // Doar proprietarul poate șterge un rând care are coloană de user.
    if (config.userCol) {
      const row = await getRow(`SELECT ${config.userCol} FROM ${tableName} WHERE uuid = $1`, [uuid]);
      if (!row) return res.status(404).json({ error: 'not found' });
      if (requireOwnership(req, res, sanitizeId(row[config.userCol])) !== true) return;
    }
    await runQuery(`DELETE FROM ${tableName} WHERE uuid = $1`, [uuid]);
    res.json({ success: true });
  }));
});

// =============================================
// ADMIN: CLEANUP TEST USERS
// =============================================
// Șterge complet conturile de test (id TEST* sau nume care conține „Test") din toate
// tabelele: users, friendships, leaderboard, streaks, badges, posts, comments, likes
// și toate tabelele sync_*. Protejat cu header X-Admin-Key.
app.post('/admin/cleanup-test-users', asyncRoute(async (req, res) => {
  const adminKey = process.env.ADMIN_KEY || 'kinetic-cleanup-2024';
  if (req.headers['x-admin-key'] !== adminKey) {
    return res.status(401).json({ error: 'Invalid admin key' });
  }

  const targets = await allRows("SELECT id, name FROM users WHERE id LIKE 'TEST%' OR name LIKE '%Test%'");
  if (targets.length === 0) return res.json({ success: true, deleted: 0 });

  const ids = targets.map((t) => t.id);
  const ph = ids.map((_, i) => `$${i + 1}`).join(',');

  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    // Prietenii (ambele direcții) și cereri pendente
    await client.query(`DELETE FROM friendships WHERE userId IN (${ph}) OR friendId IN (${ph})`, ids);
    // Leaderboard
    await client.query(`DELETE FROM leaderboard_entries WHERE userId IN (${ph})`, ids);
    // Streaks + badges
    await client.query(`DELETE FROM streaks WHERE userId IN (${ph})`, ids);
    await client.query(`DELETE FROM user_badges WHERE userId IN (${ph})`, ids);
    // Posturi ale userilor de test + comentariile/like-urile lor
    const posts = (await client.query(`SELECT id FROM feed_posts WHERE authorId IN (${ph})`, ids)).rows;
    const postIds = posts.map((p) => p.id);
    if (postIds.length > 0) {
      const pph = postIds.map((_, i) => `$${i + 1}`).join(',');
      await client.query(`DELETE FROM comments WHERE postId IN (${pph})`, postIds);
      await client.query(`DELETE FROM likes WHERE postId IN (${pph})`, postIds);
      await client.query(`DELETE FROM feed_posts WHERE id IN (${pph})`, postIds);
    }
    await client.query(`DELETE FROM comments WHERE authorId IN (${ph})`, ids);
    await client.query(`DELETE FROM likes WHERE userId IN (${ph})`, ids);
    // Toate tabelele sync_ cu coloană userId
    for (const [table, cfg] of Object.entries(SYNC_TABLES)) {
      if (cfg.userCol) {
        await client.query(`DELETE FROM sync_${table} WHERE ${cfg.userCol} IN (${ph})`, ids);
      }
    }
    // În final, userii înșiși
    await client.query(`DELETE FROM users WHERE id IN (${ph})`, ids);
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }

  res.json({ success: true, deleted: targets.length, users: targets.map((t) => t.id) });
}));

// =============================================
// ERROR HANDLING MIDDLEWARE
// =============================================
app.use((err, req, res, _next) => {
  console.error('Unhandled error:', err.message);
  if (err.message === 'Not allowed by CORS') {
    return res.status(403).json({ error: 'Origin not allowed' });
  }
  res.status(500).json({ error: 'Internal server error' });
});

// =============================================
// ROOT / START
// =============================================
app.get('/', asyncRoute(async (_req, res) => {
  const userRow = await getRow('SELECT COUNT(*)::int as c FROM users');
  const postRow = await getRow('SELECT COUNT(*)::int as c FROM feed_posts');
  const friendRow = await getRow("SELECT COUNT(*)::int as c FROM friendships WHERE status = 'accepted'");
  res.json({
    name: 'Kinetic API',
    version: '2.1.0',
    status: 'running',
    database: 'postgresql',
    stats: { users: userRow.c, posts: postRow.c, friendships: friendRow.c },
    endpoints: [
      'GET /health',
      'POST /users', 'GET /users/:id', 'GET /users/search?q=', 'DELETE /users/:id',
      'POST /friends/request', 'GET /friends/incoming/:userId', 'POST /friends/accept', 'POST /friends/reject', 'POST /friends/remove', 'GET /friends/:userId',
      'POST /posts', 'GET /feed?limit=&offset=', 'GET /posts/author/:authorId?limit=&offset=',
      'POST /comments', 'GET /comments/:postId', 'POST /posts/:postId/like', 'DELETE /posts/:postId/like', 'GET /posts/:postId/likes/count', 'GET /posts/:postId/liked/:userId',
      'POST /leaderboard', 'GET /leaderboard?metric=&limit=&offset=',
      'POST /workouts/log', 'GET /streaks/:userId', 'GET /badges', 'GET /badges/user/:userId', 'POST /badges/award'
    ]
  });
}));

async function init() {
  await waitForDb();
  await migrateSchema();
  await loadNumericColumns();
  await seedBadges();
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Kinetic backend v2.1.0 running on http://0.0.0.0:${PORT} (PostgreSQL)`);
  });
}

init().catch((e) => {
  console.error('FATAL: backend failed to initialize:', e.message);
  process.exit(1);
});
