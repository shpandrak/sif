package com.shpandrak.codegen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/18/12
 * Time: 22:09
 */
public class GeneratedAnnotation implements ITypesContainer{
    private ClassGeneratedType annotationClass;
    private List<GeneratedCodeLine> params = new ArrayList<GeneratedCodeLine>();

    public GeneratedAnnotation(ClassGeneratedType annotationClass, List<GeneratedCodeLine> params) {
        this.annotationClass = annotationClass;
        this.params.addAll(params);
    }

    public GeneratedAnnotation(ClassGeneratedType annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public void getImports(Set<String> imports) {
        this.annotationClass.getImports(imports);
        for (GeneratedCodeLine currParam : params){
            currParam.getImports(imports);
        }
    }

    public ClassGeneratedType getAnnotationClass() {
        return annotationClass;
    }

    public List<GeneratedCodeLine> getParams() {
        return params;
    }

    public void print (StringBuilder sb){
        sb.append('@').append(annotationClass.getType());
        if (!params.isEmpty()){
            sb.append('(');
            boolean first = true;
            for (GeneratedCodeLine currParam : params){
                if (first){
                    first = false;
                }else {
                    sb.append(", ");
                }
                sb.append(currParam.getCode());
            }

            sb.append(')');
        }
    }
}
