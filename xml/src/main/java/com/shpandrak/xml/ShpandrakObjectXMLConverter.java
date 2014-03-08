package com.shpandrak.xml;

import com.shpandrak.datamodel.IShpandrakObject;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.ShpandrakObjectRawData;
import com.shpandrak.datamodel.field.FieldInstance;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/20/12
 * Time: 16:29
 */
public class ShpandrakObjectXMLConverter<T extends IShpandrakObject> extends XMLConverter<T> {
    protected final ShpandrakObjectDescriptor objectDescriptor;
    private Map<String, String> fieldValues;

    public ShpandrakObjectXMLConverter(ShpandrakObjectDescriptor objectDescriptor) {
        this.objectDescriptor = objectDescriptor;
    }

    @Override
    public Class<T> getClassType() {
        return (Class<T>) objectDescriptor.getEntityClass();
    }

    @Override
    protected void appendEntity(StringBuilder sb, T entity) {
        for (FieldInstance currField : entity.getFields()){
            appendField(sb, currField);
        }
        appendAdditionalFields(sb, entity);
    }

    protected void appendField(StringBuilder sb, FieldInstance currField) {
        Object value = currField.getValue();
        if (value != null){
            sb
            .append("<").append(currField.getName()).append(">")
                .append(currField.stringValue())
            .append("</").append(currField.getName()).append(">");
        }
    }

    protected void appendAdditionalFields(StringBuilder sb, T entity){
    }

    @Override
    protected T convertToEntity() {
        return (T) objectDescriptor.instance(new ShpandrakObjectRawData(new HashMap<String, Object>(fieldValues)));
    }

    @Override
    public String getElementName() {
        return objectDescriptor.getEntityName();
    }

    @Override
    public String getWrapperElementName() {
        return objectDescriptor.getEntityPluralName();
    }

    @Override
    protected void beforeReadXML() {
        this.fieldValues = new HashMap<String, String>(objectDescriptor.getFieldDescriptorsMap().size());
    }

    @Override
    protected void readAttributes(StartElement entityStartElement) {
    }

    @Override
    protected void readElement(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException, XMLParsingException {
        String startElementName = startElement.getName().getLocalPart();
        XMLEvent textEvent = eventReader.nextEvent();
        if (!textEvent.isCharacters()){
            //throw new XMLParsingException("expected characters element under " + startElementName + " but found " + textEvent.toString());
            //skipping
            XMLEvent xmlEvent = eventReader.nextEvent();
            while (!xmlEvent.isEndElement() || !(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(startElementName))){
                xmlEvent = eventReader.nextEvent();
            }
        }else {
            fieldValues.put(startElementName, textEvent.asCharacters().getData());
        }
    }
}
