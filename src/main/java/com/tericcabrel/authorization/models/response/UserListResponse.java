package com.tericcabrel.authorization.models.response;

import com.tericcabrel.authorization.models.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
public class UserListResponse {
    private List<User> data;
}
