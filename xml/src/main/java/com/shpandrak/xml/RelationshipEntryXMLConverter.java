package com.shpandrak.xml;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.relationship.IRelationshipEntry;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/29/12
 * Time: 12:01
 */
public class RelationshipEntryXMLConverter<TARGET_CLASS extends BaseEntity, T extends IRelationshipEntry<TARGET_CLASS>> extends ShpandrakObjectXMLConverter<T> {

    EntityXMLConverter<TARGET_CLASS> targetEntityXMLConverter;
    TARGET_CLASS loadedTargetClass;

    @Override
    protected void beforeReadXML() {
        super.beforeReadXML();
        loadedTargetClass = null;
    }

    public RelationshipEntryXMLConverter(ShpandrakObjectDescriptor objectDescriptor, BaseEntityDescriptor<TARGET_CLASS> targetEntityDescriptor) {
        super(objectDescriptor);
        this.targetEntityXMLConverter = new EntityXMLConverter<TARGET_CLASS>(targetEntityDescriptor);
    }

    @Override
    protected void appendAdditionalFields(StringBuilder sb, T relationshipEntry) {
        if (relationshipEntry.getTargetEntity() != null){
            targetEntityXMLConverter.append(sb, relationshipEntry.getTargetEntity());
        }else if (relationshipEntry.getTargetEntityId() != null) {
            targetEntityXMLConverter.appendIdOnlyEntity(sb, relationshipEntry.getTargetEntityId());
        }
    }

    @Override
    protected void readElement(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException, XMLParsingException {
        if (targetEntityXMLConverter.getElementName().equals(startElement.getName().getLocalPart())){
            loadedTargetClass = targetEntityXMLConverter.fromEventReader(eventReader, startElement);
        }else {
            super.readElement(startElement, eventReader);
        }
    }

    @Override
    protected T convertToEntity() {
        T entity = super.convertToEntity();

        if (loadedTargetClass != null){
            entity.setTargetEntity(loadedTargetClass);
        }

        return entity;
    }
}
