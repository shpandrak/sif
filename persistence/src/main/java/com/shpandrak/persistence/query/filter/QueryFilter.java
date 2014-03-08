package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.OrderByClauseEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:10
 */
public class QueryFilter {
    private FieldFilterCondition condition;
    private Integer sizeLimit;
    private Integer pageNumber;
    private List<OrderByClauseEntry> orderByClause;

    public QueryFilter(FieldFilterCondition condition) {
        this(condition, null, null);
    }

    public QueryFilter(FieldFilterCondition condition, Integer sizeLimit) {
        this(condition, sizeLimit, null);
    }

    public QueryFilter(FieldFilterCondition condition, Integer sizeLimit,
                       Integer pageNumber) {
        this.condition = condition;
        this.sizeLimit = sizeLimit;
        this.pageNumber = pageNumber;
    }

    public QueryFilter(FieldFilterCondition condition, Integer sizeLimit, Integer pageNumber, List<OrderByClauseEntry> orderByClause) {
        this.condition = condition;
        this.sizeLimit = sizeLimit;
        this.pageNumber = pageNumber;
        this.orderByClause = orderByClause;
    }

    public FieldFilterCondition getCondition() {
        return condition;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public List<OrderByClauseEntry> getOrderByClause() {
        return orderByClause;
    }

    public boolean isEmpty(){
        return condition == null && (orderByClause == null || orderByClause.isEmpty());
    }

    public void setOrderByClause(List<OrderByClauseEntry> orderByClause) {
        this.orderByClause = orderByClause;
    }
}
