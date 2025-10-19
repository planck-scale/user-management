package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.constraints.FieldMatch;
import com.tericcabrel.authorization.models.entities.Coordinates;
import com.tericcabrel.authorization.models.entities.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@FieldMatch.List({
    @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
})
/*@IsUnique.List({
    @IsUnique(property = "email", repository = "UserRepository", message = "This email already exists!")
})*/
@Accessors(chain = true)
@Setter
@Getter
public class CreateUserDto {
    private String id;

    @NotBlank(message = "The first name is required")
    private String firstName;

    @NotBlank(message = "The last name is required")
    private String lastName;

    @Email(message = "Email address is not valid")
    @NotBlank(message = "The email address is required")
    private String email;

    @Size(min = 6, message = "Must be at least 6 characters")
    private String password;

    @NotBlank(message = "The timezone is required")
    private String timezone;

    @NotBlank(message = "This field is required")
    private String confirmPassword;

    private String gender;

    private String avatar;

    private boolean enabled;

    private boolean confirmed;

    private Coordinates coordinates;

    private Role role;

    public CreateUserDto() {
        enabled = true;
    }
}
