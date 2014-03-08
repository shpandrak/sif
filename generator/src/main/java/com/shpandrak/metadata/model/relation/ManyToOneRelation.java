package com.shpandrak.metadata.model.relation;

import com.shpandrak.metadata.model.SortClauseEntryDef;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/17/12
 * Time: 08:27
 */
public class ManyToOneRelation extends AbstractRelation {
    private OneToManyRelation oneToManyRelation;

    protected ManyToOneRelation() {
        super();
    }

    public ManyToOneRelation(String name, boolean mandatory, String relatedEntity, List<AbstractFieldMetadata> fields, OneToManyRelation oneToManyRelation, List<SortClauseEntryDef> relationshipSort) {
        super(name, mandatory, relatedEntity, fields, relationshipSort);
        this.oneToManyRelation = oneToManyRelation;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.ManyToOne;
    }

    public OneToManyRelation getOneToManyRelation() {
        return oneToManyRelation;
    }

    public void setOneToManyRelation(OneToManyRelation oneToManyRelation) {
        this.oneToManyRelation = oneToManyRelation;
    }
}
