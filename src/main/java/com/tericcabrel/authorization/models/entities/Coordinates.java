package com.tericcabrel.authorization.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Setter
@Getter
@ToString
public class Coordinates {
    private float lat;

    private float lon;
}
