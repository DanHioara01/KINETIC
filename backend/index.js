require('dotenv').config();

const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const rateLimit = require('express-rate-limit');
const { q, qOne, qRun, withTransaction } = require('./db');

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
    return res.status(401).json({ error: 'Authentication required' });
  }
  if (req.authUserId !== userId) {
    return res.status(403).json({ error: 'Forbidden: not your data' });
  }
  return true;
}

app.use(verifyToken);

// =============================================
// SCHEMA (PostgreSQL / Supabase)
// =============================================
// Coloanele camelCase sunt puse între ghilimele ca să rămână identice cu
// ceea ce citește aplicația Android (SELECT * → chei camelCase exacte).
const SCHEMA = `
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    "photoUri" TEXT NOT NULL DEFAULT '',
    "fcmToken" TEXT NOT NULL DEFAULT '',
    "totalVolume" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "workoutCount" INTEGER NOT NULL DEFAULT 0,
    "lastSeen" BIGINT NOT NULL DEFAULT 0,
    "createdAt" BIGINT NOT NULL DEFAULT 0,
    "isActive" INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

  CREATE TABLE IF NOT EXISTS friendships (
    id BIGSERIAL PRIMARY KEY,
    "userId" TEXT NOT NULL,
    "friendId" TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    "createdAt" BIGINT NOT NULL DEFAULT 0,
    UNIQUE("userId", "friendId")
  );
  CREATE INDEX IF NOT EXISTS idx_friends_user ON friendships("userId");
  CREATE INDEX IF NOT EXISTS idx_friends_friend ON friendships("friendId");

  CREATE TABLE IF NOT EXISTS feed_posts (
    id BIGSERIAL PRIMARY KEY,
    "authorId" TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    "activityType" TEXT NOT NULL DEFAULT 'post',
    "createdAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_posts_author ON feed_posts("authorId");
  CREATE INDEX IF NOT EXISTS idx_posts_created ON feed_posts("createdAt" DESC);

  CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    "postId" BIGINT NOT NULL,
    "authorId" TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    "createdAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_comments_post ON comments("postId");

  CREATE TABLE IF NOT EXISTS likes (
    id BIGSERIAL PRIMARY KEY,
    "postId" BIGINT NOT NULL,
    "userId" TEXT NOT NULL,
    "createdAt" BIGINT NOT NULL DEFAULT 0,
    UNIQUE("postId", "userId")
  );
  CREATE INDEX IF NOT EXISTS idx_likes_post ON likes("postId");

  CREATE TABLE IF NOT EXISTS leaderboard_entries (
    id BIGSERIAL PRIMARY KEY,
    "userId" TEXT NOT NULL,
    metric TEXT NOT NULL,
    value DOUBLE PRECISION NOT NULL DEFAULT 0,
    "periodStart" BIGINT NOT NULL DEFAULT 0,
    "periodEnd" BIGINT NOT NULL DEFAULT 0,
    UNIQUE("userId", metric, "periodStart")
  );
  CREATE INDEX IF NOT EXISTS idx_lb_metric ON leaderboard_entries(metric, value DESC);

  CREATE TABLE IF NOT EXISTS badges (
    "key" TEXT PRIMARY KEY,
    title TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    icon TEXT NOT NULL DEFAULT ''
  );

  CREATE TABLE IF NOT EXISTS user_badges (
    id BIGSERIAL PRIMARY KEY,
    "userId" TEXT NOT NULL,
    "badgeKey" TEXT NOT NULL,
    "awardedAt" BIGINT NOT NULL DEFAULT 0,
    UNIQUE("userId", "badgeKey")
  );
  CREATE INDEX IF NOT EXISTS idx_ub_user ON user_badges("userId");

  CREATE TABLE IF NOT EXISTS streaks (
    "userId" TEXT PRIMARY KEY,
    "currentStreak" INTEGER NOT NULL DEFAULT 0,
    "bestStreak" INTEGER NOT NULL DEFAULT 0,
    "lastDate" BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_antrenamente (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    "grupaMusculara" TEXT NOT NULL DEFAULT '',
    data BIGINT NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    "totalWeight" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_antren_user ON sync_antrenamente("userId");

  CREATE TABLE IF NOT EXISTS sync_exercitii (
    uuid TEXT PRIMARY KEY,
    "antrenamentUuid" TEXT NOT NULL DEFAULT '',
    "numeExercitiu" TEXT NOT NULL DEFAULT '',
    "setIndex" INTEGER NOT NULL DEFAULT 0,
    "greutateKg" DOUBLE PRECISION NOT NULL DEFAULT 0,
    repetari INTEGER NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_exerc_user ON sync_exercitii("antrenamentUuid");

  CREATE TABLE IF NOT EXISTS sync_exercises (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    "groupName" TEXT NOT NULL DEFAULT '',
    equipment TEXT NOT NULL DEFAULT '',
    "isDefault" INTEGER NOT NULL DEFAULT 1,
    "isFavorite" INTEGER NOT NULL DEFAULT 0,
    "usageCount" INTEGER NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_templates (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_user ON sync_templates("userId");

  CREATE TABLE IF NOT EXISTS sync_template_exercises (
    uuid TEXT PRIMARY KEY,
    "templateUuid" TEXT NOT NULL DEFAULT '',
    "exerciseName" TEXT NOT NULL DEFAULT '',
    "groupName" TEXT NOT NULL DEFAULT '',
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_ex ON sync_template_exercises("templateUuid");

  CREATE TABLE IF NOT EXISTS sync_personal_records (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    "exerciseName" TEXT NOT NULL DEFAULT '',
    weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    reps INTEGER NOT NULL DEFAULT 0,
    volume DOUBLE PRECISION NOT NULL DEFAULT 0,
    date BIGINT NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_pr_user ON sync_personal_records("userId");

  CREATE TABLE IF NOT EXISTS sync_muscle_recovery (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL DEFAULT '',
    "grupaMusculara" TEXT NOT NULL DEFAULT '',
    level DOUBLE PRECISION NOT NULL DEFAULT 0,
    "lastUpdated" BIGINT NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_exercise_metadata (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL DEFAULT '',
    "exerciseName" TEXT NOT NULL DEFAULT '',
    "grupaMusculara" TEXT NOT NULL DEFAULT '',
    "isFavorite" INTEGER NOT NULL DEFAULT 0,
    "isCustom" INTEGER NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_biometric_entries (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT 0,
    "weightKg" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "bodyFatPercent" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "waistCm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "hipsCm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "thighsCm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "chestCm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "armsCm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_bio_user ON sync_biometric_entries("userId");

  CREATE TABLE IF NOT EXISTS sync_food_entries (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    barcode TEXT NOT NULL DEFAULT '',
    name TEXT NOT NULL DEFAULT '',
    brand TEXT NOT NULL DEFAULT '',
    "mealType" TEXT NOT NULL DEFAULT 'snack',
    "servingSize" DOUBLE PRECISION NOT NULL DEFAULT 100,
    "servingUnit" TEXT NOT NULL DEFAULT 'g',
    calories DOUBLE PRECISION NOT NULL DEFAULT 0,
    "proteinG" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "carbsG" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "fatG" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "fiberG" DOUBLE PRECISION NOT NULL DEFAULT 0,
    timestamp BIGINT NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_food_user ON sync_food_entries("userId");

  CREATE TABLE IF NOT EXISTS sync_cardio_routes (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    "routePoints" TEXT NOT NULL DEFAULT '',
    "distanceKm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "durationMs" BIGINT NOT NULL DEFAULT 0,
    "avgSpeedKmh" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "avgPaceMinKm" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "caloriesBurned" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "startTime" BIGINT NOT NULL DEFAULT 0,
    "endTime" BIGINT NOT NULL DEFAULT 0,
    "activityType" TEXT NOT NULL DEFAULT 'running',
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_cardio_user ON sync_cardio_routes("userId");

  CREATE TABLE IF NOT EXISTS sync_rest_days (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    date BIGINT NOT NULL DEFAULT 0,
    type TEXT NOT NULL DEFAULT 'rest',
    notes TEXT NOT NULL DEFAULT '',
    activities TEXT NOT NULL DEFAULT '',
    completed INTEGER NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_rest_user ON sync_rest_days("userId");

  CREATE TABLE IF NOT EXISTS sync_ai_chat_history (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    "sessionId" INTEGER NOT NULL DEFAULT 0,
    role TEXT NOT NULL DEFAULT '',
    message TEXT NOT NULL DEFAULT '',
    timestamp BIGINT NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_chat_user ON sync_ai_chat_history("userId");

  CREATE TABLE IF NOT EXISTS sync_subscriptions (
    uuid TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    provider TEXT NOT NULL DEFAULT 'stripe',
    "subscriptionId" TEXT NOT NULL DEFAULT '',
    "planId" TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'inactive',
    "currentPeriodEnd" BIGINT NOT NULL DEFAULT 0,
    "createdAt" BIGINT NOT NULL DEFAULT 0,
    "updatedAt" BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_sub_user ON sync_subscriptions("userId");
`;

