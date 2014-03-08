package com.shpandrak.xml;

import com.shpandrak.common.xml.XMLUtil;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/21/12
 * Time: 22:37
 */
public abstract class XMLConverter<T> {

    /**
     * Returns the supported Class type
     */
    public abstract Class<T> getClassType();

    /**
     * Append the entity fields xml to stringBuilder
     * @param sb StringBuilder to use
     * @param entity the entity
     */
    protected abstract void appendEntity(StringBuilder sb, T entity);

    /**
     * create an instance according to field values map supplied by parser
     * @return new instance of the entity populated with the fields supplied
     */
    protected abstract T convertToEntity();

    /**
     * Returns the Entity tagName
     */
    public abstract String getElementName();

    /**
     * Returns the Entity Wrapper tagName (Wrapper can contain multiple entity elements as child elements
     */
    public abstract String getWrapperElementName();


    public String toXML(Collection<T> entities){
        if (entities.isEmpty()){
            return "<" + getWrapperElementName() + " />";
        }else {
            StringBuilder sb = new StringBuilder();
            sb.append('<').append(getWrapperElementName()).append('>');
            for (T currEntity : entities){
                append(sb, currEntity);
            }
            sb.append("</").append(getWrapperElementName()).append('>');
            return sb.toString();
        }

    }

    public final String toXML(T entity){
        StringBuilder sb = new StringBuilder();
        append(sb, entity);
        return sb.toString();
    }

    public String toPrettyXML(T entity){
        return XMLUtil.formatXml(toXML(entity));
    }

    public void append(StringBuilder sb, T entity){
        sb.append('<').append(getElementName());
        appendAttributes(sb, entity);

        sb.append('>');
        appendEntity(sb, entity);
        sb.append("</").append(getElementName()).append('>');
    }

    protected void appendAttributes(StringBuilder sb, T entity){
    }

    protected abstract void beforeReadXML();

    public T fromXML(String xml) throws XMLParsingException {
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(xml));

            return fromEventReader(eventReader, eventReader.nextTag().asStartElement());
        }catch (XMLStreamException e){
            throw new XMLParsingException("Failed parsing xml", e);
        }
    }

    public T fromEventReader(XMLEventReader eventReader, StartElement entityStartElement) throws XMLStreamException, XMLParsingException {
        beforeReadXML();
        readAttributes(entityStartElement);
        try{
            String entityElementName = entityStartElement.getName().getLocalPart();
            if (!entityElementName.equals(getElementName())){
                throw new XMLParsingException("entity must start with " + getElementName() + " element. found: " + entityElementName);
            }

            XMLEvent xmlEvent = eventReader.nextEvent();
            while (eventReader.hasNext() && !(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(entityElementName))){

                if (xmlEvent.isStartElement()){
                    StartElement startElement = xmlEvent.asStartElement();
                    readElement(startElement, eventReader);
                }
                xmlEvent = eventReader.nextEvent();
            }

            return convertToEntity();
        }finally {
            eventReader.close();
        }
    }

    protected abstract void readAttributes(StartElement entityStartElement);

    protected abstract void readElement(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException, XMLParsingException;

    public String emptyElement() {
        return "<" + getElementName() + "/>";
    }

    public void emptyElement(StringBuilder sb) {
        sb.append('<').append(getElementName()).append("/>");
    }
}
