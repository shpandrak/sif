package com.shpandrak.codegen;

import com.shpandrak.codegen.model.*;
import com.shpandrak.codegen.schema.SchemaCodeGenerator;
import com.shpandrak.common.model.FieldType;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.managers.DBBaseEntityManagerBean;
import com.shpandrak.database.managers.DBBasePersistableObjectManagerBean;
import com.shpandrak.database.managers.EntityAdapter;
import com.shpandrak.database.managers.PersistableObjectAdapter;
import com.shpandrak.database.table.*;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.EnumFieldDef;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.metadata.model.relation.ManyToManyRelation;
import com.shpandrak.metadata.model.relation.OneToManyRelation;
import com.shpandrak.metadata.model.relation.RelationType;
import com.shpandrak.persistence.IConnectionProvider;
import com.shpandrak.persistence.IConnectionProviderFactory;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.managers.ManagerClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
 * Date: 10/13/12
 * Time: 09:42
 */
public class DatabaseLayerGenerator extends BaseGenerator implements IPersistenceLayerMetadataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseLayerGenerator.class);
    private static final String CONNECTION_PROVIDER_PROPERTY_NAME = "connection-provider";
    private GeneratedClass layerLoaderClass;
    private String connectionProviderClassName = null;

    public static String getManagerName(GeneratedClass currPojo) {
        return currPojo.getName() + "Manager";
    }

    @Override
    public GeneratedClass getLayerLoaderClass() {
        return layerLoaderClass;
    }

    @Override
    protected void generateClasses(MetadataStore store, GeneratedPackage generatedPackage, GenerationContext generationContext) throws MetadataGeneratorException {
        GeneratedPackage pojoGeneratorGeneratedPackage = pojoGenerator.getGeneratedPackage();

        // Creating module database layer loader
        createModuleLoader(store);

        // First Pass - creating table classes

        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass dbTable = createDBTableGeneratedClass(currEntity);
            generatedPackage.addClass(dbTable);
        }
        // Second Pass

        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = pojoGeneratorGeneratedPackage.getClass(currEntity.getJavaClassName());

            GeneratedClass dbTable = generateDBTable(currEntity);

            GeneratedClass generatedPersistentEntityAdapter = generatePersistentEntityAdapter(currEntity, currPojo, dbTable);
            generatedPackage.addClass(generatedPersistentEntityAdapter);

            GeneratedClass manager = generateManager(currEntity, currPojo, generatedPersistentEntityAdapter, dbTable);
            generatedPackage.addClass(manager);

            registerManagerInModuleLoader(manager, currPojo);
        }

        // Third pass - for relationships...

        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = pojoGenerator.getGeneratedPackage().getClass(currEntity.getJavaClassName());

            List<GeneratedClass> relationshipClasses = generateRelationshipClasses(currEntity, currPojo);
            generatedPackage.addClasses(relationshipClasses);

            GeneratedClass managerGeneratedClass = generatedPackage.getClass(getManagerName(currPojo));
            //todo: add relationship tables to manager class


        }

    }


    private void createModuleLoader(MetadataStore store) {
        String moduleName = StringUtil.capitalize(store.getName());
        layerLoaderClass = new GeneratedClass(moduleName + "ModuleDatabaseLayerLoader", getGeneratedPackage().getName());
        layerLoaderClass.addLogger();
        GeneratedBody body = new GeneratedBody();

        String firstLine = "logger.info(\"Database Layer for module module " + moduleName + " has been loaded\");";
        body.addLine(new GeneratedCodeLine(firstLine));

        // Adding default connection provider in case we have a default connection provider
        if (connectionProviderClassName != null){
            String connProviderSimpleName = connectionProviderClassName.substring(connectionProviderClassName.lastIndexOf(".") + 1);
            body.addLine(new GeneratedCodeLine(PersistenceLayerManager.class.getSimpleName() +".init(new " + IConnectionProviderFactory.class.getSimpleName() + "() {\n" +
                    "            @Override\n" +
                    "            public " + IConnectionProvider.class.getSimpleName() + " create() {\n" +
                    "                return new " + connProviderSimpleName + "();\n" +
                    "            }\n" +
                    "        });",
                    Arrays.asList(
                            new ClassGeneratedType(connProviderSimpleName, connectionProviderClassName),
                            new ClassGeneratedType(PersistenceLayerManager.class),
                            new ClassGeneratedType(IConnectionProvider.class),
                            new ClassGeneratedType(IConnectionProviderFactory.class)
                            )));
        }

        layerLoaderClass.addImport(BaseEntity.class);
        layerLoaderClass.addMethod(new GeneratedMethod(MODULE_LOADER_LOAD_METHOD_NAME, GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Collections.<GeneratedAnnotation>emptyList(), true, body));

        getGeneratedPackage().addClass(layerLoaderClass);
    }

    private List<GeneratedClass> generateRelationshipClasses(DataEntityMetadata currEntity, GeneratedClass currPojo) throws MetadataGeneratorException {
        List<GeneratedClass> relationshipGeneratedClasses = new ArrayList<GeneratedClass>();
        for (AbstractRelation currRelation : currEntity.getRelations()) {
            GeneratedClass relationshipEntryGeneratedClass = pojoGenerator.getGeneratedPackage().getClass(PojoGenerator.getRelationshipEntryClassName(currEntity, currRelation));
            switch (currRelation.getRelationType()){

                case ManyToMany:
                    DataEntityMetadata relatedEntityMetadata = store.getEntityMetadataByName(currRelation.getRelatedEntity());
                    String relationshipDBTableClassName = getRelationshipTableDBClassName(currEntity, currRelation);
                    GeneratedClass generatedDBTableClass = new GeneratedClass(relationshipDBTableClassName, getGeneratedPackage().getName());
                    generatedDBTableClass.setExtendsClass(DBRelationshipTable.class);
                    List<ClassGeneratedType> typesInUseForConstructor = new ArrayList<ClassGeneratedType>();

                    // Table name static member
                    generatedDBTableClass.addMember(new GeneratedClassMember("TABLE_NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + SchemaCodeGenerator.generateRelationshipTableTableName(currEntity, currRelation) + "\";")));

                    String primaryEntityReferenceFieldName = SchemaCodeGenerator.getRelationshipTableReferenceFieldName(currEntity);
                    String relatedEntityReferenceFieldName = SchemaCodeGenerator.getRelationshipTableReferenceFieldName(relatedEntityMetadata);
                    GeneratedClass primaryTableDescriptorGeneratedClass = getGeneratedPackage().getClass(getDBTableClassName(currEntity));
                    primaryTableDescriptorGeneratedClass.addImports(new ClassGeneratedType(generatedDBTableClass));
                    GeneratedClass relatedTableDescriptorGeneratedClass = getGeneratedPackage().getClass(getDBTableClassName(relatedEntityMetadata));


                    generatedDBTableClass.addMember(new GeneratedClassMember("id", new ClassGeneratedType(DBKeyField.class), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + DBKeyField.class.getSimpleName() + "(" + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getFieldDescriptorStaticMemberName("id") + ");", Arrays.asList(new ClassGeneratedType(relationshipEntryGeneratedClass), new ClassGeneratedType(DBKeyField.class)))));
                    generatedDBTableClass.addMember(new GeneratedClassMember(primaryEntityReferenceFieldName, new ClassGeneratedType(DBRelationshipKeyField.class), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + DBRelationshipKeyField.class.getSimpleName() + "(" + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getFieldDescriptorStaticMemberName(currEntity.getName() + "Id") + " , " + primaryTableDescriptorGeneratedClass.getName() + ".id" + ");", Arrays.asList(new ClassGeneratedType(DBKeyField.class), new ClassGeneratedType(primaryTableDescriptorGeneratedClass)))));
                    generatedDBTableClass.addMember(new GeneratedClassMember(relatedEntityReferenceFieldName, new ClassGeneratedType(DBRelationshipKeyField.class), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + DBRelationshipKeyField.class.getSimpleName() + "(" + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME  + "." + PojoGenerator.getFieldDescriptorStaticMemberName("related" + StringUtil.capitalize(relatedEntityMetadata.getName()) + "Id") + ", " + relatedTableDescriptorGeneratedClass.getName() + ".id);", Arrays.asList(new ClassGeneratedType(DBKeyField.class), new ClassGeneratedType(relatedTableDescriptorGeneratedClass)))));

                    // Implement relationship table interface
                    generatedDBTableClass.addMethod(new GeneratedMethod("getSourceField", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(DBRelationshipKeyField.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(new GeneratedCodeLine("return this." + primaryEntityReferenceFieldName + ";"))));
                    generatedDBTableClass.addMethod(new GeneratedMethod("getTargetField", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(DBRelationshipKeyField.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(new GeneratedCodeLine("return this." + relatedEntityReferenceFieldName+ ";"))));

                    String fieldsList = "id, " + primaryEntityReferenceFieldName + ", " + relatedEntityReferenceFieldName;

                    List<AbstractFieldMetadata> relationshipAdditionalFields = currRelation.getFields();
                    if (relationshipAdditionalFields != null && !relationshipAdditionalFields.isEmpty()){
                        for (AbstractFieldMetadata currField : relationshipAdditionalFields){
                            addFieldMember(relationshipEntryGeneratedClass, generatedDBTableClass, currField);
                            fieldsList += ", " + currField.getName();
                        }
                    }


                    typesInUseForConstructor.add(new ClassGeneratedType(Arrays.class));
                    typesInUseForConstructor.add(new ClassGeneratedType(DBField.class));
                    typesInUseForConstructor.add(new ClassGeneratedType(Collections.class));
                    GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine("super(TABLE_NAME, Arrays.<" + DBField.class.getSimpleName() + ">asList(" + fieldsList + "));", typesInUseForConstructor);
                    generatedDBTableClass.addDefaultConstructor(GeneratedModifier.PRIVATE, new GeneratedBody(Arrays.asList(generatedCodeLine)));
                    generatedDBTableClass.addMember(new GeneratedClassMember("INSTANCE", new ClassGeneratedType(generatedDBTableClass), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + generatedDBTableClass.getName() + "();")));


                    relationshipGeneratedClasses.add(generatedDBTableClass);
                    GeneratedClass generatedRelationshipManager = generateRelationshipEntryManagerClass(currEntity, currPojo, currRelation, relationshipEntryGeneratedClass, relationshipDBTableClassName);


                    relationshipGeneratedClasses.add(generatedRelationshipManager);


                    break;
                default:
                    //todo:
            }
        }

        return relationshipGeneratedClasses;
    }

    private GeneratedClass generateRelationshipEntryManagerClass(DataEntityMetadata currEntity, GeneratedClass currPojo, AbstractRelation currRelation, GeneratedClass relationshipEntryGeneratedClass, String relationshipDBTableClassName) throws MetadataGeneratorException {
        // Creating a manager class for the relationship entry
        GeneratedClass generatedRelationshipManager = new GeneratedClass(generatedRelationshipManagerClassName(currEntity, currRelation), getGeneratedPackage().getName());
        // Manager Extends DBBaseEntityManagerBean
        generatedRelationshipManager.setExtendsClass(DBBasePersistableObjectManagerBean.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass))));

        // Manager implements generic IPersistableObjectManager interface
        generatedRelationshipManager.addInterface(IPersistableObjectManager.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass))));

        // Constructor with connectionProvider parameter
        GeneratedCodeLine ctorLine = new GeneratedCodeLine("super(" + relationshipDBTableClassName + ".INSTANCE, connectionProvider);");
        generatedRelationshipManager.addConstructor(GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("connectionProvider", new ClassGeneratedType(IDBConnectionProvider.class))), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(Arrays.asList(ctorLine)));

        // Adding default constructor
        GeneratedCodeLine defaultCtorCode = new GeneratedCodeLine("this((" + IDBConnectionProvider.class.getSimpleName() + ")" + PersistenceLayerManager.class.getSimpleName() + ".getConnectionProvider()" + ");", Arrays.asList(new ClassGeneratedType(PersistenceLayerManager.class), new ClassGeneratedType(IDBConnectionProvider.class)));
        generatedRelationshipManager.addConstructor(GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), Arrays.asList(new ClassGeneratedType(PersistenceException.class)), new GeneratedBody(defaultCtorCode));


        // Implementing getPersistEntityAdapter
        GeneratedCodeLine generatedCodeLine1 = new GeneratedCodeLine("return new " + PersistableObjectAdapter.class.getSimpleName() + "(getDbTable(), tableAlias, " + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");", Arrays.asList(new ClassGeneratedType(PersistableObjectAdapter.class)));
        generatedRelationshipManager.addMethod(new GeneratedMethod("getPersistEntityAdapter", GeneratedModifier.PROTECTED, Arrays.asList(new GeneratedParameter("tableAlias", new GeneratedType("String"))), new ClassGeneratedType(PersistableObjectAdapter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(generatedCodeLine1))));

        // Implementing getPersistEntityAdapter getEntityClass
        generatedRelationshipManager.addMethod(new GeneratedMethod("getEntityClass", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + relationshipEntryGeneratedClass.getName() + ".class;")))));

        // Implementing getDescriptor
        generatedRelationshipManager.addMethod(new GeneratedMethod("getDescriptor", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(ShpandrakObjectDescriptor.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + relationshipEntryGeneratedClass.getName() + ".DESCRIPTOR;")))));

        //register in moduleLoader
        registerManagerInModuleLoader(generatedRelationshipManager, relationshipEntryGeneratedClass);

        return generatedRelationshipManager;
    }

    public static String generatedRelationshipManagerClassName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return currEntity.getJavaClassName() + StringUtil.capitalize(currRelation.getRelationshipName()) + "Manager";
    }

    public static String getRelationshipTableDBClassName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return currEntity.getJavaClassName() + StringUtil.capitalize(currRelation.getRelationshipName()) + "RelationshipDBTable";
    }


    private void registerManagerInModuleLoader(GeneratedClass managerClass, GeneratedClass currPojo) throws MetadataGeneratorException {
        GeneratedBody staticCodeBlock = layerLoaderClass.getMethod(PojoGenerator.MODULE_LOADER_LOAD_METHOD_NAME).getBody();
        staticCodeBlock.addLine(new GeneratedCodeLine(ManagerClassFactory.class.getSimpleName() +  ".register(" + currPojo.getName() + ".class, " + managerClass.getName() + ".class);" ));
        layerLoaderClass.addImports(new ClassGeneratedType(currPojo));
        layerLoaderClass.addImports(new ClassGeneratedType(managerClass));
        layerLoaderClass.addImport(ManagerClassFactory.class);
    }


    @Override
    protected GeneratedPackage createPackage(MetadataStore store) {
        return new GeneratedPackage(store.getNamespace() + ".db");
    }

    private GeneratedClass generateManager(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass generatedQueryConverter, GeneratedClass dbTable) {
        GeneratedClass c = new GeneratedClass(getManagerName(currPojo), getGeneratedPackage().getName());

        // Manager Extends DBBaseEntityManagerBean
        c.setExtendsClass(DBBaseEntityManagerBean.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        // Manager implements generic IPersistableObjectManager interface
        c.addInterface(IEntityManager.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        // Constructor with connectionProvider parameter
        GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine("super(" + dbTable.getName() + ".INSTANCE, connectionProvider);");
        c.addConstructor(GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("connectionProvider", new ClassGeneratedType(IDBConnectionProvider.class))), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(generatedCodeLine));

        // Adding default constructor in case we have a default connection provider
        if (connectionProviderClassName != null){
            String connProviderSimpleName = connectionProviderClassName.substring(connectionProviderClassName.lastIndexOf(".") + 1);
            GeneratedCodeLine defaultCtorCode = new GeneratedCodeLine("super(" + dbTable.getName() + ".INSTANCE);");
            c.addConstructor(GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), Arrays.asList(new ClassGeneratedType(PersistenceException.class)), new GeneratedBody(defaultCtorCode));
        }

        // Implementing getPersistEntityAdapter
        GeneratedCodeLine generatedCodeLine1 = new GeneratedCodeLine("return new " + EntityAdapter.class.getSimpleName() + "<" + currPojo.getName() + ">(getDbTable(), tableAlias, " + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");", Arrays.asList(new ClassGeneratedType(generatedQueryConverter), new ClassGeneratedType(EntityAdapter.class)));
        c.addMethod(new GeneratedMethod("getPersistEntityAdapter", GeneratedModifier.PROTECTED, Arrays.asList(new GeneratedParameter("tableAlias", new GeneratedType("String"))), new ClassGeneratedType(PersistableObjectAdapter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(generatedCodeLine1))));

        // Implementing getPersistEntityAdapter getEntityClass
        c.addMethod(new GeneratedMethod("getEntityClass", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + currPojo.getName() + ".class;")))));


        // Generating convenience methods for listing by specific relationships
        for (AbstractRelation currRelation : currEntity.getRelations()){
            String relatedEntityIdParamName = currRelation.getRelatedEntity() + "Id";
            String code = "return listByRelationShip(" + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getRelationshipDescriptorStaticFieldName(currRelation) + ", " + relatedEntityIdParamName + ");";
            c.addMethod(
                    new GeneratedMethod(
                            "listBy" + StringUtil.capitalize(currRelation.getName()) + "Relationship",
                            GeneratedModifier.PUBLIC,
                            Arrays.asList(new GeneratedParameter(relatedEntityIdParamName, new ClassGeneratedType(Key.class))),
                            new ClassGeneratedType(List.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))),
                            Arrays.asList(new ClassGeneratedType(PersistenceException.class)),
                            Collections.<GeneratedAnnotation>emptyList(),
                            false,
                            new GeneratedBody(new GeneratedCodeLine(code))));
        }



        return c;
    }

    private GeneratedClass createDBTableGeneratedClass(DataEntityMetadata currEntity) throws MetadataGeneratorException {
        String className = getDBTableClassName(currEntity);
        return new GeneratedClass(className, getGeneratedPackage().getName());
    }

    private GeneratedClass generateDBTable(DataEntityMetadata currEntity) throws MetadataGeneratorException {
        String pojoEntityClassName = currEntity.getJavaClassName();
        GeneratedClass currEntityGeneratedClass = pojoGenerator.getGeneratedPojo(currEntity);

        GeneratedClass c = getGeneratedPackage().getClass(getDBTableClassName(currEntity));
        c.setExtendsClass(DBTable.class);

        List<ClassGeneratedType> typesInUseForConstructor = new ArrayList<ClassGeneratedType>();
        typesInUseForConstructor.add(new ClassGeneratedType(currEntityGeneratedClass));

        c.addMember(new GeneratedClassMember("TABLE_NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + currEntity.getTableName() + "\";")));
        c.addMember(
                new GeneratedClassMember(
                        "id",
                        new ClassGeneratedType(DBKeyField.class),
                        GeneratedModifier.PUBLIC,
                        false,
                        false,
                        true,
                        true,
                        new GeneratedCodeLine("new " + DBKeyField.class.getSimpleName() + "(" + currEntityGeneratedClass.getName() + "." +
                                PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME +"." +
                                PojoGenerator.getFieldDescriptorStaticMemberName("id") + ");")));

        String fieldsList = "id ";
        for (AbstractFieldMetadata currField : currEntity.getFields()) {
            addFieldMember(currEntityGeneratedClass, c, currField);
            fieldsList += ", " + currField.getName();
        }

        if (!currEntity.getRelations().isEmpty()){
            for (AbstractRelation currRelation : currEntity.getRelations()) {
                DataEntityMetadata relatedEntityMetadata = store.getEntityMetadataByName(currRelation.getRelatedEntity());
                GeneratedClass relatedEntityDBTableGeneratedClass =  getGeneratedPackage().getClass(getDBTableClassName(relatedEntityMetadata));
                switch (currRelation.getRelationType()){
                    case OneToMany:
                    case OneToOwner:

                        OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;

                        GeneratedClass relationshipEntryGeneratedClass = pojoGenerator.getGeneratedPackage().getClass(PojoGenerator.getRelationshipEntryClassName(currEntity, currRelation));
                        typesInUseForConstructor.add(new ClassGeneratedType(relationshipEntryGeneratedClass));

                        // Adding the Key field to the entity
                        String relationshipFieldMemberName = getRelationshipDBTableFieldName(oneToManyRelation);
                        typesInUseForConstructor.add(new ClassGeneratedType(DBEmbeddedRelationshipKeyField.class));
                        typesInUseForConstructor.add(new ClassGeneratedType(relatedEntityDBTableGeneratedClass));
                        typesInUseForConstructor.add(new ClassGeneratedType(currEntityGeneratedClass));
                        String code = "new " + DBEmbeddedRelationshipKeyField.class.getSimpleName() + "(" + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getFieldDescriptorStaticMemberName("related" + StringUtil.capitalize(currRelation.getRelatedEntity()) + "Id") + ", " + relatedEntityDBTableGeneratedClass.getName() + ".id, "+ pojoEntityClassName + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getRelationshipDescriptorStaticFieldName(currRelation) + ");";
                        GeneratedCodeLine initializerLine = new GeneratedCodeLine(code);
                        c.addMember(new GeneratedClassMember(relationshipFieldMemberName, new ClassGeneratedType(DBEmbeddedRelationshipKeyField.class), GeneratedModifier.PUBLIC, false, false, true, true, initializerLine));
                        fieldsList += ", " + relationshipFieldMemberName;

                        break;
                    case ManyToMany:
                        ManyToManyRelation manyToManyRelation = (ManyToManyRelation)currRelation;

                    default:
                        break;
                }
            }

        }

        typesInUseForConstructor.add(new ClassGeneratedType(Arrays.class));
        typesInUseForConstructor.add(new ClassGeneratedType(DBField.class));
        GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine("super(TABLE_NAME, Arrays.<" + DBField.class.getSimpleName() + ">asList(" + fieldsList + "));", typesInUseForConstructor);
        c.addDefaultConstructor(GeneratedModifier.PRIVATE, new GeneratedBody(Arrays.asList(generatedCodeLine)));
        c.addMember(new GeneratedClassMember("INSTANCE", new ClassGeneratedType(c), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + c.getName() + "();")));
        return c;

    }

    private void addFieldMember(GeneratedClass currEntityGeneratedClass, GeneratedClass c, AbstractFieldMetadata currField) throws MetadataGeneratorException {
        Class<? extends DBField> dbFieldFromMetadataType = getDBFieldFromMetadataType(currField.getType());
        c.addImport(dbFieldFromMetadataType);

        GeneratedCodeLine initializerLine;
        ClassGeneratedType memberGeneratedType;

        String fieldPersistenceNameOrNull = SchemaCodeGenerator.getFieldPersistenceNameOrNull(currField);

        List<ClassGeneratedType> typesInUse = new ArrayList<>();
        String code;
        if (currField.getType() == FieldType.ENUM){
            GeneratedEnum generatedEnumByField = pojoGenerator.getGeneratedEnumByField((EnumFieldDef) currField);

            code = "new " + dbFieldFromMetadataType.getSimpleName() + "<" + generatedEnumByField.getName() + ">(" + currEntityGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getFieldDescriptorStaticMemberName(currField.getName());
            typesInUse.add(new ClassGeneratedType(generatedEnumByField));

            memberGeneratedType = new ClassGeneratedType(dbFieldFromMetadataType, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(generatedEnumByField))));

        }else {
            code = "new " + dbFieldFromMetadataType.getSimpleName() + "(" + currEntityGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + PojoGenerator.getFieldDescriptorStaticMemberName(currField.getName());
            memberGeneratedType = new ClassGeneratedType(dbFieldFromMetadataType);
        }

        if (fieldPersistenceNameOrNull != null){
            code += ", \"" + fieldPersistenceNameOrNull + "\"";
        }
        code += ");";


        initializerLine = new GeneratedCodeLine(code, typesInUse);


        c.addMember(new GeneratedClassMember(currField.getName(), memberGeneratedType, GeneratedModifier.PUBLIC, false, false, true, true, initializerLine));
    }

    public static String getDBTableClassName(DataEntityMetadata currEntity) {
        return currEntity.getJavaClassName() + "DBTable";
    }


