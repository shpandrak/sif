package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/27/12
 * Time: 20:58
 */
public abstract class BaseEntityDescriptor<T extends BaseEntity> extends BasePersistentObjectDescriptor {
    private Map<String, EntityRelationshipDefinition> relationshipDefinitionMap;
    private EntityRelationshipDefinition ownerRelationshipDefinition;

    protected BaseEntityDescriptor() {
    }

    protected void initialize(Collection<FieldDescriptor> fieldDescriptors, FieldDescriptor<? extends Key> keyFieldDescriptor, Collection<EntityRelationshipDefinition> relationshipDefinitions, EntityRelationshipDefinition ownerRelationshipDefinition) {
        super.initialize(fieldDescriptors, keyFieldDescriptor);
        this.relationshipDefinitionMap = new HashMap<String, EntityRelationshipDefinition>(relationshipDefinitions.size());
        for (EntityRelationshipDefinition currRelationshipDefinition : relationshipDefinitions) {
            this.relationshipDefinitionMap.put(currRelationshipDefinition.getName(), currRelationshipDefinition);
        }
        this.ownerRelationshipDefinition = ownerRelationshipDefinition;
    }

    protected BaseEntityDescriptor(Collection<FieldDescriptor> fieldDescriptors, FieldDescriptor<Key> keyFieldDescriptor, Collection<EntityRelationshipDefinition> relationshipDefinitions, EntityRelationshipDefinition ownerRelationshipDefinition) {
        initialize(fieldDescriptors, keyFieldDescriptor, relationshipDefinitions, ownerRelationshipDefinition);
    }

    public Map<String, EntityRelationshipDefinition> getRelationshipDefinitionMap() {
        return relationshipDefinitionMap;
    }

    public EntityRelationshipDefinition getOwnerRelationshipDefinition() {
        return ownerRelationshipDefinition;
    }

    /**
     * Get relationship definition by name. if relationship does not exits an IllegalStateException exception is thrown
     * @param relationshipName relationship name
     * @return relationship definition
     */
    public EntityRelationshipDefinition getRelationshipDefinition(String relationshipName) {
        if (relationshipName == null) throw new IllegalArgumentException("Relationship name cannot be null, bye");
        EntityRelationshipDefinition entityRelationshipDefinition = relationshipDefinitionMap.get(relationshipName);
        if (entityRelationshipDefinition == null){
            throw new IllegalArgumentException("Invalid relationship with name " + relationshipName + " for entity type " + getClass().getName());
        }
        return entityRelationshipDefinition;
    }

    public KeyFieldDescriptor<Key> getKeyFieldDescriptor(){
        return (KeyFieldDescriptor<Key>) getOrderedFieldDescriptors().get(0);
    }

    public abstract Class<? extends BaseEntity> getEntityClass();
}
