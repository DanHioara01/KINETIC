const express = require('express');
const cors = require('cors');
const Database = require('better-sqlite3');
const path = require('path');
const admin = require('firebase-admin');
const rateLimit = require('express-rate-limit');

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
// DATABASE
// =============================================
const db = new Database(path.join(__dirname, 'kinetic.db'));
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

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
// SCHEMA
// =============================================
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    photoUri TEXT NOT NULL DEFAULT '',
    fcmToken TEXT NOT NULL DEFAULT '',
    totalVolume REAL NOT NULL DEFAULT 0,
    workoutCount INTEGER NOT NULL DEFAULT 0,
    lastSeen INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL DEFAULT 0,
    isActive INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

  CREATE TABLE IF NOT EXISTS friendships (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId TEXT NOT NULL,
    friendId TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    createdAt INTEGER NOT NULL DEFAULT 0,
    UNIQUE(userId, friendId)
  );
  CREATE INDEX IF NOT EXISTS idx_friends_user ON friendships(userId);
  CREATE INDEX IF NOT EXISTS idx_friends_friend ON friendships(friendId);

  CREATE TABLE IF NOT EXISTS feed_posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    authorId TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    activityType TEXT NOT NULL DEFAULT 'post',
    createdAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_posts_author ON feed_posts(authorId);
  CREATE INDEX IF NOT EXISTS idx_posts_created ON feed_posts(createdAt DESC);

  CREATE TABLE IF NOT EXISTS comments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    postId INTEGER NOT NULL,
    authorId TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    createdAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(postId);

  CREATE TABLE IF NOT EXISTS likes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    postId INTEGER NOT NULL,
    userId TEXT NOT NULL,
    createdAt INTEGER NOT NULL DEFAULT 0,
    UNIQUE(postId, userId)
  );
  CREATE INDEX IF NOT EXISTS idx_likes_post ON likes(postId);

  CREATE TABLE IF NOT EXISTS leaderboard_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId TEXT NOT NULL,
    metric TEXT NOT NULL,
    value REAL NOT NULL DEFAULT 0,
    periodStart INTEGER NOT NULL DEFAULT 0,
    periodEnd INTEGER NOT NULL DEFAULT 0,
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
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId TEXT NOT NULL,
    badgeKey TEXT NOT NULL,
    awardedAt INTEGER NOT NULL DEFAULT 0,
    UNIQUE(userId, badgeKey)
  );
  CREATE INDEX IF NOT EXISTS idx_ub_user ON user_badges(userId);

  CREATE TABLE IF NOT EXISTS streaks (
    userId TEXT PRIMARY KEY,
    currentStreak INTEGER NOT NULL DEFAULT 0,
    bestStreak INTEGER NOT NULL DEFAULT 0,
    lastDate INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_antrenamente (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    grupaMusculara TEXT NOT NULL DEFAULT '',
    data INTEGER NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    totalWeight REAL NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_antren_user ON sync_antrenamente(userId);

  CREATE TABLE IF NOT EXISTS sync_exercitii (
    uuid TEXT PRIMARY KEY,
    antrenamentUuid TEXT NOT NULL DEFAULT '',
    numeExercitiu TEXT NOT NULL DEFAULT '',
    setIndex INTEGER NOT NULL DEFAULT 0,
    greutateKg REAL NOT NULL DEFAULT 0,
    repetari INTEGER NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_exerc_user ON sync_exercitii(antrenamentUuid);

  CREATE TABLE IF NOT EXISTS sync_exercises (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    groupName TEXT NOT NULL DEFAULT '',
    equipment TEXT NOT NULL DEFAULT '',
    isDefault INTEGER NOT NULL DEFAULT 1,
    isFavorite INTEGER NOT NULL DEFAULT 0,
    usageCount INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_templates (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_user ON sync_templates(userId);

  CREATE TABLE IF NOT EXISTS sync_template_exercises (
    uuid TEXT PRIMARY KEY,
    templateUuid TEXT NOT NULL DEFAULT '',
    exerciseName TEXT NOT NULL DEFAULT '',
    groupName TEXT NOT NULL DEFAULT '',
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_templ_ex ON sync_template_exercises(templateUuid);

  CREATE TABLE IF NOT EXISTS sync_personal_records (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    exerciseName TEXT NOT NULL DEFAULT '',
    weight REAL NOT NULL DEFAULT 0,
    reps INTEGER NOT NULL DEFAULT 0,
    volume REAL NOT NULL DEFAULT 0,
    date INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_pr_user ON sync_personal_records(userId);

  CREATE TABLE IF NOT EXISTS sync_muscle_recovery (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL DEFAULT '',
    grupaMusculara TEXT NOT NULL DEFAULT '',
    level REAL NOT NULL DEFAULT 0,
    lastUpdated INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_exercise_metadata (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL DEFAULT '',
    exerciseName TEXT NOT NULL DEFAULT '',
    grupaMusculara TEXT NOT NULL DEFAULT '',
    isFavorite INTEGER NOT NULL DEFAULT 0,
    isCustom INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS sync_biometric_entries (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    timestamp INTEGER NOT NULL DEFAULT 0,
    weightKg REAL NOT NULL DEFAULT 0,
    bodyFatPercent REAL NOT NULL DEFAULT 0,
    waistCm REAL NOT NULL DEFAULT 0,
    hipsCm REAL NOT NULL DEFAULT 0,
    thighsCm REAL NOT NULL DEFAULT 0,
    chestCm REAL NOT NULL DEFAULT 0,
    armsCm REAL NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_bio_user ON sync_biometric_entries(userId);

  CREATE TABLE IF NOT EXISTS sync_food_entries (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    barcode TEXT NOT NULL DEFAULT '',
    name TEXT NOT NULL DEFAULT '',
    brand TEXT NOT NULL DEFAULT '',
    mealType TEXT NOT NULL DEFAULT 'snack',
    servingSize REAL NOT NULL DEFAULT 100,
    servingUnit TEXT NOT NULL DEFAULT 'g',
    calories REAL NOT NULL DEFAULT 0,
    proteinG REAL NOT NULL DEFAULT 0,
    carbsG REAL NOT NULL DEFAULT 0,
    fatG REAL NOT NULL DEFAULT 0,
    fiberG REAL NOT NULL DEFAULT 0,
    timestamp INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_food_user ON sync_food_entries(userId);

  CREATE TABLE IF NOT EXISTS sync_cardio_routes (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT '',
    routePoints TEXT NOT NULL DEFAULT '',
    distanceKm REAL NOT NULL DEFAULT 0,
    durationMs INTEGER NOT NULL DEFAULT 0,
    avgSpeedKmh REAL NOT NULL DEFAULT 0,
    avgPaceMinKm REAL NOT NULL DEFAULT 0,
    caloriesBurned REAL NOT NULL DEFAULT 0,
    startTime INTEGER NOT NULL DEFAULT 0,
    endTime INTEGER NOT NULL DEFAULT 0,
    activityType TEXT NOT NULL DEFAULT 'running',
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_cardio_user ON sync_cardio_routes(userId);

  CREATE TABLE IF NOT EXISTS sync_rest_days (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    date INTEGER NOT NULL DEFAULT 0,
    type TEXT NOT NULL DEFAULT 'rest',
    notes TEXT NOT NULL DEFAULT '',
    activities TEXT NOT NULL DEFAULT '',
    completed INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_rest_user ON sync_rest_days(userId);

  CREATE TABLE IF NOT EXISTS sync_ai_chat_history (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    sessionId INTEGER NOT NULL DEFAULT 0,
    role TEXT NOT NULL DEFAULT '',
    message TEXT NOT NULL DEFAULT '',
    timestamp INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_chat_user ON sync_ai_chat_history(userId);

  CREATE TABLE IF NOT EXISTS sync_subscriptions (
    uuid TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    provider TEXT NOT NULL DEFAULT 'stripe',
    subscriptionId TEXT NOT NULL DEFAULT '',
    planId TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'inactive',
    currentPeriodEnd INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL DEFAULT 0
  );
  CREATE INDEX IF NOT EXISTS idx_sync_sub_user ON sync_subscriptions(userId);
`);

try {
  db.prepare('SELECT isActive FROM users LIMIT 1').get();
} catch (e) {
  try {
    db.exec('ALTER TABLE users ADD COLUMN isActive INTEGER NOT NULL DEFAULT 0');
    db.exec('UPDATE users SET isActive = 1 WHERE lastSeen > 0');
    console.log('Migration: added isActive column to users table');
  } catch (e2) {
    console.log('Migration skipped:', e2.message);
  }
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
const insertBadge = db.prepare('INSERT OR IGNORE INTO badges (key, title, description, icon) VALUES (?, ?, ?, ?)');
for (const b of SEED_BADGES) {
  insertBadge.run(b.key, b.title, b.description, b.icon);
}

// =============================================
// HEALTH CHECK
// =============================================
app.get('/health', (_req, res) => {
  try {
    db.prepare('SELECT 1').get();
    res.json({ status: 'healthy', uptime: process.uptime() });
  } catch (e) {
    res.status(503).json({ status: 'unhealthy', error: e.message });
  }
});

// =============================================
// USERS
// =============================================

app.post('/users', enforceAuth, (req, res) => {
  const { id, name, photoUri, fcmToken, totalVolume, workoutCount } = req.body;
  const userId = sanitizeId(id);
  if (!userId) return res.status(400).json({ error: 'id required' });
  if (requireOwnership(req, res, userId) !== true) return;
  const now = Date.now();
  db.prepare(`INSERT INTO users (id, name, photoUri, fcmToken, totalVolume, workoutCount, lastSeen, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1) ON CONFLICT(id) DO UPDATE SET name=CASE WHEN excluded.name != '' THEN excluded.name ELSE users.name END, photoUri=CASE WHEN excluded.photoUri != '' THEN excluded.photoUri ELSE users.photoUri END, fcmToken=CASE WHEN excluded.fcmToken != '' THEN excluded.fcmToken ELSE users.fcmToken END, totalVolume=CASE WHEN excluded.totalVolume > 0 THEN excluded.totalVolume ELSE users.totalVolume END, workoutCount=CASE WHEN excluded.workoutCount > 0 THEN excluded.workoutCount ELSE users.workoutCount END, lastSeen=excluded.lastSeen, isActive=1`)
    .run(userId, sanitizeString(name), sanitizeString(photoUri), sanitizeString(fcmToken), sanitizeFloat(totalVolume), sanitizeInt(workoutCount), now, now);
  res.json({ id: userId, name: sanitizeString(name), photoUri: sanitizeString(photoUri), isActive: 1 });
});

app.get('/users/search', (req, res) => {
  const q = sanitizeString(req.query.q || '');
  if (!q) return res.json([]);

  const normalize = (s) => s.toLowerCase()
    .replace(/ș/g,'s').replace(/ț/g,'t').replace(/ă/g,'a').replace(/â/g,'a').replace(/î/g,'i')
    .replace(/ş/g,'s').replace(/ţ/g,'t');

  const normalizedQ = normalize(q);
  const likePattern = `%${normalizedQ}%`;
  const rows = db.prepare(
    `SELECT * FROM users
     WHERE LOWER(name) LIKE ? OR LOWER(id) LIKE ?
     LIMIT 20`
  ).all(likePattern, likePattern);

  res.json(rows);
});

app.get('/users/:id', (req, res) => {
  const userId = sanitizeId(req.params.id);
  if (!userId) return res.status(400).json({ error: 'invalid id' });
  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  if (!user) return res.status(404).json({ error: 'not found' });
  res.json(user);
});

app.delete('/users/:id', enforceAuth, (req, res) => {
  const targetId = sanitizeId(req.params.id);
  if (!targetId) return res.status(400).json({ error: 'invalid id' });
  if (req.authUserId !== targetId) {
    return res.status(403).json({ error: 'can only delete your own account' });
  }
  db.prepare('DELETE FROM users WHERE id = ?').run(targetId);
  res.json({ success: true });
});

// =============================================
// FRIENDSHIPS
// =============================================

app.post('/friends/request', enforceAuth, (req, res) => {
  const { fromUserId, toUserId } = req.body;
  const from = sanitizeId(fromUserId);
  const to = sanitizeId(toUserId);
  if (!from || !to) return res.status(400).json({ error: 'fromUserId and toUserId required' });
  if (from === to) return res.status(400).json({ error: 'cannot send friend request to yourself' });
  if (requireOwnership(req, res, from) !== true) return;
  const now = Date.now();
  db.prepare('INSERT OR IGNORE INTO friendships (userId, friendId, status, createdAt) VALUES (?, ?, ?, ?)')
    .run(from, to, 'pending', now);

  const sender = db.prepare('SELECT name FROM users WHERE id = ?').get(from);
  const recipient = db.prepare('SELECT fcmToken FROM users WHERE id = ?').get(to);
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

app.get('/friends/incoming/:userId', (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = db.prepare('SELECT * FROM friendships WHERE friendId = ? AND status = ? ORDER BY createdAt DESC')
    .all(userId, 'pending');
  res.json(rows);
});

app.post('/friends/accept', enforceAuth, (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  db.prepare('UPDATE friendships SET status = ? WHERE userId = ? AND friendId = ?').run('accepted', uid, fid);
  db.prepare('INSERT OR IGNORE INTO friendships (userId, friendId, status, createdAt) VALUES (?, ?, ?, ?)').run(fid, uid, 'accepted', Date.now());

  const acceptor = db.prepare('SELECT name FROM users WHERE id = ?').get(uid);
  const recipient = db.prepare('SELECT fcmToken FROM users WHERE id = ?').get(fid);
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

app.post('/friends/reject', enforceAuth, (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  db.prepare('DELETE FROM friendships WHERE userId = ? AND friendId = ?').run(uid, fid);
  db.prepare('DELETE FROM friendships WHERE userId = ? AND friendId = ?').run(fid, uid);
  res.json({ success: true });
});

app.post('/friends/remove', enforceAuth, (req, res) => {
  const { userId, friendId } = req.body;
  const uid = sanitizeId(userId);
  const fid = sanitizeId(friendId);
  if (!uid || !fid) return res.status(400).json({ error: 'userId and friendId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  db.prepare('DELETE FROM friendships WHERE userId = ? AND friendId = ? AND status = ?').run(uid, fid, 'accepted');
  db.prepare('DELETE FROM friendships WHERE userId = ? AND friendId = ? AND status = ?').run(fid, uid, 'accepted');
  res.json({ success: true });
});

app.get('/friends/:userId', (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = db.prepare('SELECT * FROM friendships WHERE (userId = ? OR friendId = ?) AND status = ? ORDER BY createdAt DESC')
    .all(userId, userId, 'accepted');
  res.json(rows);
});

// =============================================
// FEED & POSTS (with pagination)
// =============================================

app.post('/posts', postLimiter, enforceAuth, (req, res) => {
  const { authorId, content, activityType } = req.body;
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!author || !text) return res.status(400).json({ error: 'authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = db.prepare('INSERT INTO feed_posts (authorId, content, activityType, createdAt) VALUES (?, ?, ?, ?)')
    .run(author, text, sanitizeString(activityType) || 'post', now);
  res.json({ postId: result.lastInsertRowid });
});

app.get('/feed', (req, res) => {
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = db.prepare('SELECT * FROM feed_posts ORDER BY createdAt DESC LIMIT ? OFFSET ?').all(limit, offset);
  const total = db.prepare('SELECT COUNT(*) as c FROM feed_posts').get().c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
});

app.get('/posts/author/:authorId', (req, res) => {
  const authorId = sanitizeId(req.params.authorId);
  if (!authorId) return res.status(400).json({ error: 'invalid authorId' });
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);
  const rows = db.prepare('SELECT * FROM feed_posts WHERE authorId = ? ORDER BY createdAt DESC LIMIT ? OFFSET ?')
    .all(authorId, limit, offset);
  const total = db.prepare('SELECT COUNT(*) as c FROM feed_posts WHERE authorId = ?').get(authorId).c;
  res.json({ posts: rows, total, hasMore: offset + limit < total });
});

// =============================================
// COMMENTS & LIKES
// =============================================

app.post('/comments', postLimiter, enforceAuth, (req, res) => {
  const { postId, authorId, content } = req.body;
  const pid = sanitizeInt(postId, 0, 1, 1e9);
  const author = sanitizeId(authorId);
  const text = sanitizeString(content);
  if (!pid || !author || !text) return res.status(400).json({ error: 'postId, authorId and content required' });
  if (requireOwnership(req, res, author) !== true) return;
  const now = Date.now();
  const result = db.prepare('INSERT INTO comments (postId, authorId, content, createdAt) VALUES (?, ?, ?, ?)')
    .run(pid, author, text, now);
  res.json({ commentId: result.lastInsertRowid });
});

app.get('/comments/:postId', (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  if (!postId) return res.status(400).json({ error: 'invalid postId' });
  const rows = db.prepare('SELECT * FROM comments WHERE postId = ? ORDER BY createdAt ASC').all(postId);
  res.json(rows);
});

app.post('/posts/:postId/like', enforceAuth, (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  db.prepare('INSERT OR IGNORE INTO likes (postId, userId, createdAt) VALUES (?, ?, ?)').run(postId, uid, Date.now());
  res.json({ success: true });
});

app.delete('/posts/:postId/like', enforceAuth, (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.query.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, userId) !== true) return;
  db.prepare('DELETE FROM likes WHERE postId = ? AND userId = ?').run(postId, userId);
  res.json({ success: true });
});

app.get('/posts/:postId/likes/count', (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const row = db.prepare('SELECT COUNT(*) as count FROM likes WHERE postId = ?').get(postId);
  res.json({ count: row.count });
});

app.get('/posts/:postId/liked/:userId', (req, res) => {
  const postId = sanitizeInt(req.params.postId, 0, 1, 1e9);
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'userId required' });
  const liked = db.prepare('SELECT 1 FROM likes WHERE postId = ? AND userId = ?').get(postId, userId);
  res.json({ liked: !!liked });
});

// =============================================
// LEADERBOARD (with pagination)
// =============================================

app.post('/leaderboard', enforceAuth, (req, res) => {
  const { userId, metric, value, periodStart, periodEnd } = req.body;
  const uid = sanitizeId(userId);
  const met = sanitizeString(metric);
  if (!uid || !met) return res.status(400).json({ error: 'userId and metric required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();
  db.prepare('INSERT INTO leaderboard_entries (userId, metric, value, periodStart, periodEnd) VALUES (?, ?, ?, ?, ?) ON CONFLICT(userId, metric, periodStart) DO UPDATE SET value=excluded.value')
    .run(uid, met, sanitizeFloat(value), sanitizeInt(periodStart, now), sanitizeInt(periodEnd, now));
  res.json({ success: true });
});

app.get('/leaderboard', (req, res) => {
  const metric = sanitizeString(req.query.metric) || 'workouts';
  const limit = sanitizeInt(req.query.limit, 50, 1, 100);
  const offset = sanitizeInt(req.query.offset, 0, 0, 10000);

  if (metric === 'volume') {
    const rows = db.prepare(
      'SELECT id as userId, name, photoUri, totalVolume as value, workoutCount FROM users WHERE totalVolume > 0 ORDER BY totalVolume DESC LIMIT ? OFFSET ?'
    ).all(limit, offset);
    const total = db.prepare('SELECT COUNT(*) as c FROM users WHERE totalVolume > 0').get().c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  } else {
    const rows = db.prepare(
      'SELECT * FROM leaderboard_entries WHERE metric = ? ORDER BY value DESC LIMIT ? OFFSET ?'
    ).all(metric, limit, offset);
    const total = db.prepare('SELECT COUNT(*) as c FROM leaderboard_entries WHERE metric = ?').get(metric).c;
    res.json({ entries: rows, total, hasMore: offset + limit < total });
  }
});

// =============================================
// WORKOUTS / STREAKS / BADGES
// =============================================

app.post('/workouts/log', enforceAuth, (req, res) => {
  const { userId } = req.body;
  const uid = sanitizeId(userId);
  if (!uid) return res.status(400).json({ error: 'userId required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const now = Date.now();

  const streak = db.prepare('SELECT * FROM streaks WHERE userId = ?').get(uid);
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

  db.prepare('INSERT INTO streaks (userId, currentStreak, bestStreak, lastDate) VALUES (?, ?, ?, ?) ON CONFLICT(userId) DO UPDATE SET currentStreak=excluded.currentStreak, bestStreak=excluded.bestStreak, lastDate=excluded.lastDate')
    .run(uid, currentStreak, bestStreak, lastDate);

  const workoutCount = db.prepare("SELECT COUNT(*) as c FROM feed_posts WHERE authorId = ? AND activityType = 'workout'").get(uid).c;
  const commentCount = db.prepare('SELECT COUNT(*) as c FROM comments WHERE authorId = ?').get(uid).c;
  const friendCount = db.prepare("SELECT COUNT(*) as c FROM friendships WHERE userId = ? AND status = 'accepted'").get(uid).c;

  const newlyAwardedBadges = [];
  const hasBadge = (key) => db.prepare('SELECT 1 FROM user_badges WHERE userId = ? AND badgeKey = ?').get(uid, key);
  const awardBadge = (key) => { db.prepare('INSERT OR IGNORE INTO user_badges (userId, badgeKey, awardedAt) VALUES (?, ?, ?)').run(uid, key, now); newlyAwardedBadges.push(key); };

  if (workoutCount >= 1 && !hasBadge('first_workout')) awardBadge('first_workout');
  if (currentStreak >= 7 && !hasBadge('7day_streak')) awardBadge('7day_streak');
  if (currentStreak >= 30 && !hasBadge('30day_streak')) awardBadge('30day_streak');
  if (workoutCount >= 100 && !hasBadge('century_club')) awardBadge('century_club');
  if (friendCount >= 10 && !hasBadge('social_butterfly')) awardBadge('social_butterfly');
  if (commentCount >= 10 && !hasBadge('helping_hand')) awardBadge('helping_hand');

  const stats = { workoutCount, commentCount, friendCount };

  res.json({
    success: true,
    stats,
    streak: { currentStreak, bestStreak, lastDate },
    newlyAwardedBadges,
  });
});

app.get('/streaks/:userId', (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const streak = db.prepare('SELECT * FROM streaks WHERE userId = ?').get(userId);
  res.json(streak || { userId, currentStreak: 0, bestStreak: 0, lastDate: 0 });
});

app.get('/badges', (_req, res) => {
  const rows = db.prepare('SELECT * FROM badges').all();
  res.json(rows);
});

app.get('/badges/user/:userId', (req, res) => {
  const userId = sanitizeId(req.params.userId);
  if (!userId) return res.status(400).json({ error: 'invalid userId' });
  const rows = db.prepare('SELECT * FROM user_badges WHERE userId = ? ORDER BY awardedAt DESC').all(userId);
  res.json(rows);
});

app.post('/badges/award', enforceAuth, (req, res) => {
  const { userId, badgeKey } = req.body;
  const uid = sanitizeId(userId);
  const key = sanitizeString(badgeKey);
  if (!uid || !key) return res.status(400).json({ error: 'userId and badgeKey required' });
  if (requireOwnership(req, res, uid) !== true) return;
  const existing = db.prepare('SELECT 1 FROM user_badges WHERE userId = ? AND badgeKey = ?').get(uid, key);
  if (existing) return res.json({ success: false, alreadyAwarded: true });
  db.prepare('INSERT INTO user_badges (userId, badgeKey, awardedAt) VALUES (?, ?, ?)').run(uid, key, Date.now());
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

Object.entries(SYNC_TABLES).forEach(([table, config]) => {
  const tableName = `sync_${table}`;
  const placeholders = config.upsertCols.map(() => '?').join(',');
  const updateClauses = config.upsertCols.filter(c => c !== 'uuid').map(c => `${c}=excluded.${c}`).join(', ');
  const upsertSql = `INSERT INTO ${tableName} (${config.upsertCols.join(',')}) VALUES (${placeholders}) ON CONFLICT(uuid) DO UPDATE SET ${updateClauses}`;

  app.get(`/sync/${table}/:userId`, (req, res) => {
    const userId = sanitizeId(req.params.userId);
    if (!userId) return res.status(400).json({ error: 'invalid userId' });
    const since = parseInt(req.query.since) || 0;
    let rows;
    if (config.userCol) {
      rows = since > 0
        ? db.prepare(`SELECT * FROM ${tableName} WHERE ${config.userCol} = ? AND updatedAt > ?`).all(userId, since)
        : db.prepare(`SELECT * FROM ${tableName} WHERE ${config.userCol} = ?`).all(userId);
    } else {
      rows = since > 0
        ? db.prepare(`SELECT * FROM ${tableName} WHERE updatedAt > ?`).all(since)
        : db.prepare(`SELECT * FROM ${tableName}`).all();
    }
    res.json(rows);
  });

  app.post(`/sync/${table}/upsert`, enforceAuth, (req, res) => {
    const item = req.body;
    if (!item || !item.uuid) return res.status(400).json({ error: 'uuid required' });
    // Tabelele cu coloană de user trebuie să aparțină utilizatorului autentificat.
    if (config.userCol) {
      const owner = sanitizeId(item[config.userCol]);
      if (requireOwnership(req, res, owner) !== true) return;
    }
    const values = config.upsertCols.map(c => {
      if (c === 'uuid') return sanitizeId(item.uuid);
      const val = item[c];
      if (typeof val === 'string') return sanitizeString(val);
      if (typeof val === 'number') return val;
      if (typeof val === 'boolean') return val ? 1 : 0;
      return val ?? '';
    });
    db.prepare(upsertSql).run(...values);
    res.json({ success: true });
  });

  app.post(`/sync/${table}/bulk`, enforceAuth, (req, res) => {
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
    const stmt = db.prepare(upsertSql);
    const insertMany = db.transaction((rows) => {
      for (const item of rows) {
        if (!item.uuid) continue;
        const values = config.upsertCols.map(c => {
          if (c === 'uuid') return sanitizeId(item.uuid);
          const val = item[c];
          if (typeof val === 'string') return sanitizeString(val);
          if (typeof val === 'number') return val;
          if (typeof val === 'boolean') return val ? 1 : 0;
          return val ?? '';
        });
        stmt.run(...values);
      }
    });
    insertMany(items);
    res.json({ success: true, count: items.length });
  });

  app.delete(`/sync/${table}/:uuid`, enforceAuth, (req, res) => {
    const uuid = sanitizeId(req.params.uuid);
    if (!uuid) return res.status(400).json({ error: 'invalid uuid' });
    // Doar proprietarul poate șterge un rând care are coloană de user.
    if (config.userCol) {
      const row = db.prepare(`SELECT ${config.userCol} FROM ${tableName} WHERE uuid = ?`).get(uuid);
      if (!row) return res.status(404).json({ error: 'not found' });
      if (requireOwnership(req, res, sanitizeId(row[config.userCol])) !== true) return;
    }
    db.prepare(`DELETE FROM ${tableName} WHERE uuid = ?`).run(uuid);
    res.json({ success: true });
  });
});

// =============================================
// ADMIN: CLEANUP TEST USERS
// =============================================
// Șterge complet conturile de test (id TEST* sau nume care conține „Test") din toate
// tabelele: users, friendships, leaderboard, streaks, badges, posts, comments, likes
// și toate tabelele sync_*. Protejat cu header X-Admin-Key.
app.post('/admin/cleanup-test-users', (req, res) => {
  const adminKey = process.env.ADMIN_KEY || 'kinetic-cleanup-2024';
  if (req.headers['x-admin-key'] !== adminKey) {
    return res.status(401).json({ error: 'Invalid admin key' });
  }

  const targets = db.prepare(
    `SELECT id, name FROM users WHERE id LIKE 'TEST%' OR name LIKE '%Test%'`
  ).all();
  if (targets.length === 0) return res.json({ success: true, deleted: 0 });

  const ids = targets.map(t => t.id);
  const ph = ids.map(() => '?').join(',');

  const del = db.transaction(() => {
    // Prietenii (ambele direcții) și cereri pendente
    db.prepare(`DELETE FROM friendships WHERE userId IN (${ph}) OR friendId IN (${ph})`).run(...ids, ...ids);
    // Leaderboard
    db.prepare(`DELETE FROM leaderboard_entries WHERE userId IN (${ph})`).run(...ids);
    // Streaks + badges
    db.prepare(`DELETE FROM streaks WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM user_badges WHERE userId IN (${ph})`).run(...ids);
    // Posturi ale userilor de test + comentariile/like-urile lor
    const posts = db.prepare(`SELECT id FROM feed_posts WHERE authorId IN (${ph})`).all(...ids);
    const postIds = posts.map(p => p.id);
    if (postIds.length > 0) {
      const pph = postIds.map(() => '?').join(',');
      db.prepare(`DELETE FROM comments WHERE postId IN (${pph})`).run(...postIds);
      db.prepare(`DELETE FROM likes WHERE postId IN (${pph})`).run(...postIds);
      db.prepare(`DELETE FROM feed_posts WHERE id IN (${pph})`).run(...postIds);
    }
    db.prepare(`DELETE FROM comments WHERE authorId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM likes WHERE userId IN (${ph})`).run(...ids);
    // Toate tabelele sync_ cu coloană userId
    for (const [table, cfg] of Object.entries(SYNC_TABLES)) {
      if (cfg.userCol) {
        db.prepare(`DELETE FROM sync_${table} WHERE ${cfg.userCol} IN (${ph})`).run(...ids);
      }
    }
    // Tabele sync cu coloană userId dar definite manual (sync_antrenamente etc.)
    db.prepare(`DELETE FROM sync_antrenamente WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM sync_templates WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM sync_personal_records WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM sync_muscle_recovery WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM sync_exercise_metadata WHERE userId IN (${ph})`).run(...ids);
    db.prepare(`DELETE FROM sync_biometric_entries WHERE userId IN (${ph})`).run(...ids);
    // În final, userii înșiși
    db.prepare(`DELETE FROM users WHERE id IN (${ph})`).run(...ids);
  });
  del();

  res.json({ success: true, deleted: targets.length, users: targets.map(t => t.id) });
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
app.get('/', (_req, res) => {
  const userCount = db.prepare('SELECT COUNT(*) as c FROM users').get().c;
  const postCount = db.prepare('SELECT COUNT(*) as c FROM feed_posts').get().c;
  const friendCount = db.prepare('SELECT COUNT(*) as c FROM friendships WHERE status = ?').get('accepted').c;
  res.json({
    name: 'Kinetic API',
    version: '2.0.0',
    status: 'running',
    stats: { users: userCount, posts: postCount, friendships: friendCount },
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

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Kinetic backend v2.0.0 running on http://0.0.0.0:${PORT}`);
});
