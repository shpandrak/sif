package com.shpandrak.codegen.schema;

import com.shpandrak.codegen.PojoGenerator;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.CustomProperty;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.StringFieldDef;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.OneToManyRelation;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 21:03
 */
public class SchemaCodeGenerator implements IMetadataGenerator{
    private StringBuilder sb;

    @Override
    public void generate(MetadataStore store, GenerationContext generationContext) throws MetadataGeneratorException {
        sb = new StringBuilder();
        sb.append("-- Generated by Shpandrak for ").append(store.getName()).append("\n\n");


        // First Pass
        for (DataEntityMetadata currEntity : store.getEntities()){

            sb.append("-- Table for entity ").append(currEntity.getName()).append("\n");
            sb.append("DROP TABLE IF EXISTS ").append(currEntity.getTableName()).append(" CASCADE;\n\n");
            sb.append("CREATE TABLE ").append(currEntity.getTableName()).append(" (\n");
            sb.append("\tid uuid NOT NULL");
            for (AbstractFieldMetadata currField : currEntity.getFields()){
                sb.append(",\n").append("\t").append(getFieldPersistenceName(currField)).append(" ").append(getDatabaseFieldDeclaration(currField));
            }

            List<String> fkDefinitions = new ArrayList<String>();

            for (AbstractRelation currRelation : currEntity.getRelations()) {
                switch (currRelation.getRelationType()){
                    case OneToMany:
                    case OneToOwner:
                        OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;
                        String fieldName = oneToManyRelation.getFieldName();
                        if (fieldName == null){
                            fieldName = oneToManyRelation.getRelationshipName() + "Id";
                        }

                        // Creating te field
                        sb.append(",\n").append("\t").append(fieldName).append(" ").append("uuid");
                        if (oneToManyRelation.isMandatory()){
                            sb.append(" NOT NULL");
                        }

                        // Adding the field's FK
                        fkDefinitions.add("ALTER TABLE " + currEntity.getTableName() +
                                "\n\tADD CONSTRAINT " + currEntity.getTableName() + "_" +
                                oneToManyRelation.getRelationshipName() + "_fk\n\t FOREIGN KEY (" + fieldName +  ") REFERENCES " +
                                oneToManyRelation.getRelatedEntity() + ";");
                        break;
                    default:
                        break;
                }
            }


            sb.append("\n);\n\n");
            sb.append("ALTER TABLE ").append(currEntity.getTableName()).append("\n\tADD CONSTRAINT ").append(currEntity.getTableName()).append("_pkey PRIMARY KEY (id);\n");
            for (String currFK : fkDefinitions){
                sb.append(currFK);
            }
            sb.append("\n\n\n");
        }

        // Second pass - for compound relationships
        for (DataEntityMetadata currEntity : store.getEntities()){

            for (AbstractRelation currRelation : currEntity.getRelations()) {
                switch (currRelation.getRelationType()){
                    case ManyToMany:

                        // Create the relationship table
                        DataEntityMetadata relatedEntityMetadta = store.getEntityMetadataByName(currRelation.getRelatedEntity());
                        String relationshipTableName = generateRelationshipTableTableName(currEntity, currRelation);

                        String primaryTableIdFieldName = getRelationshipTableReferenceFieldName(currEntity);
                        String relatedTableIdFieldName = getRelationshipTableReferenceFieldName(relatedEntityMetadta);

                        sb.append("-- Relationship table for many-to-many relationship ").append(currRelation.getName()).append(" between ").
                                append(currEntity.getName()).append(" to ").append(currRelation.getRelatedEntity()).append(" entities\n").append(

                            "DROP TABLE IF EXISTS ").append(relationshipTableName).append(" CASCADE;\n\n").append(
                            "CREATE TABLE ").append(relationshipTableName).append(" (\n").append(
                            "    id uuid NOT NULL,\n").append(
                            "    ").append(primaryTableIdFieldName).append(" uuid NOT NULL,\n").append(
                            "    ").append(relatedTableIdFieldName).append(" uuid NOT NULL");

                        // Adding relationship additional fields
                        List<AbstractFieldMetadata> relationshipAdditionalFields = currRelation.getFields();
                        if (relationshipAdditionalFields != null && !relationshipAdditionalFields.isEmpty()){
                            for (AbstractFieldMetadata currField : relationshipAdditionalFields){
                                sb.append(",\n").append("\t").append(getFieldPersistenceName(currField)).append(" ").append(getDatabaseFieldDeclaration(currField));
                            }
                        }

                        sb.append(
                            ");\n\n");

                        sb.append(
                            "ALTER TABLE ").append(relationshipTableName).append("\n").append(
                            "    ADD CONSTRAINT ").append(relationshipTableName).append("_").append(currEntity.getTableName()).append("_fk\n").append(
                            "    FOREIGN KEY (").append(primaryTableIdFieldName).append(") REFERENCES ").append(currEntity.getTableName()).append(";\n\n");

                        sb.append(
                                "ALTER TABLE ").append(relationshipTableName).append("\n").append(
                                "    ADD CONSTRAINT ").append(relationshipTableName).append("_").append("related_").append(relatedEntityMetadta.getTableName()).append("_fk\n").append(
                            "    FOREIGN KEY (").append(relatedTableIdFieldName).append(") REFERENCES ").append(relatedEntityMetadta.getTableName()).append(";\n\n");
                        sb.append("\n\n");
                        break;


                }
            }
        }
    }

    public static String getFieldPersistenceName(AbstractFieldMetadata currField) {
        String persistenceFieldName = currField.getPersistenceFieldName();
        if (persistenceFieldName == null){
            return currField.getName();
        }else {
            return persistenceFieldName;
        }
    }

    public static String getFieldPersistenceNameOrNull(AbstractFieldMetadata currField) {
        String persistenceFieldName = currField.getPersistenceFieldName();
        if (persistenceFieldName == null){
            return null;
        }else {
            return persistenceFieldName;
        }
    }

    public static String getRelationshipTableReferenceFieldName(DataEntityMetadata currEntity) {
        return PojoGenerator.generateEntityIdFieldName(currEntity);
    }

    public static String generateRelationshipTableTableName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return currEntity.getTableName() + "_" + StringUtil.getPluralForm(currRelation.getRelationshipName());
    }

    private static String getDatabaseFieldDeclaration(AbstractFieldMetadata currField) throws MetadataGeneratorException {
        String declaration = getDBFieldType(currField);
        if (currField.isMandatory()){
            declaration += " NOT NULL";
        }

        return declaration;
    }

    private static String getDBFieldType(AbstractFieldMetadata currField) throws MetadataGeneratorException {
        switch (currField.getType()) {
            case STRING:
                StringFieldDef stringField = (StringFieldDef) currField;
                return "character varying(" + stringField.getLength() + ")";
            case DATE:
                return "bigint";
            case BOOLEAN:
                return "boolean";
            case ENUM:
                return "smallint";
            case LONG:
                return "bigint";
            case INTEGER:
                return "integer";
            case DOUBLE:
                return "double";
            default:
                throw new MetadataGeneratorException("Unsupported data type: " + currField.getType() + " for field: " + getFieldPersistenceName(currField));
        }
    }

    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
        //nop
    }

    @Override
    public void write(String rootDir) throws MetadataGeneratorException {
        try {
            FileUtils.writeStringToFile(new File(rootDir + "/src/main/resources/schema.sql"), sb.toString());
        } catch (IOException e) {
            throw new MetadataGeneratorException("Failed writing schema file", e);
        }
    }
}