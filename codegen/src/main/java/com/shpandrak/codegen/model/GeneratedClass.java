package com.shpandrak.codegen.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love
 * User: shpandrak
 * Date: 10/11/12
 * Time: 18:39
 */
public class GeneratedClass{
    private String name;
    private String packageName;
    private GeneratedModifier modifier = GeneratedModifier.PUBLIC;
    private boolean abstractClass = false;
    private boolean finalClass = false;
    private ClassGeneratedType extendsClassType = null;
    private Set<String> imports = new HashSet<String>();
    private Map<String , GeneratedClassMember> membersMap = new HashMap<String, GeneratedClassMember>();
    private List<GeneratedClassMember> members = new ArrayList<GeneratedClassMember>();
    private List<GeneratedConstructor> constructors = new ArrayList<GeneratedConstructor>();
    private Map<String, GeneratedMethod> methods = new HashMap<String, GeneratedMethod>();
    private List<GeneratedMethod> orderedMethods = new ArrayList<GeneratedMethod>();
    private Map<String, ClassGeneratedType> implementedInterfaces = new HashMap<String, ClassGeneratedType>();
    private Map<String, GeneratedAnnotation> annotations = new HashMap<String, GeneratedAnnotation>();
    private List<GeneratedBody> staticCodeBlocks = new ArrayList<GeneratedBody>();


    public GeneratedClass(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public void addAnnotation(GeneratedAnnotation annotation){
        addImports(annotation);
        this.annotations.put(annotation.getAnnotationClass().getFullName(), annotation);
    }


    public String getFullClassName(){
        return packageName + '.' + name;
    }

    public void addImport(String importName){
        imports.add(importName);
    }
    public void addImport(Class clazz){
        addImport(clazz.getCanonicalName());
    }

    public void addImports(ITypesContainer typesContainer){
        typesContainer.getImports(this.imports);
    }

    public void addMember(GeneratedClassMember member){
        GeneratedClassMember generatedClassMember = membersMap.get(member.getName());
        if (generatedClassMember != null){
            throw new IllegalStateException("Memeber with name " + member.getName() + " already exists in class " + getFullClassName());
        }
        addImports(member);
        membersMap.put(member.getName(), member);
        members.add(member);
    }

    public void addMethod(GeneratedMethod method){
        addImports(method);
        methods.put(method.getName(), method);
        orderedMethods.add(method);
    }

    public void addInterface(ClassGeneratedType interfaceClass){
        addImports(interfaceClass);
        implementedInterfaces.put(interfaceClass.getType(), interfaceClass);
    }

    public void addInterface(Class i, GeneratedGenericsImplementation genericsImplementation){
        addInterface(new ClassGeneratedType(i, genericsImplementation));
    }

    public void addInterface(Class i){
        addInterface(i, null);
    }

    public GeneratedConstructor addConstructor(GeneratedModifier modifier, List<GeneratedParameter> params, List<ClassGeneratedType> throwsList, GeneratedBody body){
        GeneratedConstructor constructor = new GeneratedConstructor(getName(), modifier, params, throwsList, body);
        addImports(constructor);
        constructors.add(constructor);
        return constructor;
    }

    public GeneratedConstructor addDefaultConstructor(GeneratedModifier modifier, GeneratedBody body){
        return addConstructor(modifier, Collections.<GeneratedParameter>emptyList(), Collections.<ClassGeneratedType>emptyList(), body);
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<String> getImports() {
        return imports;
    }

    public List<GeneratedClassMember> getMembers() {
        return members;
    }

    public List<GeneratedConstructor> getConstructors() {
        return constructors;
    }

    public List<GeneratedMethod> getMethods() {
        return orderedMethods;
    }

    public GeneratedModifier getModifier() {
        return modifier;
    }

    public void setModifier(GeneratedModifier modifier) {
        this.modifier = modifier;
    }

    public ClassGeneratedType getExtendsClassType() {
        return extendsClassType;
    }

    public void setExtendsClassType(ClassGeneratedType extendsClassType) {
        addImports(extendsClassType);
        this.extendsClassType = extendsClassType;
    }

    public void setExtendsClass(Class clazz){
        setExtendsClass(clazz, null);
    }

    public void setExtendsClass(Class clazz, GeneratedGenericsImplementation genericsImplementation){
        setExtendsClassType(new ClassGeneratedType(clazz, genericsImplementation));
    }

    public boolean isAbstractClass() {
        return abstractClass;
    }

    public void setAbstractClass(boolean abstractClass) {
        this.abstractClass = abstractClass;
    }

    public boolean isFinalClass() {
        return finalClass;
    }

    public void setFinalClass(boolean finalClass) {
        this.finalClass = finalClass;
    }

    public void addStaticCodeBlock(GeneratedBody staticCodeBlockBody){
        if (staticCodeBlockBody != null){
            addImports(staticCodeBlockBody);
            this.staticCodeBlocks.add(staticCodeBlockBody);
        }
    }

    public List<GeneratedBody> getStaticCodeBlocks() {
        return staticCodeBlocks;
    }

    public void addOverridingMethod(Class clazz, String methodName, GeneratedModifier modifier, GeneratedBody body) throws NoSuchMethodException {
        Method method = findMethod(clazz, methodName);
        List<GeneratedParameter> params = new ArrayList<GeneratedParameter>();
        for (Class<?> currParam : method.getParameterTypes()){
            if (!currParam.isPrimitive()){
                addImport(currParam.getCanonicalName());
            }
            params.add(new GeneratedParameter(currParam.getSimpleName().toLowerCase(), new GeneratedType(currParam.getSimpleName())));
        }

        List<ClassGeneratedType> throwsList = new ArrayList<ClassGeneratedType>();
        for (Class<?> currExceptionType : method.getExceptionTypes()){
            throwsList.add(new ClassGeneratedType(currExceptionType));
        }

        GeneratedMethod generatedMethod = new GeneratedMethod(methodName, modifier, params, new GeneratedType(method.getReturnType().getSimpleName()), throwsList, Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, body);
        addMethod(generatedMethod);
    }

    private Method findMethod(Class clazz, String methodName) throws NoSuchMethodException {
        for (Method currMethod : clazz.getMethods()){
            if (currMethod.getName().equals(methodName)){
                return currMethod;
            }
        }
        return clazz.getMethod(methodName);
    }

    public Map<String, ClassGeneratedType> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public Map<String, GeneratedAnnotation> getAnnotations() {
        return annotations;
    }

    /**
     * Adds SLF4J logger member named logger
     */
    public void addLogger() {
        this.addImport(LoggerFactory.class);
        this.addMember(new GeneratedClassMember("logger", new ClassGeneratedType(Logger.class), GeneratedModifier.PRIVATE, false, false, true, true, new GeneratedCodeLine("LoggerFactory.getLogger(" + this.getName() + ".class);")));
    }

    public GeneratedMethod getMethod(String methodName){
        return methods.get(methodName);
    }
}
