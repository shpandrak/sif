package com.shpandrak.common.model;


import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/10/12
 * Time: 20:01
 */
public enum FieldType {
    KEY("Key", null),
    STRING("String", String.class),
    INTEGER("int", Integer.class),
    LONG("long", Long.class),
    DATE("Date", Date.class),
    DOUBLE("double", Double.class),
    BOOLEAN("boolean", Boolean.class),
    ENUM("Enum", Enum.class);
    private FieldType(String javaNativeTypeName, Class javaType) {
        this.javaNativeTypeName = javaNativeTypeName;
        this.javaType = javaType;
    }

    private String javaNativeTypeName;
    private Class javaType;

    public String getJavaNativeTypeName() {
        return javaNativeTypeName;
    }

    public Class getJavaType() {
        return javaType;
    }
}
