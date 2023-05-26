package com.boraydata.flowregistry.common.type;

import com.boraydata.flowregistry.utils.FlowConstants;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class VarCharProcessor implements Processor {
    @Override
    public SchemaBuilder.FieldAssembler<Schema> avroSchemaProcessor(SchemaBuilder.FieldAssembler<Schema> schema, String name) {
        return schema.name(name).type().stringType().noDefault();
    }

    @Override
    public boolean isSupport(String sqlType) {
        return sqlType.startsWith(FlowConstants.TYPE_VARCHAR);
    }
}
