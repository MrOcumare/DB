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
  id          SERIAL PRIMARY KEY,
  slug        CITEXT UNIQUE NOT NULL,
  title       TEXT          NOT NULL,
  postCount   BIGINT default 0,
  threadCount BIGINT default 0,
  owner       CITEXT REFERENCES users (nickname)
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
  votes   BIGINT DEFAULT 0
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
  forumid  INTEGER,
  path     INT[] DEFAULT ARRAY[]::INT[]
);

CREATE TABLE vote
(
  id    SERIAL PRIMARY KEY,
  tid   INTEGER NOT NULL REFERENCES thread (tid),
  ownerid   INTEGER NOT NULL REFERENCES users (id),
  voice INTEGER DEFAULT 0,
  UNIQUE (ownerid, tid)
);



CREATE TABLE users_on_forum
(
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
AS
$$
BEGIN
  new.forumid = (SELECT id
                 FROM forum
                 WHERE forum.slug = new.forum);
  INSERT INTO users_on_forum (nickname, forumid, fullname, email, about)
    (SELECT new.owner,
            new.forumid,
            u.fullname,
            u.email,
            u.about
     FROM users u
     WHERE new.owner = u.nickname)
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
AS
$$
BEGIN
  INSERT INTO users_on_forum (nickname, forumid, fullname, email, about)
    (SELECT new.owner,
            (select id from forum where slug = new.forum),
            u.fullname,
            u.email,
            u.about
     FROM users u
     WHERE new.owner = u.nickname)
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


CREATE OR REPLACE FUNCTION thread_vote_inc()
  RETURNS TRIGGER
  LANGUAGE plpgsql
AS
$$
BEGIN
  UPDATE thread set votes = votes + new.voice WHERE tid = new.tid;
  RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS t_thread_vote_inc
  ON post;

CREATE TRIGGER t_thread_vote_inc
  AFTER INSERT
  ON vote
  FOR EACH ROW
EXECUTE PROCEDURE thread_vote_inc();

CREATE OR REPLACE FUNCTION thread_vote_inc_update()
  RETURNS TRIGGER
  LANGUAGE plpgsql
AS
$$
BEGIN
  IF new.voice != old.voice
  THEN
    UPDATE thread set votes = votes + (2 * new.voice) WHERE tid = new.tid;
    RETURN new;
  END IF;
  RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS t_thread_vote_inc_update
  ON post;

CREATE TRIGGER t_thread_vote_inc_update
  BEFORE UPDATE
  ON vote
  FOR EACH ROW
EXECUTE PROCEDURE thread_vote_inc_update();


CREATE OR REPLACE FUNCTION post_path()
  RETURNS TRIGGER
  LANGUAGE plpgsql
AS
$$
BEGIN
  update post set path = array_append((select path from post where pid = new.parent), new.pid) where pid = new.pid;
  RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS t_post_path
  ON post;

CREATE TRIGGER t_tpost_path
  AFTER INSERT
  ON post
  FOR EACH ROW
EXECUTE PROCEDURE post_path();

-- DROP INDEX IF EXISTS post_partial_index;
DROP INDEX IF EXISTS post_new_index;
-- DROP INDEX IF EXISTS post_owner_forum;
DROP INDEX IF EXISTS post_tid;
DROP INDEX IF EXISTS thread_owner_forum;
DROP INDEX IF EXISTS post_new_index_by_pid;
DROP INDEX IF EXISTS new_index_onPost;
DROP INDEX IF EXISTS post_threadid_created_id;
DROP INDEX IF EXISTS post_patent_threadid_id;
DROP INDEX IF EXISTS thread_forum_created;
DROP INDEX IF EXISTS forum_slug_id;
DROP INDEX IF EXISTS thread_slug_id;

DROP INDEX IF EXISTS post_path_index;

-- CREATE INDEX post_partial_index on post (pid, threadid, parent) where parent = 0; -- delete?

CREATE INDEX post_new_index on post ((path [ 1]), threadid, pid, created);

-- CREATE INDEX post_owner_forum on post (forum, owner);
CREATE INDEX post_tid on post (threadid, path);
---дал буст
-- CREATE INDEX thread_owner_forum on thread(forum, owner);

-- CREATE INDEX post_new_index_by_pid on post(pid) ;


CREATE INDEX new_index_onPost
  ON post (threadid, parent, path, pid);

CREATE INDEX post_threadid_created_id
  ON post (threadid, created, pid);

-- CREATE INDEX post_patent_threadid_id
--   ON post (parent, threadid, pid);

CREATE INDEX thread_forum_created
  ON thread (forumid, created);

------++++
-- CREATE INDEX New_Posts
--   ON post (threadid, parent, path, pid);
--
-- create index By_null_parent on post (threadid, (path[1])) where parent = 0;
--
-- CREATE INDEX post_tid_2 on post (threadid, path);