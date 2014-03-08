package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.OrderByClauseEntry;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;

import java.util.List;
import java.util.UUID;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 09:45
 */
public class EntityRelationshipDefinition<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> {
    private String name;
    private EntityRelationshipType type;
    private Class<SOURCE_CLASS> sourceClassType;
    private Class<TARGET_CLASS> targetClassType;
    private ShpandrakObjectDescriptor relationshipEntryDescriptor;
    private boolean mandatory;
    private IRelationshipEntryFieldsDescriptor relationshipEntryFieldsDescriptor;
    private EntityRelationshipDefinition<TARGET_CLASS, SOURCE_CLASS, ?> reverseRelationshipDefinition;
    private List<OrderByClauseEntry> relationshipSort;

    public EntityRelationshipDefinition(String name, EntityRelationshipType type, Class<SOURCE_CLASS> sourceClassType, Class<TARGET_CLASS> targetClassType, boolean mandatory, ShpandrakObjectDescriptor relationshipEntryDescriptor, List<OrderByClauseEntry> relationshipSort) {
        this(name, type, sourceClassType, targetClassType, mandatory, relationshipEntryDescriptor, relationshipSort, null);
    }
    public EntityRelationshipDefinition(String name, EntityRelationshipType type, Class<SOURCE_CLASS> sourceClassType, Class<TARGET_CLASS> targetClassType, boolean mandatory, ShpandrakObjectDescriptor relationshipEntryDescriptor, List<OrderByClauseEntry> relationshipSort, EntityRelationshipDefinition<TARGET_CLASS, SOURCE_CLASS, ?> reverseRelationshipDefinition) {
        this.name = name;
        this.type = type;
        this.sourceClassType = sourceClassType;
        this.targetClassType = targetClassType;
        this.mandatory = mandatory;
        this.relationshipEntryDescriptor = relationshipEntryDescriptor;
        if (!(relationshipEntryDescriptor instanceof IRelationshipEntryFieldsDescriptor)){
            throw new IllegalStateException("relationship entry descriptor " + relationshipEntryDescriptor.getClass().getCanonicalName() + " must implement " + IRelationshipEntryFieldsDescriptor.class.getCanonicalName());
        }
        this.relationshipEntryFieldsDescriptor = (IRelationshipEntryFieldsDescriptor) relationshipEntryDescriptor;
        this.relationshipSort = relationshipSort;
        this.reverseRelationshipDefinition = reverseRelationshipDefinition;
    }

    public String getName() {
        return name;
    }

    public Class<SOURCE_CLASS> getSourceClassType() {
        return sourceClassType;
    }

    public Class<TARGET_CLASS> getTargetClassType() {
        return targetClassType;
    }

    public EntityRelationshipDefinition<TARGET_CLASS, SOURCE_CLASS, ?> getReverseRelationshipDefinition() {
        return reverseRelationshipDefinition;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public EntityRelationshipType getType() {
        return type;
    }

    public ShpandrakObjectDescriptor getRelationshipEntryDescriptor() {
        return relationshipEntryDescriptor;
    }

    public List<OrderByClauseEntry> getRelationshipSort() {
        return relationshipSort;
    }

    @Override
    public String toString() {
        return "EntityRelationshipDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", sourceClassType=" + sourceClassType +
                ", targetClassType=" + targetClassType +
                ", mandatory=" + mandatory +
                '}';
    }

    public FieldDescriptor<UUID> getRelationshipEntrySourceEntityFieldDescriptor(){
        return relationshipEntryFieldsDescriptor.getSourceEntityFieldDescriptor();
    }

    public FieldDescriptor<UUID> getRelationshipEntryTargetEntityFieldDescriptor(){
        return relationshipEntryFieldsDescriptor.getTargetEntityFieldDescriptor();
    }

    public EntityRelationship instance(SOURCE_CLASS ownerEntity) {
        switch (type){
            case ONE_TO_MANY:
                return new EntityOneToManyRelationship(this, ownerEntity);
            case MANY_TO_MANY:
                return new EntityManyToManyRelationship(this, ownerEntity);
            case ONE_TO_ONE:
                return new EntityOneToOneRelationship(this, ownerEntity);
            case MANY_TO_ONE:
                return new EntityManyToOneRelationship(this, ownerEntity);
            default:
                throw new IllegalStateException("Unsupported Entity Relationship type: " + type);
        }
    }
}
