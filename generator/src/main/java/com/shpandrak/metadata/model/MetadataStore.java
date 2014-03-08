package com.shpandrak.metadata.model;

import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.ManyToOneRelation;
import com.shpandrak.metadata.model.relation.OneToOwnerEntityRelation;
import com.shpandrak.metadata.model.relation.RelationType;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created with honest love
 * User: shpandrak
 * Date: 10/10/12
 * Time: 19:24
 */
@XmlRootElement(name = "metadata_store")
public class MetadataStore {
    private String name;
    private String namespace;
    private List<EnumMetadata> enumerations = new ArrayList<EnumMetadata>();
    private List<DataEntityMetadata> entities;
    private Map<String, DataEntityMetadata> entitiesByName;

    protected MetadataStore() {
        entities = new ArrayList<DataEntityMetadata>();
    }

    public MetadataStore(String namespace, List<DataEntityMetadata> entities) {
        this.namespace = namespace;
        setEntities(entities);
        afterUnmarshal(null, null);
    }

    public void setEntities(List<DataEntityMetadata> entities) {
        this.entities = entities;
    }

    @XmlAttribute(required = true)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "entity")
    @XmlElementWrapper(name = "entities")
    public List<DataEntityMetadata> getEntities() {
        return entities;
    }

    @XmlElement(name = "enum")
    @XmlElementWrapper(name = "enumerations")
    public List<EnumMetadata> getEnumerations() {
        return enumerations;
    }

    public void setEnumerations(List<EnumMetadata> enumerations) {
        this.enumerations = enumerations;
    }

    /**
     * This method gets automatically called by the JAXB infrastructure
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent){
        entitiesByName = new HashMap<String, DataEntityMetadata>(entities.size());
        for (DataEntityMetadata currEntity : entities){
            entitiesByName.put(currEntity.getName(), currEntity);
        }

        // Creating implicit OneToOwner relationship defined by owner-entity attribute
        for (DataEntityMetadata currEntity : entities){
            String ownerEntityName = currEntity.getOwnerEntityName();
            if (ownerEntityName != null){
                DataEntityMetadata ownerEntityMetadata = entitiesByName.get(ownerEntityName);
                if (ownerEntityMetadata == null){
                    throw new IllegalStateException("Invalid owner-entity value" + ownerEntityName + " set for entity " + currEntity.getName());
                }
                List<AbstractRelation> relations = new ArrayList<AbstractRelation>(currEntity.getRelations());
                OneToOwnerEntityRelation oneToOwnerEntityRelation = new OneToOwnerEntityRelation(ownerEntityName);
                relations.add(0, oneToOwnerEntityRelation);
                currEntity.setRelations(relations);

                List<AbstractRelation> ownerEntityRelationshipsCopy = new ArrayList<AbstractRelation>(ownerEntityMetadata.getRelations());

                // Checking if reverse relationship already defined manually in file (for example to apply sort..)
                boolean foundOverriddenReverseRelationship = false;

                for (AbstractRelation currRel :  ownerEntityRelationshipsCopy){
                    if (currRel.getRelationType() == RelationType.ManyToOne &&
                        currRel.getRelatedEntity().equals(currEntity.getName())){
                        ManyToOneRelation currRelManyToOne = (ManyToOneRelation) currRel;
                        if (currRelManyToOne.getOneToManyRelation() == null){
                            currRelManyToOne.setOneToManyRelation(oneToOwnerEntityRelation);
                            foundOverriddenReverseRelationship = true;
                        }
                    }

                }

                if (!foundOverriddenReverseRelationship){
                    ownerEntityRelationshipsCopy.add(new ManyToOneRelation(currEntity.getName(), false, currEntity.getName(), Collections.<AbstractFieldMetadata>emptyList(), oneToOwnerEntityRelation, null));
                }
                ownerEntityMetadata.setRelations(ownerEntityRelationshipsCopy);
            }
        }


    }

    public DataEntityMetadata getEntityMetadataByName(String entityName){
        return entitiesByName.get(entityName);
    }

}
