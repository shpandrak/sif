package com.shpandrak.codegen.model;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/18/12
 * Time: 19:19
 */
public class ClassGeneratedType extends GeneratedType {
    private String fullName;
    GeneratedGenericsImplementation genericsImplementation;

    public ClassGeneratedType(String simpleName, String fullName) {
        this(simpleName, fullName, null);
    }

    public ClassGeneratedType(String simpleName, String fullName, GeneratedGenericsImplementation genericsImplementation) {
        super(simpleName);
        this.fullName = fullName;
        this.genericsImplementation = genericsImplementation;
    }

    public ClassGeneratedType(Class clazz) {
        this(clazz.getSimpleName(), clazz.getCanonicalName());
    }

    public ClassGeneratedType(Class clazz, GeneratedGenericsImplementation genericsImplementation) {
        this(clazz.getSimpleName(), clazz.getCanonicalName(), genericsImplementation);
    }

    public ClassGeneratedType(GeneratedClass generatedClass, GeneratedGenericsImplementation genericsImplementation) {
        this(generatedClass.getName(), generatedClass.getFullClassName(), genericsImplementation);
    }
    public ClassGeneratedType(GeneratedClass generatedClass) {
        this(generatedClass, null);
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public void print(StringBuilder sb){
        super.print(sb);
        if (genericsImplementation != null){
            genericsImplementation.print(sb);
        }
    }

    @Override
    public void getImports(Set<String> imports) {
    imports.add(this.fullName);
        if (genericsImplementation != null){
            genericsImplementation.getImports(imports);
        }
    }
}