//    private GeneratedEnum generateTableFieldsEnum(DataEntityMetadata currEntity) {
//        ArrayList<GeneratedEnumValue> enumValues = new ArrayList<GeneratedEnumValue>();
//        enumValues.add(new GeneratedEnumValue("id", 0));
//        int idx = 1;
//        for (AbstractFieldMetadata currField : currEntity.getFields()) {
//            enumValues.add(new GeneratedEnumValue(currField.getPersistentFieldName(), idx));
//            ++idx;
//        }
//        return new GeneratedEnum(getTableFieldsEnumName(currEntity), dbPackage.getPersistentFieldName(), enumValues);
//    }

    private String getTableFieldsEnumName(DataEntityMetadata currEntity) {
        return currEntity.getJavaClassName() + "TableFieldsEnum";
    }

    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        HashSet<Class<? extends IMetadataGenerator>> dependencies = new HashSet<Class<? extends IMetadataGenerator>>();
        dependencies.add(PojoGenerator.class);
        return dependencies;
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
        this.connectionProviderClassName = properties.get(CONNECTION_PROVIDER_PROPERTY_NAME);
        if (connectionProviderClassName == null){
            throw new MetadataGeneratorException("Missing mandatory property " + CONNECTION_PROVIDER_PROPERTY_NAME);
        }else {
            logger.info("Using default connection provider parameter {}", connectionProviderClassName);
        }
    }

    private GeneratedClass generatePersistentEntityAdapter(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass dbTable) throws MetadataGeneratorException {
        String resultSetVariableName = "resultSet";


        GeneratedClass c = new GeneratedClass(currPojo.getName() + "PersistEntityAdapter", getGeneratedPackage().getName());
        c.addImport(DBUtil.class);

        c.setExtendsClass(PersistableObjectAdapter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        GeneratedCodeLine ctorLine = new GeneratedCodeLine("super(dbTable, tableAlias, objectDescriptor);");
        c.addConstructor(GeneratedModifier.PUBLIC,
                Arrays.asList(
                        new GeneratedParameter("dbTable", new ClassGeneratedType(DBTable.class)),
                        new GeneratedParameter("tableAlias", new GeneratedType("String")),
                        new GeneratedParameter("objectDescriptor", new ClassGeneratedType(ShpandrakObjectDescriptor.class))
        ), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(ctorLine));

        addConvertEntityMethod(currEntity, currPojo, dbTable, resultSetVariableName, c);

        //addGenerateKeyMethod(currEntity, currPojo, dbTable, resultSetVariableName, c);

        addPrepareParamsMethod(currEntity, currPojo, dbTable, resultSetVariableName, c);



        return c;
    }

    private void addGenerateKeyMethod(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass dbTable, String resultSetVariableName, GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        String entityVarName = StringUtil.unCapitalize(currPojo.getName());
        lines.add(new GeneratedCodeLine(
                entityVarName + ".setId(UUID.randomUUID());", Arrays.asList(new ClassGeneratedType(UUID.class))));

        GeneratedBody body = new GeneratedBody(lines);
        c.addMethod(new GeneratedMethod("generateKey", GeneratedModifier.PUBLIC, Arrays.asList(new GeneratedParameter(entityVarName, new ClassGeneratedType(currPojo))), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, body));

    }

    private void addConvertEntityMethod(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass dbTable, String resultSetVariableName, GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        String entityVarName = StringUtil.unCapitalize(currPojo.getName());

        String pojoCtorParams = "";
        boolean first = true;
        for (AbstractFieldMetadata currField : currEntity.getFields()) {
            if (first) {
                first = false;
            } else {
                pojoCtorParams += ", ";
            }

            pojoCtorParams += dbTable.getName() + '.' + currField.getName() + ".convert(" + resultSetVariableName + ", tableAlias)";
        }


        for (AbstractRelation currRelation : currEntity.getRelations()) {
            switch (currRelation.getRelationType()){
                case OneToMany:
                case OneToOwner:
                    // Adding code line for initializing OneToMany relationships
                    OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;
                    String memberName = getRelationshipDBTableFieldName(oneToManyRelation);

                    String relationshipIdConversionCode =
                            dbTable.getName() + '.' + memberName + ".convert(" + resultSetVariableName + ", tableAlias)";

                    if (oneToManyRelation.isMandatory()){

                        // If the oneToMany relationship is mandatory - we should populate the related entity id via the entity ctor argument
                        if (currRelation.getRelationType() == RelationType.OneToOwner){
                            // In case the relationship is one to owner - the ctor relationship param will be the first param of the ctor
                            pojoCtorParams = relationshipIdConversionCode + ", " + pojoCtorParams;
                        }else {
                            // in other oneToMany just append relatedEntityId
                            pojoCtorParams += ", " + relationshipIdConversionCode;
                        }
                    }else {

                        // For a non-mandatory relationship, we don't have a ctor argument - therefore calling the setter
                        lines.add(new GeneratedCodeLine(
                                entityVarName + ".set" + PojoGenerator.getOneToManyRelationshipIdFieldName(oneToManyRelation) +
                                        "(" + relationshipIdConversionCode + ");"));
                    }
                    break;
                default:
                    break;
            }
        }


        String decelerationLine = "new " + currPojo.getName() + "(" + pojoCtorParams + ");";
        if (lines.isEmpty()){
            lines.add(new GeneratedCodeLine("return " + decelerationLine));
        }else {

            lines.add(0,
                    new GeneratedCodeLine(
                            currPojo.getName() + ' ' + entityVarName + " = " + decelerationLine
                    ));
            lines.add(new GeneratedCodeLine("return " + entityVarName + ";"));
        }

        GeneratedBody body = new GeneratedBody(lines);


        c.addMethod(
                new GeneratedMethod("convert",
                        GeneratedModifier.PUBLIC,
                        Arrays.asList(
                            new GeneratedParameter(resultSetVariableName, new ClassGeneratedType(ResultSet.class))),
                        new ClassGeneratedType(currPojo),
                        Arrays.asList(new ClassGeneratedType(SQLException.class)),
                        Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, body));
    }

    public static String getRelationshipDBTableFieldName(OneToManyRelation oneToManyRelation) {
        return oneToManyRelation.getName() + "Id";
    }

    private void addPrepareParamsMethod(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass dbTable, String resultSetVariableName, GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        String entityVarName = StringUtil.unCapitalize(currPojo.getName());
        String decelerationLine = "return Arrays.<Object>asList(" + dbTable.getName() + ".id.prepareForPersisting("  + entityVarName + ".getId())";
        for (AbstractFieldMetadata currField : currEntity.getFields()) {
            decelerationLine += ", " + dbTable.getName() + "." + currField.getName() + ".prepareForPersisting(";
            decelerationLine += entityVarName + '.' + PojoCodeGenUtil.generateGetterName(currField.getName(), currField.getType().getJavaNativeTypeName()) + "())";
        }



        for (AbstractRelation currRelation : currEntity.getRelations()) {
            switch (currRelation.getRelationType()){
                case OneToMany:
                case OneToOwner:
                    OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;
                    decelerationLine += ", " + dbTable.getName() + "." + getRelationshipDBTableFieldName(oneToManyRelation) + ".prepareForPersisting(" +
                                        entityVarName + '.' +
                                        PojoCodeGenUtil.generateGetterName(PojoGenerator.getOneToManyRelationshipIdFieldName(oneToManyRelation),
                                        Key.class.getSimpleName()) + "())";
                    break;
                default:
                    break;
            }
        }

        decelerationLine += ");";
        lines.add(
                new GeneratedCodeLine(
                        decelerationLine, Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(dbTable))
                ));

        GeneratedBody body = new GeneratedBody(lines);
        c.addMethod(new GeneratedMethod("prepareForPersisting", GeneratedModifier.PUBLIC, Arrays.asList(new GeneratedParameter(entityVarName, new ClassGeneratedType(currPojo))), new ClassGeneratedType(List.class, new GeneratedGenericsImplementation(Arrays.asList(new GeneratedType("Object")))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, body));
    }

    private Class<? extends DBField> getDBFieldFromMetadataType(FieldType type) throws MetadataGeneratorException {
        switch (type) {
            case STRING:
                return DBStringField.class;
            case BOOLEAN:
                return DBBooleanField.class;
            case DATE:
                return DBDateField.class;
            case ENUM:
                return DBEnumField.class;
            case DOUBLE:
                return DBDoubleField.class;
            case INTEGER:
                return DBIntegerField.class;
            case LONG:
                return DBLongField.class;
            default:
                throw new MetadataGeneratorException("Failed to generate converter for field of type " + type);

        }
    }
}
