package com.shpandrak.codegen.model;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/12/12
 * Time: 20:11
 */
public class GeneratedType implements ITypesContainer{
    private String type;

    public GeneratedType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void print(StringBuilder sb) {
        sb.append(type);
    }

    @Override
    public void getImports(Set<String> imports) {
    }
}
