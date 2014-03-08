package com.shpandrak.codegen.rest;

import com.shpandrak.codegen.BaseGenerator;
import com.shpandrak.codegen.IPersistenceLayerMetadataGenerator;
import com.shpandrak.codegen.PojoGenerator;
import com.shpandrak.codegen.model.*;
import com.shpandrak.codegen.xml.XMLConvertersGenerator;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.ManagerClassFactory;
import com.shpandrak.web.rest.BaseRestEntityResource;
import com.shpandrak.xml.EntityXMLConverter;
import com.shpandrak.xml.XMLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:42
 */
public class RestLayerGenerator extends BaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RestLayerGenerator.class);

    public static final String RESOURCE_ROOT_PATH_FIELD_NAME = "RESOURCE_ROOT_PATH";
    private static final String PERSISTENCE_LAYER_GENERATOR_PROPERTY_NAME = "persistence-layer-generator";
    private static final String ALTERNATIVE_XML_CONVERTER_CLASS_PREFIX = "alternate-xml-converter-";
    private Class<? extends IPersistenceLayerMetadataGenerator> persistenceLayerGeneratorClass = null;
    Map<String, String> alternateXMLConverterClassByEntityName = new HashMap<String, String>();

    @Override
    protected void generateClasses(MetadataStore store, GeneratedPackage generatedPackage, GenerationContext generationContext) throws MetadataGeneratorException {
        PojoGenerator pojoGenerator = getGenerator(PojoGenerator.class, generationContext);
        IPersistenceLayerMetadataGenerator persistenceLayerGenerator = getGenerator(persistenceLayerGeneratorClass, generationContext);
        XMLConvertersGenerator xmlGenerator = getGenerator(XMLConvertersGenerator.class, generationContext);

        GeneratedPackage generatedPojosPackage = pojoGenerator.getGeneratedPackage();
        GeneratedPackage generatedXMLPackage = xmlGenerator.getGeneratedPackage();

        // Generating Startup servlet
        GeneratedClass startupServlet = new GeneratedClass(StringUtil.capitalize(store.getName()) + "StartupServlet", generatedPackage.getName());
        startupServlet.setExtendsClass(HttpServlet.class);
        startupServlet.addAnnotation(new GeneratedAnnotation(new ClassGeneratedType(WebServlet.class), Arrays.asList(new GeneratedCodeLine("value=\"/whoopee\""), new GeneratedCodeLine("loadOnStartup = 1"))));
        GeneratedCodeLine initModuleLoaderLine = new GeneratedCodeLine(pojoGenerator.getModuleLoaderClass().getName() + "." + PojoGenerator.MODULE_LOADER_LOAD_METHOD_NAME + "();", Arrays.asList(new ClassGeneratedType(pojoGenerator.getModuleLoaderClass())));
        GeneratedCodeLine initManagersLine = new GeneratedCodeLine(persistenceLayerGenerator.getLayerLoaderClass().getName() + "." + IPersistenceLayerMetadataGenerator.MODULE_LOADER_LOAD_METHOD_NAME + "();", Arrays.asList(new ClassGeneratedType(persistenceLayerGenerator.getLayerLoaderClass())));
        startupServlet.addConstructor(GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(Arrays.asList(initModuleLoaderLine, initManagersLine)));
        generatedPackage.addClass(startupServlet);


        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = generatedPojosPackage.getClass(currEntity.getJavaClassName());
            GeneratedClass currXMLConverter = generatedXMLPackage.getClass(XMLConvertersGenerator.getXMLConverterName(currPojo));
            GeneratedClass restResource = generateRestResource(currEntity, currPojo, currXMLConverter);
            generatedPackage.addClass(restResource);

        }

    }

    @Override
    protected GeneratedPackage createPackage(MetadataStore store) {
        return new GeneratedPackage(store.getNamespace() + ".rest");
    }

    private GeneratedClass generateRestResource(DataEntityMetadata currEntity, GeneratedClass currPojo, GeneratedClass currXMLConverter) throws MetadataGeneratorException {
        GeneratedClass c = new GeneratedClass(getResourceName(currPojo), getGeneratedPackage().getName());
        String name = getResourcePath(currEntity);
        c.addAnnotation(new GeneratedAnnotation(new ClassGeneratedType(Path.class), Arrays.asList(new GeneratedCodeLine("\"/" + name + "\""))));
        c.setExtendsClass(BaseRestEntityResource.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));
        c.addMember(new GeneratedClassMember(RESOURCE_ROOT_PATH_FIELD_NAME, new GeneratedType("String"), GeneratedModifier.PUBLIC, false, false, true, true, new GeneratedCodeLine("\"" + name + "\";")));

        generateGetAllMethod(c);

        generateGetByIdMethod(c);


        generateCreateMethod(c);

        generateDeleteMethod(c);

        generateListRelationshipsMethods(c, currEntity);

        generateGetManagerMethod(currPojo, c);

        generateGetDescriptorMethod(c, currPojo, currEntity);

        generateGetXMLConverterMethod(c, currPojo, currEntity);

        return c;
    }

    private void generateListRelationshipsMethods(GeneratedClass c, DataEntityMetadata currEntity) {
        for (AbstractRelation currRelationship : currEntity.getRelations()){
            switch (currRelationship.getRelationType()){
                case ManyToMany:
                    List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
                    lines.add(new GeneratedCodeLine("return doListRelationship(\"" + currRelationship.getRelationshipName() + "\", id);"));
                    c.addMethod(new GeneratedMethod("list" + StringUtil.getPluralForm(StringUtil.capitalize(currRelationship.getRelationshipName())), GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("id", new GeneratedType("String"), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(PathParam.class), Arrays.asList(new GeneratedCodeLine("\"id\"")))))), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(),
                            Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(GET.class)),
                                    new GeneratedAnnotation(new ClassGeneratedType(Produces.class), Arrays.asList(new GeneratedCodeLine("MediaType.APPLICATION_XML", Arrays.asList(new ClassGeneratedType(MediaType.class))))),
                                    new GeneratedAnnotation(new ClassGeneratedType(Path.class), Arrays.asList(new GeneratedCodeLine("\"/{id}/relationship/" + currRelationship.getRelationshipName() + "\"")))
                            ),
                            false, new GeneratedBody(lines)));
                    break;
                default:
                    //todo:the rest...
                    break;
            }
        }
    }

    private void generateGetDescriptorMethod(GeneratedClass c, GeneratedClass currPojo, DataEntityMetadata currEntity) {
        GeneratedClass pojoDescriptor = pojoGenerator.getGeneratedEntityDescriptorClass(currEntity);
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return " + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ";"));
        c.addMethod(new GeneratedMethod("getDescriptor", GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(pojoDescriptor), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(lines)));
    }

    public static String getResourcePath(DataEntityMetadata currEntity) {
        return currEntity.getName();
    }

    private void generateGetXMLConverterMethod(GeneratedClass c, GeneratedClass currPojo, DataEntityMetadata currEntity) {
        GeneratedBody body = generateGetXMLConverterBody(currPojo, currEntity);
        c.addMethod(new GeneratedMethod("getXMLConverter", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(XMLConverter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, body));
    }

    protected GeneratedBody generateGetXMLConverterBody(GeneratedClass currPojo, DataEntityMetadata currEntity) {
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        String alternateXMLConverterClass = alternateXMLConverterClassByEntityName.get(currEntity.getName());
        if (alternateXMLConverterClass == null){
            lines.add(new GeneratedCodeLine("return new " + EntityXMLConverter.class.getSimpleName() + "<" + currPojo.getName() + ">(" + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");", Arrays.asList(new ClassGeneratedType(currPojo), new ClassGeneratedType(EntityXMLConverter.class))));
        }else {
            String alternateXMLConverterClassSimpleName = alternateXMLConverterClass.substring(alternateXMLConverterClass.lastIndexOf(".") + 1);
            lines.add(new GeneratedCodeLine("return new " + alternateXMLConverterClassSimpleName + "(" + currPojo.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ");", Arrays.asList(new ClassGeneratedType(currPojo), new ClassGeneratedType(alternateXMLConverterClassSimpleName, alternateXMLConverterClass))));
        }
        return new GeneratedBody(lines);
    }

    private void generateGetManagerMethod(GeneratedClass currPojo, GeneratedClass c) throws MetadataGeneratorException {

/*
        Class<? extends IConnectionProvider> connectionProviderClass;
        try {
            //noinspection unchecked
            connectionProviderClass = (Class<? extends IConnectionProvider>)Class.forName(connectionProviderClassName);

        } catch (ClassNotFoundException e) {
            throw new MetadataGeneratorException("Unable to load Connection Provider class " + connectionProviderClassName, e);
        }
*/
/*
        ArrayList<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        if (connectionProviderClassName == null){
            lines.add(new GeneratedCodeLine("return new " + currManager.getPersistentFieldName() + "();"));
        }else {
            String connProviderSimpleName = connectionProviderClassName.substring(connectionProviderClassName.lastIndexOf(".") + 1);
            lines.add(new GeneratedCodeLine("return new " + currManager.getPersistentFieldName() + "(new " + connProviderSimpleName + "());", Arrays.asList(new ClassGeneratedType(connProviderSimpleName, connectionProviderClassName))));
        }
*/
        //(IEntityManager<Country>) ManagerClassFactory.getDefaultInstance(getEntityDescriptor().getEntityClass());
        c.addMethod(
            new GeneratedMethod(
                    "getManager",
                    GeneratedModifier.PROTECTED,
                    Collections.<GeneratedParameter>emptyList(),
                    new ClassGeneratedType(IEntityManager.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo)))),
                    Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false,
                    new GeneratedBody(
                            new GeneratedCodeLine(
                                    "return (" + IEntityManager.class.getSimpleName() + "<" + currPojo.getName() + ">)" +
                                    ManagerClassFactory.class.getSimpleName() + ".getDefaultInstance(getEntityDescriptor().getEntityClass());",
                                    Arrays.<ClassGeneratedType>asList(
                                            new ClassGeneratedType(IEntityManager.class),
                                            new ClassGeneratedType(ManagerClassFactory.class),
                                            new ClassGeneratedType(currPojo))))));
    }

    private void generateGetAllMethod(GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return doList(uriInfo);"));
        c.addMethod(new GeneratedMethod("list", GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("uriInfo", new ClassGeneratedType(UriInfo.class), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Context.class))))), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(GET.class)), new GeneratedAnnotation(new ClassGeneratedType(Produces.class), Arrays.asList(new GeneratedCodeLine("MediaType.APPLICATION_XML", Arrays.asList(new ClassGeneratedType(MediaType.class)))))), false, new GeneratedBody(lines)));
    }

    private void generateGetByIdMethod(GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return doGetById(id);"));
        c.addMethod(new GeneratedMethod("getById", GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("id", new GeneratedType("String"), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(PathParam.class), Arrays.asList(new GeneratedCodeLine("\"id\"")))))), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(),
                Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(GET.class)),
                        new GeneratedAnnotation(new ClassGeneratedType(Produces.class), Arrays.asList(new GeneratedCodeLine("MediaType.APPLICATION_XML", Arrays.asList(new ClassGeneratedType(MediaType.class))))),
                        new GeneratedAnnotation(new ClassGeneratedType(Path.class), Arrays.asList(new GeneratedCodeLine("\"/{id}\"")))
                ),
                false, new GeneratedBody(lines)));
    }

    private void generateCreateMethod(GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("return doCreate(entityXML);"));
        c.addMethod(new GeneratedMethod("create", GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("entityXML", new GeneratedType("String"), Collections.<GeneratedAnnotation>emptyList())), new GeneratedType("String"), Collections.<ClassGeneratedType>emptyList(),
                Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(POST.class)),
                        new GeneratedAnnotation(new ClassGeneratedType(Produces.class), Arrays.asList(new GeneratedCodeLine("MediaType.APPLICATION_XML", Arrays.asList(new ClassGeneratedType(MediaType.class))))),
                        new GeneratedAnnotation(new ClassGeneratedType(Consumes.class), Arrays.asList(new GeneratedCodeLine("MediaType.APPLICATION_XML", Arrays.asList(new ClassGeneratedType(MediaType.class)))))
                ),
                false, new GeneratedBody(lines)));

    }

    private void generateDeleteMethod(GeneratedClass c) {
        List<GeneratedCodeLine> lines = new ArrayList<GeneratedCodeLine>();
        lines.add(new GeneratedCodeLine("doDelete(entityId);"));
        c.addMethod(new GeneratedMethod("delete", GeneratedModifier.PUBLIC,
                Arrays.<GeneratedParameter>asList(new GeneratedParameter("entityId", new GeneratedType("String"),
                        Collections.<GeneratedAnnotation>emptyList())), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(),
                Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(DELETE.class))), false, new GeneratedBody(lines)));

    }


    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        Set<Class<? extends IMetadataGenerator>> dependencies = new HashSet<Class<? extends IMetadataGenerator>>();
        dependencies.add(PojoGenerator.class);
        dependencies.add(IPersistenceLayerMetadataGenerator.class);
        return dependencies;
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
        String clazz = properties.get(PERSISTENCE_LAYER_GENERATOR_PROPERTY_NAME);
        if (clazz == null){
            throw new MetadataGeneratorException("Unsutisfied required property " + ALTERNATIVE_XML_CONVERTER_CLASS_PREFIX + " for generator " + this.getClass().getSimpleName());
        }
        try {
            persistenceLayerGeneratorClass = (Class<? extends IPersistenceLayerMetadataGenerator>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new MetadataGeneratorException("Failed Loading persistence layer generator class", e);
        }

        for (Map.Entry<String, String> currEntry : properties.entrySet()){
            if (currEntry.getKey().startsWith(ALTERNATIVE_XML_CONVERTER_CLASS_PREFIX)){
                alternateXMLConverterClassByEntityName.put(currEntry.getKey().substring(ALTERNATIVE_XML_CONVERTER_CLASS_PREFIX.length()), currEntry.getValue());
            }
        }
    }

    public static String getResourceName(GeneratedClass currPojo) {
        return currPojo.getName() + "Resource";
    }
}
