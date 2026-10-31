package com.hrgenius.controller;

import com.hrgenius.dto.LoginRequest;
import com.hrgenius.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
 private final AuthService auth;

 public AuthController(AuthService auth) {
  this.auth = auth;
 }

 @PostMapping("/login")
 public Map<String,Object> login(@Valid @RequestBody LoginRequest r) {
  return auth.login(r.username(), r.password());
 }

 @PostMapping("/candidate-register")
 public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
  auth.registerCandidate(body.get("username"), body.get("password"));
  return ResponseEntity.status(201).body(Map.of("message", "Candidate account created"));
 }

 @PostMapping("/change-password")
 public ResponseEntity<?> changePassword(@RequestBody Map<String,String> body, Authentication authentication) {
  if (authentication == null || authentication.getName() == null) {
   return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
  }
  auth.changePassword(authentication.getName(), body.get("currentPassword"), body.get("newPassword"));
  return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
 }
}
