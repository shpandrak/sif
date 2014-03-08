package com.shpandrak.codegen.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:28
 */
public class GeneratedCodeLine implements ITypesContainer{
    private String code;
    private List<ClassGeneratedType> typesInUse = new ArrayList<ClassGeneratedType>();

    public GeneratedCodeLine(String code) {
        this.code = code;
    }

    public GeneratedCodeLine(String code, List<ClassGeneratedType> typesInUse) {
        this.code = code;
        this.typesInUse.addAll(typesInUse);
    }

    public String getCode() {
        return code;
    }

    @Override
    public void getImports(Set<String> imports) {
        for (ClassGeneratedType currType : typesInUse){
            currType.getImports(imports);
        }
    }
}