async function initSchema() {
  await q(SCHEMA);
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

async function seedBadges() {
  for (const b of SEED_BADGES) {
    await q(
      'INSERT INTO badges ("key", title, description, icon) VALUES ($1, $2, $3, $4) ON CONFLICT ("key") DO NOTHING',
      [b.key, b.title, b.description, b.icon]
    );
  }
}

// =============================================
// HEALTH CHECK
// =============================================
app.get('/health', async (_req, res) => {
  try {
    await q('SELECT 1');
    res.json({ status: 'healthy', uptime: process.uptime() });
  } catch (e) {
    res.status(503).json({ status: 'unhealthy', error: e.message });
  }
});

// =============================================
// USERS
// =============================================

app.post('/users', enforceAuth, async (req, res) => {
  const { id, name, photoUri, fcmToken, totalVolume, workoutCount } = req.body;
  const userId = sanitizeId(id);
  if (!userId) return res.status(400).json({ error: 'id required' });
  if (requireOwnership(req, res, userId) !== true) return;
  const now = Date.now();
  await q(
    `INSERT INTO users (id, name, "photoUri", "fcmToken", "totalVolume", "workoutCount", "lastSeen", "createdAt", "isActive")
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 1)
     ON CONFLICT (id) DO UPDATE SET
       name = CASE WHEN EXCLUDED.name != '' THEN EXCLUDED.name ELSE users.name END,
       "photoUri" = CASE WHEN EXCLUDED."photoUri" != '' THEN EXCLUDED."photoUri" ELSE users."photoUri" END,
       "fcmToken" = CASE WHEN EXCLUDED."fcmToken" != '' THEN EXCLUDED."fcmToken" ELSE users."fcmToken" END,
       "totalVolume" = CASE WHEN EXCLUDED."totalVolume" > 0 THEN EXCLUDED."totalVolume" ELSE users."totalVolume" END,
       "workoutCount" = CASE WHEN EXCLUDED."workoutCount" > 0 THEN EXCLUDED."workoutCount" ELSE users."workoutCount" END,
       "lastSeen" = EXCLUDED."lastSeen",
       "isActive" = 1`,
    [userId, sanitizeString(name), sanitizeString(photoUri), sanitizeString(fcmToken), sanitizeFloat(totalVolume), sanitizeInt(workoutCount), now, now]
  );
  res.json({ id: userId, name: sanitizeString(name), photoUri: sanitizeString(photoUri), isActive: 1 });
});

app.get('/users/search', async (req, res) => {
  const query = sanitizeString(req.query.q || '');
  if (!query) return res.json([]);

  const normalize = (s) => s.toLowerCase()
    .replace(/ș/g, 's').replace(/ț/g, 't').replace(/ă/g, 'a').replace(/â/g, 'a').replace(/î/g, 'i')
    .replace(/ş/g, 's').replace(/ţ/g, 't');

  const normalizedQ = normalize(query);
  const likePattern = `%${normalizedQ}%`;
  const rows = await q(
    `SELECT * FROM users
     WHERE LOWER(name) LIKE $1 OR LOWER(id) LIKE $1
     LIMIT 20`,
    [likePattern]
  );

  res.json(rows);
});

app.get('/users/:id', async (req, res) => {
  const userId = sanitizeId(req.params.id);
  if (!userId) return res.status(400).json({ error: 'invalid id' });
  const user = await qOne('SELECT * FROM users WHERE id = $1', [userId]);
  if (!user) return res.status(404).json({ error: 'not found' });
  res.json(user);
});

app.delete('/users/:id', enforceAuth, async (req, res) => {
  const targetId = sanitizeId(req.params.id);
  if (!targetId) return res.status(400).json({ error: 'invalid id' });
  if (req.authUserId !== targetId) {
    return res.status(403).json({ error: 'can only delete your own account' });
  }
  await qRun('DELETE FROM users WHERE id = $1', [targetId]);
  res.json({ success: true });
});

// =============================================
// FRIENDSHIPS
// =============================================

app.post('/friends/request', enforceAuth, async (req, res) => {
  const { fromUserId, toUserId } = req.body;
  const from = sanitizeId(fromUserId);
  const to = sanitizeId(toUserId);
  if (!from || !to) return res.status(400).json({ error: 'fromUserId and toUserId required' });
  if (from === to) return res.status(400).json({ error: 'cannot send friend request to yourself' });
  if (requireOwnership(req, res, from) !== true) return;
  const now = Date.now();
  await q(
    'INSERT INTO friendships ("userId", "friendId", status, "createdAt") VALUES ($1, $2, $3, $4) ON CONFLICT ("userId", "friendId") DO NOTHING',
    [from, to, 'pending', now]
  );

  const sender = await qOne('SELECT name FROM users WHERE id = $1', [from]);
  const recipient = await qOne('SELECT "fcmToken" FROM users WHERE id = $1', [to]);
  if (recipient && recipient.fcmToken && admin.apps.length > 0) {
    admin.messaging().send({
      token: recipient.fcmToken,
      notification: {
        title: 'Friend Request',
        body: `${sender?.name || 'Someone'} sent you a friend request!`,
      },
      data: { type: 'friend_request', fromUserId: from, fromUserName: sender?.name || '' },
      android: { priority: 'high', notification: { channelId: 'friend_requests', priority: 'max' } },
    }).catch(e => console.log('FCM error:', e.message));
  }

  res.json({ success: true });
});

app.get('/friends/incoming/:userId', async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await q(
    'SELECT * FROM friendships WHERE "friendId" = $1 AND status = $2 ORDER BY "createdAt" DESC',
    [userId, 'pending']
  );
  res.json(rows);
});

