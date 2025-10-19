package com.tericcabrel.authorization.models.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

@Accessors(chain = true)
@Setter
@Getter
public class UpdateUserPermissionDto {
    @NotEmpty(message = "The field must have at least one item")
    private String[] permissions;
}
