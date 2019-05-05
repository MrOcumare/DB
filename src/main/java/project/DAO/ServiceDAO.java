package project.DAO;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class ServiceDAO {

    private final JdbcTemplate template;

    @Autowired
    public ServiceDAO(JdbcTemplate template) {
        this.template = template;

    }
    public project.models.Service getInfo() {
        return new project.models.Service(template.queryForObject(
                "select count(*) from users;",
                new Object[]{}, Long.class) , template.queryForObject(
                "select count(*) from post;",
                new Object[]{}, Long.class), template.queryForObject(
                "select count(*) from forum;",
                new Object[]{}, Long.class), template.queryForObject(
                "select count(*) from thread;",
                new Object[]{}, Long.class));
    }

    public void truncateDB() {
        template.update(
                "truncate post, forum, thread, users, vote CASCADE;"
        );
    }
}