app.post('/friends/accept', enforceAuth, async (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await q('UPDATE friendships SET status = $1 WHERE "userId" = $2 AND "friendId" = $3', ['accepted', uid, fid]);
  await q(
    'INSERT INTO friendships ("userId", "friendId", status, "createdAt") VALUES ($1, $2, $3, $4) ON CONFLICT ("userId", "friendId") DO NOTHING',
    [fid, uid, 'accepted', Date.now()]
  );

  const acceptor = await qOne('SELECT name FROM users WHERE id = $1', [uid]);
  const recipient = await qOne('SELECT "fcmToken" FROM users WHERE id = $1', [fid]);
  if (recipient && recipient.fcmToken && admin.apps.length > 0) {
    admin.messaging().send({
      token: recipient.fcmToken,
      notification: { title: 'Friend Request Accepted', body: `${acceptor?.name || 'Someone'} accepted your friend request!` },
      data: { type: 'friend_accepted', fromUserName: acceptor?.name || '' },
      android: { priority: 'high', notification: { channelId: 'kinetic_notifications', priority: 'max' } },
    }).catch(e => console.log('FCM error:', e.message));
  }

  res.json({ success: true });
});

app.post('/friends/reject', enforceAuth, async (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await q('DELETE FROM friendships WHERE "userId" = $1 AND "friendId" = $2', [uid, fid]);
  await q('DELETE FROM friendships WHERE "userId" = $1 AND "friendId" = $2', [fid, uid]);
  res.json({ success: true });
});

