package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/26/12
 * Time: 13:23
 */
public class EnumFieldDef extends AbstractFieldMetadata {
    private String enumName;

    protected EnumFieldDef() {
    }

    @Override
    public FieldType getType() {
        return FieldType.ENUM;
    }

    @XmlElement(name = "enum-name")
    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }
}
