package com.boraydata.test;

import com.boraydata.workflow.WorkFlowClient;

import java.util.List;

public class KStreamStudioTest {
    public static void main(String[] args) {
        String s="{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"join_test\",\n" +
                "    \"sources\": [\n" +
                "      {\"frn\": \"stream:csv1\"},\n" +
                "      {\"frn\": \"stream:csv2\"}\n" +
                "    ],\n" +
                "    \"transforms\": [\n" +
                "      {\n" +
                "        \"frn\": \"func:CsvParser\",\n" +
                "        \"from\": \"csv1\",\n" +
                "        \"to\": \"a\",\n" +
                "        \"params\": {\n" +
                "          \"mapping\": [\n" +
                "            {\n" +
                "              \"SqlType\": \"bigint\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"SqlType\": \"bigint\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"frn\": \"func:CsvParser\",\n" +
                "        \"from\": \"csv2\",\n" +
                "        \"to\": \"b\",\n" +
                "        \"params\": {\n" +
                "          \"mapping\": [\n" +
                "            {\n" +
                "              \"SqlType\": \"bigint\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"SqlType\": \"bigint\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"frn\": \"func:SqlJoin\",\n" +
                "        \"from\": \"a,b\",\n" +
                "        \"to\": \"c\",\n" +
                "        \"params\": {\n" +
                "          \"mapping\": [\n" +
                "            {\n" +
                "              \"table1\": \"a\",\n" +
                "              \"columns\": \"a1, a2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"table2\": \"b\",\n" +
                "              \"columns\": \"b1, b2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"sql\": \"select * from table1, table2 where a1 > 0\"\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"output\": {\n" +
                "      \"frn\": \"stream:db1\",\n" +
                "      \"from\": \"c\",\n" +
                "      \"destinations\": [\n" +
                "        {\n" +
                "          \"type\": \"mysql\",\n" +
                "          \"table\": \"test\",\n" +
                "          \"mode\": \"upsert\",\n" +
                "          \"host\": \"flow2\",\n" +
                "          \"port\": \"3306\",\n" +
                "          \"database\": \"flowtest\",\n" +
                "          \"username\": \"flowsinktest\",\n" +
                "          \"password\": \"Flow2sinktest@flow\",\n" +
                "          \"batchsize\": 1,\n" +
                "          \"batch_interval\": 2000,\n" +
                "          \"max_retry\": 0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        List<String> streams=WorkFlowClient.parseWorkflowStreams(s);
        System.out.println("streams="+streams);
    }
}
