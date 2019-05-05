package project.controllers;

import project.DAO.ForumDAO;
import project.DAO.PostDAO;
import project.DAO.ThreadDAO;
import project.DAO.UserDAO;
import project.models.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


@RestController
@RequestMapping("/api/post")
public class PostController {
    private final PostDAO postDAO;
    private final UserDAO userDAO;
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final Message err;

    public PostController(PostDAO postDAO, UserDAO userDAO, ForumDAO forumDAO, ThreadDAO threadDAO) {
        err = new Message("---");
        this.postDAO = postDAO;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.userDAO = userDAO;
    }


    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> updatePost(@PathVariable("id") long id,
                                        @RequestBody Post body) {
        Post buf = postDAO.getPostById(id);
        if (buf == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find such user"));
        }

        if (body.getMessage() != null && !buf.getMessage().equals(body.getMessage())) {
            buf.setMessage(body.getMessage());
            buf.setEdited(true);
            postDAO.changePost(buf);
        }
        return ResponseEntity.status(HttpStatus.OK).body(buf);
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDetails(@PathVariable("id") long id,
                                        @RequestParam(value = "related", required = false) String[] related) {
//        Post buf = pdao.getPostById(id);
//        if (buf == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
//        }
        Post buf;
        try {
            buf = postDAO.getPostByIdPerf(id);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        Details dt = new Details(null, null, buf, null);
        if (related != null) {
            if (Arrays.asList(related).contains("user")) {
                dt.setAuthor(userDAO.getUser(buf.getAuthor()));
            }
            if (Arrays.asList(related).contains("forum")) {
                dt.setForum(forumDAO.getForumBySlug(buf.getForum()));
            }
            if (Arrays.asList(related).contains("thread")) {
                dt.setThread(threadDAO.getThreadById(buf.getThread()));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(dt);
    }
}
