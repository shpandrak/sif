package com.shpandrak.metadata.model.relation;

import com.shpandrak.metadata.model.field.AbstractFieldMetadata;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;

/**
 * Made with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 16:30
 */
public class OneToManyRelation extends AbstractRelation{
    private String fieldName;

    protected OneToManyRelation() {
    }

    public OneToManyRelation(String name, boolean mandatory, String relatedEntity, String fieldName) {
        super(name, mandatory, relatedEntity, Collections.<AbstractFieldMetadata>emptyList(), null);
        this.fieldName = fieldName;
    }

    @XmlElement(required = false)
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    @Override
    public RelationType getRelationType() {
        return RelationType.OneToMany;
    }
}
