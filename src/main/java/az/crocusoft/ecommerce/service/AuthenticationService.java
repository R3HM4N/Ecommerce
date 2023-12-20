package az.crocusoft.ecommerce.service;

import az.crocusoft.ecommerce.dto.UserDto;
import az.crocusoft.ecommerce.dto.UserRequest;
import az.crocusoft.ecommerce.dto.AuthResponse;
import az.crocusoft.ecommerce.enums.Role;
import az.crocusoft.ecommerce.exception.EmailAlreadyExistsException;
import az.crocusoft.ecommerce.exception.UserNameAlreadyExistsException;
import az.crocusoft.ecommerce.model.User;
import az.crocusoft.ecommerce.repository.TokenRepository;
import az.crocusoft.ecommerce.repository.UserRepository;
import az.crocusoft.ecommerce.token.Token;
import az.crocusoft.ecommerce.token.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private  final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse save(UserDto userDto) {
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .name(userDto.getName())

                .surname(userDto.getSurname())
                .role(Role.USER).build();
        boolean isEmailExists = userRepository.existsByEmail(userDto.getEmail());
        boolean isUserNameExists = userRepository.existsByUsername(userDto.getUsername());

        if (isEmailExists) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (isUserNameExists) {
            throw new UserNameAlreadyExistsException("Username already exists");
        }

        User saveUser= userRepository.save(user);
        var token = jwtService.generateToken(user);
        var refreshToken=jwtService.generateRefreshToken(user);
        saveUserToken(saveUser, token);
        return AuthResponse.builder().
                userId(saveUser.getId())
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();

    }


    public AuthResponse auth(UserRequest userRequest) {
        Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));

        if(authentication.isAuthenticated()){
            User user = userRepository.findByUsername(userRequest.getUsername()).orElseThrow();
            String token = jwtService.generateToken(user);
            var refreshToken=jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user,token);
            return AuthResponse.builder().
                    userId(user.getId())
                    .accessToken(token).refreshToken(refreshToken)
                    .build();
        }

        else {
            throw new UsernameNotFoundException("invalid user request");
        }

    }
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);

        });
        tokenRepository.saveAll(validUserTokens);
    }

    public User getSignedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getPrincipal());
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        username = jwtService.findUsername(refreshToken);
        if (username != null) {

            var user = this.userRepository.findByUsername(username)
                    .orElseThrow();
            if (jwtService.tokenControl(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