app.post('/friends/remove', enforceAuth, async (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await q('DELETE FROM friendships WHERE "userId" = $1 AND "friendId" = $2 AND status = $3', [uid, fid, 'accepted']);
  await q('DELETE FROM friendships WHERE "userId" = $1 AND "friendId" = $2 AND status = $3', [fid, uid, 'accepted']);
  res.json({ success: true });
});

app.get('/friends/:userId', async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await q(
    'SELECT * FROM friendships WHERE ("userId" = $1 OR "friendId" = $1) AND status = $2 ORDER BY "createdAt" DESC',
    [userId, 'accepted']
  );
  res.json(rows);
});

// =============================================
// FEED & POSTS (with pagination)
// =============================================

app.post('/posts', postLimiter, enforceAuth, async (req, res) => {
  const { authorId, content, activityType } = req.body;
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!author || !text) return res.status(400).json({ error: 'authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = await qOne(
    'INSERT INTO feed_posts ("authorId", content, "activityType", "createdAt") VALUES ($1, $2, $3, $4) RETURNING id',
    [author, text, sanitizeString(activityType) || 'post', now]
  );
  res.json({ postId: result.id });
});

app.get('/feed', async (req, res) => {
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = await q('SELECT * FROM feed_posts ORDER BY "createdAt" DESC LIMIT $1 OFFSET $2', [limit, offset]);
  const totalRow = await qOne('SELECT COUNT(*)::int AS c FROM feed_posts');
  const total = totalRow.c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
});

