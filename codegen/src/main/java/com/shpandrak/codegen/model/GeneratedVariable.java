package com.shpandrak.codegen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/12/12
 * Time: 20:02
 */
public abstract class GeneratedVariable implements ITypesContainer  {
    private String name;
    private GeneratedType type;
    private List<GeneratedAnnotation> annotationList = new ArrayList<GeneratedAnnotation>();

    protected GeneratedVariable(String name, GeneratedType type) {
        this.name = name;
        this.type = type;
    }

    protected GeneratedVariable(String name, GeneratedType type, List<GeneratedAnnotation> annotationList) {
        this.name = name;
        this.type = type;
        this.annotationList.addAll(annotationList);
    }

    public String getName() {
        return name;
    }

    public GeneratedType getType() {
        return type;
    }

    public void print(StringBuilder sb) {
        for (GeneratedAnnotation currAnnotation : annotationList) {
            currAnnotation.print(sb);
            sb.append(' ');
        }
        type.print(sb);
        sb.append(' ').append(name);
    }

    public List<GeneratedAnnotation> getAnnotationList() {
        return annotationList;
    }

    @Override
    public void getImports(Set<String> imports) {
        type.getImports(imports);
        for (GeneratedAnnotation currAnnotation : annotationList){
            currAnnotation.getImports(imports);
        }
    }
}
