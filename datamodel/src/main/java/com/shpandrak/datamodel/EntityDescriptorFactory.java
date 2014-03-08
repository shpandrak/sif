package com.shpandrak.datamodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/27/12
 * Time: 23:15
 */
public abstract class EntityDescriptorFactory {
    private static final Map<Class<? extends BaseEntity>, BaseEntityDescriptor> map = new HashMap<Class<? extends BaseEntity>, BaseEntityDescriptor>();

    public static void register(Class<? extends BaseEntity> clazz, BaseEntityDescriptor descriptor){
        map.put(clazz, descriptor);
    }

    public static <T extends BaseEntity> BaseEntityDescriptor<T> get(Class<T> clazz){
        return map.get(clazz);
    }
}
