package com.shpandrak.persistence.query.filter;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:14
 */
public class CompoundFieldFilterCondition extends FieldFilterCondition {
    private FieldFilterLogicalOperatorType logicalOperatorType;
    private FieldFilterCondition leftSide;
    private FieldFilterCondition rightSide;

    public CompoundFieldFilterCondition(
            final FieldFilterLogicalOperatorType logicalOperatorType,
            final FieldFilterCondition leftSide, final FieldFilterCondition rightSide) {
        this.logicalOperatorType = logicalOperatorType;
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public CompoundFieldFilterCondition() {
    }

    public FieldFilterLogicalOperatorType getLogicalOperatorType() {
        return logicalOperatorType;
    }

    public FieldFilterCondition getLeftSide() {
        return leftSide;
    }

    public FieldFilterCondition getRightSide() {
        return rightSide;
    }

    public void setLogicalOperatorType(
            final FieldFilterLogicalOperatorType logicalOperatorType) {
        this.logicalOperatorType = logicalOperatorType;
    }

    public void addCondition(final BasicFieldFilterCondition condition) {
        if (leftSide == null) {
            leftSide = condition;
        } else if (leftSide != null && rightSide == null) {
            final CompoundFieldFilterCondition left = new CompoundFieldFilterCondition(
                    logicalOperatorType, leftSide, condition);
            leftSide = left;
            logicalOperatorType = null;
        }
    }

    public boolean isValid() {
        return leftSide != null && rightSide != null
                && logicalOperatorType != null;
    }

    @Override
    public String toString() {
        try {
            String output = leftSide.toString();
            if (getLogicalOperatorType() != null) {
                output = output + getLogicalOperatorType();
            }
            if (rightSide != null) {
                output = output + rightSide.toString();
            }
            return output;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return super.toString();
    }

}
