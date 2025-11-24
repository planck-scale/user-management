package com.tericcabrel.authorization.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListAllUsersDto {

    @JsonProperty("filters")
    private FilterCondition filters;

    @JsonProperty("sort")
    private List<SortCriterion> sort;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("size")
    private Integer size;

    private List<String> fields;
}
