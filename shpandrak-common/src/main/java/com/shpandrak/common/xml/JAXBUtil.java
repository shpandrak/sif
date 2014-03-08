package com.shpandrak.common.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

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
 * Date: 10/13/12
 * Time: 11:02
 */
public abstract class JAXBUtil {
     public  static <T> T readObject(Class<T> clazz, File file) throws JAXBException {
         JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         return  (T) unmarshaller.unmarshal(file);
     }
     public  static <T> T readObject(Class<T> clazz, InputStream is) throws JAXBException {
         JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         return  (T) unmarshaller.unmarshal(is);
     }
}
