package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.persistence.PersistentField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:14
 */
public abstract class FieldFilterCondition {

    public static FieldFilterCondition concatenate(
            FieldFilterLogicalOperatorType logicalOperator,
            FieldFilterCondition firstCondition,
            FieldFilterCondition secondCondition) {
        return concatenate(logicalOperator, firstCondition, secondCondition,
                new FieldFilterCondition[0]);
    }

    public static FieldFilterCondition concatenate(
            FieldFilterLogicalOperatorType logicalOperator,
            FieldFilterCondition firstCondition,
            FieldFilterCondition secondCondition,
            FieldFilterCondition... moreConditions) {
        CompoundFieldFilterCondition retVal = new CompoundFieldFilterCondition(
                logicalOperator, firstCondition, secondCondition);
        if (moreConditions != null && moreConditions.length > 0) {
            for (FieldFilterCondition curr : moreConditions) {
                retVal = new CompoundFieldFilterCondition(logicalOperator,
                        retVal, curr);
            }
        }
        return retVal;
    }

    public static <T> FieldFilterCondition createInClause(
            FieldDescriptor<T> field,
            Collection<T> values) {
        if (values == null || values.isEmpty()) return  null;
        if (values.size() == 1){
            return new BasicFieldFilterCondition<T>(field, FilterConditionOperatorType.EQUALS, values.iterator().next());
        }else {
            Iterator<T> iterator = values.iterator();
            FieldFilterCondition compoundCondition = concatenate(FieldFilterLogicalOperatorType.OR, new BasicFieldFilterCondition<T>(field, FilterConditionOperatorType.EQUALS, iterator.next()), new BasicFieldFilterCondition<T>(field, FilterConditionOperatorType.EQUALS, iterator.next()));
            while (iterator.hasNext()){
                compoundCondition = concatenate(FieldFilterLogicalOperatorType.OR, compoundCondition,  new BasicFieldFilterCondition<T>(field, FilterConditionOperatorType.EQUALS, iterator.next()));
            }
            return compoundCondition;

        }
    }

    public static FieldFilterCondition concatenate(
            FieldFilterLogicalOperatorType logicalOperator,
            Collection<FieldFilterCondition> conditions) {
        if (conditions.isEmpty()) {
            throw new IllegalStateException("Cannot create an empty condition");
        } else if (conditions.size() == 1) {
            return conditions.iterator().next();
        } else {
            List<FieldFilterCondition> conditionList = new ArrayList<FieldFilterCondition>(
                    conditions);
            CompoundFieldFilterCondition retVal = new CompoundFieldFilterCondition(
                    logicalOperator, conditionList.remove(0),
                    conditionList.remove(0));
            while (!conditionList.isEmpty()) {
                retVal = new CompoundFieldFilterCondition(logicalOperator,
                        retVal, conditionList.remove(0));
            }
            return retVal;
        }
    }


}
