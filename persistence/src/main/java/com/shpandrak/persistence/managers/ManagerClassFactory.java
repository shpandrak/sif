package com.shpandrak.persistence.managers;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.IPersistableShpandrakObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/30/12
 * Time: 08:09
 */
public class ManagerClassFactory {
    private static final Map<Class<? extends IPersistableShpandrakObject>, Class<? extends IPersistableObjectManager>> map = new HashMap<Class<? extends IPersistableShpandrakObject>, Class<? extends IPersistableObjectManager>>();

    public static void register(Class<? extends IPersistableShpandrakObject> clazz, Class<? extends IPersistableObjectManager> managerClass){
        map.put(clazz, managerClass);
    }

    public static <T extends IPersistableShpandrakObject> Class<IPersistableObjectManager<T>> get(Class<T> clazz){
        return (Class<IPersistableObjectManager<T>>) map.get(clazz);
    }

    public static <T extends IPersistableShpandrakObject> IPersistableObjectManager<T> getDefaultInstance(Class<T> clazz){
        Class<IPersistableObjectManager<T>> managerClass = get(clazz);
        if (managerClass == null){
            throw new IllegalArgumentException("Unable to find Base Manager class for entity " + clazz.getName());
        }
        try {
            return managerClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        }
    }

}
