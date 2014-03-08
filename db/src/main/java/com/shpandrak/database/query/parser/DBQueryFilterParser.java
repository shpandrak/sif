package com.shpandrak.database.query.parser;

import com.shpandrak.database.managers.DBBaseReadOnlyManagerBean;
import com.shpandrak.database.managers.DBManagerFactory;
import com.shpandrak.database.query.DBQueryException;import com.shpandrak.database.table.DBEmbeddedRelationshipKeyField;
import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBRelationshipTable;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.OrderByClauseEntry;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.persistence.query.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:34
 */
public class DBQueryFilterParser {
    private static final Logger logger = LoggerFactory.getLogger(DBQueryFilterParser.class);
    private QueryFilter queryFilter;
    //todo: table shouldn't be here...
    private DBTable table;
    private String sql = null;
    private List<Object> params = null;
    private String tableAlias = null;

    public QueryFilter getQueryFilter() {
        return queryFilter;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }


    public DBQueryFilterParser(QueryFilter queryFilter, DBTable table) {
        this.queryFilter = queryFilter;
        //todo: not here..
        this.table = table;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void parse() throws DBQueryException {

        // No query - return all rows
        if (this.queryFilter == null){
            sql = table.getAllFieldsSQL();
            return;
        }

        // Parse the condition
        FieldFilterCondition condition = queryFilter.getCondition();
        QueryParsingContext context = new QueryParsingContext(table);

        if (condition != null){
            getQueryFilterParser(condition).parse(condition, context);
        }

        // Build SQL and parameters list
        params = new ArrayList<>();
        tableAlias = context.getTableAlias();
        StringBuilder sbWhereClause = new StringBuilder("where ");
        StringBuilder sb = new StringBuilder();
        sb.append(table.getAllFieldsSQL(context.getTableAlias()));
        for (JointTable currJointTable : context.getJointTables()){
            sb.append(", ").append(currJointTable.getTableName()).append(' ').append(currJointTable.getTableAlias());
            sbWhereClause.append(currJointTable.getCondition()).append(" and ");
            params.addAll(currJointTable.getParams());
        }

        if (tableAlias == null){
            for (SimpleQueryCondition currCondition : context.getWhereClauseConditions()){
                sbWhereClause.append(currCondition.getFieldName()).append(currCondition.getOperator()).append(currCondition.getValueString()).append(" and ");
                params.addAll(currCondition.getConditionParams());
            }
        }else {
            for (SimpleQueryCondition currCondition : context.getWhereClauseConditions()){
                sbWhereClause.append(tableAlias).append('.').append(currCondition.getFieldName()).append(currCondition.getOperator()).append(currCondition.getValueString()).append(" and ");
                params.addAll(currCondition.getConditionParams());
            }
        }

        // Chopping the last and operator...
        sbWhereClause.setLength(sbWhereClause.length() - 5);
        sb.append(' ').append(sbWhereClause);

        if (queryFilter.getOrderByClause() != null){
            boolean first = true;
            for (OrderByClauseEntry currOrderByEntry : queryFilter.getOrderByClause()){
                if (first){
                    first = false;
                    sb.append(" order by ");
                }else {
                    sb.append(", ");

                }
                sb.append(currOrderByEntry.getFieldName()).append(' ').append(currOrderByEntry.isAscending()? "ASC" :  "DESC");
            }
        }

        sql = sb.toString();
    }

    public void parseOld() throws DBQueryException {
        if (this.queryFilter == null){
            sql = table.getAllFieldsSQL();
            return;
        }
        params = new ArrayList<>();


        FieldFilterCondition condition = queryFilter.getCondition();

        if (condition instanceof BasicFieldFilterCondition) {
            sql = table.getAllFieldsSQL();
            BasicFieldFilterCondition basicFieldFilterCondition = (BasicFieldFilterCondition) condition;
            sql += " where ";
            appendBasicFilter(basicFieldFilterCondition);

        } else if (condition instanceof CompoundFieldFilterCondition){
            //todo: now supporting only simple conditions for demo...
            CompoundFieldFilterCondition compoundFieldFilterCondition = (CompoundFieldFilterCondition) condition;

            parseCompoundCondition(compoundFieldFilterCondition);

        } else if (condition instanceof RelationshipFilterCondition) {
            RelationshipFilterCondition relationshipFilterCondition = (RelationshipFilterCondition) condition;
            EntityRelationshipDefinition relationshipDefinition = relationshipFilterCondition.getRelationshipDefinition();
            switch (relationshipDefinition.getType()) {
                case ONE_TO_MANY:
                    DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = table.getEmbeddedRelationshipField(relationshipDefinition);
                    sql = table.getAllFieldsSQL();
                    sql += " where ";
                    sql += embeddedRelationshipUUIDField.getPersistentFieldName() + " = ?";
                    params.add(relationshipFilterCondition.getRelatedEntityId());
                    break;
                case MANY_TO_MANY:

                    DBBaseReadOnlyManagerBean relationshipEntryManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(((BasePersistentObjectDescriptor) relationshipDefinition.getRelationshipEntryDescriptor()).getEntityClass());
                    tableAlias = "t";
                    DBRelationshipTable relationshipTable = (DBRelationshipTable) relationshipEntryManager.getDbTable();

                    sql = table.getAllFieldsSQL(tableAlias) + ", " +
                    relationshipTable.getName() + " rel " +
                    "where " +
                            "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = " + tableAlias + ".id and " +
                            "rel." + relationshipTable.getTargetField().getPersistentFieldName() + " = ?";
                    params.add(relationshipFilterCondition.getRelatedEntityId());
                    break;
                default:
                    //todo:do
                    break;
            }

        } else
            throw new UnsupportedOperationException("Still not supporting compound conditions.. sorry...");
    }

    private void parseCompoundCondition(CompoundFieldFilterCondition compoundFieldFilterCondition) {
        switch (compoundFieldFilterCondition.getLogicalOperatorType()){
            case AND:
                break;
            case OR:
                break;
        }
    }

    private void appendBasicFilter(BasicFieldFilterCondition basicFieldFilterCondition) {
        DBField dbField = table.getFieldsMap().get(basicFieldFilterCondition.getField().getName());
        sql += basicFieldFilterCondition.getField().getName() + basicFieldFilterCondition.getOperatorType().getSqlRepresentation() + " ?";
        params.add(dbField.prepareForPersisting(basicFieldFilterCondition.getValue()));
    }

    public  static <T extends FieldFilterCondition> IFilterQueryParser<T> getQueryFilterParser(T condition){
        if (condition instanceof BasicFieldFilterCondition) {
            return (IFilterQueryParser<T>) new BasicFieldFilterConditionQueryParser();
        } else if (condition instanceof CompoundFieldFilterCondition){
            return (IFilterQueryParser<T>) new CompoundFieldFilterConditionQueryParser();
        } else if (condition instanceof RelationshipFilterCondition) {
            return (IFilterQueryParser<T>) new RelationshipFilterConditionQueryParser();
        } else{
            throw new UnsupportedOperationException(condition.getClass().getCanonicalName() + " Query condition is currently unsupported");
        }


    }


}
