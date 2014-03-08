package com.shpandrak.metadata.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/30/13
 * Time: 16:13
 */
public class GenerationContext {
    private List<IMetadataGenerator> previousGenerators = new ArrayList<IMetadataGenerator>();
    private Map<Class<? extends IMetadataGenerator>, IMetadataGenerator> previousGeneratorsByType = new HashMap<Class<? extends IMetadataGenerator>, IMetadataGenerator>();

    public List<IMetadataGenerator> getPreviousGenerators() {
        return previousGenerators;
    }

    public void addGenerator(IMetadataGenerator generator) {
        previousGenerators.add(generator);
        previousGeneratorsByType.put(generator.getClass(), generator);
    }


    public <T extends IMetadataGenerator> T getPreviousGeneratorByType(Class<T> clazz) {
        //noinspection unchecked
        T generator = (T) previousGeneratorsByType.get(clazz);
        if (generator == null){
            for (IMetadataGenerator currPrevGenerator : previousGenerators){
                if (clazz.isInstance(currPrevGenerator)){
                    generator = (T) currPrevGenerator;
                    break;
                }
            }
        }
        return generator;
    }
}
