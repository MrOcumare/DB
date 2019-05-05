package project.controllers;

import project.DAO.ServiceDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private final ServiceDAO sdao;

    public ServiceController(ServiceDAO serviceDAO) {
        this.sdao = serviceDAO;
    }


    @RequestMapping(path = "/status", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDetails() {
        return ResponseEntity.status(HttpStatus.OK).body(sdao.getInfo());
    }

    @RequestMapping(path = "/clear", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> clearDB() {
        sdao.truncateDB();
        return ResponseEntity.status(HttpStatus.OK).body("CLEARED!!!");
    }
}
