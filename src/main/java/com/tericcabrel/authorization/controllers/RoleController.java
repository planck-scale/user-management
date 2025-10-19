package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.CreateRoleDto;
import com.tericcabrel.authorization.models.dtos.UpdateRolePermissionDto;
import com.tericcabrel.authorization.models.entities.Permission;
import com.tericcabrel.authorization.models.entities.Role;
import com.tericcabrel.authorization.models.response.RoleListResponse;
import com.tericcabrel.authorization.models.response.RoleResponse;
import com.tericcabrel.authorization.services.interfaces.PermissionService;
import com.tericcabrel.authorization.services.interfaces.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/roles")
public class RoleController {
    private final RoleService roleService;

    private final PermissionService permissionService;

    public RoleController(PermissionService permissionService, RoleService roleService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAuthority('create:role')")
    @PostMapping
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleDto createRoleDto){
        Role role = roleService.save(createRoleDto);

        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasAuthority('read:roles')")
    @GetMapping
    public ResponseEntity<RoleListResponse> all(){
        return ResponseEntity.ok(new RoleListResponse(roleService.findAll()));
    }

    @PreAuthorize("hasAuthority('read:role')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> one(@PathVariable String id) throws ResourceNotFoundException {
        Role role = roleService.findById(id);

        return ResponseEntity.ok(new RoleResponse(role));
    }

    @PreAuthorize("hasAuthority('update:role')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> update(@PathVariable String id, @Valid @RequestBody CreateRoleDto createRoleDto)
        throws ResourceNotFoundException {
        return ResponseEntity.ok(new RoleResponse(roleService.update(id, createRoleDto)));
    }

    @PreAuthorize("hasAuthority('delete:role')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        roleService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('add:permission')")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleResponse> addPermissions(@PathVariable String id, @Valid @RequestBody UpdateRolePermissionDto updateRolePermissionDto)
        throws ResourceNotFoundException {
        Role role = roleService.findById(id);

        Arrays.stream(updateRolePermissionDto.getPermissions()).forEach(permissionName -> {
            Optional<Permission> permission = permissionService.findByName(permissionName);

            if (permission.isPresent() && !role.hasPermission(permissionName)) {
                role.addPermission(permission.get());
            }
        });

        Role roleUpdated = roleService.update(role);

        return ResponseEntity.ok().body(new RoleResponse(roleUpdated));
    }

    @PreAuthorize("hasAuthority('remove:permission')")
    @DeleteMapping("/{id}/permissions")
    public ResponseEntity<RoleResponse> removePermissions(@PathVariable String id, @Valid @RequestBody UpdateRolePermissionDto updateRolePermissionDto)
        throws ResourceNotFoundException {
        Role role = roleService.findById(id);

        Arrays.stream(updateRolePermissionDto.getPermissions()).forEach(permissionName -> {
            Optional<Permission> permission = permissionService.findByName(permissionName);

            if (permission.isPresent() && role.hasPermission(permissionName)) {
                role.removePermission(permission.get());
            }
        });

        Role roleUpdated = roleService.update(role);

        return ResponseEntity.ok().body(new RoleResponse(roleUpdated));
    }
}
