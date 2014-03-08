package com.shpandrak.codegen.model;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/12/12
 * Time: 20:10
 */
public class GeneratedMethod implements ITypesContainer{
    private String name;
    private GeneratedModifier modifier;
    private List<GeneratedParameter> params;
    private GeneratedType returnType;
    private GeneratedBody body;
    private List<ClassGeneratedType> throwsList;
    private boolean staticMethod;
    private Map<String, GeneratedAnnotation> annotations = new HashMap<String, GeneratedAnnotation>();

    public GeneratedMethod(String name, GeneratedModifier modifier, List<GeneratedParameter> params, GeneratedType returnType, List<ClassGeneratedType> throwsList, List<GeneratedAnnotation> annotations, boolean staticMethod, GeneratedBody body) {
        this.name = name;
        this.modifier = modifier;
        this.params = new ArrayList<GeneratedParameter>(params);
        this.returnType = returnType;
        this.body = body;
        this.throwsList = new ArrayList<ClassGeneratedType>(throwsList);
        this.staticMethod = staticMethod;
        for (GeneratedAnnotation currAnnotation : annotations){
            addAnnotation(currAnnotation);
        }
    }

    public void addAnnotation(GeneratedAnnotation annotation){
        this.annotations.put(annotation.getAnnotationClass().getFullName(), annotation);
    }

    public String getName() {
        return name;
    }

    public List<GeneratedParameter> getParams() {
        return params;
    }

    public GeneratedType getReturnType() {
        return returnType;
    }

    public GeneratedModifier getModifier() {
        return modifier;
    }

    public List<ClassGeneratedType> getThrowsList() {
        return throwsList;
    }

    public GeneratedBody getBody() {
        return body;
    }

    public Map<String, GeneratedAnnotation> getAnnotations() {
        return annotations;
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }

    public void setStaticMethod(boolean staticMethod) {
        this.staticMethod = staticMethod;
    }

    @Override
    public void getImports(Set<String> imports) {
        if (this.returnType != null){
            this.returnType.getImports(imports);
        }
        for (GeneratedType currThrowable : throwsList){
            currThrowable.getImports(imports);
        }
        for (GeneratedVariable currParam : params){
            currParam.getImports(imports);
        }
        for (GeneratedAnnotation currAnnotation : annotations.values()){
            currAnnotation.getImports(imports);
        }
        body.getImports(imports);
    }
}