app.get('/posts/author/:authorId', async (req, res) => {
  const authorId = sanitizeId(req.params.authorId);
  if (!authorId) return res.status(400).json({ error: 'invalid authorId' });
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = await q(
    'SELECT * FROM feed_posts WHERE "authorId" = $1 ORDER BY "createdAt" DESC LIMIT $2 OFFSET $3',
    [authorId, limit, offset]
  );
  const totalRow = await qOne('SELECT COUNT(*)::int AS c FROM feed_posts WHERE "authorId" = $1', [authorId]);
  const total = totalRow.c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
});

// =============================================
// COMMENTS & LIKES
// =============================================

app.post('/comments', postLimiter, enforceAuth, async (req, res) => {
  const { postId, authorId, content } = req.body;
  const pid = sanitizeInt(postId, 0, 1, 1e9);
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!pid || !author || !text) return res.status(400).json({ error: 'postId, authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = await qOne(
    'INSERT INTO comments ("postId", "authorId", content, "createdAt") VALUES ($1, $2, $3, $4) RETURNING id',
    [pid, author, text, now]
  );
  res.json({ commentId: result.id });
});

app.get('/comments/:postId', async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  if (!postId) return res.status(400).json({ error: 'invalid postId' });
  const rows = await q('SELECT * FROM comments WHERE "postId" = $1 ORDER BY "createdAt" ASC', [postId]);
  res.json(rows);
});

app.post('/posts/:postId/like', enforceAuth, async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  await q(
    'INSERT INTO likes ("postId", "userId", "createdAt") VALUES ($1, $2, $3) ON CONFLICT ("postId", "userId") DO NOTHING',
    [postId, uid, Date.now()]
  );
  res.json({ success: true });
});

app.delete('/posts/:postId/like', enforceAuth, async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.query.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, userId) !== true) return;
  await q('DELETE FROM likes WHERE "postId" = $1 AND "userId" = $2', [postId, userId]);
  res.json({ success: true });
});

app.get('/posts/:postId/likes/count', async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const row = await qOne('SELECT COUNT(*)::int AS count FROM likes WHERE "postId" = $1', [postId]);
  res.json({ count: row.count });
});

app.get('/posts/:postId/liked/:userId', async (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  const liked = await qOne('SELECT 1 FROM likes WHERE "postId" = $1 AND "userId" = $2', [postId, userId]);
  res.json({ liked: !!liked });
});

// =============================================
// LEADERBOARD (with pagination)
// =============================================

