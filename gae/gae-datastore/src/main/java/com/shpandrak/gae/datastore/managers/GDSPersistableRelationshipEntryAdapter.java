package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.datamodel.relationship.BasePersistableRelationshipEntry;

import java.util.Map;
import java.util.UUID;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:15
 */
public class GDSPersistableRelationshipEntryAdapter<T extends BasePersistableRelationshipEntry> extends GDSPersistableObjectAdapter<T> {
    public GDSPersistableRelationshipEntryAdapter(BasePersistentObjectDescriptor<T> descriptor, String alias) {
        super(descriptor, alias);
    }

    public void generateKey(T currObject) {
        currObject.setId(new EntityKey(KeyFactory.createKeyString(KeyFactory.stringToKey(currObject.getSourceEntityId().toString()), getDescriptor().getEntityClass().getSimpleName(), UUID.randomUUID().toString())));
    }
}
