package com.shpandrak.web.rest.filter;

import com.shpandrak.datamodel.BasePersistableShpandrakObject;
import com.shpandrak.datamodel.BaseShpandrakObject;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FilterConditionOperatorType;
import com.shpandrak.persistence.query.filter.QueryFilter;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/26/13
 * Time: 11:32
 */
public class PersistableObjectURIQueryFilterParser {
    public static final String queryParamName = "q";
    public static <T extends BasePersistableShpandrakObject> QueryFilter parse(ShpandrakObjectDescriptor objectDescriptor, UriInfo uriInfo) throws URIQueryParsingException {
        List<String> queryParams = uriInfo.getQueryParameters().get(queryParamName);
        if (queryParams == null || queryParams.isEmpty()){
            return null;
        }

        String queryString = queryParams.get(0);
        //todo:really parse nicely - currently only poc implementation...
        //supporting only one condition of type a=b...
        int i = queryString.indexOf("=");
        if (i == -1){
            throw new URIQueryParsingException("Invalid query condition format :" + queryString);
        }
        String fieldName = queryString.substring(0, i);
        String value = queryString.substring(i+1);

        // Validate field name
        FieldDescriptor fieldDescriptor = objectDescriptor.getFieldDescriptorsMap().get(fieldName);
        if (fieldDescriptor == null){
            throw  new URIQueryParsingException("Invalid field name \"" + fieldName + "\" used for entity " + objectDescriptor.getEntityName() + " query was: " + queryString);
        }

        return new QueryFilter(new BasicFieldFilterCondition(fieldDescriptor, FilterConditionOperatorType.EQUALS, fieldDescriptor.fromString(value)));

    }
}
