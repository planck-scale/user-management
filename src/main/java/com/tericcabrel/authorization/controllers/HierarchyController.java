package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.models.dtos.CreateGroupDto;
import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.Role;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.UserResponse;
import com.tericcabrel.authorization.services.interfaces.HierarchyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/hierarchy")
@Validated
public class HierarchyController {

    @Autowired
    private HierarchyService hierarchyService;


    // @PreAuthorize("hasAuthority('create:group')")
    @PostMapping("/group")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupDto createGroupDto) {

        Group group = hierarchyService.createGroup(createGroupDto.getName(), createGroupDto.getParentName());
        return ResponseEntity.ok(group);
    }

    // @PreAuthorize("hasAuthority('update:user')")
    @PutMapping("/user/{email}/group/{groupName}")
    public ResponseEntity<UserResponse> addUserToGroup(@PathVariable String email, @PathVariable String groupName) {

        User user = hierarchyService.addUserToGroup(email, groupName);
        return ResponseEntity.ok(new UserResponse(user));
    }


}
