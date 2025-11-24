package com.tericcabrel.authorization.models.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FilterCondition {

    private String logic; // AND, OR
    private String field; // null if logic != null
    private String operator; // eq, gte, lt, in, etc.
    private Object value;
    private List<FilterCondition> conditions; // nested filters
}
