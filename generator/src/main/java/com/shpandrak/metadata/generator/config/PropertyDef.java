package com.shpandrak.metadata.generator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * Made with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 14:13
 */

public class PropertyDef {
    private String name;
    private String value;

    protected PropertyDef() {
    }

    public PropertyDef(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
