package com.shpandrak.codegen.model;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/23/12
 * Time: 23:43
 */
public class GeneratedParameter extends GeneratedVariable {
    public GeneratedParameter(String name, GeneratedType type) {
        super(name, type);
    }

    public GeneratedParameter(String name, GeneratedType type, List<GeneratedAnnotation> annotationList) {
        super(name, type, annotationList);
    }
}
