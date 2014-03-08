package com.shpandrak.metadata.generator;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/11/12
 * Time: 18:28
 */
public class MetadataGeneratorException extends Exception{
    public MetadataGeneratorException(String s) {
        super(s);
    }

    public MetadataGeneratorException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
