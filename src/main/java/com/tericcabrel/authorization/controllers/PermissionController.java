package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.entities.Permission;
import com.tericcabrel.authorization.models.response.PermissionListResponse;
import com.tericcabrel.authorization.models.response.PermissionResponse;
import com.tericcabrel.authorization.services.interfaces.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.tericcabrel.authorization.utils.Constants.PERMISSION_NOT_FOUND_MESSAGE;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PermissionListResponse> all(){
        return ResponseEntity.ok(new PermissionListResponse(permissionService.findAll()));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> one(@PathVariable String id)
        throws ResourceNotFoundException {
        Optional<Permission> permission = permissionService.findById(id);

        if (permission.isEmpty()) {
            throw new ResourceNotFoundException(PERMISSION_NOT_FOUND_MESSAGE);
        }

        return ResponseEntity.ok(new PermissionResponse(permission.get()));
    }
}
