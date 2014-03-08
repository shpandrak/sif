package com.shpandrak.codegen.model;

import java.util.Collections;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/12/12
 * Time: 20:18
 */
public class GeneratedConstructor extends GeneratedMethod {
    GeneratedConstructor(String name, GeneratedModifier modifier, List<GeneratedParameter> params, List<ClassGeneratedType> throwsList, GeneratedBody body) {
        super(name, modifier, params, null, throwsList, Collections.<GeneratedAnnotation>emptyList(), false, body);
    }
}
