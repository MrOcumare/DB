package project.DAO;


import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//import project.models.Post;
//import project.models.SlugOrID;
//import project.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.models.Post;
import project.models.Thread;
import project.models.User;
import project.models.Vote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;

import javax.sound.midi.SysexMessage;


@Service
public class ThreadDAO {

    private final JdbcTemplate template;

    @Autowired
    public ThreadDAO(JdbcTemplate template) {
        this.template = template;

    }

    public Integer[] createThread(Thread body) {
        Integer[] result = {0, 0};
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement pst = con.prepareStatement(
                        "insert into thread(slug, forum, title, message, owner, votes, created)"
                                + " values(?,?,?,?,?,?,?::timestamptz)" + " returning tid",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, body.getSlug());
                pst.setString(2, body.getForum());
                pst.setString(3, body.getTitle());
                pst.setString(4, body.getMessage());
                pst.setString(5, body.getAuthor());
                pst.setLong(6, body.getVotes());
                pst.setString(7, body.getCreated());
                String sql = "UPDATE forum "+
                        "set threadcount = threadcount + 1 "+
                        "WHERE slug = ?::citext";
                template.update(sql, body.getForum());
                return pst;
            }, keyHolder);
            result[0] = 201;
            result[1] = keyHolder.getKey().intValue();
            return result;
        } catch (DuplicateKeyException e) {
            result[0] = 409;
            return result;
        } catch (DataAccessException e) {
            result[0] = 404;
            return result;
        }
    }

    public List<Thread> getThreads(String forum, Integer limit, String since, Boolean desc) {
        try {
            //System.out.println(forumid + " " + limit + " " + since + " " + desc );
            List<Object> myObj = new ArrayList<>();
            final StringBuilder myStr = new StringBuilder("select * from thread where forumid = (select id from forum where slug = ?::citext) ");
            myObj.add(forum);
            if (since != null) {
                if (desc) {
                    myStr.append(" and created <= ?::timestamptz ");
                } else {
                    myStr.append(" and created >= ?::timestamptz ");
                }
                myObj.add(since);
            }
            myStr.append(" order by created ");
            if (desc) {
                myStr.append(" desc ");
            }
            if (limit != null) {
                myStr.append(" limit ? ");
                myObj.add(limit);
            }
            //System.out.println(myStr.toString());
            return template.query(myStr.toString()
                    , myObj.toArray(), THREAD_MAPPER);
        } catch (DataAccessException e) {
            return null;
        }
    }
    public Integer getThreadIDbySlugOrID(String key) {
        try {
            return template.queryForObject(
                    "SELECT tid FROM thread WHERE tid = ?",
                    Integer.class, Integer.parseInt(key));
        } catch (NumberFormatException e) {
            return template.queryForObject(
                    "SELECT tid FROM thread WHERE slug = ?::citext",
                    Integer.class, key);
        }
    }

    public Thread getThreadbySlugOrID(String key) {
        try {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE tid = ?",
                    THREAD_MAPPER, Integer.parseInt(key));
        } catch (NumberFormatException e) {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE slug = ?::citext",
                    THREAD_MAPPER, key);
        }
    }
    public Thread getThreadById(long id) {
        try {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE tid = ?",
                    THREAD_MAPPER, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void vote(String key, Vote vt) {
        try {
            Integer id = Integer.parseInt(key);

            String sql = "INSERT INTO vote (ownerid, tid, voice)" +
                    "    SELECT( SELECT id FROM users WHERE lower(nickname) = lower(?)) AS uid," +
                    " ?, " +
                    "    ? " +
                    "    ON CONFLICT (ownerid, tid)" +
                    "    DO UPDATE SET voice = EXCLUDED.voice;";
            template.update(sql, vt.getNickname(), id, vt.getVoice());

        } catch (Exception e) {
            String sql = "INSERT INTO vote (ownerid, tid, voice) VALUES ((SELECT id " +
                    "                                                    FROM users " +
                    "                                                    WHERE lower(nickname) = lower(?)), (SELECT tid " +
                    "                                                                                                      FROM thread " +
                    "                                                                                                      WHERE " +
                    "                                                                                                        lower(slug) = " +
                    "                                                                                                        lower(?)), " +
                    "                                                   (?)) " +
                    "ON CONFLICT (ownerid, tid) " +
                    "  DO UPDATE SET voice = EXCLUDED.voice;";
            template.update(sql, vt.getNickname(), key, vt.getVoice());
        }

    }

    public Integer chagenThread(Thread body) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement pst = con.prepareStatement(
                        "update thread set" +
                                "  message = COALESCE(?, message)," +
                                "  title = COALESCE(?, title)" +
                                "where tid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, body.getMessage());
                pst.setString(2, body.getTitle());
                pst.setLong(3, body.getId());
                return pst;
            }, keyHolder);

        } catch (Exception e) {
            return 409;
        }
        return 201;
    }



    /////
    public List<Post> getPosts(long threadId, Integer limit, Integer since, String sort, Boolean desc) {
        List<Object> myObj = new ArrayList<>();
        if (sort.equals("flat")) {
            StringBuilder myStr = new StringBuilder("select * from post where threadid = ?");
            myObj.add(threadId);
            if (since != null) {
                if (desc) {
                    myStr.append(" and pid < ?");
                } else {
                    myStr.append(" and pid > ?");
                }
                myObj.add(since);
            }
            myStr.append(" order by created ");
            if (desc) {
                myStr.append(" desc, pid desc ");
            } else {
                myStr.append(", pid");
            }
            if (limit != null) {
                myStr.append(" limit ? ");
                myObj.add(limit);
            }

            return template.query(myStr.toString()
                    , myObj.toArray(), POST_MAPPER);
        } else if (sort.equals("tree")) {
            StringBuilder myStr = new StringBuilder("select * from post where threadid = ?");
            myObj.add(threadId);
            if (since != null) {
                if (desc) {
                    myStr.append(" and path < (select path from post where pid = ?) ");
                } else {
                    myStr.append(" and path > (select path from post where pid = ?) ");
                }
                myObj.add(since);
            }
            myStr.append(" order by path ");
            if (desc) {
                myStr.append(" desc ");
            }
            if (limit != null) {
                myStr.append(" limit ? ");
                myObj.add(limit);
            }

            return template.query(myStr.toString()
                    , myObj.toArray(), POST_MAPPER);


            //////////
        } else {


            StringBuilder myStr = new StringBuilder("SELECT * FROM post WHERE threadid = ? AND path[1] IN (SELECT DISTINCT path[1] FROM Post ");
            myObj.add(threadId);
            if (desc) {
                if (since != null) {
                    myStr.append("WHERE threadid = ? AND path[1] < ( SELECT path[1] FROM Post WHERE pid = ? ) ORDER BY path[1] DESC LIMIT ? ) ORDER BY path[1] DESC ");
                    myObj.add(threadId);
                    myObj.add(since);
                    myObj.add(limit);
                } else {
                    myStr.append("WHERE threadid = ?  ORDER BY path[1] DESC LIMIT ? ) ORDER BY path[1] DESC ");
                    myObj.add(threadId);

                    myObj.add(limit);
                }

            } else {
                if (since != null) {
                    myStr.append(" WHERE threadid = ?  AND path[1] > (SELECT path[1] FROM Post WHERE pid = ? ) ORDER BY path[1] LIMIT ? ) ORDER BY path[1] ");
                    myObj.add(threadId);
                    myObj.add(since);
                    myObj.add(limit);
                } else {
                    myStr.append(" WHERE threadid = ?   ORDER BY path[1] LIMIT ? ) ORDER BY path[1] ");
                    myObj.add(threadId);

                    myObj.add(limit);
                }
            }
            myStr.append(" , path;");
            return template.query(myStr.toString()
                    , myObj.toArray(), POST_MAPPER);


//            final StringBuilder sql = new StringBuilder("SELECT * FROM Post WHERE thread=").append(threadID)
//                    .append(" AND path[1] IN (SELECT DISTINCT path[1] FROM Post ");
//
//            if (desc == true) {
//                if (since != 0 && !since.equals(MAX_LONG)) {
//                    sql.append("WHERE thread=").append(threadID)
//                            .append(" AND path[1]<(SELECT path[1] FROM Post WHERE id=").append(since)
//                            .append(") ORDER BY path[1] DESC LIMIT ").append(limit)
//                            .append(") ORDER BY path[1] DESC ");
//                } else {
//                    sql.append("WHERE thread=").append(threadID)
//                            .append(" AND path[1]<").append(since)
//                            .append(" ORDER BY path[1] DESC LIMIT ").append(limit)
//                            .append(") ORDER BY path[1] DESC ");
//                }
//            } else {
//                if (since != 0 && !since.equals(MAX_LONG)) {
//                    sql.append(" WHERE thread=").append(threadID)
//                            .append(" AND path[1]>(SELECT path[1] FROM Post WHERE id=").append(since)
//                            .append(") ORDER BY path[1] LIMIT ").append(limit)
//                            .append(") ORDER BY path[1] ");
//                } else {
//                    sql.append(" WHERE thread=").append(threadID)
//                            .append(" AND path[1]>").append(since)
//                            .append(" ORDER BY path[1] LIMIT ").append(limit)
//                            .append(") ORDER BY path[1]  ");
//                }
//            }
//            sql.append(" , path;");
//
//            return jdbcTemplate.query(sql.toString(), new PostMapper());




//            if (since != null) {
//                if (desc) {
//                    myStr.append(" (select pid from post where parent = 0 and threadid = ? and path[1] < (SELECT path[1] FROM post WHERE pid = ?) order by pid desc  limit ? ) as TT on  path[1] = TT.pid and p.threadid = ? order by TT.pid desc, p.path asc;");
//
//                } else {
//                    myStr.append(" (select pid from post where parent = 0 and threadid = ? and path[1] > (SELECT path[1] FROM post WHERE pid = ?) order by pid asc limit ? ) as TT on  path[1] = TT.pid and p.threadid = ? order by TT.pid asc, p.path asc;");
//                }
//                myObj.add(threadId);
//                myObj.add(since);
//                myObj.add(limit);
//                myObj.add(threadId);
//
//
//            } else if (limit != null) {
//                if (desc) {
//                    myStr.append(" (select pid  from post where parent = 0 and threadid = ? order by pid desc limit ? ) as TT on  path[1] = TT.pid and p.threadid = ? order by TT.pid desc, p.path asc;");
//                } else {
//                    myStr.append(" (select pid  from post where parent = 0 and threadid = ? order by pid asc limit ? ) as TT on  path[1] = TT.pid and p.threadid = ? order by TT.pid asc, p.path asc ;");
//                }
//                myObj.add(threadId);
//                myObj.add(limit);
//                myObj.add(threadId);
//
//            }

//            return template.query(myStr.toString()
//                    , myObj.toArray(), POST_MAPPER);
        }

    }




    /////




    private static final RowMapper<Thread> THREAD_MAPPER = (res, num) -> {
        long votes = res.getLong("votes");
        Long id = res.getLong("tid");
        Long forumid = res.getLong("forumid");
        String slug = res.getString("slug");
        String owner = res.getString("owner");
        String forum = res.getString("forum");
        Timestamp created = res.getTimestamp("created");
        String message = res.getString("message");
        String title = res.getString("title");
        return new Thread(slug, forum, title, message, owner, id, votes, created, forumid);
    };

    private static final RowMapper<Post> POST_MAPPER = (res, num) -> {
        Long id = res.getLong("pid");
        Long forumid = res.getLong("forumid");
        Long parent = res.getLong("parent");
        Long threadid = res.getLong("threadid");
        boolean isedited = res.getBoolean("isedited");
        String author = res.getString("owner");
        String message = res.getString("message");
        String forum = res.getString("forum");
        Timestamp created = res.getTimestamp("created");
        Array path = res.getArray("path");
        return new Post(id, forumid ,parent, threadid, isedited, author, message, forum, created, (Object[]) path.getArray());
    };


}
