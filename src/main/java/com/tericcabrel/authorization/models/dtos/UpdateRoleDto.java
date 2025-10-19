package com.tericcabrel.authorization.models.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Accessors(chain = true)
@Setter
@Getter
public class UpdateRoleDto {
    @NotBlank(message = "The userId is required")
    private String userId;

    @NotEmpty(message = "The field must have at least one item")
    private String[] roles;
}
