package com.shpandrak.web.rest;

import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.IReadOnlyManager;
import com.shpandrak.xml.XMLConverter;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love.
 * User: shpandrak
 * Date: 10/22/12
 * Time: 10:20
 */
public abstract class BaseRestResource<T> {

    protected abstract IReadOnlyManager<T> getReadOnlyManager();

    protected abstract XMLConverter<T> getXMLConverter();

    protected String doList(UriInfo uriInfo) {
        //todo:return response...
        List<T> all;
        try {
            all = getReadOnlyManager().list();
        } catch (PersistenceException e) {
            //todo:do
            throw new RuntimeException(e);
        }
        XMLConverter<T> xmlConverter = getXMLConverter();
        return  xmlConverter.toXML(all);
    }

}
