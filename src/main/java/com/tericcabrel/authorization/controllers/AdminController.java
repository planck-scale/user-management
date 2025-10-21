package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.CreateUserDto;
import com.tericcabrel.authorization.models.entities.Role;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.UserResponse;
import com.tericcabrel.authorization.services.interfaces.RoleService;
import com.tericcabrel.authorization.services.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.tericcabrel.authorization.utils.Constants.ROLE_ADMIN;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admins")
public class AdminController {
  private final RoleService roleService;

  private final UserService userService;

  public AdminController(RoleService roleService, UserService userService) {
    this.roleService = roleService;
    this.userService = userService;
  }

  @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
  @PostMapping("/create-user")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserDto createUserDto)
      throws ResourceNotFoundException {
    Role roleUser = roleService.findByName(ROLE_ADMIN);

    createUserDto.setRole(roleUser)
        .setConfirmed(true)
        .setEnabled(true);

    User user = userService.save(createUserDto);

    return ResponseEntity.ok(new UserResponse(user));
  }

  @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    userService.delete(id);

    return ResponseEntity.noContent().build();
  }
}
