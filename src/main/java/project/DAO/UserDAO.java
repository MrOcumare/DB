package project.DAO;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import project.models.User;
import org.springframework.dao.*;


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Service
//@Transactional
public class UserDAO {

    private final JdbcTemplate template;

    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }

    public User createUser(User body) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO users(fullname, nickname, email, about)" + " VALUES(?,?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getFullname());
                statement.setString(2, body.getNickname());
                statement.setString(3, body.getEmail());
                statement.setString(4, body.getAbout());
                return statement;
            }, keyHolder);
            return body;
        } catch (DuplicateKeyException e) {
            return null;
        }
    }
    public int changeUser(User user) {
        if (getUser(user.getNickname()) == null) {
            return 404;
        }GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement pst = con.prepareStatement(
                        "update users set" +
                                "  fullname = COALESCE(?, fullname)," +
                                "  about = COALESCE(?, about)," +
                                "  email = COALESCE(?, email)" +
                                "where LOWER(nickname) = LOWER(?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, user.getFullname());
                pst.setString(2, user.getAbout());
                pst.setString(3, user.getEmail());
                pst.setString(4, user.getNickname());
                return pst;
            }, keyHolder);
        } catch (Exception e) {
            return 409;
        }
        return 201;
    }
    public User getUser(String nickname) {
        try {
            return template.queryForObject(
                    "SELECT * from users as u where u.nickname = ?::citext;",
                    USER_MAPPER, nickname);
        } catch (DataAccessException e) {
            return null;
        }

    }


    public List<User> getConflictUsers(String nickname, String email) {
//        try {
        List<Object> obj = new ArrayList<>();
        obj.add(email);
        obj.add(nickname);
        final StringBuilder query =
                new StringBuilder("SELECT * from users as u where u.email = ?::citext or u.nickname = ?::citext;");
        return template.query(query.toString(), obj.toArray(), USER_MAPPER);
//        } catch (DataAccessException e) {
//            return null;
//        }
    }



    public static final RowMapper<User> USER_MAPPER = (res, num) -> {
        String nickname = res.getString("nickname");
        String email = res.getString("email");
        String fullname = res.getString("fullname");
        String about = res.getString("about");
        if (res.wasNull()) {
            about = null;
        }
        return new User(fullname, nickname, email, about);
    };

}
