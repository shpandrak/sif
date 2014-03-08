package com.shpandrak.database.managers;

import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.managers.ManagerClassFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/30/12
 * Time: 08:17
 */
public abstract class DBManagerFactory {
    public static <T extends IPersistableShpandrakObject> IPersistableObjectManager<T> getManager(Class<T> clazz, IDBConnectionProvider connectionProvider) {
        Class<IPersistableObjectManager<T>> baseManagerClass = ManagerClassFactory.get(clazz);
        if (baseManagerClass == null){
            throw new IllegalArgumentException("Unable to find Base Manager class for entity " + clazz.getName());
        }
        try {
            if (connectionProvider != null){
                return baseManagerClass.getConstructor(IDBConnectionProvider.class).newInstance(connectionProvider);
            }else {
                return baseManagerClass.getConstructor().newInstance();
            }
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to load manager class for entity class " + clazz.getName(), e);
        }
    }

    public static <T extends BaseEntity> IPersistableObjectManager<T> getManager(Class<T> clazz) {
        return getManager(clazz, null);
    }
}
