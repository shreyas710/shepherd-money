package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        try {
            User checkExistingUser = userRepository.findByEmail(payload.getEmail());
            if (checkExistingUser != null) {
                // return 400 error if user with the given email already exists
                System.out.println("User already exists");
                return ResponseEntity.badRequest().body(400);
            }
            User newUser = new User();
            newUser.setName(payload.getName());
            newUser.setEmail(payload.getEmail());
            userRepository.save(newUser);
            return ResponseEntity.ok(newUser.getId());
        } catch(Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error creating user: " + e);
            return ResponseEntity.badRequest().body(500);
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        try {
            User delUser = userRepository.findById(userId).orElse(null);
            if(delUser != null) {
                // deleting user automatically deletes all his credit cards because of the linking
                userRepository.delete(delUser);
                return ResponseEntity.ok("User deleted successfully");
            }
            return ResponseEntity.badRequest().body("User does not exist");
        } catch(Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error deleting user: " + e);
            return ResponseEntity.badRequest().body("Error deleting user");
        }
    }
}
