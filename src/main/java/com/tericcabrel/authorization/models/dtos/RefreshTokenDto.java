package com.tericcabrel.authorization.models.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Accessors(chain = true)
@Setter
@Getter
public class RefreshTokenDto {
    @NotBlank(message = "The token is required")
    private String token;
}
