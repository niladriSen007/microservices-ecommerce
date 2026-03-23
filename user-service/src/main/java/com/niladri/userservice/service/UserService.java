package com.niladri.userservice.service;

import com.niladri.userservice.dto.UserRequest;
import com.niladri.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserById(String id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(String id, UserRequest userRequest);

    void deleteUser(String id);
}

