package com.shpandrak.database.table;

import java.util.Collection;

/**
 * Created with love
 * User: shpandrak
 * Date: 2/17/13
 * Time: 11:03
 */
public abstract class DBRelationshipTable extends DBTable{

    public DBRelationshipTable(String tableName, Collection<DBField> fields) {
        super(tableName, fields);
    }

    public abstract DBRelationshipKeyField getSourceField();

    public abstract DBRelationshipKeyField getTargetField();

}
