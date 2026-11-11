
package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.Role;

public record UserResponse(

    Long id,

    String name,

    String username,

    Role role

) {}
