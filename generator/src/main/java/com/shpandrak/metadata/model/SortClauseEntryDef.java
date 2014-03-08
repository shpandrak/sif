package com.shpandrak.metadata.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with love
 * User: shpandrak
 * Date: 5/14/13
 * Time: 08:38
 */
@XmlRootElement(name = "sort-entry")
public class SortClauseEntryDef {
    private String field;
    private boolean ascending;

    protected SortClauseEntryDef() {
    }

    public SortClauseEntryDef(String field, boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
