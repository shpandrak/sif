package com.shpandrak.metadata.generator.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/13/12
 * Time: 10:57
 */
public class GenerationDef {
    private String className;
    private List<PropertyDef> properties;

    public GenerationDef() {
    }

    @XmlElement(name="class")
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<PropertyDef> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDef> properties) {
        this.properties = properties;
    }
}
