package com.shpandrak.gae.datastore.query;

import java.util.Arrays;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:31
 */
class GDSSimpleQueryCondition {
    private String fieldName;
    private String operator;
    private String valueString;
    private List<Object> conditionParams;


    GDSSimpleQueryCondition(String fieldName, String operator, String valueString, Object conditionParam) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.valueString = valueString;
        this.conditionParams = Arrays.asList(conditionParam);
    }

    GDSSimpleQueryCondition(String fieldName, String operator, String valueString, List<Object> conditionParams) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.valueString = valueString;
        this.conditionParams = conditionParams;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOperator() {
        return operator;
    }

    public String getValueString() {
        return valueString;
    }

    public List<Object> getConditionParams() {
        return conditionParams;
    }
}
