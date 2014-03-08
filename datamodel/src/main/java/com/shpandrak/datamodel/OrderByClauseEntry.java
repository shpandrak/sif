package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 5/14/13
 * Time: 07:49
 */
public class OrderByClauseEntry {
    private final String fieldName;
    private final boolean ascending;

    public OrderByClauseEntry(FieldDescriptor fieldDescriptor, boolean ascending) {
        this.fieldName = fieldDescriptor.getName();
        this.ascending = ascending;
    }

    public OrderByClauseEntry(String fieldName, boolean ascending) {
        this.fieldName = fieldName;
        this.ascending = ascending;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isAscending() {
        return ascending;
    }
}
