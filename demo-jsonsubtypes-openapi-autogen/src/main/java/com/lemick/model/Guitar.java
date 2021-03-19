package com.lemick.model;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class Guitar extends Instrument {

    private int numberOfStrings;

    public int getNumberOfStrings() {
        return numberOfStrings;
    }

    public void setNumberOfStrings(int numberOfStrings) {
        this.numberOfStrings = numberOfStrings;
    }
}
