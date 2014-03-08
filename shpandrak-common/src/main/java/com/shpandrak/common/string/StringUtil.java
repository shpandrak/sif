package com.shpandrak.common.string;

/**
 * Created with creepy intentions
 * User: shpandrak
 * Date: 10/20/12
 * Time: 10:17
 */
public abstract class StringUtil {
    public static String capitalize(String s){
        if (s == null) return null;
        if (s.isEmpty()) return s;
        if (s.length() == 1) return s.toUpperCase();
        return s.substring(0,1).toUpperCase() + s.substring(1);

    }
    public static String unCapitalize(String s){
        if (s == null) return null;
        if (s.isEmpty()) return s;
        if (s.length() == 1) return s.toLowerCase();
        return s.substring(0,1).toLowerCase() + s.substring(1);
    }
    public static String getPluralForm(String s){
        if (s == null) return null;
        if (s.isEmpty()) return s;
        //todo: better dude...
        return s + "s";
    }

}
