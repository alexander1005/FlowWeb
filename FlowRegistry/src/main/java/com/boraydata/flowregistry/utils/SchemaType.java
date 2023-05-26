package com.boraydata.flowregistry.utils;

public enum SchemaType {
    csv, avro, protobuf, database, orc, parquet, json, redis, e1, empty;

    public static final SchemaType[] values = SchemaType.values();

    public static SchemaType valueOf(Integer i) {
        return values[i - 1];
    }

    public static Integer value(SchemaType schemaType) {
        return schemaType.ordinal() + 1;
    }
}
