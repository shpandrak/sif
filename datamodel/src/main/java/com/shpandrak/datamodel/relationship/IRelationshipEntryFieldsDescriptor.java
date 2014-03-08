package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;

import java.util.UUID;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/31/12
 * Time: 16:29
 */
public interface IRelationshipEntryFieldsDescriptor {

    KeyFieldDescriptor getSourceEntityFieldDescriptor();

    KeyFieldDescriptor getTargetEntityFieldDescriptor();
}
