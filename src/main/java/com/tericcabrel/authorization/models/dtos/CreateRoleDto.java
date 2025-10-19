package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.models.entities.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Accessors(chain = true)
@Setter
@Getter
@ToString
public class CreateRoleDto {
    @NotBlank(message = "The name is required")
    private String name;

    private String description;

    private boolean isDefault;

    public Role toRole() {
        return new Role()
            .setName(this.name)
            .setDescription(this.description)
            .setDefault(this.isDefault);
    }
}
