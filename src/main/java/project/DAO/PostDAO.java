package project.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import project.models.Post;
import project.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;
@Service
public class PostDAO {

    private final JdbcTemplate template;
    @Autowired
    public PostDAO(JdbcTemplate template) {
        this.template = template;
    }

    public Integer createPosts(ArrayList<Post> bodyList, Thread th) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            for (Post body : bodyList) {
                body.setForum(th.getForum());
                body.setThread(th.getId());
                body.setForumid(th.getForumid());
                body.setCreated(bodyList.get(0).getCreated());
                Integer chuf = getParentTidPost(body.getParent());
                if ((chuf == null && body.getParent() != 0) || (chuf != null && chuf != body.getThread())) {

                    return 409;
                }
                template.update(con -> {
                    PreparedStatement pst = con.prepareStatement(
                            "insert into post(parent, threadid, isedited, owner, message, forum, created, forumid)"
                                    + " values(?,?,?,?,?,?,?::timestamptz,?)" + " returning pid",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                   // System.out.println("ABROCADABRA");
                    pst.setLong(1, body.getParent());
                   // System.out.println(body.getParent());
                    pst.setLong(2, body.getThread());
                   // System.out.println(body.getThread());
                    pst.setBoolean(3, body.getisEdited());
                   // System.out.println(body.getisEdited());
                    pst.setString(4, body.getAuthor());
                   // System.out.println(body.getAuthor());
                    pst.setString(5, body.getMessage());
                   // System.out.println(body.getMessage());
                    pst.setString(6, body.getForum());
                   // System.out.println(body.getForum());
                    pst.setString(7, body.getCreated());
                   // System.out.println(body.getCreated());
                    pst.setLong(8, body.getForumid());
//                    System.out.println(body.getForumid());
                    return pst;
                }, keyHolder);
                body.setId(keyHolder.getKey().intValue());
            }
            if (bodyList.size() > 0) {
                String sql = "UPDATE forum "+
                        "set postCount = postCount + ?" +
                        "WHERE slug = ?::citext";
                template.update(sql, bodyList.size(), bodyList.get(0).getForum());
            }
            return 201;
        } catch (Exception e) {

            return 404;
        }
    }

    public Integer getParentTidPost(long id) {
        try {
            return template.queryForObject(
                    "SELECT threadid FROM post WHERE pid = ?",
                    Integer.class, id);
        } catch (DataAccessException e) {
            return null;
        }
    }
    public Post getPostById(long id) {
        try {
            return template.queryForObject(
                    "SELECT * FROM post WHERE pid = ?",
                    POST_MAPPER, id);
        } catch (DataAccessException e) {
            return null;
        }
    }
    public Post getPostByIdPerf(long id) {
        return template.queryForObject(
                "SELECT * FROM post WHERE pid = ?",
                POST_MAPPER, id);

    }

    public void changePost(Post body) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "update post set" +
                            "  message = COALESCE(?, message), " +
                            "  isedited = COALESCE(true, isedited) " +
                            "where pid = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, body.getMessage());
            pst.setLong(2, body.getId());
            return pst;
        }, keyHolder);
    }
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
        return new Post(id,forumid,  parent, threadid, isedited, author, message, forum, created, (Object[]) path.getArray());
    };
}
