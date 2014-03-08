package com.shpandrak.codegen.model;

import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/12/12
 * Time: 18:20
 */
public class GeneratedClassMember extends GeneratedVariable{
    private GeneratedModifier modifier;
    private boolean getter;
    private boolean setter;
    private GeneratedCodeLine initializer;
    private boolean staticMember;
    private boolean finalMember;


    public GeneratedClassMember(String name, GeneratedType type, GeneratedModifier modifier, boolean getter, boolean setter, boolean staticMember, boolean finalMember, GeneratedCodeLine initializer) {
        super(name, type);
        this.getter = getter;
        this.setter = setter;
        this.modifier = modifier;
        this.initializer = initializer;
        this.finalMember = finalMember;
        this.staticMember = staticMember;
    }

    public GeneratedClassMember(String name, GeneratedType type, GeneratedModifier modifier, boolean getter, boolean setter, boolean staticMember, boolean finalMember) {
        this(name, type, modifier, getter, setter, staticMember, finalMember, null);
    }

    public GeneratedModifier getModifier() {
        return modifier;
    }

    public boolean isGetter() {
        return getter;
    }

    public boolean isSetter() {
        return setter;
    }

    public GeneratedCodeLine getInitializer() {
        return initializer;
    }

    public boolean isFinalMember() {
        return finalMember;
    }

    public boolean isStaticMember() {
        return staticMember;
    }

    @Override
    public void getImports(Set<String> imports) {
        super.getImports(imports);
        if (initializer != null){
            initializer.getImports(imports);
        }
    }
}
