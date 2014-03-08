package com.shpandrak.web.rest;

import com.shpandrak.datamodel.BasePersistableShpandrakObject;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.managers.IReadOnlyManager;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.web.rest.filter.PersistableObjectURIQueryFilterParser;
import com.shpandrak.web.rest.filter.URIQueryParsingException;
import com.shpandrak.xml.XMLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/16/12
 * Time: 08:45
 */
public abstract class BaseRestPersistableObjectResource<T extends BasePersistableShpandrakObject> extends BaseRestShpandrakObjectResource<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseRestPersistableObjectResource.class);

    protected abstract IPersistableObjectManager<T> getManager();

    @Override
    protected IReadOnlyManager<T> getReadOnlyManager() {
        return getManager();
    }

    @Override
    protected String doList(UriInfo uriInfo){
        //todo:return response...
        List<T> all;
        try {
            all = listFromDB(uriInfo);
        } catch (Exception e) {
            //todo:do
            logger.error("Error listing entities", e);
            throw new RuntimeException(e);
        }

        XMLConverter<T> xmlConverter = getXMLConverter();
        return xmlConverter.toXML(all);


    }

    protected List<T> listFromDB(UriInfo uriInfo) throws PersistenceException, URIQueryParsingException {
        // Parsing query Filter

        QueryFilter queryFilter = PersistableObjectURIQueryFilterParser.parse(getDescriptor(), uriInfo);

        return getReadOnlyManager().list(queryFilter);
    }

    protected String doGetById(String id) {
        //todo:return response...
        T entity;
        try {
            entity = getReadOnlyManager().getById(((KeyFieldDescriptor<Key>)getDescriptor().getOrderedFieldDescriptors().get(0)).fromString(id));
        } catch (PersistenceException e) {
            //todo:do
            logger.error("Error getting an entity by Id", e);
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();
        if (entity == null){
            getXMLConverter().emptyElement(sb);
        }else {
            getXMLConverter().append(sb, entity);
        }
        return sb.toString();
    }



    protected String doCreate(String xml) {
        //todo:return response...
        T entity;
        try {
            entity = getXMLConverter().fromXML(xml);
            getManager().create(entity);

        } catch (Exception e) {
            //todo:do
            logger.error("Error creating entity", e);
            throw new RuntimeException(e);
            //return "<error>" + e.getMessage() +"</error>";
        }

        return doGetById(entity.getId().toString());
    }

    protected boolean doDelete(String id) {
        //todo:return response...
        T entity;
        try {
            return getManager().delete(((FieldDescriptor<Key>) getDescriptor().getOrderedFieldDescriptors().get(0)).fromString(id));

        } catch (Exception e) {
            //todo:do
            logger.error("Error deleting entity", e);
            throw new RuntimeException(e);
            //return "<error>" + e.getMessage() +"</error>";
        }
    }



}
