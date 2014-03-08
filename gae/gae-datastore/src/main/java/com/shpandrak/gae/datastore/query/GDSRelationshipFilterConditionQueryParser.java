package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.shpandrak.common.model.FieldType;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.BasePersistableShpandrakObject;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.relationship.BasePersistableRelationshipEntry;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.gae.datastore.managers.GDSPersistableObjectAdapter;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.ManagerClassFactory;
import com.shpandrak.persistence.query.filter.RelationshipFilterCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 15:29
 */
public class GDSRelationshipFilterConditionQueryParser implements IGDSFilterQueryParser<RelationshipFilterCondition>{
    @Override
    public Query.Filter parse(RelationshipFilterCondition condition, GDSQueryParsingContext context) throws PersistenceException {
        EntityRelationshipDefinition relationshipDefinition = condition.getRelationshipDefinition();
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                // Checking whether this is an owner one to many relationship
                Key relatedEntityKey = null;
                if (condition.getRelatedEntityId() != null){
                    relatedEntityKey = KeyFactory.stringToKey(condition.getRelatedEntityId().toString());
                }

                if (condition.getRelationshipDefinition().equals(((BaseEntityDescriptor) context.getDescriptor()).getOwnerRelationshipDefinition())){
                    if (relatedEntityKey != null){
                        context.setQueryAncestor(relatedEntityKey);
                    }
                    return null;
                }else {
                    return new Query.FilterPredicate(relationshipDefinition.getName() + "Key", Query.FilterOperator.EQUAL, relatedEntityKey);
                }
            case MANY_TO_MANY:
                // No joins.. can be a smart ass if must (e.g. post query filter..)
                //todo: fetch only the ids
                List<BasePersistableRelationshipEntry> relatedRelationshipEntries = ManagerClassFactory.getDefaultInstance(((BasePersistentObjectDescriptor) condition.getRelationshipDefinition().getRelationshipEntryDescriptor()).getEntityClass()).listByField(condition.getRelationshipDefinition().getRelationshipEntryTargetEntityFieldDescriptor(), condition.getRelatedEntityId());
                if (!relatedRelationshipEntries.isEmpty()){
                    if (relatedRelationshipEntries.size() == 1){
                        return new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, GDSPersistableObjectAdapter.getFieldForGDS(FieldType.KEY, relatedRelationshipEntries.get(0).getSourceEntityId()));
                    }else {
                        ArrayList<Key> sourceEntityIds = new ArrayList<Key>(relatedRelationshipEntries.size());
                        for (BasePersistableRelationshipEntry currRelatedEntityEntry : relatedRelationshipEntries){
                            sourceEntityIds.add((Key) GDSPersistableObjectAdapter.getFieldForGDS(FieldType.KEY, currRelatedEntityEntry.getSourceEntityId()));
                        }

                        return new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, sourceEntityIds);
                    }
                }
                break;
            default:
                //todo:do
                break;
        }
        return null;
    }

}
