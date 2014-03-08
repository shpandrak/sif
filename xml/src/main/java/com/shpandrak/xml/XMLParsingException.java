package com.shpandrak.xml;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/4/12
 * Time: 07:20
 */
public class XMLParsingException extends Exception {
    public XMLParsingException(String s) {
        super(s);
    }

    public XMLParsingException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
