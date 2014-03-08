package com.shpandrak.web.rest.filter;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/26/13
 * Time: 11:53
 */
public class URIQueryParsingException extends Exception{
    public URIQueryParsingException(String s) {
        super(s);
    }

    public URIQueryParsingException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
