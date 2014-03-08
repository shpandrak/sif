package com.shpandrak.datamodel;

import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/20/12
 * Time: 17:27
 */
public class ShpandrakObjectRawData {
    private Map<String, Object> fieldsData;

    public ShpandrakObjectRawData(Map<String, Object> fieldsData) {
        this.fieldsData = fieldsData;
    }

    public Map<String, Object> getFieldsData() {
        return fieldsData;
    }
}
