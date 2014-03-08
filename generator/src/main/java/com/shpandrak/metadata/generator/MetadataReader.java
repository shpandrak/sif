package com.shpandrak.metadata.generator;

import com.shpandrak.common.xml.JAXBUtil;
import com.shpandrak.metadata.model.MetadataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/10/12
 * Time: 23:14
 */
public class MetadataReader {
    private static final Logger logger = LoggerFactory.getLogger(MetadataReader.class);
    private String fileName;
    private String rootDir;
    private List<MetadataGeneratorJob> metadataGeneratorJobs = new ArrayList<MetadataGeneratorJob>();

    public MetadataReader(String fileName, String rootDir) {
        this.fileName = fileName;
        this.rootDir = rootDir;
    }

    public void addGeneratorJob(MetadataGeneratorJob job){
        metadataGeneratorJobs.add(job);
    }

    public void generate() throws MetadataGeneratorException {
        File file = new File(fileName);
        if (!file.exists()){
            throw new MetadataGeneratorException("File " + fileName + " does not exit");
        }
        logger.debug("file {} exists", file.getAbsolutePath());

        MetadataStore store;
        try {
            logger.info("Reading metadata store from file {}", file.getAbsolutePath());
            store = JAXBUtil.readObject(MetadataStore.class, file);
        } catch (JAXBException e) {
            throw new MetadataGeneratorException("Failed parsing metadata store from file " + file.getAbsolutePath(), e);
        }


        // First pass - generate
        GenerationContext generationContext = new GenerationContext();

        for (MetadataGeneratorJob currJob : metadataGeneratorJobs){

            IMetadataGenerator currGenerator = currJob.getGenerator();

            // Validating dependencies (some generators require other generators to run first
            Set<Class<? extends IMetadataGenerator>> dependenciesCopy = new HashSet<Class<? extends IMetadataGenerator>>(currGenerator.getDependencies());
            if (!dependenciesCopy.isEmpty()){
                for (Class<? extends IMetadataGenerator> currDependency : currGenerator.getDependencies()){
                    for (IMetadataGenerator currPrevGenerator : generationContext.getPreviousGenerators()){
                        if (currDependency.isAssignableFrom(currPrevGenerator.getClass())){
                            dependenciesCopy.remove(currDependency);
                        }
                    }
                }
            }

            if (!dependenciesCopy.isEmpty()){
                throw new MetadataGeneratorException("Unsatisfied Generator dependency. generator of type " + currGenerator.getClass().getSimpleName() + " Requires " + dependenciesCopy.iterator().next().getSimpleName() + " to run before. To resolve please update the shpandrak_generator.xml file");
            }


            logger.info("Generating data for generator " + currGenerator.getClass().getCanonicalName());
            currGenerator.generate(store, generationContext);
            generationContext.addGenerator(currGenerator);
        }

        // Second pass - write files
        for (MetadataGeneratorJob currJob : metadataGeneratorJobs){
            if (currJob.isPrint()){
                currJob.getGenerator().write(rootDir);
            }
        }
    }


}
