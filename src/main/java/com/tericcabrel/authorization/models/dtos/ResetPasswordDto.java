package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.constraints.FieldMatch;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@FieldMatch.List({
    @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
})
@Accessors(chain = true)
@Setter
@Getter
public class ResetPasswordDto {
    @NotBlank(message = "The token is required")
    private String token;

    @Size(min = 6, message = "Must be at least 6 characters")
    @NotBlank(message = "This field is required")
    private String password;

    @NotBlank(message = "This field is required")
    private String confirmPassword;
}
