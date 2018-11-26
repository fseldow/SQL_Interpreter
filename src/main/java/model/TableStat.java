package model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TableStat {
    private String alias;
    private int count;
    Map<String, ColumnStat> fieldStatSchema;

    public TableStat(String alias) {
        this.alias = alias;
        fieldStatSchema = new HashMap<>();
    }

    public String getAlias() {
        return this.alias;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public void setFieldStatSchema (String schemaKey, ColumnStat columnStat) {
        fieldStatSchema.put(schemaKey, columnStat);
    }

    public void paserFromStatString (String data) {
        String [] splits = data.split("\\s+");
        if (splits.length < 2) {
            throw new IllegalArgumentException();
        }
        this.count = Integer.valueOf(splits[1]);
        for (int i = 2; i< splits.length; i+=3) {
            String schemaKey = alias + "." + splits[i];
            int minValue = Integer.valueOf(splits[i+1]);
            int maxValue = Integer.valueOf(splits[i+2]);
            fieldStatSchema.put(schemaKey, new ColumnStat(minValue, maxValue));
        }
    }

    public ColumnStat getStat (String schemaKey) {
        if (!fieldStatSchema.containsKey(schemaKey)) {
            return null;
        }
        return fieldStatSchema.get(schemaKey);
    }

    public void removeColumn(String schemaKey) {
        if (fieldStatSchema.containsKey(schemaKey)) {
            fieldStatSchema.remove(schemaKey);
        }
    }

    public void resetColumnData(String schemaKey, Integer minValue, Integer maxValue) {
        if (minValue != null) {
            fieldStatSchema.get(schemaKey).minValue = minValue;
        }
        if (maxValue != null) {
            fieldStatSchema.get(schemaKey).maxValue = maxValue;
        }
    }
}