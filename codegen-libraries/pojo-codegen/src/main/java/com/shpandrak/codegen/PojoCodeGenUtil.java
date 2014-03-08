package com.shpandrak.codegen;

import com.shpandrak.codegen.model.ClassGeneratedType;
import com.shpandrak.codegen.model.GeneratedType;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.common.model.FieldType;

import java.util.Date;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:54
 */
public abstract class PojoCodeGenUtil {
    public static GeneratedType getGeneratedTypeFromMetadataType(FieldType fieldType){
        switch (fieldType){
            case DATE:
                return new ClassGeneratedType(Date.class);
            default:
                return new GeneratedType(fieldType.getJavaType().getSimpleName());
        }
    }

    public static String generateGetterName(String getterName, String typeName) {
        if ("boolean".equalsIgnoreCase(typeName)) {
            return  "is" + getterName.substring(0, 1).toUpperCase() + getterName.substring(1);
        } else {
            return  "get" + getterName.substring(0, 1).toUpperCase() + getterName.substring(1);
        }
    }

    public static String generateGetterName(AbstractFieldMetadata currField) {
        return generateGetterName(currField.getName(), currField.getType().getJavaNativeTypeName());
    }

    public static String generateJavaClassName(String entityName){
        return entityName.substring(0,1).toUpperCase() + entityName.substring(1);
    }

}
