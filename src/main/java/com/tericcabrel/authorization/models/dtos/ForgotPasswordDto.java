package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.constraints.Exists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Exists.List({
    @Exists(property = "email", repository = "UserRepository", message = "This email doesn't exists in the db!")
})
@Accessors(chain = true)
@Setter
@Getter
public class ForgotPasswordDto {

    @Email(message = "Email address is not valid")
    @NotBlank(message = "The email address is required")
    private String email;
}
