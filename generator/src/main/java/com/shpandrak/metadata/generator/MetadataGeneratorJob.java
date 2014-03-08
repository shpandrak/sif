package com.shpandrak.metadata.generator;

/**
 * Created with love
 * User: shpandrak
 * Date: 1/25/13
 * Time: 19:03
 */
public class MetadataGeneratorJob {
    private IMetadataGenerator generator;
    private boolean print;

    public MetadataGeneratorJob(IMetadataGenerator generator, boolean print) {
        this.generator = generator;
        this.print = print;
    }

    public IMetadataGenerator getGenerator() {
        return generator;
    }

    public boolean isPrint() {
        return print;
    }
}
