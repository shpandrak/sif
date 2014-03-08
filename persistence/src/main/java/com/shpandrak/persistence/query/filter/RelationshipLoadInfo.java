package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.datamodel.relationship.RelationshipLoadLevel;

/**
 * Copyright (c) 2013, Amit Lieberman
 * All rights reserved.
 *
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 3, 29 June 2007
 *
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License
 *
 * Created with love
 * User: shpandrak
 * Date: 8/17/13
 * Time: 07:49
 */
public class RelationshipLoadInfo {
    private final EntityRelationshipDefinition relationshipDefinition;
    private final RelationshipLoadLevel loadLevel;

    public RelationshipLoadInfo(EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel loadLevel) {
        this.relationshipDefinition = relationshipDefinition;
        this.loadLevel = loadLevel;
    }

    public EntityRelationshipDefinition getRelationshipDefinition() {
        return relationshipDefinition;
    }

    public RelationshipLoadLevel getLoadLevel() {
        return loadLevel;
    }
}
