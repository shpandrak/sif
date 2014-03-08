package com.shpandrak.database.managers;

import com.shpandrak.database.DBException;
import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.converters.LongQueryConverter;
import com.shpandrak.database.query.QueryService;
import com.shpandrak.database.query.QueryServiceBean;
import com.shpandrak.database.query.parser.DBQueryFilterParser;
import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBKeyField;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistentField;
import com.shpandrak.persistence.managers.IReadOnlyManager;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FilterConditionOperatorType;
import com.shpandrak.persistence.query.filter.QueryFilter;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:03
 */
public abstract class DBBaseReadOnlyManagerBean<T extends IPersistableShpandrakObject> implements IReadOnlyManager<T> {
    protected DBTable dbTable;
    protected IDBConnectionProvider connectionProvider;
    protected QueryService queryService;


    protected IQueryConverter<T> getQueryConverter(){
        return getQueryConverter(null);
    }

    protected abstract IQueryConverter<T> getQueryConverter(String tableAlias);

    protected abstract Class<T> getEntityClass();

    protected abstract ShpandrakObjectDescriptor getDescriptor();

    protected DBBaseReadOnlyManagerBean(DBTable dbTable, IDBConnectionProvider connectionProvider, QueryService queryService) {
        this.connectionProvider = connectionProvider;
        this.dbTable = dbTable;
        this.queryService = queryService;
    }

    protected DBBaseReadOnlyManagerBean(DBTable dbTable, IDBConnectionProvider connectionProvider) {
        this(dbTable, connectionProvider, new QueryServiceBean(connectionProvider));
    }

    @Override
    public List<T> list() throws PersistenceException {
        return list(null);
    }

    @Override
    public List<T> list(QueryFilter filter) throws DBException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        return getQueryService().getList(queryFilterParser.getSql(), getQueryConverter(queryFilterParser.getTableAlias()), queryFilterParser.getParams());
    }

    @Override
    public <F> List<T> listByField(FieldDescriptor<F> field, F value) throws PersistenceException {
        return list(new QueryFilter(new BasicFieldFilterCondition<F>(field, FilterConditionOperatorType.EQUALS, value)));
    }

    @Override
    public Map<Key, T> getMapById() throws DBException {
        return getMapByField((FieldDescriptor<Key>)getDescriptor().getFieldDescriptorsMap().get("id"));
    }

    @Override
    public Map<Key, T> getMapById(QueryFilter filter) throws PersistenceException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        return getMapByField(dbFieldFromField((FieldDescriptor<Key>) getDescriptor().getFieldDescriptorsMap().get("id")), queryFilterParser.getTableAlias(), queryFilterParser.getSql(), queryFilterParser.getParams());
    }

    @Override
    public <F> T getByField(FieldDescriptor<F> field, F value) throws PersistenceException {
        List<T> list = listByField(field, value);
        if (list.isEmpty()) {
            return null;
        }
        else if (list.size() > 1){
            throw new PersistenceException("Multiple entries found by field " + field.getName() + " with value " + value + " while expected only one result");
        }
        return list.get(0);
    }

    @Override
    public <F> Map<F, T> getMapByField(FieldDescriptor<F> field) throws DBException {
        return getMapByField(dbFieldFromField(field), null);
    }

    @Override
    public <F> Map<F, T> getMapByField(FieldDescriptor<F> field, QueryFilter filter) throws PersistenceException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, getDbTable());
        return getMapByField(dbFieldFromField(field), queryFilterParser.getTableAlias(), queryFilterParser.getSql(), queryFilterParser.getParams());
    }

    protected  <F> DBField<F> dbFieldFromField(FieldDescriptor<F> field) {
        return getDbTable().getFieldsMap().get(field.getName());
    }

    protected  <F> Map<F, T> getMapByField(PersistentField<F> field, String tableAlias) throws DBException {
        return getMapByField(field, tableAlias, dbTable.getAllFieldsSQL(), null);
    }

    protected  <F> Map<F, T> getMapByField(PersistentField<F> field, String tableAlias, String sql, List<Object> params) throws DBException {
        return getQueryService().getMap(sql, ((DBField<F>)field).getQueryConverter(tableAlias), getQueryConverter(tableAlias), params);
    }


    @Override
    public T getById(Key id) throws PersistenceException {
        String sql = dbTable.getAllFieldsSQL() + " where id = ?";
        List<Object> params = DBUtil.keyAsParams(id);
        return getQueryService().getSingleValue(sql, getQueryConverter(), params);
    }

    @Override
    public List<T> getByIds(Collection<Key> ids) throws PersistenceException {
        //todo:implement in clause.. too lazy today...
        List<T> listByIds = new ArrayList<T>(ids.size());
        for (Key currId : ids){
            listByIds.add(getById(currId));
        }
        return listByIds;
    }

    protected Map<Key, T> getMapByIdForSQL(String sql, List<Object> params, String tableAlias) throws DBException {
        return getQueryService().getMap(sql, ((DBKeyField)dbTable.getFields().get(0)).getQueryConverter(tableAlias), getQueryConverter(tableAlias), params);
    }


    protected QueryService getQueryService() {
        return queryService;
    }

    public DBTable getDbTable() {
        return dbTable;
    }

    protected IDBConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public <F> Collection<F> listFieldValues(FieldDescriptor<F> fieldDescriptor, QueryFilter filter) throws PersistenceException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        //todo: create an sql with only the desired field... for better efficiency...
        return getQueryService().getList(queryFilterParser.getSql(), dbFieldFromField(fieldDescriptor).getQueryConverter(queryFilterParser.getTableAlias()), queryFilterParser.getParams());
    }

    @Override
    public Set<Key> listIds(QueryFilter filter) throws DBException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        //todo: create an sql with only the desired field... for better efficiency...
        return getQueryService().getSet(queryFilterParser.getSql(), ((DBKeyField) dbTable.getFieldsMap().get("id")).getQueryConverter(queryFilterParser.getTableAlias()), queryFilterParser.getParams());
    }

    @Override
    public Key getObjectId(QueryFilter filter) throws PersistenceException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        //todo: create an sql with only the desired field... for better efficiency...
        return getQueryService().getSingleValue(queryFilterParser.getSql(), ((DBKeyField) dbTable.getFieldsMap().get("id")).getQueryConverter(queryFilterParser.getTableAlias()), queryFilterParser.getParams());
    }

    @Override
    public T findObject(QueryFilter filter) throws PersistenceException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        return getQueryService().getSingleValue(queryFilterParser.getSql(), getQueryConverter(queryFilterParser.getTableAlias()), queryFilterParser.getParams());
    }

    @Override
    public long count() throws DBException {
        return getQueryService().getSingleValue("select count(*) from " + dbTable.getName(), new LongQueryConverter(1), null);
    }

    @Override
    public long count(QueryFilter filter) throws DBException {
        DBQueryFilterParser queryFilterParser = new DBQueryFilterParser(filter, dbTable);
        queryFilterParser.parse();
        //todo: create an sql with count - don't do this ugly trick...
        String sql = queryFilterParser.getSql();
        sql = "select count(*) " + sql.substring(sql.indexOf("from"));
        return getQueryService().getSingleValue(sql, new LongQueryConverter(1), queryFilterParser.getParams());
    }

    @Override
    public <F> long countByField(FieldDescriptor<F> fieldDescriptor, F value) throws DBException {
        return count(new QueryFilter(new BasicFieldFilterCondition<F>(fieldDescriptor, FilterConditionOperatorType.EQUALS, value)));
    }
}
