package com.shpandrak.codegen;

import com.shpandrak.codegen.model.*;
import com.shpandrak.codegen.util.GeneratedClassPrinter;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.common.util.Pair;
import com.shpandrak.datamodel.*;
import com.shpandrak.datamodel.field.*;
import com.shpandrak.datamodel.relationship.*;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.*;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.EnumFieldDef;
import com.shpandrak.common.model.FieldType;
import com.shpandrak.metadata.model.relation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
 * Date: 10/12/12
 * Time: 00:16
 */
public class PojoGenerator implements IMetadataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PojoGenerator.class);
    private MetadataStore store;
    private Map<String, GeneratedEnum> generatedEnumerations;
    private GeneratedPackage generatedPackage;
    private static final String RELATIONSHIPS_SET_FIELD_NAME = "RELATIONSHIPS";
    public static final String MODULE_LOADER_LOAD_METHOD_NAME = "load";
    private static final String MODULE_LOADER_MODULE_NAME_STATIC_FIELD_NAME = "MODULE_NAME";
    public static final String ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME = "DESCRIPTOR";
    private GeneratedClass moduleLoaderClass;

    @Override
    public void generate(MetadataStore store, GenerationContext generationContext) throws MetadataGeneratorException {
        this.store = store;
        this.generatedEnumerations = new HashMap<String, GeneratedEnum>(store.getEnumerations().size());

        this.generatedPackage = new GeneratedPackage(store.getNamespace() + ".model");

        // Creating the module loader class
        createModuleLoader(store);

        // Converting schema into pojos
        generateClasses();
    }

    private void createModuleLoader(MetadataStore store) {
        String moduleName = StringUtil.capitalize(store.getName());
        moduleLoaderClass = new GeneratedClass(moduleName + "ModuleLoader", generatedPackage.getName());
        moduleLoaderClass.addMember(new GeneratedClassMember(MODULE_LOADER_MODULE_NAME_STATIC_FIELD_NAME, new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + moduleName + "\";")));
        moduleLoaderClass.addLogger();
        String firstLine = "logger.info(\"module {} has been loaded\", " + MODULE_LOADER_MODULE_NAME_STATIC_FIELD_NAME + ");";
        moduleLoaderClass.addImport(BaseEntity.class);
        moduleLoaderClass.addMethod(new GeneratedMethod(MODULE_LOADER_LOAD_METHOD_NAME, GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Collections.<GeneratedAnnotation>emptyList(), true, new GeneratedBody(Arrays.asList(new GeneratedCodeLine(firstLine)))));

        generatedPackage.addClass(moduleLoaderClass);
    }

    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
    }

    @Override
    public void write(String rootDir) throws MetadataGeneratorException {
        for (GeneratedClass currClass : generatedPackage.getClasses()){
            try {
                new GeneratedClassPrinter(currClass, rootDir + "/target/generated-sources/shpangen/").generate();
            } catch (IOException e) {
                throw new MetadataGeneratorException("Failed creating file for class " + currClass.getName(), e);
            }
        }
    }


    public GeneratedPackage getGeneratedPackage() {
        return generatedPackage;
    }



    public void generateClasses() throws MetadataGeneratorException {

        for (EnumMetadata currEnum : store.getEnumerations()) {
            ArrayList<GeneratedEnumValue> values = new ArrayList<GeneratedEnumValue>();
            int i = 0;
            for (EnumEntryMetadata enumEntryMetadata : currEnum.getEntries()) {
                values.add(new GeneratedEnumValue(enumEntryMetadata.getName(), i++));
            }
            GeneratedEnum e = new GeneratedEnum(StringUtil.capitalize(currEnum.getName()), store.getNamespace() + ".model", values);
            generatedEnumerations.put(currEnum.getName(), e);
            generatedPackage.addClass(e);
        }


        // First Pass
        for (DataEntityMetadata currEntity : store.getEntities()){

            // Creating the class
            generatedPackage.addClass(convertClass(currEntity));
        }



        // Second Pass - Inheritance
        for (DataEntityMetadata currEntity : store.getEntities()){

            // Getting the class
            GeneratedClass c = this.generatedPackage.getClass(currEntity.getJavaClassName());
            setClassInheritance(currEntity, c);

        }

        // Third Pass - Constructors
        for (DataEntityMetadata currEntity : store.getEntities()){

            // Getting the class
            GeneratedClass c = this.generatedPackage.getClass(currEntity.getJavaClassName());

            // Creating the constructors
            convertConstructors(currEntity, c);
        }

        // Fourth Pass
        for (DataEntityMetadata currEntity : store.getEntities()){

            // Getting the class
            GeneratedClass c = this.generatedPackage.getClass(currEntity.getJavaClassName());

            // Generating entity descriptor
            GeneratedClass currEntityDescriptor = new GeneratedClass(getEntityDescriptorClassName(currEntity), generatedPackage.getName());
            currEntityDescriptor.setExtendsClass(BaseEntityDescriptor.class);
            //currEntityDescriptor.setExtendsClass(BaseEntityDescriptor.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(c))));
            currEntityDescriptor.addMember(new GeneratedClassMember("NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + currEntity.getName() + "\";")));
            currEntityDescriptor.addMember(new GeneratedClassMember("PLURAL_NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + StringUtil.getPluralForm(currEntity.getName()) + "\";")));

            currEntityDescriptor.addMethod(new GeneratedMethod("getEntityName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return NAME;")))));
            currEntityDescriptor.addMethod(new GeneratedMethod("getEntityPluralName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return PLURAL_NAME;")))));
            currEntityDescriptor.addMethod(new GeneratedMethod("getEntityClass", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(c)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + c.getName() + ".class;")))));
            String idDescriptorFieldName = getFieldDescriptorStaticMemberName("id");
            String descriptorsFieldsListString = idDescriptorFieldName;
            currEntityDescriptor.addMember(new GeneratedClassMember(idDescriptorFieldName, new ClassGeneratedType(EntityKeyFieldDescriptor.class), GeneratedModifier.PUBLIC, false, false, false, true, new GeneratedCodeLine("new " + EntityKeyFieldDescriptor.class.getSimpleName() + "(\"id\");")));

            for (AbstractFieldMetadata currField : currEntity.getFields()){
                String fieldDescriptorStaticMemberName = addDescriptorFieldMember(currEntityDescriptor, currField);
                descriptorsFieldsListString += ", " + fieldDescriptorStaticMemberName;
            }

            String ownerObjectRelationshipDescriptor = "null";

            List<ClassGeneratedType> typesInUse = new ArrayList<ClassGeneratedType>(Arrays.asList(new ClassGeneratedType(FieldDescriptor.class), new ClassGeneratedType(Arrays.class)));
            for (AbstractRelation currRelation : currEntity.getRelations()) {
                if (currRelation.getRelationType() == RelationType.OneToOwner){
                    ownerObjectRelationshipDescriptor = getRelationshipDescriptorStaticFieldName(currRelation);
                }
            }

            GeneratedCodeLine initializeLine = new GeneratedCodeLine("initialize(Arrays.<" + FieldDescriptor.class.getSimpleName() + ">asList(" +  descriptorsFieldsListString + "), " + idDescriptorFieldName + "," + RELATIONSHIPS_SET_FIELD_NAME + ", " + ownerObjectRelationshipDescriptor + ");", typesInUse);
            GeneratedCodeLine line = new GeneratedCodeLine(EntityDescriptorFactory.class.getSimpleName() + ".register(getEntityClass(), this);", Arrays.asList(new ClassGeneratedType(EntityDescriptorFactory.class)));
            GeneratedBody ctorBody = new GeneratedBody(Arrays.asList(initializeLine, line));


            currEntityDescriptor.addConstructor(null, Collections.<GeneratedParameter>emptyList(), Collections.<ClassGeneratedType>emptyList(), ctorBody);


            c.addMember(new GeneratedClassMember(ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME, new ClassGeneratedType(currEntityDescriptor), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + currEntityDescriptor.getName() + "();")));
            c.addMethod(new GeneratedMethod("getEntityDescriptor", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(BaseEntityDescriptor.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME  + ";")))));

            // Converting the members
            convertMembers(c, currEntityDescriptor, currEntity.getFields());

            generatedPackage.addClass(currEntityDescriptor);
        }

        // Fifth freaking Pass
        for (DataEntityMetadata currEntity : store.getEntities()){

            // Getting the class
            GeneratedClass c = this.generatedPackage.getClass(currEntity.getJavaClassName());

            // Generating entity descriptor
            GeneratedClass currEntityDescriptor = getGeneratedEntityDescriptorClass(currEntity);

            // Parsing relationships
            parseRelationships(currEntity, c, currEntityDescriptor);


        }


    }

    private void setClassInheritance(DataEntityMetadata currEntity, GeneratedClass c) {

        if (currEntity.getExtendsEntity() == null){
            c.setExtendsClassType(new ClassGeneratedType(BaseEntity.class));
        }else {
            DataEntityMetadata baseEntityMetadata = store.getEntityMetadataByName(currEntity.getExtendsEntity());
            if (baseEntityMetadata == null){
                throw new IllegalArgumentException("Invalid base entity: " + currEntity.getExtendsEntity() + " for entity " + currEntity.getName());
            }
            GeneratedClass baseEntityGeneratedClass = getGeneratedPojo(baseEntityMetadata);
            c.setExtendsClassType(new ClassGeneratedType(baseEntityGeneratedClass));
        }

    }

    private String addDescriptorFieldMember(GeneratedClass currEntityDescriptor, AbstractFieldMetadata currField) throws MetadataGeneratorException {
        String fieldDescriptorStaticMemberName = getFieldDescriptorStaticMemberName(currField.getName());
        ClassGeneratedType fieldDescriptorType = convertFieldToFieldDescriptorType(currField);
        GeneratedCodeLine initializer;
        if (currField.getType() == FieldType.ENUM){
            EnumFieldDef enumField = (EnumFieldDef)currField;
            ClassGeneratedType enumType = new ClassGeneratedType(getGeneratedEnumByField(enumField));
            initializer = new GeneratedCodeLine("new " + fieldDescriptorType.getType() + "<" + enumType.getType() + ">(" + enumType.getType() +".class, " + enumType.getType() + ".values(), \"" + currField.getName() + "\");");
        }else {
            String code = "new " + fieldDescriptorType.getType() + "(\"" + currField.getName() + "\"";
            if (currField.isKey()){
                code += ", true";
            }
            code += ");";

            initializer = new GeneratedCodeLine(code);
        }
        currEntityDescriptor.addMember(new GeneratedClassMember(fieldDescriptorStaticMemberName, fieldDescriptorType, GeneratedModifier.PUBLIC, false, false, false, true, initializer));
        return fieldDescriptorStaticMemberName;
    }

    public static String getFieldDescriptorStaticMemberName(String fieldName) {
        return fieldName + "FieldDescriptor";
    }

    public static String getEntityDescriptorClassName(DataEntityMetadata currEntity) {
        return currEntity.getJavaClassName() + "EntityDescriptor";
    }


    private void convertConstructors(DataEntityMetadata currEntity, GeneratedClass c) throws MetadataGeneratorException {
        String fieldsListString = "this.id";
        List<Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>>> paramsPermutations = new ArrayList<Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>>>();

        // Creating the first permutation
        Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>> firstPermutation = Pair.of(new ArrayList<GeneratedParameter>(), new ArrayList<GeneratedCodeLine>());
        paramsPermutations.add(firstPermutation);

        List<GeneratedCodeLine> ctorLines = new ArrayList<GeneratedCodeLine>();
        ctorLines.add(new GeneratedCodeLine("this();"));

        for (AbstractFieldMetadata currField : currEntity.getFields()){
            firstPermutation.getA().add(new GeneratedParameter(currField.getName(), convertFieldToType(currField)));
            ctorLines.add(new GeneratedCodeLine("set" + StringUtil.capitalize(currField.getName()) + "(" + currField.getName() + ");"));
            fieldsListString += ", this." + currField.getName();
        }

        for (AbstractRelation currRelation : currEntity.getRelations()) {
            switch (currRelation.getRelationType()){
                case OneToMany:
                case OneToOwner:
                    OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;
                    String memberName = getOneToManyRelationshipIdFieldName(oneToManyRelation);


                    if (oneToManyRelation.isMandatory()){
                        String relationshipIdParamName = oneToManyRelation.getName() + "Id";
                        String relationshipEntityParamName = oneToManyRelation.getName();
                        // Adding one permutation for id and one for actual entity


                        List<Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>>> newPermutations = new ArrayList<Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>>>();
                        for (Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>> currPermutation : paramsPermutations){

                            Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>> entityObjectPermutation = Pair.of(new ArrayList<GeneratedParameter>(currPermutation.getA()), new ArrayList<GeneratedCodeLine>(currPermutation.getB()));
                            newPermutations.add(entityObjectPermutation);

                            // Adding entity Id param permutation
                            GeneratedParameter generatedIdParam = new GeneratedParameter(relationshipIdParamName, new ClassGeneratedType(Key.class));

                            // In case this relationship represents owner relationship - the new parameter should be first
                            if (oneToManyRelation.getRelationType() == RelationType.OneToOwner){
                                currPermutation.getA().add(0, generatedIdParam);
                            }else {
                                currPermutation.getA().add(generatedIdParam);
                            }
                            currPermutation.getB().add(new GeneratedCodeLine("set" + memberName + "(" + relationshipIdParamName + ");"));


                            // Adding entity Object param permutation
                            GeneratedParameter generatedParameter = new GeneratedParameter(relationshipEntityParamName, new ClassGeneratedType(getGeneratedPojo(store.getEntityMetadataByName(currRelation.getRelatedEntity()))));

                            // In case this relationship represents owner relationship - the new parameter should be first
                            if (oneToManyRelation.getRelationType() == RelationType.OneToOwner){
                                entityObjectPermutation.getA().add(0, generatedParameter);
                            }else {
                                entityObjectPermutation.getA().add(generatedParameter);
                            }

                            entityObjectPermutation.getB().add(new GeneratedCodeLine(getRelationshipGetterName(currRelation) + "().setTargetEntity(" + relationshipEntityParamName + ");"));
                        }
                        paramsPermutations.addAll(newPermutations);

                    }else {
                        ctorLines.add(new GeneratedCodeLine("set" + memberName + "(null);"));
                    }
                    break;
                default:
                    break;
            }
        }


        for (Pair<ArrayList<GeneratedParameter>, ArrayList<GeneratedCodeLine>> currParamsPermutation : paramsPermutations){
            GeneratedBody body = new GeneratedBody(ctorLines);
            body.addLines(currParamsPermutation.getB());
            List<GeneratedParameter> currCTorParameters = currParamsPermutation.getA();

            // Avoid creating no-args ctor - we're covering that separately by always creating default ctor
            if (!currCTorParameters.isEmpty()){
                c.addConstructor(GeneratedModifier.PUBLIC, currCTorParameters, Collections.<ClassGeneratedType>emptyList(), body);
            }
        }


        c.addDefaultConstructor(GeneratedModifier.PUBLIC, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("initialize(this.id, " + Arrays.class.getSimpleName() + ".<" + FieldInstance.class.getSimpleName() + ">asList(" + fieldsListString + "));",Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldInstance.class))))));

    }

    private GeneratedClass convertClass(DataEntityMetadata currEntity) {
        GeneratedClass generatedClass = new GeneratedClass(currEntity.getJavaClassName(), store.getNamespace() + ".model");

        // Loading the class (Module loader)
        //generatedClass.addStaticCodeBlock(new GeneratedBody(Arrays.asList(new GeneratedCodeLine(moduleLoaderClass.getName() + "." + MODULE_LOADER_LOAD_METHOD_NAME + "(" + generatedClass.getName() + ".class);" , Arrays.asList(new ClassGeneratedType(moduleLoaderClass))))));

        return generatedClass;
    }

    private void convertMembers(GeneratedClass c, GeneratedClass currEntityDescriptor, List<AbstractFieldMetadata> fields) throws MetadataGeneratorException {
        // Always adding an Id member
        GeneratedClassMember member = new GeneratedClassMember("id", new ClassGeneratedType(KeyField.class), GeneratedModifier.PRIVATE, false, false, false, false, new GeneratedCodeLine("new " + KeyField.class.getSimpleName() + "(" + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." +  getFieldDescriptorStaticMemberName("id") + ");"));
        c.addMember(member);

        // For every attribute
        for (AbstractFieldMetadata currField : fields){
            addShpandrakFieldMemberWithGetterSetter(c, currField, currEntityDescriptor);
        }
    }

    private void addShpandrakFieldMemberWithGetterSetter(GeneratedClass c, AbstractFieldMetadata currField, GeneratedClass currEntityDescriptor) throws MetadataGeneratorException {

        ClassGeneratedType memberType;

        switch (currField.getType()){
            case ENUM:
                EnumFieldDef enumField = (EnumFieldDef)currField;
                GeneratedEnum generatedEnum = generatedEnumerations.get(enumField.getEnumName());
                if (generatedEnum == null){
                    throw new MetadataGeneratorException("Invalid enum type for field " + enumField.getName()  +": " + enumField.getEnumName());
                }
                memberType = new ClassGeneratedType(com.shpandrak.datamodel.field.EnumField.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(generatedEnum))));
                break;
            case DATE:
                memberType = new ClassGeneratedType(DateField.class);
                break;
            case STRING:
                memberType = new ClassGeneratedType(StringField.class);
                break;
            case BOOLEAN:
                memberType = new ClassGeneratedType(BooleanField.class);
                break;
            case INTEGER:
                memberType = new ClassGeneratedType(IntegerField.class);
                break;
            case LONG:
                memberType = new ClassGeneratedType(LongField.class);
                break;
            case DOUBLE:
                memberType = new ClassGeneratedType(DoubleField.class);
                break;
            default:
                throw new MetadataGeneratorException("Unsupported field type " + currField.getName()  +": " + currField.getType());
        }
        String fieldDescriptorStaticMemberName = getFieldDescriptorStaticMemberName(currField.getName());
        GeneratedCodeLine initializer;

        switch (currField.getType()){
            case ENUM:
                EnumFieldDef enumField = (EnumFieldDef)currField;
                ClassGeneratedType enumType = new ClassGeneratedType(getGeneratedEnumByField(enumField));
                initializer = new GeneratedCodeLine("new " + memberType.getType() + "<" + enumType.getType() + ">(" + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + fieldDescriptorStaticMemberName +");", Arrays.asList(new ClassGeneratedType(currEntityDescriptor)));
                break;
            default:
                initializer = new GeneratedCodeLine("new " + memberType.getType() +"(" + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + fieldDescriptorStaticMemberName +");", Arrays.asList(new ClassGeneratedType(currEntityDescriptor)));
                break;
        }
        GeneratedClassMember member = new GeneratedClassMember(currField.getName(), memberType, GeneratedModifier.PRIVATE, false, false, false, false, initializer);
        c.addMember(member);
        GeneratedType fieldActualType = convertFieldToType(currField);
        String getterName = GeneratedClassPrinter.generateGetterName(member.getName(), fieldActualType.getType());

        c.addMethod(new GeneratedMethod(getterName, GeneratedModifier.PUBLIC,
                Collections.<GeneratedParameter>emptyList(),
                fieldActualType,
                Collections.<ClassGeneratedType>emptyList(),
                Collections.<GeneratedAnnotation>emptyList(),
                false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + member.getName() + ".getValue();")))));

        String setterName = "set" + member.getName().substring(0, 1).toUpperCase() + member.getName().substring(1);
        c.addMethod(new GeneratedMethod(setterName,
                GeneratedModifier.PUBLIC,
                Arrays.asList(new GeneratedParameter(member.getName(), fieldActualType)),
                new GeneratedType("void"),
                Collections.<ClassGeneratedType>emptyList(),
                Collections.<GeneratedAnnotation>emptyList(),
                false, new GeneratedBody(Arrays.asList(
                new GeneratedCodeLine("this." + member.getName() + ".setValue(" + member.getName() + ");")))));


    }

    private void parseRelationships(DataEntityMetadata currEntity, GeneratedClass c, GeneratedClass entityDescriptorClass) throws MetadataGeneratorException {
        List<ClassGeneratedType> typesInUserForRelationshipInitialization = new ArrayList<ClassGeneratedType>();
        String relationshipsInitializer;
        if (currEntity.getRelations().isEmpty()){
            relationshipsInitializer = "Collections.<" + EntityRelationshipDefinition.class.getSimpleName() + ">emptySet();";
            typesInUserForRelationshipInitialization.add(new ClassGeneratedType(Collections.class));
        }else {
            relationshipsInitializer = "new " + HashSet.class.getSimpleName() + "<" + EntityRelationshipDefinition.class.getSimpleName() + ">(Arrays.<" + EntityRelationshipDefinition.class.getSimpleName() + ">asList(";
            typesInUserForRelationshipInitialization.add(new ClassGeneratedType(Arrays.class));
            typesInUserForRelationshipInitialization.add(new ClassGeneratedType(HashSet.class));

            boolean firstRelation = true;
            for (AbstractRelation currRelation : currEntity.getRelations()) {
                String relatedEntityClassName = PojoCodeGenUtil.generateJavaClassName(currRelation.getRelatedEntity());
                DataEntityMetadata targetEntityMetadata = store.getEntityMetadataByName(currRelation.getRelatedEntity());
                GeneratedClass targetEntityGeneratedClass = getGeneratedPojo(targetEntityMetadata);
                String relationshipMemberName = getRelationshipDescriptorStaticFieldName(currRelation);
                String relationshipEntryEntityDescriptorClassName = getRelationshipEntryEntityDescriptorClassName(currEntity, currRelation);
                String relationshipEntryClassName = getRelationshipEntryClassName(currEntity, currRelation);
                String relationshipGetterName = getRelationshipGetterName(currRelation);
                EntityRelationshipType entityRelationshipType;
                ClassGeneratedType relatedEntityGeneratedClass = new ClassGeneratedType(generatedPackage.getClass(relatedEntityClassName));

                Class<? extends EntityRelationship> relationshipClass;
                // Generating relationship entry class
                GeneratedClass generatedRelationshipEntry = new GeneratedClass(relationshipEntryClassName, generatedPackage.getName());


                String fieldsListString;
                ArrayList<GeneratedParameter> params = new ArrayList<GeneratedParameter>(currEntity.getFields().size());
                List<GeneratedCodeLine> ctorLinesWithTargetEntityId = new ArrayList<GeneratedCodeLine>();
                ArrayList<GeneratedCodeLine> ctorLinesWithTargetEntity = new ArrayList<GeneratedCodeLine>();

                ctorLinesWithTargetEntityId.add(new GeneratedCodeLine("this();"));
                ctorLinesWithTargetEntityId.add(new GeneratedCodeLine("setTargetEntityId(targetEntityId);"));

                ctorLinesWithTargetEntity.add(new GeneratedCodeLine("this();"));
                ctorLinesWithTargetEntity.add(new GeneratedCodeLine("setTargetEntity(targetEntity);"));



                List<GeneratedParameter> ctorParamsWithTargetEntity = new ArrayList<GeneratedParameter>();
                ctorParamsWithTargetEntity.add(new GeneratedParameter("targetEntity", relatedEntityGeneratedClass));

                List<GeneratedParameter> ctorParamsWithTargetEntityId = new ArrayList<GeneratedParameter>();
                ctorParamsWithTargetEntityId.add(new GeneratedParameter("targetEntityId", new ClassGeneratedType(Key.class)));

                generatedPackage.addClass(generatedRelationshipEntry);


                // Generating relationship entry class descriptor
                GeneratedClass generatedRelationshipEntryEntityDescriptor = new GeneratedClass(relationshipEntryEntityDescriptorClassName, generatedPackage.getName());
                generatedRelationshipEntry.addMember(new GeneratedClassMember(ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME, new ClassGeneratedType(generatedRelationshipEntryEntityDescriptor), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("new " + generatedRelationshipEntryEntityDescriptor.getName() + "();")));
                generatedRelationshipEntry.addMethod(new GeneratedMethod("getObjectDescriptor", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(generatedRelationshipEntryEntityDescriptor), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ";")))));

                String relationshipEntryName = currRelation.getRelationshipName() + "RelationshipEntry";
                String relationshipEntryPluralName = currRelation.getRelationshipName() + "RelationshipEntries";
                generatedRelationshipEntryEntityDescriptor.addMember(new GeneratedClassMember("NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + relationshipEntryName + "\";")));
                generatedRelationshipEntryEntityDescriptor.addMember(new GeneratedClassMember("PLURAL_NAME", new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + relationshipEntryPluralName + "\";")));

                generatedRelationshipEntryEntityDescriptor.addMethod(new GeneratedMethod("getEntityName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return NAME;")))));
                generatedRelationshipEntryEntityDescriptor.addMethod(new GeneratedMethod("getEntityPluralName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return PLURAL_NAME;")))));
                generatedRelationshipEntryEntityDescriptor.addMethod(new GeneratedMethod("getEntityClass", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(generatedRelationshipEntry)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + generatedRelationshipEntry.getName() + ".class;")))));


                String currEntityFieldDescriptorMemberName = getFieldDescriptorStaticMemberName(generateEntityIdFieldName(currEntity));
                generatedRelationshipEntryEntityDescriptor.addMember(new GeneratedClassMember(currEntityFieldDescriptorMemberName, new ClassGeneratedType(EntityKeyFieldDescriptor.class), GeneratedModifier.PUBLIC, false, false, false, true, new GeneratedCodeLine("new " + EntityKeyFieldDescriptor.class.getSimpleName() + "(\"" + generateEntityIdFieldName(currEntity) + "\");")));
                String targetEntityFieldDescriptorMemberName = getFieldDescriptorStaticMemberName("related" + StringUtil.capitalize(currRelation.getRelatedEntity()) + "Id");
                generatedRelationshipEntryEntityDescriptor.addMember(new GeneratedClassMember(targetEntityFieldDescriptorMemberName, new ClassGeneratedType(EntityKeyFieldDescriptor.class), GeneratedModifier.PUBLIC, false, false, false, true, new GeneratedCodeLine("new " + EntityKeyFieldDescriptor.class.getSimpleName() + "(\"" + getRelationshipIdFieldName(currRelation) + "\");")));

                generatedRelationshipEntryEntityDescriptor.addInterface(IRelationshipEntryFieldsDescriptor.class);
                generatedRelationshipEntryEntityDescriptor.addMethod(new GeneratedMethod("getSourceEntityFieldDescriptor", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(EntityKeyFieldDescriptor.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + currEntityFieldDescriptorMemberName + ";")))));
                generatedRelationshipEntryEntityDescriptor.addMethod(new GeneratedMethod("getTargetEntityFieldDescriptor", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(EntityKeyFieldDescriptor.class), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + targetEntityFieldDescriptorMemberName + ";")))));



                String currEntityFieldInstanceMemberName = generateEntityIdFieldName(currEntity);
                generatedRelationshipEntry.addMember(
                        new GeneratedClassMember(
                                currEntityFieldInstanceMemberName,
                                new ClassGeneratedType(KeyField.class),
                                GeneratedModifier.PUBLIC,
                                false,
                                false,
                                false,
                                true,
                                new GeneratedCodeLine("new " + KeyField.class.getSimpleName() + "(" + generatedRelationshipEntry.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + currEntityFieldDescriptorMemberName + ");")));

                String targetEntityFieldMemberName = "related" + StringUtil.capitalize(targetEntityMetadata.getName()) + "Id";
                generatedRelationshipEntry.addMember(
                        new GeneratedClassMember(
                                targetEntityFieldMemberName,
                                new ClassGeneratedType(KeyField.class),
                                GeneratedModifier.PUBLIC,
                                false,
                                false,
                                false,
                                true,
                                new GeneratedCodeLine("new " + KeyField.class.getSimpleName() + "(" + generatedRelationshipEntry.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + targetEntityFieldDescriptorMemberName + ");")));
                boolean isRelationshipEntryPersistent;


                String reverseRelationshipInitializer = null;
                switch (currRelation.getRelationType()){
                    case OneToOwner:
                    case OneToMany:
                        isRelationshipEntryPersistent = false;

                        OneToManyRelation oneToManyRelation = (OneToManyRelation)currRelation;

                        generatedRelationshipEntry.setExtendsClass(RelationshipEntry.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(relatedEntityGeneratedClass)));

                        entityRelationshipType = EntityRelationshipType.ONE_TO_MANY;
                        relationshipClass = EntityOneToManyRelationship.class;
                        fieldsListString = currEntityFieldDescriptorMemberName + "," + targetEntityFieldDescriptorMemberName;

                        // Adding the Key field to the entity - with OneToManyRelation the relationship is by default loaded with id level at minimum
                        // So we can create a safe getter
                        String memberName = getOneToManyRelationshipIdFieldName(oneToManyRelation);
                        GeneratedCodeLine getterCodeLine = new GeneratedCodeLine("return " + relationshipGetterName + "().getTargetEntityId();");
                        GeneratedCodeLine setterCodeLine = new GeneratedCodeLine(relationshipGetterName + "().setTargetEntityId(id);");
                        c.addMethod(new GeneratedMethod("get" + memberName, GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Key.class), Collections.<ClassGeneratedType>emptyList(), Collections.<GeneratedAnnotation>emptyList(), false, new GeneratedBody(Arrays.asList(getterCodeLine))));
                        c.addMethod(new GeneratedMethod("set" + memberName, GeneratedModifier.PUBLIC, Arrays.asList(new GeneratedParameter("id", new ClassGeneratedType(Key.class))), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Collections.<GeneratedAnnotation>emptyList(), false, new GeneratedBody(Arrays.asList(setterCodeLine))));

                        String myFieldsList = "this." + currEntityFieldInstanceMemberName + ", this." + targetEntityFieldMemberName;
                        GeneratedCodeLine initializerCtorLine = new GeneratedCodeLine(
                                "initialize(" + myFieldsList + ", " + Arrays.class.getSimpleName() + ".<" + FieldInstance.class.getSimpleName() + ">asList(" + myFieldsList + "));",
                                Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldInstance.class)));
                        generatedRelationshipEntry.addDefaultConstructor(GeneratedModifier.PUBLIC,
                                new GeneratedBody(
                                        initializerCtorLine));

                        generatedRelationshipEntry.addConstructor(GeneratedModifier.PUBLIC,
                                Arrays.asList(
                                        new GeneratedParameter(currEntityFieldInstanceMemberName,new ClassGeneratedType(Key.class)),
                                        new GeneratedParameter(targetEntityFieldMemberName, new ClassGeneratedType(Key.class))),
                                Collections.<ClassGeneratedType>emptyList(),
                        new GeneratedBody(Arrays.asList(
                                initializerCtorLine,
                                new GeneratedCodeLine("setSourceEntityId(" + currEntityFieldInstanceMemberName +");"),
                                new GeneratedCodeLine("setTargetEntityId(" + targetEntityFieldMemberName +");")
                                )));

                        break;
                    case ManyToMany:
                        isRelationshipEntryPersistent = true;

                        generatedRelationshipEntry.setExtendsClass(BasePersistableRelationshipEntry.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(relatedEntityGeneratedClass)));

                        String idFieldDescriptorMemberName = getFieldDescriptorStaticMemberName("id");
                        generatedRelationshipEntryEntityDescriptor.addMember(new GeneratedClassMember(idFieldDescriptorMemberName, new ClassGeneratedType(EntityKeyFieldDescriptor.class), GeneratedModifier.PUBLIC, false, false, false, true, new GeneratedCodeLine("new " + EntityKeyFieldDescriptor.class.getSimpleName() + "(\"id\");")));
                        fieldsListString = idFieldDescriptorMemberName + "," + currEntityFieldDescriptorMemberName + "," + targetEntityFieldDescriptorMemberName;

                        entityRelationshipType = EntityRelationshipType.MANY_TO_MANY;
                        relationshipClass = EntityManyToManyRelationship.class;
                        ManyToManyRelation manyToManyRelation = (ManyToManyRelation)currRelation;

                        // Adding additional members if exist
                        convertMembers(generatedRelationshipEntry, generatedRelationshipEntryEntityDescriptor, currRelation.getFields());

                        String staticFieldsList = "this.id, this." + currEntityFieldInstanceMemberName + ", this." + targetEntityFieldMemberName;
                        String additionalFieldsListString = "this.id, this." + currEntityFieldInstanceMemberName + ", this." + targetEntityFieldMemberName;

                        for (AbstractFieldMetadata currField : currRelation.getFields()){
                            ctorParamsWithTargetEntity.add(new GeneratedParameter(currField.getName(), convertFieldToType(currField)));
                            ctorParamsWithTargetEntityId.add(new GeneratedParameter(currField.getName(), convertFieldToType(currField)));
                            additionalFieldsListString += ", this." + currField.getName();
                            ctorLinesWithTargetEntity.add(new GeneratedCodeLine("set" + StringUtil.capitalize(currField.getName()) + "(" + currField.getName() + ");"));
                            ctorLinesWithTargetEntityId.add(new GeneratedCodeLine("set" + StringUtil.capitalize(currField.getName()) + "(" + currField.getName() + ");"));
                        }

                        ctorParamsWithTargetEntityId.addAll(params);
                        ctorParamsWithTargetEntity.addAll(params);


                        generatedRelationshipEntry.addDefaultConstructor(GeneratedModifier.PUBLIC, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("initialize(" + staticFieldsList + ", " + Arrays.class.getSimpleName() + ".<" + FieldInstance.class.getSimpleName() + ">asList(" + additionalFieldsListString + "));",Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldInstance.class))))));


                        // Ctor with target entity
                        generatedRelationshipEntry.addConstructor(GeneratedModifier.PUBLIC, ctorParamsWithTargetEntity, Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(ctorLinesWithTargetEntity));

                        // Ctor with target entity Id
                        generatedRelationshipEntry.addConstructor(GeneratedModifier.PUBLIC, ctorParamsWithTargetEntityId, Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(ctorLinesWithTargetEntityId));


                        break;
                    case ManyToOne:
                        isRelationshipEntryPersistent = false;
                        generatedRelationshipEntry.setExtendsClass(RelationshipEntry.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(relatedEntityGeneratedClass)));

                        fieldsListString = currEntityFieldDescriptorMemberName + "," + targetEntityFieldDescriptorMemberName;

                        entityRelationshipType = EntityRelationshipType.MANY_TO_ONE;
                        relationshipClass = EntityManyToOneRelationship.class;
                        ManyToOneRelation manyToOneRelation = (ManyToOneRelation)currRelation;

                        // Adding additional members if exist
                        //todo: currently not supporting this
                        //convertMembers(generatedRelationshipEntry, generatedRelationshipEntryEntityDescriptor, currRelation.getFields());

                        String fieldsList = "this." + currEntityFieldInstanceMemberName + ", this." + targetEntityFieldMemberName;

                        ctorParamsWithTargetEntityId.addAll(params);
                        ctorParamsWithTargetEntity.addAll(params);


                        OneToManyRelation reverseRelationship = manyToOneRelation.getOneToManyRelation();
                        if (reverseRelationship != null){
                            typesInUserForRelationshipInitialization.add(new ClassGeneratedType(targetEntityGeneratedClass));
                            reverseRelationshipInitializer =
                                    targetEntityGeneratedClass.getName() + "." +
                                    ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." +
                                    getRelationshipDescriptorStaticFieldName(reverseRelationship);

                        }

                        generatedRelationshipEntry.addDefaultConstructor(
                                GeneratedModifier.PUBLIC,
                                new GeneratedBody(
                                        new GeneratedCodeLine(
                                                "initialize(" + fieldsList+ ", " + Arrays.class.getSimpleName() + ".<" + FieldInstance.class.getSimpleName() + ">asList(" + fieldsList + "));",
                                                Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldInstance.class)))));


                        // Ctor with target entity
                        generatedRelationshipEntry.addConstructor(GeneratedModifier.PUBLIC, ctorParamsWithTargetEntity, Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(ctorLinesWithTargetEntity));

                        // Ctor with target entity Id
                        generatedRelationshipEntry.addConstructor(GeneratedModifier.PUBLIC, ctorParamsWithTargetEntityId, Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(ctorLinesWithTargetEntityId));



                        break;

                    default:
                        throw new IllegalArgumentException("Unsupported relationship type " + currRelation.getRelationType());
                }


                List<AbstractFieldMetadata> relationshipAdditionalFields = currRelation.getFields();
                if (relationshipAdditionalFields != null && !relationshipAdditionalFields.isEmpty()){
                    for (AbstractFieldMetadata currField : relationshipAdditionalFields){
                        String fieldDescriptorStaticMemberName = addDescriptorFieldMember(generatedRelationshipEntryEntityDescriptor, currField);
                        fieldsListString += ", " + fieldDescriptorStaticMemberName;
                    }
                }

                GeneratedCodeLine descriptorInitializerLine;
                if (isRelationshipEntryPersistent){
                    //generatedRelationshipEntryEntityDescriptor.setExtendsClass(BasePersistentObjectDescriptor.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(generatedRelationshipEntry))));
                    generatedRelationshipEntryEntityDescriptor.setExtendsClass(BasePersistentObjectDescriptor.class);
                    descriptorInitializerLine = new GeneratedCodeLine("initialize(" + Arrays.class.getSimpleName() + ".<" + FieldDescriptor.class.getSimpleName() + ">asList(" + fieldsListString + "), " + getFieldDescriptorStaticMemberName("id") + ");", Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldDescriptor.class)));
                }else {
                    //generatedRelationshipEntryEntityDescriptor.setExtendsClass(ShpandrakObjectDescriptor.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(generatedRelationshipEntry))));
                    generatedRelationshipEntryEntityDescriptor.setExtendsClass(ShpandrakObjectDescriptor.class);
                    descriptorInitializerLine = new GeneratedCodeLine("initialize(" + Arrays.class.getSimpleName() + ".<" + FieldDescriptor.class.getSimpleName() + ">asList(" + fieldsListString + "));", Arrays.asList(new ClassGeneratedType(Arrays.class), new ClassGeneratedType(FieldDescriptor.class)));
                }

                generatedRelationshipEntryEntityDescriptor.addConstructor(GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(Arrays.asList(descriptorInitializerLine)));
                generatedPackage.addClass(generatedRelationshipEntryEntityDescriptor);




                // Adding relationship getter
                c.addMethod(
                        new GeneratedMethod(relationshipGetterName,
                                GeneratedModifier.PUBLIC,
                                Collections.<GeneratedParameter>emptyList(),
                                new ClassGeneratedType(relationshipClass, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(c), relatedEntityGeneratedClass, new ClassGeneratedType(generatedRelationshipEntry)))),
                                Collections.<ClassGeneratedType>emptyList(),
                                Collections.<GeneratedAnnotation>emptyList(),
                                false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return (" + relationshipClass.getSimpleName() + "<" + c.getName() + ", " + relatedEntityClassName + ", " + generatedRelationshipEntry.getName() +">)getRelationship(" + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + "." + relationshipMemberName + ");")))
                        ));


                typesInUserForRelationshipInitialization.add(new ClassGeneratedType(EntityRelationshipType.class));

                // Relationship sort parsing
                String relationshipSortParam = "null";
                if (currRelation.getRelationshipSort() != null && !currRelation.getRelationshipSort().isEmpty()){
                    typesInUserForRelationshipInitialization.add(new ClassGeneratedType(OrderByClauseEntry.class));
                    typesInUserForRelationshipInitialization.add(new ClassGeneratedType(Arrays.class));
                    relationshipSortParam = "Arrays.<" + OrderByClauseEntry.class.getSimpleName()+ ">asList(";
                    boolean first = true;
                    for (SortClauseEntryDef currRelSort : currRelation.getRelationshipSort()){
                        if (first){
                            first = false;
                        }else {
                            relationshipSortParam += ", ";
                        }
                        relationshipSortParam += "new " + OrderByClauseEntry.class.getSimpleName() + "(\"" + currRelSort.getField() + "\", " + (currRelSort.isAscending()? "true" : "false") + ")";
                    }

                    relationshipSortParam += ")";
                }


                // Adding the relationship descriptor static member
                String code = "new " + EntityRelationshipDefinition.class.getSimpleName() +
                        "<" + c.getName() + ", " + relatedEntityClassName + ", " + generatedRelationshipEntry.getName() + ">(\"" +
                        currRelation.getRelationshipName() + "\", " +
                        EntityRelationshipType.class.getSimpleName() + "." + entityRelationshipType.name() + ", " +
                        c.getName() + ".class, " + relatedEntityClassName + ".class, " +
                        Boolean.toString(currRelation.isMandatory()) + "," +
                        relationshipEntryClassName + "." + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ", " +
                        relationshipSortParam;

                if (reverseRelationshipInitializer != null){
                    code += ", " + reverseRelationshipInitializer;
                }

                code += ");";

                GeneratedClassMember currRelationshipMember = new GeneratedClassMember(
                        relationshipMemberName,
                        new ClassGeneratedType(EntityRelationshipDefinition.class,
                                new GeneratedGenericsImplementation(
                                        Arrays.<GeneratedType>asList(
                                                new ClassGeneratedType(c),
                                                relatedEntityGeneratedClass,
                                                new ClassGeneratedType(generatedRelationshipEntry))
                                )),
                        GeneratedModifier.PUBLIC,
                        false,
                        false,
                        false,
                        true,
                        new GeneratedCodeLine(
                                code)
                );
                entityDescriptorClass.addMember(currRelationshipMember);


                if (firstRelation){
                    firstRelation = false;
                }else {
                    relationshipsInitializer += ", ";
                }
                relationshipsInitializer += relationshipMemberName;
            }

            relationshipsInitializer += "));";
        }

        c.addMethod(new GeneratedMethod("getRelationshipDescriptors", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Map.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new GeneratedType("String"), new ClassGeneratedType(EntityRelationshipDefinition.class)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ".getRelationshipDefinitionMap();")))));

        // Add relationships set static member
        entityDescriptorClass.addMember(new GeneratedClassMember(
                RELATIONSHIPS_SET_FIELD_NAME,
                new ClassGeneratedType(Set.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(EntityRelationshipDefinition.class)))),
                GeneratedModifier.PRIVATE, false, false, false, true, new GeneratedCodeLine(relationshipsInitializer, typesInUserForRelationshipInitialization)));

        //entityDescriptorClass.addMethod(new GeneratedMethod("getRelationshipDescriptors", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Set.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(EntityRelationshipDefinition.class)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + RELATIONSHIPS_SET_FIELD_NAME + ";")))));
        registerEntityDescriptorInModuleLoader(entityDescriptorClass , c);

    }

    public static String getRelationshipGetterName(AbstractRelation currRelation) {
        return "get" + StringUtil.capitalize(currRelation.getRelationshipName()) + "Relationship";
    }

    public static String generateEntityIdFieldName(DataEntityMetadata entity) {
        return entity.getName() + "Id";
    }

    public static String getRelationshipEntryEntityDescriptorClassName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return StringUtil.capitalize(currEntity.getName()) + StringUtil.capitalize(currRelation.getRelationshipName()) + "RelationshipEntryDescriptor";
    }

    public static String getRelationshipEntryClassName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return StringUtil.capitalize(currEntity.getName()) + StringUtil.capitalize(currRelation.getRelationshipName()) + "RelationshipEntry";
    }

    public static String getRelationshipDescriptorStaticFieldName(AbstractRelation currRelation) {
        return currRelation.getRelationshipName() + "RelationshipDescriptor";
    }

    private void registerEntityDescriptorInModuleLoader(GeneratedClass entityDescriptorClass, GeneratedClass currPojo) throws MetadataGeneratorException {
        GeneratedBody staticCodeBlock = getModuleLoaderClass().getMethod(MODULE_LOADER_LOAD_METHOD_NAME).getBody();
        staticCodeBlock.addLine(new GeneratedCodeLine(EntityDescriptorFactory.class.getSimpleName() +  ".register(" + currPojo.getName() + ".class, " + currPojo.getName() + "." + ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");" ));
        getModuleLoaderClass().addImports(new ClassGeneratedType(currPojo));
        getModuleLoaderClass().addImports(new ClassGeneratedType(entityDescriptorClass));
        getModuleLoaderClass().addImport(EntityDescriptorFactory.class);



    }


    public static String getOneToManyRelationshipIdFieldName(OneToManyRelation oneToManyRelation) {
        String memberName = oneToManyRelation.getFieldName();
        if (memberName == null){
            memberName = oneToManyRelation.getName();
            if (memberName == null){
                memberName = oneToManyRelation.getRelatedEntity() + "Id";
            }else {
                memberName += "Id";
            }
        }
        return StringUtil.capitalize(memberName);
    }

    public static String getRelationshipIdFieldName(AbstractRelation relation) {
        if (relation instanceof OneToManyRelation){
            OneToManyRelation oneToManyRelation = (OneToManyRelation) relation;
            String  memberName = oneToManyRelation.getName();
            if (memberName == null){
                memberName = oneToManyRelation.getRelatedEntity() + "Id";
            }else {
                memberName = memberName + "Id";
            }
            return StringUtil.unCapitalize(memberName);
        }else {
            return relation.getRelatedEntity() + "Id";
        }
    }


    private GeneratedType convertFieldToType(AbstractFieldMetadata currField) throws MetadataGeneratorException {
        switch (currField.getType()){
            case ENUM:
                EnumFieldDef enumField = (EnumFieldDef)currField;
                GeneratedEnum generatedEnum = generatedEnumerations.get(enumField.getEnumName());
                if (generatedEnum == null){
                    throw new MetadataGeneratorException("Invalid enum type for field " + enumField.getName()  +": " + enumField.getEnumName());
                }

            return new ClassGeneratedType(getGeneratedEnumByField(enumField));
            default:
                return new ClassGeneratedType(currField.getType().getJavaType());
        }
    }


    private ClassGeneratedType convertFieldToFieldDescriptorType(AbstractFieldMetadata currField) throws MetadataGeneratorException {
        switch (currField.getType()){
            case BOOLEAN:
                return new ClassGeneratedType(BooleanFieldDescriptor.class);
            case STRING:
                return new ClassGeneratedType(StringFieldDescriptor.class);
            case ENUM:
                EnumFieldDef enumField = (EnumFieldDef)currField;
                ClassGeneratedType enumType = new ClassGeneratedType(getGeneratedEnumByField(enumField));
                return new ClassGeneratedType(EnumFieldDescriptor.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(enumType)));
            case DATE:
                return new ClassGeneratedType(DateFieldDescriptor.class);
            case INTEGER:
                return new ClassGeneratedType(IntegerFieldDescriptor.class);
            case LONG:
                return new ClassGeneratedType(LongFieldDescriptor.class);
            case DOUBLE:
                return new ClassGeneratedType(DoubleFieldDescriptor.class);
            default:
                throw new IllegalStateException("Unsupported field type " + currField.getType());
        }
    }

    public GeneratedEnum getGeneratedEnumByField(EnumFieldDef field) throws MetadataGeneratorException {
        GeneratedEnum generatedEnum = generatedEnumerations.get(field.getEnumName());
        if (generatedEnum == null){
            throw new MetadataGeneratorException("Invalid enum type for field " + field.getName()  +": " + field.getEnumName());
        }
        return generatedEnum;

    }

    public GeneratedClass getModuleLoaderClass() {
        return moduleLoaderClass;
    }

    public GeneratedClass getGeneratedPojo(DataEntityMetadata entityMetadata){
        return generatedPackage.getClass(entityMetadata.getJavaClassName());
    }

    public GeneratedClass getGeneratedEntityDescriptorClass(DataEntityMetadata entityMetadata){
        return generatedPackage.getClass(getEntityDescriptorClassName(entityMetadata));
    }

    public GeneratedClass getGeneratedRelationshipEntryClass(DataEntityMetadata currEntity, AbstractRelation currRelation){
        return generatedPackage.getClass(getRelationshipEntryClassName(currEntity, currRelation));
    }
}
