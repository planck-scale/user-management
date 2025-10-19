package com.tericcabrel.authorization.models.response;

import com.tericcabrel.authorization.models.entities.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
public class PermissionListResponse {
    private List<Permission> data;
}
