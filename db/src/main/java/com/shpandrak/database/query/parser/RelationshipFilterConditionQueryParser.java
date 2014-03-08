package com.shpandrak.database.query.parser;

import com.shpandrak.database.managers.DBBaseReadOnlyManagerBean;
import com.shpandrak.database.managers.DBManagerFactory;
import com.shpandrak.database.table.DBEmbeddedRelationshipKeyField;
import com.shpandrak.database.table.DBRelationshipTable;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.persistence.query.filter.RelationshipFilterCondition;

import java.util.Arrays;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:25
 */
class RelationshipFilterConditionQueryParser implements IFilterQueryParser<RelationshipFilterCondition> {
    @Override
    public void parse(RelationshipFilterCondition condition, QueryParsingContext context) {
        DBTable table = context.getTable();
        EntityRelationshipDefinition relationshipDefinition = condition.getRelationshipDefinition();
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = table.getEmbeddedRelationshipField(relationshipDefinition);
                context.addSimpleQueryCondition(new SimpleQueryCondition(embeddedRelationshipUUIDField.getPersistentFieldName(), "=", "?",  embeddedRelationshipUUIDField.prepareForPersisting(condition.getRelatedEntityId())));
                break;
            case MANY_TO_MANY:
                DBBaseReadOnlyManagerBean relationshipEntryManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(((BasePersistentObjectDescriptor) relationshipDefinition.getRelationshipEntryDescriptor()).getEntityClass());
                if (context.getTableAlias() == null){
                    context.setTableAlias("t");
                }
                DBRelationshipTable relationshipTable = (DBRelationshipTable) relationshipEntryManager.getDbTable();
                String relTableAlias = relationshipDefinition.getName();
                context.addJointTable(new JointTable(relationshipTable.getName(), relTableAlias,
                                relTableAlias + "." + relationshipTable.getSourceField().getPersistentFieldName() + " = " + context.getTableAlias() + ".id and " +
                                relTableAlias + "." + relationshipTable.getTargetField().getPersistentFieldName() + " = ?",
                        Arrays.<Object>asList(relationshipTable.getTargetField().prepareForPersisting(condition.getRelatedEntityId()))));
                break;
            default:
                //todo:do
                break;
        }
    }
}
