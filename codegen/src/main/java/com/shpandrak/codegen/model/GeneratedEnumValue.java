package com.shpandrak.codegen.model;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/20/12
 * Time: 10:24
 */
public class GeneratedEnumValue {
    private String name;
    private int ordinal;

    public GeneratedEnumValue(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
