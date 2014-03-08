package com.shpandrak.database.table.relationship;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.managers.ShpandrakObjectQueryConverter;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.relationship.BasePersistableRelationshipEntry;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/7/12
 * Time: 11:25
 */
public class RelationshipEntryQueryConverter<TARGET_CLASS extends BaseEntity> extends ShpandrakObjectQueryConverter<BasePersistableRelationshipEntry<TARGET_CLASS>>{
    private final IQueryConverter<TARGET_CLASS> targetEntityQueryConverter;

    /**
     * Use this constructor to load the relationship with "Id level"
     * @param dbTable relationship table
     * @param tableAlias table alias
     * @param objectDescriptor relationship entry descriptor
     */
    public RelationshipEntryQueryConverter(DBTable dbTable, String tableAlias, ShpandrakObjectDescriptor objectDescriptor) {
        super(dbTable, tableAlias, objectDescriptor);
        targetEntityQueryConverter = null;
    }

    /**
     * Use this constructor to load the relationship with "Full Level"
     * @param dbTable relationship table
     * @param tableAlias table alias
     * @param objectDescriptor relationship entry descriptor
     * @param targetEntityQueryConverter target entity query converter - used to load the target entity
     */
    public RelationshipEntryQueryConverter(DBTable dbTable, String tableAlias, ShpandrakObjectDescriptor objectDescriptor, IQueryConverter<TARGET_CLASS> targetEntityQueryConverter) {
        super(dbTable, tableAlias, objectDescriptor);
        this.targetEntityQueryConverter = targetEntityQueryConverter;
    }


    @Override
    public BasePersistableRelationshipEntry<TARGET_CLASS> convert(ResultSet resultSet) throws SQLException {

        BasePersistableRelationshipEntry<TARGET_CLASS> convertedEntry = super.convert(resultSet);
        if (targetEntityQueryConverter != null){
            // Load Full
            TARGET_CLASS targetEntity = this.targetEntityQueryConverter.convert(resultSet);
            convertedEntry.setTargetEntity(targetEntity);
        }
        return convertedEntry;
    }
}
