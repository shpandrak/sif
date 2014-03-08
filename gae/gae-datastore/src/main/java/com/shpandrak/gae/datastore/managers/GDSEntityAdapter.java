package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.relationship.EntityOneToManyRelationship;
import com.shpandrak.datamodel.relationship.EntityRelationship;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.persistence.PersistenceException;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/12/13
 * Time: 15:02
 */
public class GDSEntityAdapter<T extends BaseEntity> extends GDSPersistableObjectAdapter<T> {
    public GDSEntityAdapter(BaseEntityDescriptor<T> descriptor, String alias) {
        super(descriptor, alias);
    }

    @Override
    protected BaseEntityDescriptor<T> getDescriptor() {
        return (BaseEntityDescriptor<T>) super.getDescriptor();
    }

    @Override
    protected Entity prepareForPersisting(T object) {
        Entity entity = super.prepareForPersisting(object);

        // Getting the oneToMany relationships
        for (EntityRelationship currRelationship : object.getLoadedRelationships()){
            switch (currRelationship.getDefinition().getType()){
                case ONE_TO_MANY:
                    Key keyValue = null;
                    com.shpandrak.datamodel.field.Key targetEntityId = ((EntityOneToManyRelationship) currRelationship).getTargetEntityId();
                    if (targetEntityId != null){
                        keyValue = KeyFactory.stringToKey(targetEntityId.toString());
                    }
                    entity.setProperty(currRelationship.getDefinition().getName() + "Key", keyValue);
                    break;
            }
        }

        return entity;
    }

    @Override
    public T convert(Entity dsEntity) {
        T entity = super.convert(dsEntity);
        // Getting the oneToMany relationships

        for (EntityRelationshipDefinition currRelationshipDef : getDescriptor().getRelationshipDefinitionMap().values()){
            switch (currRelationshipDef.getType()){
                case ONE_TO_MANY:
                    Key relKeyProperty = (Key) dsEntity.getProperty(currRelationshipDef.getName() + "Key");
                    EntityKey targetEntityId = null;
                    if (relKeyProperty != null) {
                        targetEntityId = new EntityKey(KeyFactory.keyToString(relKeyProperty));
                    }
                    ((EntityOneToManyRelationship)entity.getRelationship(currRelationshipDef)).setTargetEntityId(targetEntityId);

                    break;
            }
        }



        return entity;
    }

    @Override
    public void generateKey(T currObject) throws PersistenceException {

        // If ownerRelationshipExists - we add the owner entity key as the datastore parent key for the entity
        EntityRelationshipDefinition ownerRelationshipDefinition = getDescriptor().getOwnerRelationshipDefinition();
        if (ownerRelationshipDefinition == null){
            super.generateKey(currObject);
        }else {
            com.shpandrak.datamodel.field.Key ownerEntityId = currObject.getOneToManyRelationship(ownerRelationshipDefinition).getTargetEntityId();
            if (ownerEntityId == null){
                throw new PersistenceException("Failed generating key for entity " + getDescriptor().getEntityName() + " because id for owner entity is null - " + ownerRelationshipDefinition.toString());
            }
            Key parentKey = KeyFactory.stringToKey(ownerEntityId.toString());
            String keyName = generateKeyName(currObject);
            currObject.setId(new EntityKey(KeyFactory.createKeyString(parentKey, getDescriptor().getEntityClass().getSimpleName(), keyName)));
        }

    }
}
