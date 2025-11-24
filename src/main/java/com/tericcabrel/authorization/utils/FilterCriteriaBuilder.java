package com.tericcabrel.authorization.utils;

import com.tericcabrel.authorization.models.dtos.FilterCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FilterCriteriaBuilder {

    public static Query buildDynamicQuery(FilterCondition filters, List<String> fields) {

        Criteria criteria = FilterCriteriaBuilder.build(filters);
        Query query = new Query(criteria);
        // Add support for fields projection
        if (fields != null && !fields.isEmpty()) {
            fields.forEach(field -> query.fields().include(field));
        }
        log.debug("query {}", query);
        return query;
    }

    public static Criteria build(FilterCondition condition) {
        // Logical group: AND / OR
        // log.debug("building condition {}", condition);
        if (condition.getLogic() != null) {
            List<FilterCondition> subConditions = condition.getConditions();
            Criteria[] criteriaArray = subConditions.stream()
                    .map(FilterCriteriaBuilder::build)
                    .toArray(Criteria[]::new);

            return switch (condition.getLogic().toUpperCase()) {
                case "AND" -> new Criteria().andOperator(criteriaArray);
                case "OR" -> new Criteria().orOperator(criteriaArray);
                default -> throw new IllegalArgumentException("Unknown logic: " + condition.getLogic());
            };
        }

        // Leaf node: actual field comparison
        return buildFieldCriteria(condition);
    }

    private static Criteria buildFieldCriteria(FilterCondition condition) {
        String field = condition.getField();
        String op = condition.getOperator();
        Object value = condition.getValue();
        log.debug("buildFieldCriteria field->{}, op->{}, value->{}", field, op, value);
        return switch (op.toLowerCase()) {
            case "eq" -> Criteria.where(field).is(value);
            case "ne" -> Criteria.where(field).ne(value);
            case "gt" -> Criteria.where(field).gt(value);
            case "gte" -> Criteria.where(field).gte(value);
            case "lt" -> Criteria.where(field).lt(value);
            case "lte" -> Criteria.where(field).lte(value);
            case "in" -> Criteria.where(field).in((List<?>) value);
            case "nin" -> Criteria.where(field).nin((List<?>) value);
            case "regex" -> Criteria.where(field).regex(value.toString());
            default -> throw new IllegalArgumentException("Unsupported operator: " + op);
        };
    }
}
