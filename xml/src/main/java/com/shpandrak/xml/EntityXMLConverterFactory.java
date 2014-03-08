package com.shpandrak.xml;

import com.shpandrak.datamodel.BaseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/25/12
 * Time: 23:44
 */
public abstract class EntityXMLConverterFactory {
    private static final Map<Class<? extends BaseEntity>, EntityXMLConverter> convertersMap = new HashMap<Class<? extends BaseEntity>, EntityXMLConverter>();

    public static <T extends BaseEntity> void register(Class<T> entityClass, EntityXMLConverter<T> converter){
        convertersMap.put(entityClass, converter);
    }

    public static <T extends BaseEntity> EntityXMLConverter<T> getConverter(Class<T> entityClass){
        return convertersMap.get(entityClass);
    }
}
