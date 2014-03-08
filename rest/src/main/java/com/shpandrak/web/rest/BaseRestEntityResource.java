package com.shpandrak.web.rest;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.EntityDescriptorFactory;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.datamodel.relationship.IRelationshipEntry;
import com.shpandrak.datamodel.relationship.RelationshipLoadLevel;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.web.rest.filter.PersistableObjectURIQueryFilterParser;
import com.shpandrak.web.rest.filter.URIQueryParsingException;
import com.shpandrak.xml.RelationshipEntryXMLConverter;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/16/12
 * Time: 08:45
 */
public abstract class BaseRestEntityResource<T extends BaseEntity> extends BaseRestPersistableObjectResource<T> {

    protected abstract IEntityManager<T> getManager();

    public BaseEntityDescriptor<T> getEntityDescriptor(){
        return (BaseEntityDescriptor<T>) getDescriptor();
    }

    @Override
    protected List<T> listFromDB(UriInfo uriInfo) throws PersistenceException, URIQueryParsingException {
        QueryFilter queryFilter = PersistableObjectURIQueryFilterParser.parse(getDescriptor(), uriInfo);
        return getManager().list(queryFilter, RelationshipLoadLevel.FULL);
    }

    protected String doListRelationship(String relationshipName, String stringId){

        Key id = getEntityDescriptor().getKeyFieldDescriptor().fromString(stringId);
        EntityRelationshipDefinition entityRelationshipDefinition = getEntityDescriptor().getRelationshipDefinitionMap().get(relationshipName);
        if (entityRelationshipDefinition == null){
            throw new IllegalArgumentException("Invalid relationship name " + relationshipName + " for entity " + getEntityDescriptor());
        }
        try{
            List<IRelationshipEntry> list = getManager().listRelationshipEntries(entityRelationshipDefinition, id);
            RelationshipEntryXMLConverter relationshipEntryXMLConverter = new RelationshipEntryXMLConverter(entityRelationshipDefinition.getRelationshipEntryDescriptor(), EntityDescriptorFactory.get(entityRelationshipDefinition.getTargetClassType()));
            return relationshipEntryXMLConverter.toXML(list);

        } catch (PersistenceException e) {
            //todo:do
            throw new RuntimeException(e);
        }

    }

}
