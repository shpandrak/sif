package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.Key;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/21/12
 * Time: 12:24
 */
public interface IPersistableShpandrakObject extends IShpandrakObject {

    Key getId();

    void setId(Key id);
}
