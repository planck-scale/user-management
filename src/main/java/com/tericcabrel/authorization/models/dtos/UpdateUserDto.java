package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.models.entities.Coordinates;
import com.tericcabrel.authorization.models.entities.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Accessors(chain = true)
@Setter
@Getter
public class UpdateUserDto {
    private String firstName;

    private String lastName;

    private String timezone;

    private String gender;

    private String avatar;

    private boolean enabled;

    private boolean confirmed;

    private Coordinates coordinates;

    private Set<Role> roles;
}
