package com.shpandrak.xml;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.EntityDescriptorFactory;
import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/24/12
 * Time: 11:59
 */
public class EntityXMLConverter<T extends BaseEntity> extends ShpandrakObjectXMLConverter<T> {

    public static final String RELATIONSHIPS_ELEMENT_TAG_NAME = "entity-relationships";
    public static final String RELATIONSHIP_ELEMENT_TAG_NAME = "entity-relationship";
    private BaseEntityDescriptor<T> entityDescriptor;

    private Key entityId = null;
    Map<String, List<IRelationshipEntry>> relationships = null;

    public EntityXMLConverter(BaseEntityDescriptor<T> entityDescriptor) {
        super(entityDescriptor);
        this.entityDescriptor = entityDescriptor;
    }


    @Override
    protected void beforeReadXML() {
        super.beforeReadXML();
        entityId = null;
        relationships = null;
    }

    @Override
    protected void readElement(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException, XMLParsingException {
        if (RELATIONSHIPS_ELEMENT_TAG_NAME.equals(startElement.getName().getLocalPart())) {
            relationships = parseRelationships(eventReader);
        } else {
            super.readElement(startElement, eventReader);
        }
    }


    @Override
    protected void readAttributes(StartElement entityStartElement) {
        super.readAttributes(entityStartElement);
        Attribute idAttribute = entityStartElement.getAttributeByName(new QName("id"));
        if (idAttribute != null) {
            this.entityId = entityDescriptor.getKeyFieldDescriptor().fromString(idAttribute.getValue());
        }
    }

    private Map<String, List<IRelationshipEntry>> parseRelationships(XMLEventReader eventReader) throws XMLStreamException, XMLParsingException {
        Map<String, List<IRelationshipEntry>> entriesMapByRelationshipName = new HashMap<String, List<IRelationshipEntry>>();
        XMLEvent xmlEvent = eventReader.nextEvent();
        while (!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(RELATIONSHIPS_ELEMENT_TAG_NAME))) {
            if (xmlEvent.isStartElement()) {
                StartElement relationshipsStartElement = xmlEvent.asStartElement();
                if (!relationshipsStartElement.getName().getLocalPart().equals(RELATIONSHIP_ELEMENT_TAG_NAME)) {
                    throw new XMLParsingException("Invalid relationships xml format. expected: " + RELATIONSHIP_ELEMENT_TAG_NAME + " found: " + relationshipsStartElement.getName().getLocalPart());
                }else {
                    Attribute relationshipNameAttribute = relationshipsStartElement.getAttributeByName(new QName("name"));
                    if (relationshipNameAttribute == null){
                        throw new XMLParsingException(RELATIONSHIP_ELEMENT_TAG_NAME + " element must have a name attribute (stands for valid relationship name");
                    }
                    String relationshipName = relationshipNameAttribute.getValue();

                    // Verifying relationship name exist in class descriptor
                    EntityRelationshipDefinition<T, BaseEntity, IRelationshipEntry<BaseEntity>> relationshipDefinition = entityDescriptor.getRelationshipDefinition(relationshipName);
                    BaseEntityDescriptor<BaseEntity> targetEntityDescriptor = EntityDescriptorFactory.<BaseEntity>get(relationshipDefinition.getTargetClassType());
                    EntityXMLConverter<BaseEntity> relatedEntityXMLConverter = new EntityXMLConverter<BaseEntity>(targetEntityDescriptor);


                    xmlEvent = eventReader.nextEvent();
                    while (!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(RELATIONSHIP_ELEMENT_TAG_NAME))) {
                        if (xmlEvent.isStartElement()) {
                            StartElement currRelatedEntityStartElement = xmlEvent.asStartElement();


                            RelationshipEntryXMLConverter<BaseEntity, IRelationshipEntry<BaseEntity>> relationshipEntryXMLConverter = new RelationshipEntryXMLConverter<BaseEntity, IRelationshipEntry<BaseEntity>>(relationshipDefinition.getRelationshipEntryDescriptor(), targetEntityDescriptor);
                            switch (relationshipDefinition.getType()){
                                case ONE_TO_MANY:
                                    entriesMapByRelationshipName.put(relationshipName, Arrays.asList(parseOneToManyRelationship(eventReader, currRelatedEntityStartElement, relationshipEntryXMLConverter)));
                                    break;
                                case MANY_TO_MANY:
                                    List<IRelationshipEntry> relationshipEntryList = entriesMapByRelationshipName.get(relationshipName);
                                    if (relationshipEntryList == null){
                                        relationshipEntryList = new ArrayList<IRelationshipEntry>();
                                        entriesMapByRelationshipName.put(relationshipName, relationshipEntryList);
                                    }
                                    String wrapperTagName = currRelatedEntityStartElement.getName().getLocalPart();
                                    if (!wrapperTagName.equals(relationshipEntryXMLConverter.getWrapperElementName())){
                                        throw new XMLParsingException("Invalid many-to-many relationship format. wrapper element expected: " + relationshipEntryXMLConverter.getWrapperElementName() + " but found " + wrapperTagName);
                                    }
                                    xmlEvent = eventReader.nextEvent();
                                    while (!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(wrapperTagName))) {
                                        if (xmlEvent.isStartElement()){
                                            relationshipEntryList.add(parseOneToManyRelationship(eventReader, xmlEvent.asStartElement(), relationshipEntryXMLConverter));
                                        }
                                        xmlEvent = eventReader.nextEvent();
                                    }


                                        break;
                                case ONE_TO_ONE:
                                    //todo:
                                    break;
                            }
                        }
                        xmlEvent = eventReader.nextEvent();
                    }
                }
            }
            xmlEvent = eventReader.nextEvent();
        }
        return entriesMapByRelationshipName;
    }

    private IRelationshipEntry parseOneToManyRelationship(XMLEventReader eventReader, StartElement currRelatedEntityStartElement, RelationshipEntryXMLConverter<BaseEntity, IRelationshipEntry<BaseEntity>> relationshipEntryXMLConverter) throws XMLParsingException, XMLStreamException {
        return relationshipEntryXMLConverter.fromEventReader(eventReader, currRelatedEntityStartElement);
    }

    @Override
    protected void appendField(StringBuilder sb, FieldInstance currField) {
        if (! "id".equals(currField.getName())){
            super.appendField(sb, currField);
        }
    }

    public void appendIdOnlyEntity(StringBuilder sb, Key id) {
        sb.append('<').append(getElementName())
        .append(" id=\"").append(id).append("\"/>");
    }


    @Override
    protected void appendAttributes(StringBuilder sb, T entity) {
        super.appendAttributes(sb, entity);
        if (entity.getId() != null) {
            sb.append(" id=\"").append(entity.getId()).append("\"");
        }
    }

    @Override
    protected void appendAdditionalFields(StringBuilder sb, T entity) {
        List<EntityRelationship> loadedRelationships = entity.getLoadedRelationships();
        if (!loadedRelationships.isEmpty()) {
            sb.append('<').append(RELATIONSHIPS_ELEMENT_TAG_NAME).append('>');
            for (EntityRelationship currRelationship : loadedRelationships) {

                BaseEntityDescriptor<BaseEntity> targetEntityDescriptor = EntityDescriptorFactory.get(currRelationship.getDefinition().getTargetClassType());
                // Printing to xml only "satisfied" relationships
                if (currRelationship.isSatisfied()){
                    sb.append('<').append(RELATIONSHIP_ELEMENT_TAG_NAME).append(" name=\"").append(currRelationship.getDefinition().getName()).append("\">");
                    switch (currRelationship.getDefinition().getType()) {
                        case ONE_TO_MANY:
                            EntityOneToManyRelationship oneToManyRelationship = (EntityOneToManyRelationship) currRelationship;
                            RelationshipEntryXMLConverter<BaseEntity, RelationshipEntry<BaseEntity>> relationshipEntryConverter = new RelationshipEntryXMLConverter<BaseEntity, RelationshipEntry<BaseEntity>>(oneToManyRelationship.getDefinition().getRelationshipEntryDescriptor(), targetEntityDescriptor);
                            relationshipEntryConverter.append(sb, oneToManyRelationship.getRelationshipEntry());
                            break;
                        case MANY_TO_MANY:
                            EntityManyToManyRelationship<T, BaseEntity, BasePersistableRelationshipEntry<BaseEntity>> manyRelationship = (EntityManyToManyRelationship) currRelationship;
                            RelationshipEntryXMLConverter relationshipEntryConverterManyToMany = new RelationshipEntryXMLConverter<BaseEntity, BasePersistableRelationshipEntry<BaseEntity>>(manyRelationship.getDefinition().getRelationshipEntryDescriptor(), targetEntityDescriptor);
                            Map<Key, BasePersistableRelationshipEntry<BaseEntity>> relationshipEntries = manyRelationship.getRelationshipEntriesByTargetEntityId();
                            if (!relationshipEntries.isEmpty()){

                                // appending wrapper tag
                                sb.append("<").append(relationshipEntryConverterManyToMany.getWrapperElementName()).append(">");

                                // Appending all related entities
                                for (Map.Entry<Key, BasePersistableRelationshipEntry<BaseEntity>> currTargetEntityEntry : relationshipEntries.entrySet() ){
                                    relationshipEntryConverterManyToMany.append(sb, currTargetEntityEntry.getValue());
                                }

                                // Closing wrapper tag
                                sb.append("</").append(relationshipEntryConverterManyToMany.getWrapperElementName()).append(">");
                            }

                            break;
                        case MANY_TO_ONE:
                            EntityManyToOneRelationship<T, BaseEntity, RelationshipEntry<BaseEntity>> manyToOneRelationship = (EntityManyToOneRelationship) currRelationship;
                            RelationshipEntryXMLConverter relationshipEntryConverterManyToOne = new RelationshipEntryXMLConverter<BaseEntity, RelationshipEntry<BaseEntity>>(manyToOneRelationship.getDefinition().getRelationshipEntryDescriptor(), targetEntityDescriptor);
                            List<RelationshipEntry<BaseEntity>> manyToOneEntries = manyToOneRelationship.getOrderedRelationshipEntries();
                            if (!manyToOneEntries.isEmpty()){

                                // appending wrapper tag
                                sb.append("<").append(relationshipEntryConverterManyToOne.getWrapperElementName()).append(">");

                                // Appending all related entities
                                for (RelationshipEntry<BaseEntity> currTargetEntityEntry : manyToOneEntries ){
                                    relationshipEntryConverterManyToOne.append(sb, currTargetEntityEntry);
                                }

                                // Closing wrapper tag
                                sb.append("</").append(relationshipEntryConverterManyToOne.getWrapperElementName()).append(">");
                            }

                            break;

                        default:
                            //todo:support all types..

                            break;
                    }

                    sb.append("</").append(RELATIONSHIP_ELEMENT_TAG_NAME).append('>');
                }
            }
            sb.append("</").append(RELATIONSHIPS_ELEMENT_TAG_NAME).append('>');
        }
    }

    @Override
    protected T convertToEntity() {
        T entity = super.convertToEntity();
        if (entityId != null){
            entity.setId(entityId);
        }

        if (relationships != null){
            for (Map.Entry<String, List<IRelationshipEntry>> currRelationshipEntry : relationships.entrySet()){
                EntityRelationship relationship = entity.getRelationship(currRelationshipEntry.getKey());
                switch (relationship.getDefinition().getType()){
                    case ONE_TO_MANY:
                        ((EntityOneToManyRelationship)relationship).setRelationshipEntry((RelationshipEntry) currRelationshipEntry.getValue().get(0));
                        break;
                    case MANY_TO_MANY:
                        EntityManyToManyRelationship manyToManyRelationship = (EntityManyToManyRelationship) relationship;
                        for (IRelationshipEntry currRelatedEntityEntry : currRelationshipEntry.getValue()){
                            manyToManyRelationship.addNewRelation((BasePersistableRelationshipEntry) currRelatedEntityEntry);
                        }
                        break;
                    case ONE_TO_ONE:
                        //todo:do
                        break;
                }
            }
        }

        return entity;
    }
}
