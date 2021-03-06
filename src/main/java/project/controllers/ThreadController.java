package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.DAO.ForumDAO;
import project.DAO.ThreadDAO;
import project.DAO.PostDAO;
import project.DAO.UserDAO;
import project.models.Forum;
import project.models.Message;
import project.models.Post;
import project.models.Thread;
import project.models.Vote;
import project.models.User;

import java.util.ArrayList;

@ResponseBody
@RestController
@RequestMapping("/api/thread")
public class ThreadController {

    private final PostDAO postDAO;
    private final UserDAO userDAO;
    private final ThreadDAO treadDAO;
    private final Message err;

    public ThreadController( PostDAO postDAO, UserDAO userDAO, ThreadDAO treadDAO){
        err = new Message("--");
        this.postDAO = postDAO;
        this.userDAO = userDAO;
        this.treadDAO = treadDAO;
    }

    @RequestMapping(path = "/{slug_or_id}/create", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> createPost(@PathVariable("slug_or_id") String slug_or_id, @RequestBody ArrayList<Post> bodyList ) {
        Thread buf;
        try {
            buf = treadDAO.getThreadbySlugOrID(slug_or_id);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        Integer res = postDAO.createPosts(bodyList, buf);
        if (res == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        } else if (res == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(bodyList);
    }

    @RequestMapping(path = "/{slug_or_id}/vote", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> vote(@PathVariable("slug_or_id") String slug_or_id,
                                  @RequestBody Vote body) {
        try {
            treadDAO.vote(slug_or_id, body);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        return ResponseEntity.status(HttpStatus.OK).body(treadDAO.getThreadbySlugOrID(slug_or_id));
    }

    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDetails(@PathVariable("slug_or_id") String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(treadDAO.getThreadbySlugOrID(slug_or_id));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
    }
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> postDetails(@PathVariable("slug_or_id") String slug_or_id, @RequestBody Thread body) {
        Thread buf = null;
        try {
            buf = treadDAO.getThreadbySlugOrID(slug_or_id);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No such thread"));
        }

        if (buf == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No such thread"));
        }
        if (body.getMessage() != null) {
            buf.setMessage(body.getMessage());
        }
        if (body.getTitle() != null) {
            buf.setTitle(body.getTitle());
        }
        treadDAO.chagenThread(buf);
        return ResponseEntity.status(HttpStatus.OK).body(buf);
    }

    @RequestMapping(path = "/{slug_or_id}/posts", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
                                      @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc,
                                      @RequestParam(value = "since", required = false) Integer since) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(treadDAO.getPosts(treadDAO.getThreadIDbySlugOrID(slug_or_id), limit, since, sort, desc));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

    }

}
