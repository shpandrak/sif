package com.shpandrak.database.util;

import com.shpandrak.datamodel.field.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 10:37
 */
public abstract class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    public static <T extends Enum> T getEnumFromResultSet(ResultSet rset, Enum field, T[] enumValues) throws SQLException {
        return getEnumFromResultSet(rset, field.name(), enumValues);
    }

    public static <T extends Enum> T getEnumFromResultSet(ResultSet rset, String field, T[] enumValues) throws SQLException {
        int enumOrdinal = rset.getInt(field);
        return rset.wasNull() ? null : enumValues[enumOrdinal];
    }

    public static <T extends Enum> T getEnumFromResultSet(ResultSet rset, int field, T[] enumValues) throws SQLException {
        int enumOrdinal = rset.getInt(field);
        return rset.wasNull() ? null : enumValues[enumOrdinal];
    }


    public static String getStringFromResultSetEmptyIsNull(ResultSet rset, String fieldName) throws SQLException {
        String string = rset.getString(fieldName);
        if (string != null && string.isEmpty()) return null;
        return string;
    }

    public static String getStringFromResultSetEmptyIsNull(ResultSet rset, int column) throws SQLException {
        String string = rset.getString(column);
        if (string != null && string.isEmpty()) return null;
        return string;
    }


    public static long getPositiveLongValueFromResultset(ResultSet rset, String fieldName) throws SQLException {
        long retVal = rset.getLong(fieldName);
        return rset.wasNull() ? -1 : retVal;
    }

    public static Long getLongFromResultSet(ResultSet rset, String fieldName) throws SQLException {
        long retVal = rset.getLong(fieldName);
        return rset.wasNull() ? null : retVal;
    }

    public static Long getLongFromResultSet(ResultSet rset, Enum field) throws SQLException {
        return getLongFromResultSet(rset, field.name());
    }

    public static Long getLongFromResultSet(ResultSet rset, int fieldIdx) throws SQLException {
        long retVal = rset.getLong(fieldIdx);
        return rset.wasNull() ? null : retVal;
    }

    public static long getTimeStampForSql(long date) {
        return date;
    }

    public static Long getTimeStampForSql(Date date) {
        //There is some SQLs use is null on date filed.
        if (date == null) return null; //IEntity.TIME_UNKNOWN;
        else return date.getTime();
    }

    public static Long getIdForSql(long id) {
        if (id <= 0) return null;
        else return id;
    }

    public static String getExternalIdForSql(UUID id) {
        if (id == null) return null;
        return id.toString();
    }

    public static Long getLongDateForSQL(Date date) {
        if (date == null) return null;
        else return date.getTime();
    }

    public static Long getPositiveLongForSql(long positiveMayBeNull) {
        if (positiveMayBeNull <= 0) return null;
        else return positiveMayBeNull;
    }

    public static Integer getBooleanForSql(Boolean value) {
        return (value == null) ? null : (value ? 1 : 0);
    }

    public static Date getDateFromResultSet(ResultSet rset, Enum field) throws SQLException {
        return getDateFromResultSet(rset, field.name());
    }

    public static long getDateAsLong(ResultSet rset, Enum field) throws SQLException {
        return getDateAsLong(rset, field.name());
    }

    public static long getDateAsLong(ResultSet rset, String column)
            throws SQLException {
        return rset.getLong(column);
    }

    public static long getDateAsLong(ResultSet rset, int column) throws SQLException {
        return rset.getLong(column);
    }

    public static Date getDateFromResultSet(ResultSet rset, String column)
            throws SQLException {
        long timestamp = rset.getLong(column);
        if (timestamp == 0) return null;
        return new Date(timestamp);
    }

    public static Date getDateFromResultSet(ResultSet rset, int column)
            throws SQLException {
        long timestamp = rset.getLong(column);
        if (timestamp == 0) return null;
        return new Date(timestamp);
    }


    public static boolean getBooleanFromResultSet(ResultSet rset, String field) throws SQLException {
        return rset.getBoolean(field);
    }

    public static boolean getBooleanFromResultSet(ResultSet rset, int fieldNumber) throws SQLException {
        return rset.getBoolean(fieldNumber);
    }

    public static Boolean getNullableBooleanFromResultSet(ResultSet rset, String fieldName) throws SQLException {
        Boolean retVal = rset.getBoolean(fieldName);
        return rset.wasNull() ? null : retVal;
    }

    public static Boolean getNullableBooleanFromResultSet(ResultSet rset, Enum field) throws SQLException {
        return getNullableBooleanFromResultSet(rset, field.name());
    }

    public static Integer getEnumForSQL(Enum enm) {
        return (enm == null) ? null : enm.ordinal();
    }

    public static List<Object> longAsParams(long value) {
        return Arrays.<Object>asList(value);
    }

    public static List<Object> uuidAsParams(UUID id) {
        return Arrays.<Object>asList(getUUIDForSql(id));
    }

    public static List<Object> keyAsParams(Key key) {
        return Arrays.<Object>asList(getKeyForSQL(key));
    }

    public static String getStringForSql(String s) {
        return s;
    }

    public static String getStringForSql(String s, int maxLength) {
        if ((s != null) && (s.length() > maxLength)) {
            StringBuilder builder = new StringBuilder("The given string length (");
            builder.append(s.length());
            builder.append(") is longer then the maximum column length. Truncating to '");
            builder.append(maxLength);
            builder.append("' characters");

            logger.warn(builder.toString());
            s = s.substring(0, maxLength - 1);
        }

        return getStringForSql(s);
    }

    public static String getNonEmptyStringForSql(String s, String emptyStringReplacement) {
        String value = s;
        if (value == null || "".equals(value)) {
            value = emptyStringReplacement;
        }
        return getStringForSql(value);

    }

    public static int getIntForSql(int n) {
        return n;
    }


    public static String getTimeZoneForSQL(TimeZone timeZone) {
        return timeZone == null ? getStringForSql(TimeZone.getDefault().getID()) : getStringForSql(timeZone.getID());
    }

    public static int getIntFromResultSet(ResultSet rset, Enum field) throws SQLException {
        return getIntFromResultSet(rset, field.name());
    }

    public static int getIntFromResultSet(ResultSet rset, String field) throws SQLException {
        return rset.getInt(field);
    }

    public static int getIntFromResultSet(ResultSet rset, int fieldIndex) throws SQLException {
        return rset.getInt(fieldIndex);
    }

    public static Integer getIntegerFromResultSet(ResultSet rset, int fieldIndex) throws SQLException {
        int value = rset.getInt(fieldIndex);
        return value == 0 ? null : value;
    }

    public static Date getLongDateFromResultSet(ResultSet rset, Enum field) throws SQLException {
        return getLongDateFromResultSet(rset, field.name());
    }

    public static Date getLongDateFromResultSet(ResultSet rset, String fieldName) throws SQLException {
        long retVal = rset.getLong(fieldName);
        return rset.wasNull() ? null : new Date(retVal);
    }

    public static String getEnumInClause(Set<? extends Enum> values) {
        String result;

        if (values.isEmpty()) {
            return "in (null)";
        } else {
            List<Enum> lst = new ArrayList<Enum>(values);
            result = "in (" + getEnumForSQL(lst.get(0));
            for (int i = 1; i < lst.size(); ++i) {

                result += "," + getEnumForSQL(lst.get(i));
            }

            result += ")";
        }

        return result;
    }

    public static UUID getUUIDFromResultSet(ResultSet rset, String name) throws SQLException {

        String val = rset.getString(name);
        if (val != null && !val.isEmpty())
            return UUID.fromString(rset.getString(name));
        return null;
    }

    public static Object getUUIDForSql(UUID uuid) {
        if (uuid != null)
            return uuid;
        return null;

    }
    public static Object getKeyForSQL(Key key) {
        if (key != null)
            return key.toPersistentFormat();
        return null;
    }

    public static String nameWithAliasPrefix(String name, String aliasPrefix){
        return aliasPrefix == null ? name : aliasPrefix + "_" + name;
    }
}
