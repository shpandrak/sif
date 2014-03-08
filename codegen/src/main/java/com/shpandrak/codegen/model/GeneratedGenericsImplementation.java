package com.shpandrak.codegen.model;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/18/12
 * Time: 18:52
 */
public class GeneratedGenericsImplementation implements ITypesContainer{
    private List<GeneratedType> types;

    public GeneratedGenericsImplementation(List<GeneratedType> types) {
        this.types = types;
    }

    public List<GeneratedType> getTypes() {
        return types;
    }

    public void print(StringBuilder sb){
        sb.append('<');
        boolean first = true;
        for (GeneratedType currType : types){
            if (first){
                first = false;
            }else {
                sb.append(", ");
            }
            currType.print(sb);
        }
        sb.append('>');
    }

    @Override
    public void getImports(Set<String> imports) {
        for (GeneratedType currType : types){
            currType.getImports(imports);
        }
    }
}

