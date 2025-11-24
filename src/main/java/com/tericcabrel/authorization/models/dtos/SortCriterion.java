package com.tericcabrel.authorization.models.dtos;

import lombok.Data;

@Data
public class SortCriterion {
    private String field;
    private String direction;
}