package com.tericcabrel.authorization.models.dtos;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class CreateGroupMemberDto {

    @NotBlank(message = "The email is required")
    private String email;

    @NotBlank(message = "The groupName is required")
    private String group;
}
