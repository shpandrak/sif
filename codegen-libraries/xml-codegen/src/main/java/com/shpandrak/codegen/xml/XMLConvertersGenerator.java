package com.shpandrak.codegen.xml;

import com.shpandrak.codegen.BaseGenerator;
import com.shpandrak.codegen.PojoCodeGenUtil;
import com.shpandrak.codegen.PojoGenerator;
import com.shpandrak.codegen.model.*;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.field.AbstractFieldMetadata;
import com.shpandrak.metadata.model.field.EnumFieldDef;
import com.shpandrak.xml.EntityXMLConverter;
import com.shpandrak.xml.EntityXMLConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:42
 */
public class XMLConvertersGenerator extends BaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(XMLConvertersGenerator.class);
    public static final String ROOT_ELEMENT_TAG_NAME_FIELD = "ROOT_ELEMENT_TAG_NAME";
    public static final String WRAPPER_ELEMENT_TAG_NAME_FIELD = "WRAPPER_ELEMENT_TAG_NAME";

    @Override
    protected GeneratedPackage createPackage(MetadataStore store) {
        return new GeneratedPackage(store.getNamespace() + ".xml");
    }

    @Override
    protected void generateClasses(MetadataStore store, GeneratedPackage generatedPackage, GenerationContext generationContext) throws MetadataGeneratorException {
        GeneratedPackage pojoPackage = pojoGenerator.getGeneratedPackage();
        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = pojoPackage.getClass(currEntity.getJavaClassName());

//            GeneratedClass xmlConverter = generateXmlConverter(currEntity, currPojo);
//
//            registerConverterInModuleLoader(xmlConverter, currPojo);
//
//            generatedPackage.addClass(xmlConverter);

        }

    }

    private void registerConverterInModuleLoader(GeneratedClass converter, GeneratedClass currPojo) throws MetadataGeneratorException {
        GeneratedBody staticCodeBlock = pojoGenerator.getModuleLoaderClass().getMethod(PojoGenerator.MODULE_LOADER_LOAD_METHOD_NAME).getBody();
        staticCodeBlock.addLine(new GeneratedCodeLine(EntityXMLConverterFactory.class.getSimpleName() + ".register(" + currPojo.getName() + ".class, new " + converter.getName() + "());" ));
        pojoGenerator.getModuleLoaderClass().addImports(new ClassGeneratedType(currPojo));
        pojoGenerator.getModuleLoaderClass().addImports(new ClassGeneratedType(converter));
        pojoGenerator.getModuleLoaderClass().addImport(EntityXMLConverterFactory.class);



    }

    private GeneratedClass generateXmlConverter(DataEntityMetadata currEntity, GeneratedClass currPojo) throws MetadataGeneratorException {
        GeneratedClass c = new GeneratedClass(getXMLConverterName(currPojo), getGeneratedPackage().getName());

        // Extends EntityXMLConverter
        c.setExtendsClass(EntityXMLConverter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));
        c.addDefaultConstructor(GeneratedModifier.PUBLIC, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("super(" + currEntity.getJavaClassName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");"))));

        // Static members for element name and wrapper element name
        c.addMember(new GeneratedClassMember(ROOT_ELEMENT_TAG_NAME_FIELD, new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + generateRootTagName(currEntity) + "\";")));
        c.addMember(new GeneratedClassMember(WRAPPER_ELEMENT_TAG_NAME_FIELD, new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + generateWrapperRootTagName(currEntity) + "\";")));

        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();

        for (AbstractFieldMetadata currField : currEntity.getFields()){
            String fieldNameConstant = getFieldNameConstant(currField);
            c.addMember(new GeneratedClassMember(fieldNameConstant, new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + currField.getName() + "\";")));


            lines.add(new GeneratedCodeLine("sb.append(\"<\").append(" + fieldNameConstant + ").append(\">\")"));
            lines.add(new GeneratedCodeLine("\t.append(entity." + PojoCodeGenUtil.generateGetterName(currField) + "())"));
            lines.add(new GeneratedCodeLine("\t.append(\"</\").append(" + fieldNameConstant + ").append(\">\");"));
        }

        //lines.add(new GeneratedCodeLine("sb.append(\"</\").append(" + ROOT_ELEMENT_TAG_NAME_FIELD + ").append(\">\");"));

        c.addMethod(new GeneratedMethod("appendEntity", GeneratedModifier.PROTECTED, Arrays.<GeneratedParameter>asList(new GeneratedParameter("sb", new ClassGeneratedType(StringBuilder.class)), new GeneratedParameter("entity", new ClassGeneratedType(currPojo))), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(lines)));

        c.addMethod(new GeneratedMethod("getElementName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + ROOT_ELEMENT_TAG_NAME_FIELD + ";")))));

        c.addMethod(new GeneratedMethod("getWrapperElementName", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + WRAPPER_ELEMENT_TAG_NAME_FIELD + ";")))));

        c.addMethod(new GeneratedMethod("getClassType", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + currPojo.getName() + ".class;")))));




        addConvertToEntityMethod(currEntity, currPojo, c);

        return c;
    }

    public static String generateRootTagName(DataEntityMetadata currEntity) {
        return currEntity.getName();
    }

    public static String generateWrapperRootTagName(DataEntityMetadata currEntity) {
        return StringUtil.getPluralForm(currEntity.getName());
    }

    private void addConvertToEntityMethod(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass c) throws MetadataGeneratorException {

        String code = "return new " + currPojo.getName() + "(";
        ArrayList<ClassGeneratedType> typesInUse = new ArrayList<ClassGeneratedType>();

        boolean first = true;
        for (AbstractFieldMetadata currField : currEntity.getFields()){
            String fieldNameConstant = getFieldNameConstant(currField);

            if (first){
                first = false;
            }else {
                code += ",";
            }
            code += "\n\t\t\t";

            switch (currField.getType()){
                case STRING:
                    code += "values.get(" + fieldNameConstant + ")";
                    break;
                case ENUM:
                    GeneratedEnum generatedEnumByField = pojoGenerator.getGeneratedEnumByField((EnumFieldDef) currField);
                    code += generatedEnumByField.getName() + ".valueOf(" + "values.get(" + fieldNameConstant + "))";
                    typesInUse.add(new ClassGeneratedType(generatedEnumByField));
                    break;
                case BOOLEAN:
                    code += "Boolean.valueOf(" + "values.get(" + fieldNameConstant + "))";
                    break;
                case DATE:
                    code += "new Date(Long.valueOf(" + "values.get(" + fieldNameConstant + ")))";
                    typesInUse.add(new ClassGeneratedType(Date.class));
                    break;
                case DOUBLE:
                    code += "Double.valueOf(" + "values.get(" + fieldNameConstant + "))";
                    break;
                case INTEGER:
                    code += "Integer.valueOf(" + "values.get(" + fieldNameConstant + "))";
                    break;
                case LONG:
                    code += "Long.valueOf(" + "values.get(" + fieldNameConstant + "))";
                    break;
                default:
                    throw new MetadataGeneratorException("Unsupported field type " + currField.getType() + " for field " + currField.getName() + " and entity " + currEntity.getName());
            }
        }


/*
        for (AbstractRelation currRelation : currEntity.getRelations()) {
            switch (currRelation.getRelationType()){
                case OneToMany:
                    if (first){
                        first = false;
                    }else {
                        code += ",";
                    }
                    // At this stage we don't serialize relationships. bye
                    code += "\n\t\t\tnull";
                    break;
                default:
                    break;
            }
        }
*/

        code += ");";

        GeneratedCodeLine convertToEntityCode = new GeneratedCodeLine(code, typesInUse);
        c.addMethod(new GeneratedMethod("convertToEntity", GeneratedModifier.PROTECTED, Arrays.asList(new GeneratedParameter("values", new ClassGeneratedType(Map.class, new GeneratedGenericsImplementation(Arrays.asList(new GeneratedType("String"), new GeneratedType("String")))))), new ClassGeneratedType(currPojo), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(convertToEntityCode))));
    }

    public static String getFieldNameConstant(AbstractFieldMetadata currField) {
        return currField.getName().toUpperCase() + "_FIELD";
    }


    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        Set<Class<? extends IMetadataGenerator>> dependencies = new HashSet<Class<? extends IMetadataGenerator>>();
        dependencies.add(PojoGenerator.class);
        return dependencies;
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
    }

    public static String getXMLConverterName(GeneratedClass currPojo) {
        return currPojo.getName() + "XMLConverter";
    }
}
