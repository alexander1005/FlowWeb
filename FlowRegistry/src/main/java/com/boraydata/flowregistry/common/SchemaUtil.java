package com.boraydata.flowregistry.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boraydata.flowregistry.common.type.*;
import com.boraydata.flowregistry.utils.FlowConstants;
import com.boraydata.flowregistry.utils.SchemaData;
import com.boraydata.flowregistry.utils.SchemaType;
import lombok.Getter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.boraydata.flowregistry.utils.SchemaType.*;

@Getter
@Component
public class SchemaUtil {

    private final Map<SchemaType, Function<JSONObject, String>> schemaParser = new HashMap<>();
    private final List<Processor> processors;

    public SchemaUtil() {
        this.processors = setupProcessor();

        this.schemaParser.put(csv, (JSONObject streamJson) -> {
            List<SchemaData> schemaData = JSONObject.parseArray(streamJson.getJSONArray(FlowConstants.BEFORE_DATA).toJSONString(), SchemaData.class);
            JSONObject schemaJsonObject = streamJson.getJSONObject(FlowConstants.BEFORE_DEF);
            schemaJsonObject.put(FlowConstants.AFTER_COLUMN_MAPPING, schemaData);
            StringBuilder strBuilder = schemaData.stream().map(s -> new StringBuilder(s.getName())).reduce((a, b) -> a.append(FlowConstants.SEPARATOR).append(b)).orElse(null);
            schemaJsonObject.put(FlowConstants.AFTER_COLUMNS, strBuilder.toString());
            return JSON.toJSONString(schemaJsonObject);
        });

        this.schemaParser.put(avro, (JSONObject streamJson) -> {
            List<SchemaData> schemaData = JSONObject.parseArray(streamJson.getJSONArray(FlowConstants.BEFORE_DATA).toJSONString(), SchemaData.class);
            SchemaBuilder.FieldAssembler<Schema> flowSchema = SchemaBuilder.record(FlowConstants.RECORD_NAME).namespace(FlowConstants.NAME_SPACE).fields();
            SchemaBuilder.FieldAssembler<Schema> schema = schemaData.stream().map(s -> {
                Processor processor = getType(processors, s.getSqlType());
                if (processor == null)
                    throw new RuntimeException("SqlType illegal");
                return processor.avroSchemaProcessor(flowSchema, s.getName());
            }).reduce((f, s) -> s).orElse(null);
            return schema.endRecord().toString();
        });

        this.schemaParser.put(protobuf, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

        this.schemaParser.put(database, (JSONObject streamJson) -> {
            List<SchemaData> schemaData = JSONObject.parseArray(streamJson.getJSONArray(FlowConstants.BEFORE_DATA).toJSONString(), SchemaData.class);
            JSONObject schemaJsonObject = streamJson.getJSONObject(FlowConstants.BEFORE_DEF);
            schemaJsonObject.put(FlowConstants.AFTER_COLUMN_MAPPING, schemaData);
            StringBuilder strBuilder = schemaData.stream().map(s -> new StringBuilder(s.getName())).reduce((a, b) -> a.append(FlowConstants.SEPARATOR).append(b)).orElse(null);
            schemaJsonObject.put(FlowConstants.AFTER_COLUMNS, strBuilder.toString());
            return JSON.toJSONString(schemaJsonObject);
        });

        this.schemaParser.put(orc, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

        this.schemaParser.put(parquet, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

        this.schemaParser.put(json, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

        this.schemaParser.put(redis, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

        this.schemaParser.put(empty, (JSONObject streamJson) -> JSON.toJSONString(streamJson));

    }

    private List<Processor> setupProcessor() {
        List<Processor> processors = new ArrayList<>();
        processors.add(new BigIntProcessor());
        processors.add(new BooleanProcessor());
        processors.add(new CharProcessor());
        processors.add(new DateProcessor());
        processors.add(new DecimalProcessor());
        processors.add(new DoubleProcessor());
        processors.add(new FloatProcessor());
        processors.add(new IntProcessor());
        processors.add(new SmallIntProcessor());
        processors.add(new TextProcessor());
        processors.add(new TimestampProcessor());
        processors.add(new TinyIntProcessor());
        processors.add(new VarCharProcessor());
        return processors;
    }

    private Processor getType(List<Processor> processors, String sqlType) {
        for (Processor pro : processors)
            if (pro.isSupport(sqlType))
                return pro;
        return null;
    }
}
