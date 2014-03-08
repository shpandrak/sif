package com.shpandrak.metadata.model;

import javax.xml.bind.annotation.XmlValue;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/26/12
 * Time: 13:12
 */
public class EnumEntryMetadata {
    private String name;

    protected EnumEntryMetadata() {
    }

    public EnumEntryMetadata(String name) {
        this.name = name;
    }

    @XmlValue
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
