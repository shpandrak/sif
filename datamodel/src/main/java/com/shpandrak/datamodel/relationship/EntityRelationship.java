package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 10:39
 */
public abstract class EntityRelationship<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> {
    private final SOURCE_CLASS ownerEntity;
    private final EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition;
    private RelationshipLoadLevel loadLevel = RelationshipLoadLevel.NONE;

    public abstract boolean isSatisfied();

    //todo:ugly ugly patch - taking generated ids - need to solve it in a proper way (either pre-generate or register for generated key event or implement key interface.
    public abstract void prepareForPersisting();

    protected EntityRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity) {
        this.definition = definition;
        this.ownerEntity =  ownerEntity;
    }

    public EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> getDefinition() {
        return definition;
    }

    public RelationshipLoadLevel getLoadLevel() {
        return loadLevel;
    }

    protected void setLoadLevelForce(RelationshipLoadLevel loadLevel) {
        this.loadLevel = loadLevel;
    }
    protected void setLoadLevel(RelationshipLoadLevel loadLevel) {
        switch (this.loadLevel){
            case ID:
                if (this.loadLevel != RelationshipLoadLevel.FULL){
                    this.loadLevel = loadLevel;
                }
                break;
            default:
                this.loadLevel = loadLevel;
                break;
        }
    }

    public SOURCE_CLASS getOwnerEntity() {
        return ownerEntity;
    }

    public abstract void setOwnerEntityId(Key ownerEntityId);

    @Override
    public String toString() {
        return "EntityRelationship{" +
                "definition=" + definition +
                "ownerEntity=" + ownerEntity +
                ", loadLevel=" + loadLevel +
                '}';
    }
}
