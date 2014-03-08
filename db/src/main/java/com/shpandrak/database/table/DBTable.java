package com.shpandrak.database.table;

import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:14
 */
public class DBTable {
    private String name;
    private Map<String, DBField> fieldsMap;
    private Map<EntityRelationshipDefinition, DBEmbeddedRelationshipKeyField> embeddedRelationshipFields;
    private List<DBField> fields;
    private final String ALL_FIELDS_STRING;
    private final String SQL_INSERT_STATEMENT;
    private final String SQL_UPDATE_STATEMENT;
    private final String SQL_DELETE_STATEMENT;
    private final String SQL_SELECT_STATEMENT;

    public DBTable(String tableName, Collection<DBField> fields) {
        this.name = tableName;
        this.fields = new ArrayList<>(fields);
        this.fieldsMap = new HashMap<>(fields.size());
        this.embeddedRelationshipFields = new HashMap<>(fields.size() - 1);
        for (DBField currField : fields){
            fieldsMap.put(currField.getFieldDescriptor().getName(), currField);
            currField.setTable(this);
            if (currField instanceof DBEmbeddedRelationshipKeyField){
                DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = (DBEmbeddedRelationshipKeyField) currField;
                embeddedRelationshipFields.put(embeddedRelationshipUUIDField.getRelationshipDefinition(), embeddedRelationshipUUIDField);
            }
        }

        String allFieldsList = "";
        String insertFieldsString = "";
        String updateFieldsString = "";
        int idx = 0;
        for (DBField currField : fields){
            if (idx > 0){
                allFieldsList += ", ";
                insertFieldsString += ", ";
                if (idx > 1){
                    updateFieldsString += ", " + currField.getPersistentFieldName() + "=?";
                }else {
                    updateFieldsString += currField.getPersistentFieldName() + "=?";
                }
            }

            allFieldsList += currField.getPersistentFieldName();
            insertFieldsString += "?";
            ++idx;
        }
        ALL_FIELDS_STRING = allFieldsList;
        SQL_INSERT_STATEMENT = "insert into " + name + " values (" + insertFieldsString + ")";
        SQL_UPDATE_STATEMENT = "update " + name + " set " + updateFieldsString + " where id = ?";
        SQL_DELETE_STATEMENT = "delete from " + name + " where id = ?";
        SQL_SELECT_STATEMENT = "select " + ALL_FIELDS_STRING + " from " + name;


    }

    public String getName() {
        return name;
    }

    public Map<String, DBField> getFieldsMap() {
        return fieldsMap;
    }

    public List<DBField> getFields() {
        return fields;
    }

    public DBEmbeddedRelationshipKeyField getEmbeddedRelationshipField(EntityRelationshipDefinition relationshipDefinition) {
        return embeddedRelationshipFields.get(relationshipDefinition);
    }

    public String getAllFieldsSQL(){
        return SQL_SELECT_STATEMENT;
    }

    public String getInsertSQL() {
        return SQL_INSERT_STATEMENT;
    }

    public String getUpdateSQL(){
        return SQL_UPDATE_STATEMENT;
    }

    public String getDeleteByIdSQL() {
        return SQL_DELETE_STATEMENT;
    }

    public String getAllFieldsSQL(String tableAlias){
        if (tableAlias == null){
            return SQL_SELECT_STATEMENT;
        }else {
            return "select " + getAllFieldsAsString(tableAlias) + " from " + name + " " + tableAlias;
        }
    }


    public String getAllFieldsAsString() {
        return getAllFieldsAsString(null);

    }
    public String getAllFieldsAsString(String alias) {
        if (alias == null){
            return ALL_FIELDS_STRING;
        }else {
            // If we have a table alias building the fields string from scratch
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (DBField currField : fields){
                if (first){
                    first = false;
                }else {
                    sb.append(", ");
                }

                sb.append(alias).append('.').append(currField.getPersistentFieldName()).append(
                // Adding as [alias]_name because in some implementations getting field names with "." (e.g. t.id) does not work - using t_id
                " as ").append(alias).append('_').append(currField.getPersistentFieldName());
            }

            return sb.toString();
        }
    }
}
