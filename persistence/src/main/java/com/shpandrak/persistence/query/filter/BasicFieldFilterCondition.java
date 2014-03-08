package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.field.FieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:16
 */
public class BasicFieldFilterCondition<T> extends FieldFilterCondition {
    private FieldDescriptor<T> field;
    private FilterConditionOperatorType operatorType;
    private T value;

    public static <T> BasicFieldFilterCondition<T> build(FieldDescriptor<T> field,
                                                         FilterConditionOperatorType operatorType,
                                                         T value){
        return new BasicFieldFilterCondition<T>(field, operatorType, value);
    }

    public BasicFieldFilterCondition(FieldDescriptor<T> field,
                                     FilterConditionOperatorType operatorType, T value) {
        this.field = field;
        this.operatorType = operatorType;
        this.value = value;
    }

    public FieldDescriptor<T> getField() {
        return field;
    }

    public FilterConditionOperatorType getOperatorType() {
        return operatorType;
    }

    public T getValue() {
        return value;
    }

    public void setOperatorType(FilterConditionOperatorType operatorType) {
        this.operatorType = operatorType;
    }


    @Override
    public String toString() {
        return field.toString() + " " +  operatorType + ' ' + value;
    }


}
