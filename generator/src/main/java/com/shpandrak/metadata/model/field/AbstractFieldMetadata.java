package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;
import com.shpandrak.metadata.model.CustomProperty;
import com.shpandrak.metadata.model.DataEntityMetadata;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/7/12
 * Time: 17:35
 */
public abstract class AbstractFieldMetadata {
    private String name;
    private String persistenceFieldName = null;
    private boolean mandatory;
    private boolean key;
    private List<CustomProperty> customProperties;
    private Map<String, CustomProperty> customPropertiesMap;


    protected AbstractFieldMetadata() {
    }

    protected AbstractFieldMetadata(String name) {
        this.name = name;
    }

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="persistence-field-name", required = false)
    public String getPersistenceFieldName() {
        return persistenceFieldName;
    }

    public void setPersistenceFieldName(String persistenceFieldName) {
        this.persistenceFieldName = persistenceFieldName;
    }

    @XmlElement
    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @XmlElement(defaultValue = "false", required = false)
    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }


    @XmlElement(name = "custom-property")
    @XmlElementWrapper(name = "field-custom-properties")
    public List<CustomProperty> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(List<CustomProperty> customProperties) {
        this.customProperties = customProperties;
    }

    public abstract FieldType getType();

    /**
     * This method gets automatically called by the JAXB infrastructure
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent){
        if (customProperties == null || customProperties.isEmpty()){
            this.customPropertiesMap = Collections.emptyMap();
        }else {
            this.customPropertiesMap = new HashMap<>(customProperties.size());
            for (CustomProperty currProp : this.customProperties){
                customPropertiesMap.put(currProp.getName(), currProp);
            }
        }
    }

    public CustomProperty getCustomProperty(String name){
        return customPropertiesMap.get(name);
    }

}
