package com.tericcabrel.authorization.models.response;

import com.tericcabrel.authorization.models.entities.User;
import lombok.Data;

import java.util.List;

@Data
public class ListAllUsersResponse {

    private List<User> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
