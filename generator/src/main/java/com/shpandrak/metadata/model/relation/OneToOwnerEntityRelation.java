package com.shpandrak.metadata.model.relation;

/**
 * Made with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 16:30
 */
public class OneToOwnerEntityRelation extends OneToManyRelation{

    public OneToOwnerEntityRelation(String ownerEntity) {
        super(ownerEntity, true, ownerEntity, null);
    }

    protected OneToOwnerEntityRelation() {
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.OneToOwner;
    }
}
