package com.boraydata.flowregistry.common.type;

import com.boraydata.flowregistry.utils.FlowConstants;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalProcessor implements Processor {

    private int precision = 10;
    private int scale = 0;

    @Override
    public SchemaBuilder.FieldAssembler<Schema> avroSchemaProcessor(SchemaBuilder.FieldAssembler<Schema> schema, String name) {
        return schema.name(name).type(LogicalTypes.decimal(precision, scale).addToSchema(Schema.create(Schema.Type.BYTES))).noDefault();
    }

    @Override
    public boolean isSupport(String sqlType) {
        Matcher m = Pattern.compile("[0-9A-Z]+").matcher(sqlType);
        if (m.find())
            precision = Integer.parseInt(m.group());
        if (m.find())
            scale = Integer.parseInt(m.group());
        return sqlType.startsWith(FlowConstants.TYPE_DECIMAL);
    }
}
