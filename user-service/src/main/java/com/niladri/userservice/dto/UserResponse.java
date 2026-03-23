package com.niladri.userservice.dto;

import com.niladri.userservice.model.Address;
import com.niladri.userservice.model.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
//    private String keyCloakId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Roles role;
    private Address address;


}

