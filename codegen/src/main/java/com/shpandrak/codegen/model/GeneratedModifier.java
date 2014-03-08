package com.shpandrak.codegen.model;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/12/12
 * Time: 18:21
 */
public enum GeneratedModifier {
    PUBLIC,
    PRIVATE,
    PROTECTED;


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
