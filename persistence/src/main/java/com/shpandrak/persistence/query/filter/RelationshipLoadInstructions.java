package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2013, Amit Lieberman
 * All rights reserved.
 * <p/>
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 3, 29 June 2007
 * <p/>
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p/>
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License
 * <p/>
 * Created with love
 * User: shpandrak
 * Date: 8/17/13
 * Time: 08:00
 */
public class RelationshipLoadInstructions {
    private final Map<EntityRelationshipDefinition, RelationshipLoadInfo> relationshipLoadInfoMap;

    public RelationshipLoadInstructions() {
        this.relationshipLoadInfoMap = new HashMap<EntityRelationshipDefinition, RelationshipLoadInfo>();
    }

    public RelationshipLoadInstructions withRelationship(RelationshipLoadInfo relationshipLoadInfo){
        addRelationshipLoadInfo(relationshipLoadInfo);
        return this;
    }

    public RelationshipLoadInstructions(Collection<RelationshipLoadInfo> loadInfo) {
        this.relationshipLoadInfoMap = new HashMap<EntityRelationshipDefinition, RelationshipLoadInfo>(loadInfo.size());
        for (RelationshipLoadInfo currLoadInfo : loadInfo){
            addRelationshipLoadInfo(currLoadInfo);
        }
    }

    private void addRelationshipLoadInfo(RelationshipLoadInfo currLoadInfo) {
        this.relationshipLoadInfoMap.put(currLoadInfo.getRelationshipDefinition(), currLoadInfo);
    }

    public Map<EntityRelationshipDefinition, RelationshipLoadInfo> getRelationshipLoadInfoMap() {
        return relationshipLoadInfoMap;
    }

    public Collection<RelationshipLoadInfo> getLoadInfo() {
        return relationshipLoadInfoMap.values();
    }
}
