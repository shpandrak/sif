package com.shpandrak.metadata.generator;

import com.shpandrak.metadata.model.MetadataStore;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/11/12
 * Time: 18:20
 */
public interface IMetadataGenerator {

    void generate(MetadataStore store, GenerationContext generationContext) throws MetadataGeneratorException;

    Set<Class<? extends IMetadataGenerator>> getDependencies();

    void setProperties(Map<String, String> properties) throws MetadataGeneratorException;

    void write(String rootDir) throws MetadataGeneratorException;
}
