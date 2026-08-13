// =============================================
// PostgreSQL (Supabase) connection layer
// =============================================
// Folosește variabila de mediu DATABASE_URL (ex: stringul de conexiune de la
// Supabase). int8 (bigint) este returnat ca Number, ca să serializăm identic
// cu vechea bază SQLite (fără stringuri pentru createdAt/lastSeen/id-uri).
const { Pool } = require('pg');

// int8 → Number
require('pg').types.setTypeParser(20, (v) => (v == null ? null : parseInt(v, 10)));
// numeric → Number
require('pg').types.setTypeParser(1700, (v) => (v == null ? null : parseFloat(v)));

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }, // Supabase cere SSL
  max: 10,
  idleTimeoutMillis: 30000,
});

pool.on('error', (err) => {
  console.error('Unexpected error on idle PostgreSQL client:', err.message);
});

// Toate rândurile rezultatului.
async function q(text, params = []) {
  const res = await pool.query(text, params);
  return res.rows;
}

// Primul rând (sau undefined).
async function qOne(text, params = []) {
  const res = await pool.query(text, params);
  return res.rows[0];
}

// Execută și întoarce { changes, lastInsertRowid }.
async function qRun(text, params = []) {
  const res = await pool.query(text, params);
  return { changes: res.rowCount ?? 0, lastInsertRowid: res.rows?.[0]?.id ?? null };
}

// Rulează fn(client) într-o tranzacție (BEGIN/COMMIT/ROLLBACK).
async function withTransaction(fn) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await fn(client);
    await client.query('COMMIT');
    return result;
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

module.exports = { pool, q, qOne, qRun, withTransaction };
