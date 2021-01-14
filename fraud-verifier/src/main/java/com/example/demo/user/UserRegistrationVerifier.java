package com.example.demo.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserRegistrationVerifier {
  @GetMapping("/verify")
  ResponseEntity<String> verifyUser(@RequestParam("id") Long id,
                                    @RequestParam("age") Long age) {

    System.out.println("*************************************");
    System.out.println("INSIDE FRAUD VERIFIED USER REGISTRATION VERIFIER");
    System.out.println("Id = " + id);
    System.out.println("Age = " + age);
    System.out.println("*************************************");

    if(age < 18) {
      return ResponseEntity.ok("NOT-OK");
    }

    return ResponseEntity.ok("OK");

  }
}
