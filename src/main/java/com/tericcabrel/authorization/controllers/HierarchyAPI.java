package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.models.dtos.CreateGroupDto;
import com.tericcabrel.authorization.models.dtos.CreateGroupMemberDto;
import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.UserListResponse;
import com.tericcabrel.authorization.models.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface HierarchyAPI {

    @PostMapping("/group")
    ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupDto createGroupDto);

    @GetMapping("/group/{groupName}")
    ResponseEntity<Group> getGroup(@PathVariable String groupName);

    @PutMapping("/group/member")
    ResponseEntity<UserResponse> addUserToGroup(@Valid @RequestBody CreateGroupMemberDto member);

    @GetMapping("/group/{groupName}/members")
    ResponseEntity<UserListResponse> getGroupMembers(@PathVariable String groupName);

    @GetMapping("/siblings/{email}")
    ResponseEntity<UserListResponse> getSiblings(@PathVariable String email);

    @GetMapping("/user/{email}/children")
    public ResponseEntity<UserListResponse> getChildren(@PathVariable String email);
}
