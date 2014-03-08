package com.shpandrak.persistence;

import com.shpandrak.datamodel.relationship.EntityRelationship;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/24/12
 * Time: 11:00
 */
public class UnsatisfiedRelationshipException extends PersistenceException {
    public UnsatisfiedRelationshipException(EntityRelationship relationship) {
        super("Unsatisfied Relationship: " + relationship.toString());
    }
}
