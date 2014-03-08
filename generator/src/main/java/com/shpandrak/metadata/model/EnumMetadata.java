package com.shpandrak.metadata.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/26/12
 * Time: 13:11
 */
public class EnumMetadata {
    private String name;
    private List<EnumEntryMetadata> entries;

    protected EnumMetadata() {
    }

    public EnumMetadata(String name, List<EnumEntryMetadata> entries) {
        this.name = name;
        this.entries = entries;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "enumentry")
    public List<EnumEntryMetadata> getEntries() {
        return entries;
    }

    public void setEntries(List<EnumEntryMetadata> entries) {
        this.entries = entries;
    }
}
