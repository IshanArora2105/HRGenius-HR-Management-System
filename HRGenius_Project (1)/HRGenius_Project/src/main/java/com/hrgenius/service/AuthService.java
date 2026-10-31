package com.hrgenius.service;
import com.hrgenius.entity.User;
import com.hrgenius.repository.UserRepository;
import com.hrgenius.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {
 private final UserRepository users;
 private final PasswordEncoder encoder;
 private final JwtService jwt;

 public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
  this.users = users;
  this.encoder = encoder;
  this.jwt = jwt;
 }

 public Map<String,Object> login(String username, String password) {
  User u = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
  if (!"ACTIVE".equals(u.getStatus()) || !encoder.matches(password, u.getPassword())) {
   throw new IllegalArgumentException("Invalid credentials");
  }
  return Map.of("token", jwt.generate(u.getUsername(), u.getRole()), "tokenType", "Bearer", "role", u.getRole(), "username", u.getUsername());
 }

 public void registerCandidate(String username, String password) {
  if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Username already exists");
  User u = new User();
  u.setUsername(username);
  u.setPassword(encoder.encode(password));
  u.setRole("CANDIDATE");
  u.setStatus("ACTIVE");
  users.save(u);
 }

 public void changePassword(String username, String currentPassword, String newPassword) {
  User u = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
  if (!encoder.matches(currentPassword, u.getPassword())) {
   throw new IllegalArgumentException("Current password is incorrect");
  }
  u.setPassword(encoder.encode(newPassword));
  users.save(u);
 }
}
