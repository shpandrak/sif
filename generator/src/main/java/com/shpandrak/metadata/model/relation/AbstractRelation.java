package com.shpandrak.metadata.model.relation;

import com.shpandrak.metadata.model.SortClauseEntryDef;
import com.shpandrak.metadata.model.field.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.List;

/**
 * Made with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 16:30
 */
public abstract class AbstractRelation {
    private String name;
    private boolean mandatory;
    private String relatedEntity;
    private List<AbstractFieldMetadata> fields;
    private List<SortClauseEntryDef> relationshipSort;

    public abstract RelationType getRelationType();

    protected AbstractRelation() {
        this.fields = new ArrayList<AbstractFieldMetadata>();
    }

    protected AbstractRelation(String name, boolean mandatory, String relatedEntity, List<AbstractFieldMetadata> fields, List<SortClauseEntryDef> relationshipSort) {
        this.name = name;
        this.mandatory = mandatory;
        this.relatedEntity = relatedEntity;
        this.fields = fields;
        this.relationshipSort = relationshipSort;
    }

    @XmlElement(required = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(defaultValue = "true")
    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @XmlElement(name = "related-entity", required = true)
    public String getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getRelationshipName(){
        if (name != null){
            return name;
        }else{
            return relatedEntity;
        }
    }

    @XmlElementWrapper(name = "fields", required = false)
    @XmlElements({
            @XmlElement(name = "string-field", type=StringFieldDef.class),
            @XmlElement(name = "date-field", type=DateFieldDef.class),
            @XmlElement(name = "enum-field", type=EnumFieldDef.class),
            @XmlElement(name = "boolean-field", type=BooleanFieldDef.class),
            @XmlElement(name = "integer-field", type=IntegerFieldDef.class),
            @XmlElement(name = "long-field", type=LongFieldDef.class),
            @XmlElement(name = "double-field", type=DoubleFieldDef.class)

    })
    public List<AbstractFieldMetadata> getFields() {
        return fields;
    }

    public void setFields(List<AbstractFieldMetadata> fields) {
        this.fields = fields;
    }

    @XmlElementWrapper(name = "relationship-sort", required = false)
    @XmlElement(name = "sort-entry")
    public List<SortClauseEntryDef> getRelationshipSort() {
        return relationshipSort;
    }

    public void setRelationshipSort(List<SortClauseEntryDef> relationshipSort) {
        this.relationshipSort = relationshipSort;
    }
}
