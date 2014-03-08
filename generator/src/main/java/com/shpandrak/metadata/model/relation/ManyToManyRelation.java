package com.shpandrak.metadata.model.relation;

import com.shpandrak.metadata.model.SortClauseEntryDef;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/17/12
 * Time: 08:27
 */
public class ManyToManyRelation extends AbstractRelation {
    private String relatedEntityOneToManyRelationshipName;

    protected ManyToManyRelation() {
        super();
    }

    public ManyToManyRelation(String name, boolean mandatory, String relatedEntity, List<AbstractFieldMetadata> fields, String relatedEntityOneToManyRelationshipName, List<SortClauseEntryDef> relationshipSort) {
        super(name, mandatory, relatedEntity, fields, relationshipSort);
        this.relatedEntityOneToManyRelationshipName = relatedEntityOneToManyRelationshipName;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.ManyToMany;
    }

    @XmlElement(name = "related-entity-one-to-many-relationship-name", required = true, nillable = false)
    public String getRelatedEntityOneToManyRelationshipName() {
        return relatedEntityOneToManyRelationshipName;
    }

    public void setRelatedEntityOneToManyRelationshipName(String relatedEntityOneToManyRelationshipName) {
        this.relatedEntityOneToManyRelationshipName = relatedEntityOneToManyRelationshipName;
    }
}
