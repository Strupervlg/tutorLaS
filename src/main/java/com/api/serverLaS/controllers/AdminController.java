package com.api.serverLaS.controllers;

import com.api.serverLaS.services.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    public DatabaseService databaseService;

    @PostMapping("/fill-database")
    public ResponseEntity<String> fill() {
        databaseService.fillDatabase();
        return new ResponseEntity<String>("database is filled", new HttpHeaders(), HttpStatus.OK);
    }

}
