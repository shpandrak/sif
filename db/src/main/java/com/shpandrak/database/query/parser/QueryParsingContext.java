package com.shpandrak.database.query.parser;

import com.shpandrak.database.table.DBTable;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:08
 */
class QueryParsingContext {
    private final DBTable table;
    private String tableAlias = null;
    private Map<String, JointTable> jointTablesByAlias = new HashMap<String, JointTable>();
    private List<SimpleQueryCondition> whereClauseConditions = new ArrayList<SimpleQueryCondition>();

    QueryParsingContext(DBTable table) {
        this.table = table;
    }

    public DBTable getTable() {
        return table;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public Collection<JointTable> getJointTables() {
        return jointTablesByAlias.values();
    }

    public List<SimpleQueryCondition> getWhereClauseConditions() {
        return whereClauseConditions;
    }

    public void addSimpleQueryCondition(SimpleQueryCondition simpleQueryCondition){
        whereClauseConditions.add(simpleQueryCondition);
    }

    public void addJointTable(JointTable jointTable){
        JointTable oldValue = jointTablesByAlias.put(jointTable.getTableAlias(), jointTable);
        if (oldValue != null){
            throw new IllegalStateException("Created more than one joint table with alias + " + jointTable.getTableAlias() + " " + jointTable + " and " + oldValue);
        }
    }
}