app.post('/leaderboard', enforceAuth, async (req, res) => {
  const { userId, metric, value, periodStart, periodEnd } = req.body;
  const uid = sanitizeId(userId);
  const met = sanitizeString(metric);
  if (!uid || !met) return res.status(400).json({ error: 'userId and metric required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();
  await q(
    'INSERT INTO leaderboard_entries ("userId", metric, value, "periodStart", "periodEnd") VALUES ($1, $2, $3, $4, $5) ON CONFLICT ("userId", metric, "periodStart") DO UPDATE SET value = EXCLUDED.value',
    [uid, met, sanitizeFloat(value), sanitizeInt(periodStart, now), sanitizeInt(periodEnd, now)]
  );
  res.json({ success: true });
});

app.get('/leaderboard', async (req, res) => {
  const metric = sanitizeString(req.query.metric) || 'workouts';
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);

  if (metric === 'volume') {
    const rows = await q(
      'SELECT id AS "userId", name, "photoUri", "totalVolume" AS value, "workoutCount" FROM users WHERE "totalVolume" > 0 ORDER BY "totalVolume" DESC LIMIT $1 OFFSET $2',
      [limit, offset]
    );
    const totalRow = await qOne('SELECT COUNT(*)::int AS c FROM users WHERE "totalVolume" > 0');
    const total = totalRow.c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  } else {
    const rows = await q(
      'SELECT * FROM leaderboard_entries WHERE metric = $1 ORDER BY value DESC LIMIT $2 OFFSET $3',
      [metric, limit, offset]
    );
    const totalRow = await qOne('SELECT COUNT(*)::int AS c FROM leaderboard_entries WHERE metric = $1', [metric]);
    const total = totalRow.c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  }
});

// =============================================
// WORKOUTS / STREAKS / BADGES
// =============================================

app.post('/workouts/log', enforceAuth, async (req, res) => {
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();

  const streak = await qOne('SELECT * FROM streaks WHERE "userId" = $1', [uid]);
  let currentStreak = 1;
  let bestStreak = 1;
  let lastDate = now;

  if (streak) {
    const last = streak.lastDate;
    const diffHours = (now - last) / (1000 * 60 * 60);
    if (diffHours >= 24 && diffHours <= 48) {
      currentStreak = streak.currentStreak + 1;
    } else if (diffHours > 48) {
      currentStreak = 1;
    } else {
      currentStreak = streak.currentStreak;
    }
    bestStreak = Math.max(currentStreak, streak.bestStreak);
    lastDate = now;
  }

  await q(
    `INSERT INTO streaks ("userId", "currentStreak", "bestStreak", "lastDate")
     VALUES ($1, $2, $3, $4)
     ON CONFLICT ("userId") DO UPDATE SET
       "currentStreak" = EXCLUDED."currentStreak",
       "bestStreak" = EXCLUDED."bestStreak",
       "lastDate" = EXCLUDED."lastDate"`,
    [uid, currentStreak, bestStreak, lastDate]
  );

  const wRow = await qOne(`SELECT COUNT(*)::int AS c FROM feed_posts WHERE "authorId" = $1 AND "activityType" = 'workout'`, [uid]);
  const cRow = await qOne(`SELECT COUNT(*)::int AS c FROM comments WHERE "authorId" = $1`, [uid]);
  const fRow = await qOne(`SELECT COUNT(*)::int AS c FROM friendships WHERE "userId" = $1 AND status = 'accepted'`, [uid]);
  const workoutCount = wRow.c;
  const commentCount = cRow.c;
  const friendCount = fRow.c;

  const newlyAwardedBadges = [];
  const hasBadge = async (key) => {
    const r = await qOne('SELECT 1 FROM user_badges WHERE "userId" = $1 AND "badgeKey" = $2', [uid, key]);
    return !!r;
  };
  const awardBadge = async (key) => {
    await q('INSERT INTO user_badges ("userId", "badgeKey", "awardedAt") VALUES ($1, $2, $3) ON CONFLICT ("userId", "badgeKey") DO NOTHING', [uid, key, now]);
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
});

app.get('/streaks/:userId', async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const streak = await qOne('SELECT * FROM streaks WHERE "userId" = $1', [userId]);
  res.json(streak || { userId, currentStreak: 0, bestStreak: 0, lastDate: 0 });
});

app.get('/badges', async (_req, res) => {
  const rows = await q('SELECT * FROM badges');
  res.json(rows);
});

app.get('/badges/user/:userId', async (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = await q('SELECT * FROM user_badges WHERE "userId" = $1 ORDER BY "awardedAt" DESC', [userId]);
  res.json(rows);
});

app.post('/badges/award', enforceAuth, async (req, res) => {
  const { userId, badgeKey } = req.body;
  const uid = sanitizeId(userId);
  const key = sanitizeString(badgeKey);
  if (!uid || !key) return res.status(400).json({ error: 'userId and badgeKey required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const existing = await qOne('SELECT 1 FROM user_badges WHERE "userId" = $1 AND "badgeKey" = $2', [uid, key]);
  if (existing) return res.json({ success: false, alreadyAwarded: true });
  await q('INSERT INTO user_badges ("userId", "badgeKey", "awardedAt") VALUES ($1, $2, $3)', [uid, key, Date.now()]);
  res.json({ success: true });
});

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

// Coloanele camelCase se pun între ghilimele (identic cu schema).
const quoteCol = (c) => `"${c}"`;

Object.entries(SYNC_TABLES).forEach(([table, config]) => {
  const tableName = `sync_${table}`;
  const quotedCols = config.upsertCols.map(quoteCol);
  const placeholders = config.upsertCols.map((_, i) => `$${i + 1}`).join(',');
  const updateClauses = config.upsertCols.filter(c => c !== 'uuid').map(c => `${quoteCol(c)}=EXCLUDED.${quoteCol(c)}`).join(', ');
  const upsertSql = `INSERT INTO ${tableName} (${quotedCols.join(',')}) VALUES (${placeholders}) ON CONFLICT (uuid) DO UPDATE SET ${updateClauses}`;

  const sanitizeRow = (item) => config.upsertCols.map((c) => {
    if (c === 'uuid') return sanitizeId(item.uuid);
    const val = item[c];
    if (typeof val === 'string') return sanitizeString(val);
    if (typeof val === 'number') return val;
    if (typeof val === 'boolean') return val ? 1 : 0;
    return val ?? '';
  });

  app.get(`/sync/${table}/:userId`, async (req, res) => {
    const userId = sanitizeId(req.params.userId);
    if (!userId) return res.status(400).json({ error: 'invalid userId' });
    const since = parseInt(req.query.since, 10) || 0;
    let rows;
    if (config.userCol) {
      rows = since > 0
        ? await q(`SELECT * FROM ${tableName} WHERE ${quoteCol(config.userCol)} = $1 AND "updatedAt" > $2`, [userId, since])
        : await q(`SELECT * FROM ${tableName} WHERE ${quoteCol(config.userCol)} = $1`, [userId]);
    } else {
      rows = since > 0
        ? await q(`SELECT * FROM ${tableName} WHERE "updatedAt" > $1`, [since])
        : await q(`SELECT * FROM ${tableName}`);
    }
    res.json(rows);
  });

  app.post(`/sync/${table}/upsert`, enforceAuth, async (req, res) => {
    const item = req.body;
    if (!item || !item.uuid) return res.status(400).json({ error: 'uuid required' });
    if (config.userCol) {
      const owner = sanitizeId(item[config.userCol]);
      if (requireOwnership(req, res, owner) !== true) return;
    }
    const values = sanitizeRow(item);
    await q(upsertSql, values);
    res.json({ success: true });
  });

  app.post(`/sync/${table}/bulk`, enforceAuth, async (req, res) => {
    const items = req.body.items;
    if (!Array.isArray(items)) return res.status(400).json({ error: 'items array required' });
    if (config.userCol) {
      for (const item of items) {
        if (!item.uuid) continue;
        const owner = sanitizeId(item[config.userCol]);
        if (requireOwnership(req, res, owner) !== true) return;
      }
    }
    await withTransaction(async (client) => {
      for (const item of items) {
        if (!item.uuid) continue;
        await client.query(upsertSql, sanitizeRow(item));
      }
    });
    res.json({ success: true, count: items.length });
  });

  app.delete(`/sync/${table}/:uuid`, enforceAuth, async (req, res) => {
    const uuid = sanitizeId(req.params.uuid);
    if (!uuid) return res.status(400).json({ error: 'invalid uuid' });
    if (config.userCol) {
      const row = await qOne(`SELECT ${quoteCol(config.userCol)} FROM ${tableName} WHERE uuid = $1`, [uuid]);
      if (!row) return res.status(404).json({ error: 'not found' });
      if (requireOwnership(req, res, sanitizeId(row[config.userCol])) !== true) return;
    }
    await qRun(`DELETE FROM ${tableName} WHERE uuid = $1`, [uuid]);
    res.json({ success: true });
  });
});

// =============================================
// ADMIN: CLEANUP TEST USERS
// =============================================
// Șterge complet conturile de test (id TEST* sau nume care conține "Test") din toate
// tabelele: users, friendships, leaderboard, streaks, badges, posts, comments, likes
// și toate tabelele sync_*. Protejat cu header X-Admin-Key.
app.post('/admin/cleanup-test-users', async (req, res) => {
  const adminKey = process.env.ADMIN_KEY || 'kinetic-cleanup-2024';
  if (req.headers['x-admin-key'] !== adminKey) {
    return res.status(401).json({ error: 'Invalid admin key' });
  }

  const targets = await q(`SELECT id, name FROM users WHERE id LIKE 'TEST%' OR name LIKE '%Test%'`);
  if (targets.length === 0) return res.json({ success: true, deleted: 0 });

  const ids = targets.map(t => t.id);
  const ph = ids.map((_, i) => `$${i + 1}`).join(',');
  const phBoth = ids.map((_, i) => `$${i + 1}`).concat(ids.map((_, i) => `$${ids.length + i + 1}`)).join(',');
  const dupArgs = [...ids, ...ids];

  await withTransaction(async (client) => {
    // Prietenii (ambele direcții) și cereri pendente
    await client.query(`DELETE FROM friendships WHERE "userId" IN (${phBoth}) OR "friendId" IN (${phBoth})`, [...dupArgs, ...dupArgs]);
    // Leaderboard
    await client.query(`DELETE FROM leaderboard_entries WHERE "userId" IN (${ph})`, ids);
    // Streaks + badges
    await client.query(`DELETE FROM streaks WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM user_badges WHERE "userId" IN (${ph})`, ids);
    // Posturi ale userilor de test + comentariile/like-urile lor
    const posts = await client.query(`SELECT id FROM feed_posts WHERE "authorId" IN (${ph})`, ids);
    const postIds = posts.rows.map(p => p.id);
    if (postIds.length > 0) {
      const pph = postIds.map((_, i) => `$${i + 1}`).join(',');
      await client.query(`DELETE FROM comments WHERE "postId" IN (${pph})`, postIds);
      await client.query(`DELETE FROM likes WHERE "postId" IN (${pph})`, postIds);
      await client.query(`DELETE FROM feed_posts WHERE id IN (${pph})`, postIds);
    }
    await client.query(`DELETE FROM comments WHERE "authorId" IN (${ph})`, ids);
    await client.query(`DELETE FROM likes WHERE "userId" IN (${ph})`, ids);
    // Toate tabelele sync_ cu coloană userId
    for (const [table, cfg] of Object.entries(SYNC_TABLES)) {
      if (cfg.userCol) {
        await client.query(`DELETE FROM sync_${table} WHERE ${quoteCol(cfg.userCol)} IN (${ph})`, ids);
      }
    }
    // Tabele sync cu coloană userId definite manual
    await client.query(`DELETE FROM sync_antrenamente WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM sync_templates WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM sync_personal_records WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM sync_muscle_recovery WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM sync_exercise_metadata WHERE "userId" IN (${ph})`, ids);
    await client.query(`DELETE FROM sync_biometric_entries WHERE "userId" IN (${ph})`, ids);
    // În final, userii înșiși
    await client.query(`DELETE FROM users WHERE id IN (${ph})`, ids);
  });

  res.json({ success: true, deleted: targets.length, users: targets.map(t => t.id) });
});

// =============================================
// NOTIFICATIONS: BROADCAST (FCM → toți utilizatorii)
// =============================================
// Trimite o notificare push la toți utilizatorii cu fcmToken stocat (ex: anunț
// pentru un release nou). Protejat cu header X-Admin-Key (același ca la cleanup).
// Body: { title, body, data?: {k:v}, channelId?, dryRun? }
app.post('/notifications/broadcast', async (req, res) => {
  const adminKey = process.env.ADMIN_KEY || 'kinetic-cleanup-2024';
  if (req.headers['x-admin-key'] !== adminKey) {
    return res.status(401).json({ error: 'Invalid admin key' });
  }
  if (admin.apps.length === 0) {
    return res.status(503).json({ error: 'Firebase Admin not initialized - push notifications disabled' });
  }

  const { title, body, data, channelId, dryRun } = req.body || {};
  const safeTitle = sanitizeString(title || 'Kinetic');
  const safeBody = sanitizeString(body || '');
  if (!safeBody && !Object.keys(data || {}).length) {
    return res.status(400).json({ error: 'body or data required' });
  }

  const tokens = (await q(`SELECT "fcmToken" FROM users WHERE "fcmToken" != ''`)).map(r => r.fcmToken);
  if (tokens.length === 0) {
    return res.json({ success: true, sent: 0, failed: 0, total: 0, dryRun: dryRun === true });
  }

  const payloadData = {};
  for (const [k, v] of Object.entries(data || {})) {
    if (typeof v === 'string') payloadData[k] = sanitizeString(v);
  }

  const android = { priority: 'high', notification: { channelId: sanitizeString(channelId) || 'kinetic_notifications', priority: 'max' } };

  // FCM acceptă max 500 token-uri pe mesaj multicast → trimitem în bucăți.
  const CHUNK = 500;
  let sent = 0;
  let failed = 0;
  for (let i = 0; i < tokens.length; i += CHUNK) {
    const chunk = tokens.slice(i, i + CHUNK);
    try {
      const result = await admin.messaging().sendEachForMulticast({
        tokens: chunk,
        notification: { title: safeTitle, body: safeBody },
        data: payloadData,
        android,
        dryRun: dryRun === true,
      });
      sent += result.successCount;
      failed += result.failureCount;
    } catch (e) {
      console.log('FCM broadcast chunk error:', e.message);
      failed += chunk.length;
    }
  }

  res.json({ success: true, total: tokens.length, sent, failed, dryRun: dryRun === true });
});

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
app.get('/', async (_req, res) => {
  const u = await qOne('SELECT COUNT(*)::int AS c FROM users');
  const p = await qOne('SELECT COUNT(*)::int AS c FROM feed_posts');
  const f = await qOne(`SELECT COUNT(*)::int AS c FROM friendships WHERE status = 'accepted'`);
  res.json({
    name: 'Kinetic API',
    version: '2.0.0',
    status: 'running',
    stats: { users: u.c, posts: p.c, friendships: f.c },
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
});

async function start() {
  try {
    await initSchema();
    await seedBadges();
    console.log('Schema + seed badges OK');
  } catch (e) {
    console.error('Schema init failed:', e.message);
  }
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Kinetic backend v2.0.0 running on http://0.0.0.0:${PORT}`);
  });
}

start();