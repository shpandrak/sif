package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 10:08
 */
public abstract class BaseEntity extends BasePersistableShpandrakObject{

    // Entity relationships map
    private Map<EntityRelationshipDefinition, EntityRelationship> relationships = new HashMap<EntityRelationshipDefinition, EntityRelationship>(getRelationshipDescriptors().size());

    // Relationships definitions set
    public abstract Map<String, EntityRelationshipDefinition> getRelationshipDescriptors();

    protected EntityRelationshipDefinition getEntityRelationshipDefinition(String relationshipName) {
        return getEntityDescriptor().getRelationshipDefinition(relationshipName);
    }

    public abstract BaseEntityDescriptor getEntityDescriptor();

    @Override
    public ShpandrakObjectDescriptor getObjectDescriptor() {
        return getEntityDescriptor();
    }

    public <S extends BaseEntity,T extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<T>> EntityOneToManyRelationship<S, T, REL_ENTRY_CLASS> getOneToManyRelationship(EntityRelationshipDefinition<S, T, REL_ENTRY_CLASS> definition){
        return (EntityOneToManyRelationship<S, T, REL_ENTRY_CLASS>) getRelationship(definition);
    }

    public <S extends BaseEntity,T extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<T>> EntityManyToManyRelationship<S, T, REL_ENTRY_CLASS> getManyToManyRelationship(EntityRelationshipDefinition<S, T, REL_ENTRY_CLASS> definition){
        return (EntityManyToManyRelationship<S, T, REL_ENTRY_CLASS>) getRelationship(definition);
    }



    public EntityRelationship getRelationship(String relationshipName){
        return getRelationship(getEntityRelationshipDefinition(relationshipName));

    }


    public <S extends BaseEntity,T extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<T>> EntityRelationship<S, T, REL_ENTRY_CLASS> getRelationship(EntityRelationshipDefinition<S, T, REL_ENTRY_CLASS> definition){
        EntityRelationship entityRelationship = relationships.get(definition);
        if (entityRelationship == null){
            if (!getRelationshipDescriptors().containsKey(definition.getName())){
                throw new IllegalArgumentException("Relationship requested is not owned by the source class. source class: " + getClass().getSimpleName() + " definition:" + definition.toString());
            }else {
                EntityRelationship<S, T, REL_ENTRY_CLASS> newRelationshipInstance = definition.instance((S)this);
                relationships.put(definition, newRelationshipInstance);
                return newRelationshipInstance;
            }
        }else {
            return entityRelationship;
        }
    }

    public List<EntityRelationship> getLoadedRelationships(){
        List<EntityRelationship> relationshipList = new ArrayList<EntityRelationship>(getRelationshipDescriptors().size());
        for (EntityRelationship currRelationship : relationships.values()) {
            if (currRelationship.getLoadLevel() != RelationshipLoadLevel.NONE){
                relationshipList.add(currRelationship);
            }
        }
        return relationshipList;
    }

    @Override
    public void setId(Key id) {
        super.setId(id);

        // Setting id for all uninitialized relationships
        for (EntityRelationship currRelationship : relationships.values()){
            currRelationship.setOwnerEntityId(id);
        }
    }
}
