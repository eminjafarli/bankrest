package com.project.bankrest.DTO;

import com.project.bankrest.Entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String username;
    private Role role;
    private int CardCount;
}