package com.shpandrak.metadata.generator;

import com.shpandrak.common.xml.JAXBUtil;
import com.shpandrak.metadata.generator.config.GenerationDef;
import com.shpandrak.metadata.generator.config.GeneratorConfig;
import com.shpandrak.metadata.generator.config.PropertyDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/10/12
 * Time: 18:58
 */
public class GeneratorMain {
    private static final Logger logger = LoggerFactory.getLogger(GeneratorMain.class);
    private static final String DEFAULT_CONF_FILE_PATH = "/src/main/resources/shpandrak_generator.xml";

    public static void main(String []args) throws JAXBException, MetadataGeneratorException {
        String rootDir = "./";
        if (args.length > 0){
            rootDir = args[0];
        }

        logger.info("Started Shpandrak Generator for root dir {}", rootDir);

/*
        InputStream resourceAsStream = GeneratorMain.class.getClassLoader().getResourceAsStream("shpandrak_generator.xml");
        if (resourceAsStream == null){
            throw new MetadataGeneratorException("File " + fileName + " does not exist in classpath");
        }
*/


        File file = new File(rootDir + DEFAULT_CONF_FILE_PATH);
        if (!file.exists()){
            throw new MetadataGeneratorException("File " + file.getAbsolutePath() + " does not exist");
        }

        GeneratorConfig config = JAXBUtil.readObject(GeneratorConfig.class, file);


        String storeFilePath;
        List<GenerationDef> completeGeneratorsList;
        Map<String, GenerationDef> overridingGeneratorsDefPropertiesByClassName = new HashMap<String, GenerationDef>();

        if (config.getParentConfiguration() == null){
            completeGeneratorsList = config.getGenerators();
            String storeFileName = config.getStoreFile();
            if (storeFileName == null){
                throw new MetadataGeneratorException("root configuration file must define exact store file name");
            }
            storeFilePath = rootDir + "/" + storeFileName;

        }else {
            String parentConfigurationName = config.getParentConfiguration();
            logger.info("Scanning for parent configuration file " + parentConfigurationName);
            File parentConfigurationFile = new File(rootDir + "/../" + DEFAULT_CONF_FILE_PATH);
            if (!parentConfigurationFile.exists()){
                throw new MetadataGeneratorException("Parent configuration file " + parentConfigurationFile.getAbsolutePath() + " does not exist");
            }

            // Reading configuration file xml
            GeneratorConfig parentConfig = JAXBUtil.readObject(GeneratorConfig.class, parentConfigurationFile);

            // Verifying that the file found is the actual parent specified
            if (!parentConfig.getName().equals(parentConfigurationName)){
                throw new MetadataGeneratorException("Parent configuration file " + parentConfigurationFile.getAbsolutePath() +
                        " does not match parent name defined for configuration " + config.getName() + " desired: " +
                        parentConfigurationName + ". encountered " + parentConfig.getName());
            }

            String storeFileName = parentConfig.getStoreFile();
            if (storeFileName == null){
                throw new MetadataGeneratorException("root configuration file must define exact store file name");
            }

            storeFilePath = rootDir + "/../" + storeFileName;
            completeGeneratorsList = parentConfig.getGenerators();
            for (GenerationDef currDef : config.getGenerators()){
                overridingGeneratorsDefPropertiesByClassName.put(currDef.getClassName(), currDef);
            }
        }

        // Marking for printing only the generators in the current configuration
        Set<String> generatorForPrint = new HashSet<String>();
        for (GenerationDef currGenDef : config.getGenerators()){
            generatorForPrint.add(currGenDef.getClassName());
        }

        MetadataReader reader = new MetadataReader(storeFilePath, rootDir);

        for (GenerationDef currGeneratorDef : completeGeneratorsList){
            try {
                Object o = GeneratorMain.class.getClassLoader().loadClass(currGeneratorDef.getClassName()).newInstance();
                Map<String, String> properties = new HashMap<String, String>();
                if (currGeneratorDef.getProperties() != null){
                    for (PropertyDef currProp : currGeneratorDef.getProperties()){
                        properties.put(currProp.getName(), currProp.getValue());
                    }
                }

                // Checking if we have overriding properties
                GenerationDef overridingGenerator = overridingGeneratorsDefPropertiesByClassName.get(currGeneratorDef.getClassName());
                if (overridingGenerator != null && overridingGenerator.getProperties() != null){
                    for (PropertyDef currProp : overridingGenerator.getProperties()){
                        properties.put(currProp.getName(), currProp.getValue());
                    }
                }



                IMetadataGenerator gen = (IMetadataGenerator) o;
                gen.setProperties(properties);

                boolean printFiles = generatorForPrint.contains(currGeneratorDef.getClassName());
                reader.addGeneratorJob(new MetadataGeneratorJob(gen, printFiles));
            } catch (ClassNotFoundException e) {
                throw new MetadataGeneratorException("Failed loading generator " + currGeneratorDef.getClassName(), e);
            } catch (InstantiationException e) {
                throw new MetadataGeneratorException("Failed initializing generator " + currGeneratorDef.getClassName(), e);
            } catch (IllegalAccessException e) {
                throw new MetadataGeneratorException("Failed constructing generator " + currGeneratorDef.getClassName(), e);
            }
        }

        reader.generate();


    }

}
