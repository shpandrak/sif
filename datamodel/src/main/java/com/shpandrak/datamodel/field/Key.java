package com.shpandrak.datamodel.field;

import java.io.Serializable;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:29
 */
public interface Key extends Serializable {
    void fromString(String value);
    String toString();
    boolean equals(Object o);
    public int hashCode();
    Object toPersistentFormat();
}
