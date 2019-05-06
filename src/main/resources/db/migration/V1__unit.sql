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


CREATE TABLE users_on_forum (
                              id       SERIAL PRIMARY KEY,
                              nickname CITEXT,
                              fullname TEXT,
                              email    CITEXT,
                              about    TEXT,
                              forumid  INTEGER,
                              UNIQUE (forumid, nickname)
);

CREATE OR REPLACE FUNCTION forum_threads_inc()
  RETURNS TRIGGER
  LANGUAGE plpgsql
AS $$
BEGIN
  new.forumid = (SELECT id
                 FROM forum
                 WHERE lower(forum.slug) = lower(new.forum));
  INSERT INTO users_on_forum (nickname, forumid, fullname, email, about)
    (SELECT
       new.owner,
       new.forumid,
       u.fullname,
       u.email,
       u.about
     FROM users u
     WHERE lower(new.owner) = lower(u.nickname))
  ON CONFLICT DO NOTHING;

  RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS t_forum_threads_inc
  ON thread;

CREATE TRIGGER t_forum_threads_inc
  BEFORE INSERT
  ON thread
  FOR EACH ROW
EXECUTE PROCEDURE forum_threads_inc();


CREATE OR REPLACE FUNCTION forum_posts_inc()
  RETURNS TRIGGER
  LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO users_on_forum (nickname, forumid, fullname, email, about)
    (SELECT
       new.owner,
       (select id from forum where slug = new.forum),
       u.fullname,
       u.email,
       u.about
     FROM users u
     WHERE lower(new.owner) = lower(u.nickname))
  ON CONFLICT DO NOTHING;
  RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS t_forum_posts_inc
  ON post;

CREATE TRIGGER t_forum_posts_inc
  BEFORE INSERT
  ON post
  FOR EACH ROW
EXECUTE PROCEDURE forum_posts_inc();