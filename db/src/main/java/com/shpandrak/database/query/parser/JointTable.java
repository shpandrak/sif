package com.shpandrak.database.query.parser;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:09
 */
class JointTable {
    private String tableName;
    private String tableAlias;
    private String condition;
    private List<Object> params;

    JointTable(String tableName, String tableAlias, String condition, List<Object> params) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.condition = condition;
        this.params = params;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getCondition() {
        return condition;
    }

    public List<Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "JointTable{" +
                "tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", condition='" + condition + '\'' +
                ", params=" + params +
                '}';
    }
}
