package com.shpandrak.codegen;

import com.shpandrak.codegen.model.GeneratedClass;
import com.shpandrak.codegen.model.GeneratedPackage;
import com.shpandrak.codegen.util.GeneratedClassPrinter;
import com.shpandrak.metadata.generator.GenerationContext;
import com.shpandrak.metadata.generator.IMetadataGenerator;
import com.shpandrak.metadata.generator.MetadataGeneratorException;
import com.shpandrak.metadata.model.MetadataStore;

import java.io.IOException;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/22/12
 * Time: 10:39
 * Optional Helper base class generator with some common methods
 */
public abstract class BaseGenerator implements IMetadataGenerator {
    private GeneratedPackage generatedPackage;
    protected PojoGenerator pojoGenerator;
    protected MetadataStore store;

    @Override
    public void generate(MetadataStore store, GenerationContext generationContext) throws MetadataGeneratorException {
        this.generatedPackage = createPackage(store);
        this.pojoGenerator = getGenerator(PojoGenerator.class, generationContext);
        this.store = store;
        generateClasses(store, this.generatedPackage, generationContext);
    }

    private void printClasses(String rootDir) throws MetadataGeneratorException {
        try {
            for (GeneratedClass currClass : generatedPackage.getClasses()) {
                new GeneratedClassPrinter(currClass, rootDir + "/target/generated-sources/shpangen/").generate();
            }
        } catch (IOException e) {
            throw new MetadataGeneratorException("Failed printing classes", e);
        }
    }


    protected abstract void generateClasses(MetadataStore store, GeneratedPackage generatedPackage, GenerationContext generationContext) throws MetadataGeneratorException;

    protected abstract GeneratedPackage createPackage(MetadataStore store);

    protected <T extends IMetadataGenerator> T getGenerator(Class<T> clazz, GenerationContext generatorContext) throws MetadataGeneratorException {
        T previousGeneratorByType = generatorContext.getPreviousGeneratorByType(clazz);
        if (previousGeneratorByType == null){
            throw new MetadataGeneratorException(this.getClass().getSimpleName() + " Generator must be preceded by " + clazz.getSimpleName());
        }
        return previousGeneratorByType;
    }

    public GeneratedPackage getGeneratedPackage() {
        return generatedPackage;
    }

    @Override
    public void write(String rootDir) throws MetadataGeneratorException {
        printClasses(rootDir);
    }
}
