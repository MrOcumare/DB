package project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;
import project.DAO.UserDAO;
import project.models.User;
import org.springframework.http.HttpStatus;
import project.models.Message;


@ResponseBody
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final Message err;
    private UserDAO userDAO;


    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
        err = new Message("---");
    }

    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createUser(@RequestBody User body, @PathVariable("nickname") String nickname) {
        body.setNickname(nickname);
        User user = userDAO.createUser(body);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(userDAO.getConflictUsers(body.getNickname(), body.getEmail()));
    }

    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getProfileUser(@PathVariable("nickname") String nickname) {
       User getter =  userDAO.getUser(nickname);
       if (getter != null) {
           return ResponseEntity.status(HttpStatus.OK).body(getter);
       } else {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find such User"));
       }
//        try {
//            return ResponseEntity.status(HttpStatus.OK).body(userDAO.getUser(nickname));
//        } catch (DataAccessException e) {
//            Message m = new Message("Cant find such User");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
//        }
    }
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> changeUser(@RequestBody User body, @PathVariable("nickname") String nickname) {
        body.setNickname(nickname);
        Integer result = userDAO.changeUser(body);
        if (result == 201) {
            return ResponseEntity.status(HttpStatus.OK).body(userDAO.getUser(nickname));
        } else if (result == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find such User"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Conflicting with another user"));
        }
    }

}