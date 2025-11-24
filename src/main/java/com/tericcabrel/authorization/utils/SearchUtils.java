package com.tericcabrel.authorization.utils;

import com.tericcabrel.authorization.models.dtos.SortCriterion;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public class SearchUtils {

    public static Sort buildSort(List<SortCriterion> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortCriteria.stream()
                .map(criterion -> {
                    Sort.Direction direction = "desc".equalsIgnoreCase(criterion.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    return new Sort.Order(direction, criterion.getField());
                })
                .collect(Collectors.toList());

        return Sort.by(orders);
    }

    public static Pageable buildPageable(Integer page, Integer size, Sort sort) {
        if (page == null || size == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(page, size, sort);
    }
}
