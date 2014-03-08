package com.shpandrak.codegen.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/20/12
 * Time: 10:04
 */
public class GeneratedEnum extends GeneratedClass {
    private List<GeneratedEnumValue> values;

    public GeneratedEnum(String name, String packageName, List<GeneratedEnumValue> values) {
        super(name, packageName);
        this.values = values;
    }

    public List<GeneratedEnumValue> getValues() {
        return values;
    }
}
