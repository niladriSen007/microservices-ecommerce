package com.niladri.userservice.service.impl;

import com.niladri.userservice.dto.UserRequest;
import com.niladri.userservice.dto.UserResponse;
import com.niladri.userservice.exception.DuplicateEmailException;
import com.niladri.userservice.exception.UserNotFoundException;
import com.niladri.userservice.model.User;
import com.niladri.userservice.repository.UserRepository;
import com.niladri.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
//                .password(userRequest.getPassword())
//                .role(userRequest.getRole() != null ? userRequest.getRole() : "USER")
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(String id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(String id, UserRequest userRequest) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!existingUser.getEmail().equals(userRequest.getEmail())
                && userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException("User with email " + userRequest.getEmail() + " already exists");
        }

        existingUser.setFirstName(userRequest.getFirstName());
        existingUser.setLastName(userRequest.getLastName());
        existingUser.setEmail(userRequest.getEmail());
//        existingUser.setPassword(userRequest.getPassword());
//        if (userRequest.getRole() != null) {
//            existingUser.setRole(userRequest.getRole());
//        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated with id: {}", updatedUser.getId());
        return mapToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

