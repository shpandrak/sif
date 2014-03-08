package com.shpandrak.persistence.query.filter;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:17
 */
public enum FilterConditionOperatorType {
    EQUALS("=", "="), GREATER_THEN(">", ">"), GREATER_OR_EQUALS(">=", ">="), LESS_THEN(
            "<", "<"), LESS_OR_EQUALS("<=", "<="), NOT_EQUALS("!=", "<>");

    private final String stringRepresentation;
    private final String sqlRepresentation;

    FilterConditionOperatorType(String stringRepresentation,
                                String sqlRepresentation) {
        this.stringRepresentation = stringRepresentation;
        this.sqlRepresentation = sqlRepresentation;
    }

    public String getSqlRepresentation() {
        return sqlRepresentation;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

}
