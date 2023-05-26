package com.boraydata.flowregistry.common.type;

import com.boraydata.flowregistry.utils.FlowConstants;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class TinyIntProcessor implements Processor {
    @Override
    public SchemaBuilder.FieldAssembler<Schema> avroSchemaProcessor(SchemaBuilder.FieldAssembler<Schema> schema, String name) {
        return schema.name(name).type().intType().noDefault();
    }

    @Override
    public boolean isSupport(String sqlType) {
        return FlowConstants.TYPE_TINYINT.equals(sqlType);
    }
}
