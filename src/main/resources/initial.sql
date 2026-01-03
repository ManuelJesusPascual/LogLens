CREATE TABLE IF NOT EXISTS api_keys (
  id INTEGER PRIMARY KEY,
  key TEXT UNIQUE,
  plan TEXT,
  requests INTEGER,
  last_reset DATE
);
