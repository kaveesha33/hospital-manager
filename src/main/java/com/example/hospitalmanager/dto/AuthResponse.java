
package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.Role;

public record AuthResponse(

    String token,

    String type,

    long expiresIn,

    Role role

) {}
