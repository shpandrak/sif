package com.shpandrak.codegen;

import com.shpandrak.codegen.model.*;
import com.shpandrak.common.string.StringUtil;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.gae.datastore.managers.*;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.DataEntityMetadata;
import com.shpandrak.metadata.model.MetadataStore;
import com.shpandrak.metadata.model.relation.AbstractRelation;
import com.shpandrak.persistence.IConnectionProvider;
import com.shpandrak.persistence.IConnectionProviderFactory;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.managers.ManagerClassFactory;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/10/13
 * Time: 20:15
 */
public class GoogleDatastoreLayerGenerator extends BaseGenerator implements IPersistenceLayerMetadataGenerator {
    private GeneratedClass layerLoaderClass;

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

        createModuleLoader(store);

        // First Pass - generating managers
        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = pojoGeneratorGeneratedPackage.getClass(currEntity.getJavaClassName());

            GeneratedClass manager = generateManager(currEntity, currPojo);
            generatedPackage.addClass(manager);

            registerManagerInModuleLoader(manager, currPojo);

        }

        // Second Pass - generating relationship entries managers
        for (DataEntityMetadata currEntity : store.getEntities()) {
            GeneratedClass currPojo = pojoGenerator.getGeneratedPackage().getClass(currEntity.getJavaClassName());

            GeneratedClass managerGeneratedClass = generatedPackage.getClass(getManagerName(currPojo));

            List<GeneratedClass> relationshipClasses = generateRelationshipClasses(currEntity, currPojo);
            generatedPackage.addClasses(relationshipClasses);



        }


    }

    private List<GeneratedClass> generateRelationshipClasses(DataEntityMetadata currEntity, GeneratedClass currPojo) throws MetadataGeneratorException {
        List<GeneratedClass> relationshipGeneratedClasses = new ArrayList<GeneratedClass>();
        for (AbstractRelation currRelation : currEntity.getRelations()) {
            GeneratedClass relationshipEntryGeneratedClass = pojoGenerator.getGeneratedPackage().getClass(PojoGenerator.getRelationshipEntryClassName(currEntity, currRelation));
            switch (currRelation.getRelationType()){

                case ManyToMany:
                    GeneratedClass generatedRelationshipManager = generateRelationshipEntryManagerClass(currEntity, currPojo, currRelation, relationshipEntryGeneratedClass);
                    relationshipGeneratedClasses.add(generatedRelationshipManager);


                    break;
                default:
                    //todo:
            }
        }

        return relationshipGeneratedClasses;
    }

    private GeneratedClass generateRelationshipEntryManagerClass(DataEntityMetadata currEntity, GeneratedClass currPojo, AbstractRelation currRelation, GeneratedClass relationshipEntryGeneratedClass) throws MetadataGeneratorException {
        // Creating a manager class for the relationship entry
        GeneratedClass generatedRelationshipManager = new GeneratedClass(generatedRelationshipManagerClassName(currEntity, currRelation), getGeneratedPackage().getName());
        // Manager Extends DBBaseEntityManagerBean
        generatedRelationshipManager.setExtendsClass(GDSBasePersistableObjectManagerBean.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass))));

        // Manager implements generic IPersistableObjectManager interface
        generatedRelationshipManager.addInterface(IPersistableObjectManager.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass))));

        // Constructor with connectionProvider parameter
        GeneratedCodeLine ctorLine = new GeneratedCodeLine("super(connectionProvider);");
        generatedRelationshipManager.addConstructor(GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("connectionProvider", new ClassGeneratedType(GDSConnectionProvider.class))), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(Arrays.asList(ctorLine)));

        // Adding default constructor in case we have a default connection provider
        generatedRelationshipManager.addConstructor(
                GeneratedModifier.PUBLIC,
                Collections.<GeneratedParameter>emptyList(),
                Arrays.asList(new ClassGeneratedType(PersistenceException.class)),
                new GeneratedBody());

        // Implementing getPersistObjectAdapter
        GeneratedCodeLine generatedCodeLine1 = new GeneratedCodeLine("return new " + GDSPersistableRelationshipEntryAdapter.class.getSimpleName() + "<" + relationshipEntryGeneratedClass.getName() + ">(" + relationshipEntryGeneratedClass.getName() + "." + PojoGenerator.ENTITY_DESCRIPTOR_STATIC_MEMBER_NAME + ", alias);", Arrays.asList(new ClassGeneratedType(GDSPersistableRelationshipEntryAdapter.class)));
        generatedRelationshipManager.addMethod(new GeneratedMethod("getPersistObjectAdapter", GeneratedModifier.PROTECTED, Arrays.asList(new GeneratedParameter("alias", new GeneratedType("String"))), new ClassGeneratedType(GDSPersistableRelationshipEntryAdapter.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(generatedCodeLine1))));

        // Implementing getPersistEntityAdapter getEntityClass
        generatedRelationshipManager.addMethod(new GeneratedMethod("getEntityClass", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(Class.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + relationshipEntryGeneratedClass.getName() + ".class;")))));

        // Implementing getDescriptor
        generatedRelationshipManager.addMethod(new GeneratedMethod("getDescriptor", GeneratedModifier.PROTECTED, Collections.<GeneratedParameter>emptyList(), new ClassGeneratedType(BasePersistentObjectDescriptor.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(relationshipEntryGeneratedClass)))), Collections.<ClassGeneratedType>emptyList(), Arrays.asList(new GeneratedAnnotation(new ClassGeneratedType(Override.class))), false, new GeneratedBody(Arrays.asList(new GeneratedCodeLine("return " + relationshipEntryGeneratedClass.getName() + ".DESCRIPTOR;")))));

        //register in moduleLoader
        registerManagerInModuleLoader(generatedRelationshipManager, relationshipEntryGeneratedClass);

        return generatedRelationshipManager;
    }

    public static String generatedRelationshipManagerClassName(DataEntityMetadata currEntity, AbstractRelation currRelation) {
        return currEntity.getJavaClassName() + StringUtil.capitalize(currRelation.getRelationshipName()) + "Manager";
    }

    private void createModuleLoader(MetadataStore store) {
        String moduleName = StringUtil.capitalize(store.getName());
        layerLoaderClass = new GeneratedClass(moduleName + "ModuleGoogleDatastoreLayerLoader", getGeneratedPackage().getName());
        layerLoaderClass.addLogger();
        String firstLine = "logger.info(\"Database Layer for module module " + moduleName + " has been loaded\");";
        layerLoaderClass.addImport(BaseEntity.class);
        GeneratedBody body = new GeneratedBody(Arrays.asList(new GeneratedCodeLine(firstLine)));
        body.addLine(new GeneratedCodeLine(PersistenceLayerManager.class.getSimpleName() +".init(new " + DefaultGDSConnectionProviderFactory.class.getSimpleName() + "());",
                Arrays.asList(
                        new ClassGeneratedType(DefaultGDSConnectionProviderFactory.class),
                        new ClassGeneratedType(PersistenceLayerManager.class)
                )));


        layerLoaderClass.addMethod(new GeneratedMethod(MODULE_LOADER_LOAD_METHOD_NAME, GeneratedModifier.PUBLIC, Collections.<GeneratedParameter>emptyList(), new GeneratedType("void"), Collections.<ClassGeneratedType>emptyList(), Collections.<GeneratedAnnotation>emptyList(), true, body));

        getGeneratedPackage().addClass(layerLoaderClass);
    }


    private GeneratedClass generateManager(DataEntityMetadata currEntity, GeneratedClass currPojo) {
        GeneratedClass c = new GeneratedClass(getManagerName(currPojo), getGeneratedPackage().getName());

        // Manager Extends DBBaseEntityManagerBean
        c.setExtendsClass(GDSBaseEntityManagerBean.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        // Manager implements generic IPersistableObjectManager interface
        c.addInterface(IEntityManager.class, new GeneratedGenericsImplementation(Arrays.<GeneratedType>asList(new ClassGeneratedType(currPojo))));

        // Constructor with connectionProvider parameter
        GeneratedCodeLine generatedCodeLine = new GeneratedCodeLine("super(connectionProvider);");
        c.addConstructor(GeneratedModifier.PUBLIC, Arrays.<GeneratedParameter>asList(new GeneratedParameter("connectionProvider", new ClassGeneratedType(GDSConnectionProvider.class))), Collections.<ClassGeneratedType>emptyList(), new GeneratedBody(generatedCodeLine));

        // Adding default constructor for default connection provider
        c.addConstructor(
                GeneratedModifier.PUBLIC,
                Collections.<GeneratedParameter>emptyList(),
                Arrays.asList(new ClassGeneratedType(PersistenceException.class)),
                new GeneratedBody());


        // Implementing getEntityClass
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

    private void registerManagerInModuleLoader(GeneratedClass managerClass, GeneratedClass currPojo) throws MetadataGeneratorException {
        GeneratedBody staticCodeBlock = layerLoaderClass.getMethod(PojoGenerator.MODULE_LOADER_LOAD_METHOD_NAME).getBody();
        staticCodeBlock.addLine(new GeneratedCodeLine(ManagerClassFactory.class.getSimpleName() +  ".register(" + currPojo.getName() + ".class, " + managerClass.getName() + ".class);" ));
        layerLoaderClass.addImports(new ClassGeneratedType(currPojo));
        layerLoaderClass.addImports(new ClassGeneratedType(managerClass));
        layerLoaderClass.addImport(ManagerClassFactory.class);
    }




    @Override
    protected GeneratedPackage createPackage(MetadataStore store) {
        return new GeneratedPackage(store.getNamespace() + ".gae.datastore");
    }

    @Override
    public Set<Class<? extends IMetadataGenerator>> getDependencies() {
        HashSet<Class<? extends IMetadataGenerator>> dependencies = new HashSet<Class<? extends IMetadataGenerator>>();
        dependencies.add(PojoGenerator.class);
        return dependencies;
    }

    @Override
    public void setProperties(Map<String, String> properties) throws MetadataGeneratorException {
    }
}
