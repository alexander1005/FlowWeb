package com.boraydata.flowregistry.common.type;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public interface Processor {
    SchemaBuilder.FieldAssembler<Schema> avroSchemaProcessor(SchemaBuilder.FieldAssembler<Schema> schema, String name);

    boolean isSupport(String sqlType);
}
