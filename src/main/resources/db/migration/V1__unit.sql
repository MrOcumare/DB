CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users
(
  id       SERIAL PRIMARY KEY,
  fullname text,
  nickname CITEXT COLLATE ucs_basic NOT NULL UNIQUE,
  email    CITEXT                   NOT NULL UNIQUE,
  about    text
);
CREATE TABLE forum
(
  id    SERIAL PRIMARY KEY,
  slug  CITEXT UNIQUE NOT NULL,
  title TEXT          NOT NULL,
  postCount   BIGINT default 0,
  threadCount BIGINT default 0,
  owner CITEXT REFERENCES users (nickname)
);

CREATE TABLE thread
(
  tid     SERIAL PRIMARY KEY,
  slug    CITEXT UNIQUE,
  owner   CITEXT REFERENCES users (nickname),
  forum   CITEXT REFERENCES forum (slug),
  forumid INTEGER,
  created TIMESTAMP WITH TIME ZONE,
  message TEXT NOT NULL,
  title   TEXT NOT NULL,
  votes   BIGINT
);

CREATE table post
(
  pid      SERIAL PRIMARY KEY,
  owner    CITEXT REFERENCES users (nickname),
  created  TIMESTAMP WITH TIME ZONE,
  forum    CITEXT REFERENCES forum (slug),
  isEdited BOOLEAN default false,
  message  TEXT NOT NULL,
  parent   INTEGER DEFAULT 0,
  threadid INTEGER REFERENCES thread (tid),
  path     INT []
);

CREATE TABLE vote
(
  id    SERIAL PRIMARY KEY,
  tid   INTEGER NOT NULL REFERENCES thread (tid),
  owner CITEXT NOT NULL REFERENCES users (nickname),
  voice INTEGER DEFAULT 0,
  UNIQUE (owner, tid)
);