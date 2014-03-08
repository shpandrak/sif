package com.shpandrak.metadata.model;

import com.shpandrak.metadata.model.field.*;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.ManyToManyRelation;
import com.shpandrak.metadata.model.relation.ManyToOneRelation;
import com.shpandrak.metadata.model.relation.OneToManyRelation;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love
 * User: shpandrak
 * Date: 10/7/12
 * Time: 17:29
 */
@XmlRootElement(name = "entity")
public class DataEntityMetadata {
    private String name;
    private String persistenceEntityName = null;
    private String ownerEntityName;
    private String extendsEntity;
    private List<AbstractFieldMetadata> fields;
    private List<AbstractRelation> relations;

    protected DataEntityMetadata() {
        fields = new ArrayList<AbstractFieldMetadata>();
        relations = new ArrayList<AbstractRelation>();
    }

    public DataEntityMetadata(String name, String ownerEntityName, String extendsEntity, List<AbstractFieldMetadata> fields, List<AbstractRelation> relations) {
        this.name = name;
        this.ownerEntityName = ownerEntityName;
        this.extendsEntity = extendsEntity;
        this.fields = fields;
        this.relations = relations;
    }

    public String getJavaClassName(){
        return this.name.substring(0,1).toUpperCase() + this.name.substring(1);
    }

    public String getTableName(){
        return persistenceEntityName != null ? persistenceEntityName : this.name;
    }

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }


    @XmlAttribute(required = false, name = "persistence-entity-name")
    public String getPersistenceEntityName() {
        return persistenceEntityName;
    }

    public void setPersistenceEntityName(String persistenceEntityName) {
        this.persistenceEntityName = persistenceEntityName;
    }

    @XmlAttribute(name = "owner-entity")
    public String getOwnerEntityName() {
        return ownerEntityName;
    }

    public void setOwnerEntityName(String ownerEntityName) {
        this.ownerEntityName = ownerEntityName;
    }

    @XmlAttribute(name = "extends-entity")
    public String getExtendsEntity() {
        return extendsEntity;
    }

    public void setExtendsEntity(String extendsEntity) {
        this.extendsEntity = extendsEntity;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setFields(List<AbstractFieldMetadata> fields) {
        this.fields = fields;
    }

    @XmlElementWrapper(name = "fields", required = true)
    @XmlElements({
            @XmlElement(name = "string-field", type=StringFieldDef.class),
            @XmlElement(name = "date-field", type=DateFieldDef.class),
            @XmlElement(name = "enum-field", type=EnumFieldDef.class),
            @XmlElement(name = "boolean-field", type=BooleanFieldDef.class),
            @XmlElement(name = "integer-field", type=IntegerFieldDef.class),
            @XmlElement(name = "long-field", type=LongFieldDef.class),
            @XmlElement(name = "double-field", type=DoubleFieldDef.class)
    })
    public List<AbstractFieldMetadata> getFields() {
        return fields;
    }

    @XmlElementWrapper(name = "relations", required = false)
    @XmlElements({
            @XmlElement(name = "one-to-many", type=OneToManyRelation.class),
            @XmlElement(name = "many-to-many", type=ManyToManyRelation.class),
            @XmlElement(name = "many-to-one", type=ManyToOneRelation.class)
    })

    public List<AbstractRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<AbstractRelation> relations) {
        this.relations = relations;
    }
}
