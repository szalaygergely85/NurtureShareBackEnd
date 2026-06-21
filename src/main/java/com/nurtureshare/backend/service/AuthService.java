package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.request.LoginRequest;
import com.nurtureshare.backend.dto.request.RegisterRequest;
import com.nurtureshare.backend.dto.response.AuthResponse;
import com.nurtureshare.backend.exception.UnauthorizedException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.Pregnancy;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.model.UserSettings;
import com.nurtureshare.backend.model.enums.AppMode;
import com.nurtureshare.backend.model.enums.UserRole;
import com.nurtureshare.backend.repository.CoupleRepository;
import com.nurtureshare.backend.repository.PregnancyRepository;
import com.nurtureshare.backend.repository.UserRepository;
import com.nurtureshare.backend.repository.UserSettingsRepository;
import com.nurtureshare.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final PregnancyRepository pregnancyRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            // Generic message — do not confirm which emails are registered.
            throw new IllegalArgumentException("Unable to register with the provided details.");
        }

        UserRole role = req.getRole() != null ? req.getRole() : UserRole.MOTHER;

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role(role)
                .build();
        user = userRepository.save(user);
        log.info("Registered new user: {} (role={})", user.getEmail(), role);

        UserSettings settings = UserSettings.builder()
                .user(user)
                .notificationsEnabled(true)
                .appMode(AppMode.PREGNANCY)
                .build();
        userSettingsRepository.save(settings);

        String pairingCode = generatePairingCode();
        Couple couple = Couple.builder()
                .user1(user)
                .pairingCode(pairingCode)
                .build();
        couple = coupleRepository.save(couple);
        log.info("Created couple for user {} with pairing code: {}", user.getEmail(), pairingCode);

        Pregnancy pregnancy = Pregnancy.builder()
                .couple(couple)
                .dueDate(LocalDate.now().plusDays(280))
                .appMode(AppMode.PREGNANCY)
                .build();
        pregnancyRepository.save(pregnancy);

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        // Use the same exception/message whether the email is unknown or the
        // password is wrong, so attackers cannot enumerate registered accounts.
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    private String generatePairingCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(5);
            for (int i = 0; i < 5; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (coupleRepository.findByPairingCode(code).isPresent());
        return code;
    }
}
