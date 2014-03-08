package com.shpandrak.codegen;

import com.shpandrak.codegen.model.GeneratedClass;
import com.shpandrak.metadata.generator.IMetadataGenerator;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/16/13
 * Time: 09:11
 */
public interface IPersistenceLayerMetadataGenerator extends IMetadataGenerator {
    public static final String MODULE_LOADER_LOAD_METHOD_NAME = "load";

    GeneratedClass getLayerLoaderClass();
}
