package com.shpandrak.persistence.managers;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.relationship.EntityRelationship;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.UnsatisfiedRelationshipException;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/24/12
 * Time: 10:52
 */
public abstract class EntityRelationshipValidator {
    public static <T extends BaseEntity> void validateRelationshipsForCreate(T entity) throws PersistenceException{

        // Checking for mandatory relationships
        for (EntityRelationshipDefinition currRelationshipDef : entity.getRelationshipDescriptors().values()){
            EntityRelationship relationship = entity.getRelationship(currRelationshipDef);

            //todo: this is an ugly ugly patch - take the decision whether to pre-generate keys or not - and go with it!
            relationship.prepareForPersisting();
            if (currRelationshipDef.isMandatory()){
                if (!relationship.isSatisfied()){
                    throw new UnsatisfiedRelationshipException(relationship);
                }
            }
        }
    }
}
