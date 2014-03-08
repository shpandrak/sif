package com.shpandrak.metadata.generator.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/13/12
 * Time: 10:52
 */
@XmlRootElement(name = "generatorConfiguration")
public class GeneratorConfig {
    private String name;
    private String parentConfiguration;
    private String storeFile;
    private List<GenerationDef> generators;

    public GeneratorConfig() {
    }

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(required = false)
    public String getParentConfiguration() {
        return parentConfiguration;
    }

    public void setParentConfiguration(String parentConfiguration) {
        this.parentConfiguration = parentConfiguration;
    }

    @XmlElementWrapper(name = "generators")
    @XmlElement(name = "generator")
    public List<GenerationDef> getGenerators() {
        return generators;
    }

    public void setGenerators(List<GenerationDef> generators) {
        this.generators = generators;
    }

    @XmlElement(required = false)
    public String getStoreFile() {
        return storeFile;
    }

    public void setStoreFile(String storeFile) {
        this.storeFile = storeFile;
    }
}
