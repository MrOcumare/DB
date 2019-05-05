//package project.controllers;
//
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.dao.DataAccessException;
//import org.springframework.web.bind.annotation.*;
//import project.DAO.ForumDAO;
//import project.DAO.UserDAO;
//import project.models.User;
//import project.models.Forum;
//import org.springframework.http.HttpStatus;
//import project.models.Message;

package project.controllers;

import project.DAO.ForumDAO;
import project.DAO.UserDAO;
import project.DAO.ThreadDAO;
import project.models.Message;
import project.models.User;
import project.models.Thread;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.Forum;

import java.util.List;


@ResponseBody
@RestController
@RequestMapping("/api/forum")
public class ForumController {
    private final ForumDAO forumDAO;
    private final UserDAO userDAO;
    private final ThreadDAO treadDAO;
    private final Message err;

    public ForumController(ForumDAO forumDAO, UserDAO userDAO, ThreadDAO treadDAO){
        err = new Message("--");
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.treadDAO = treadDAO;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> createForum(@RequestBody Forum body) {
        User us = userDAO.getUser(body.getUser());
        if (us != null) {
            body.setUser(us.getNickname());
        }
        Integer result = forumDAO.createForum(body);
        if (result == 201) {
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else if (result == 404)  {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find such User"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(forumDAO.getConflictForum(body.getUser()));
        }
    }
    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getProfileUser(@PathVariable("slug") String slug) {
        Forum getter =  forumDAO.getForum(slug);
        if (getter != null) {
            return ResponseEntity.status(HttpStatus.OK).body(getter);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find such Forum"));
        }
    }

    @RequestMapping(path = "/{forum}/create", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> createThread(@RequestBody Thread body, @PathVariable("forum") String forum) {
        body.setForum(forum);
        Integer[] result = treadDAO.createThread(body);
        if (result[0] == 201) {
            body.setId(result[1]);
            body.setForum(forumDAO.getForum(forum).getSlug());
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else if (result[0] == 404){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find such User or thread"));
        }
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(treadDAO.getThreadbySlugOrID(body.getSlug()));
        }
    }

    @RequestMapping(path = "/{forum}/threads", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getThreads(@PathVariable String forum,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "since", required = false) String since,
                                        @RequestParam(value = "desc", required = false) boolean desc) {

        List<Thread> result  = treadDAO.getThreads(forum, limit, since, desc);
        if (result != null & forumDAO.getForum(forum) != null) {
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
//        try {
//
//            return ResponseEntity.status(HttpStatus.OK).body(treadDAO.getThreads(forum, limit, since, desc));
//
//        } catch (DataAccessException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
//        }
    }

    @RequestMapping(path = "/{forum}/users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUsers(@PathVariable String forum,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "since", required = false) String since,
                                      @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {
        if (forumDAO.getForum(forum) != null ) {
            return ResponseEntity.status(HttpStatus.OK).body(forumDAO.getUsers(forum, limit, since, desc));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
//        try {
//            return ResponseEntity.status(HttpStatus.OK).body(forumDAO.getUsers(forum, limit, since, desc));
//        } catch (DataAccessException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
//        }
    }



}
