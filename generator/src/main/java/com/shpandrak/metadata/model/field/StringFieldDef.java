package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/10/12
 * Time: 20:33
 */
public class StringFieldDef extends AbstractFieldMetadata{
    private int length;

    protected StringFieldDef() {
    }

    public StringFieldDef(String name, int length) {
        super(name);
        this.length = length;
    }

    @XmlElement(name = "max-length")
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public FieldType getType() {
        return FieldType.STRING;
    }
}
