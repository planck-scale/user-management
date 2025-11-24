package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.events.OnRegistrationCompleteEvent;
import com.tericcabrel.authorization.exceptions.PasswordNotMatchException;
import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.*;
import com.tericcabrel.authorization.models.entities.Permission;
import com.tericcabrel.authorization.models.entities.Role;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.ListAllUsersResponse;
import com.tericcabrel.authorization.models.response.UserListResponse;
import com.tericcabrel.authorization.models.response.UserResponse;
import com.tericcabrel.authorization.services.FileStorageServiceImpl;
import com.tericcabrel.authorization.services.interfaces.PermissionService;
import com.tericcabrel.authorization.services.interfaces.RoleService;
import com.tericcabrel.authorization.services.interfaces.UserService;
import com.tericcabrel.authorization.utils.AuthUtils;
import com.tericcabrel.authorization.utils.JwtTokenUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.tericcabrel.authorization.utils.Constants.*;
import static com.tericcabrel.authorization.utils.Constants.ROLE_NOT_FOUND_MESSAGE;
import static com.tericcabrel.authorization.utils.Constants.SWG_AUTH_REGISTER_ERROR;


@Slf4j
@RestController
@RequestMapping(value = "/users")
@Validated
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenUtil tokenService;

    @PreAuthorize("hasAuthority('read:users')")
    @PostMapping("/search")
    public ResponseEntity<ListAllUsersResponse> search(@RequestBody ListAllUsersDto request,
                                                       @RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring(7);
        String tenantId = tokenService.getTenantFromTokenUnsecure(token);
        return ResponseEntity.ok(userService.findAll(request, tenantId));
    }

    // @PreAuthorize("hasAuthority('read:users')")
    // @GetMapping("/all")
    public ResponseEntity<UserListResponse> all(){
        return ResponseEntity.ok(new UserListResponse(userService.findAll()));
    }

    @PreAuthorize("hasAuthority('read:users')")
    @GetMapping("/all/{role}")
    public ResponseEntity<UserListResponse> findByRole(@PathVariable String role) {
        log.debug("finding usrs with role {}", role);
        return ResponseEntity.ok(new UserListResponse(userService.findAllWithRole(role)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser() throws ResourceNotFoundException {
        log.debug("returning current user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(new UserResponse(userService.findByEmail(authentication.getName())));
    }

    @PreAuthorize("hasAuthority('read:user')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> one(@PathVariable String id)
        throws ResourceNotFoundException {
        return ResponseEntity.ok(new UserResponse(userService.findByEmail(id)));
    }

    @PreAuthorize("hasAuthority('update:user')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable String id, @RequestBody UpdateUserDto updateUserDto)
        throws ResourceNotFoundException {
        return ResponseEntity.ok(new UserResponse(userService.update(id, updateUserDto)));
    }

    @PreAuthorize("hasAuthority('change:password')")
    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable String id, @Valid @RequestBody UpdatePasswordDto updatePasswordDto
    ) throws PasswordNotMatchException, ResourceNotFoundException {
        User user = userService.updatePassword(id, updatePasswordDto);

        if (user == null) {
            throw new PasswordNotMatchException(PASSWORD_NOT_MATCH_MESSAGE);
        }

        return ResponseEntity.ok(new UserResponse(user));
    }

    @PreAuthorize("hasAuthority('delete:user')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String tenantId = tokenService.getTenantFromTokenUnsecure(token);
        userService.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('change:picture')")
    @PostMapping("/{id}/picture")
    public ResponseEntity<UserResponse> uploadPicture(
        @PathVariable String id,
        @RequestParam(name = "file", required = false) MultipartFile file,
        @RequestParam("action")
        @Pattern(regexp = "[ud]", message = "The valid value can be \"u\" or \"d\"")
        @Size(max = 1, message = "This field length can't be greater than 1")
        @NotBlank(message = "This field is required")
                    String action
    ) throws IOException, ResourceNotFoundException {
        User user = null;
        UpdateUserDto updateUserDto = new UpdateUserDto();

        if (action.equals("u")) {
            String fileName = fileStorageServiceImpl.storeFile(file);

            updateUserDto.setAvatar(fileName);

            user = userService.update(id, updateUserDto);
        } else if (action.equals("d")) {
            user = userService.findById(id);

            if (user.getAvatar() != null) {
                boolean deleted = fileStorageServiceImpl.deleteFile(user.getAvatar());

                if (deleted) {
                    user.setAvatar(null);
                    userService.update(user);
                }
            }
        } else {
            log.info(USER_PICTURE_NO_ACTION_MESSAGE);
        }

        return ResponseEntity.ok().body(new UserResponse(user));
    }


    @PreAuthorize("hasAuthority('add:permission')")
    @PutMapping("/permissions")
    public ResponseEntity<UserResponse> assignPermissions(@Valid @RequestBody UpdateUserPermissionDto updateUserPermissionDto)
        throws ResourceNotFoundException {
        User user = userService.findByEmail(updateUserPermissionDto.getEmail());

        Arrays.stream(updateUserPermissionDto.getPermissions()).forEach(permissionName -> {
            Optional<Permission> permission = permissionService.findByName(permissionName);

            if (permission.isPresent() && !user.hasPermission(permissionName)) {
                user.addPermission(permission.get());
            }
        });
        userService.update(user);
        return ResponseEntity.ok().body(new UserResponse(user));
    }

    @PreAuthorize("hasAuthority('remove:permission')")
    @DeleteMapping("/permissions")
    public ResponseEntity<User> revokePermissions(@Valid @RequestBody UpdateUserPermissionDto updateUserPermissionDto)
        throws ResourceNotFoundException {
        User user = userService.findByEmail(updateUserPermissionDto.getEmail());

        Arrays.stream(updateUserPermissionDto.getPermissions()).forEach(permissionName -> {
            Optional<Permission> permission = permissionService.findByName(permissionName);

            if (permission.isPresent() && user.hasPermission(permissionName)) {
                user.removePermission(permission.get());
            }
        });

        userService.update(user);

        return ResponseEntity.ok().body(user);
    }

    @PreAuthorize("hasAuthority('update:user')")
    @PostMapping(value = "/create")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserDto createUserDto) {
        try {
            log.debug("saving user {}", createUserDto);
            Role roleUser = roleService.findByName(ROLE_USER);
            createUserDto.setRole(roleUser);
            String tenantId = AuthUtils.getTenantId();
            createUserDto.setTenantId(tenantId);
            User user = userService.save(createUserDto);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            Map<String, String> result = new HashMap<>();
            result.put("message", SWG_AUTH_REGISTER_ERROR);
            log.error("Register User: " + ROLE_NOT_FOUND_MESSAGE);
            return ResponseEntity.badRequest().body(result);
        }
    }
}
