package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.models.dtos.CreateGroupDto;
import com.tericcabrel.authorization.models.dtos.CreateGroupMemberDto;
import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.UserListResponse;
import com.tericcabrel.authorization.models.response.UserResponse;
import com.tericcabrel.authorization.services.interfaces.HierarchyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/ext/hierarchy")
@Validated
public class HierarchyController implements HierarchyAPI {

    @Autowired
    protected HierarchyService hierarchyService;

    @PreAuthorize("hasAuthority('create:group')")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupDto createGroupDto) {

        Group group = hierarchyService.createGroup(createGroupDto.getName(), createGroupDto.getParentName());
        return ResponseEntity.ok(group);
    }

    @PreAuthorize("hasAuthority('read:group')")
    public ResponseEntity<Group> getGroup(@PathVariable String groupName) {
        Group group = hierarchyService.getGroup(groupName);
        return ResponseEntity.ok(group);
    }

    @PreAuthorize("hasAuthority('update:user')")
    public ResponseEntity<UserResponse> addUserToGroup(@Valid @RequestBody CreateGroupMemberDto member) {

        User user = hierarchyService.addUserToGroup(member);
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<UserListResponse> getGroupMembers(@PathVariable String groupName) {

        List<User> users = hierarchyService.findUsersInSubtree(groupName);
        return ResponseEntity.ok(new UserListResponse(users));
    }

    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<UserListResponse> getSiblings(@PathVariable String email) {

        List<User> users = hierarchyService.findSiblings(email);
        return ResponseEntity.ok(new UserListResponse(users));
    }

    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<UserListResponse> getChildren(@PathVariable String email) {

        List<User> users = hierarchyService.findChildren(email);
        return ResponseEntity.ok(new UserListResponse(users));
    }
}
