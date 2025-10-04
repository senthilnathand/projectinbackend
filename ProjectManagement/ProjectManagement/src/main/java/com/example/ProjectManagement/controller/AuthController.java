package com.example.ProjectManagement.controller;
import com.example.ProjectManagement.model.User;
import com.example.ProjectManagement.repository.UserRepository;
import com.example.ProjectManagement.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password())
		);
		String token = jwtService.generateToken(request.username(),Collections.singletonList(authentication.getAuthorities()));
		return ResponseEntity.ok(Map.of("token", token));
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
		}
		User user = User.builder()
				.username(request.username())
				.passwordHash(passwordEncoder.encode(request.password()))
				.roles(request.roles)
				.build();
		userRepository.save(user);
		return ResponseEntity.ok(Map.of("message", "User registered"));
	}

	public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
	public record RegisterRequest(@NotBlank String username, @NotBlank String password ,@NotBlank String roles) {}

	@GetMapping("/me")
	public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
		if (principal == null) return ResponseEntity.status(401).build();
		User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
		return ResponseEntity.ok(Map.of(
				"username", user.getUsername(),
				"roles", user.getRoles()
		));
	}
}



