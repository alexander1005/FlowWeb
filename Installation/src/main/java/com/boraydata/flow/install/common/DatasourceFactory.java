package com.boraydata.flow.install.common;

import java.util.*;

public class DatasourceFactory {

    public static Properties dscPropers;

    static {
        dscPropers = new Properties();
        dscPropers.setProperty("ORACLE", "oracle.jdbc.driver.OracleDriver");
        dscPropers.setProperty("DB2", "com.ibm.db2.jcc.DB2Driver");
        dscPropers.setProperty("MYSQL", "com.mysql.jdbc.Driver");
        dscPropers.setProperty("POSTGRES", "org.postgresql.Driver");
        dscPropers.setProperty("VOLTDB", "org.voltdb.jdbc.Driver");
        dscPropers.setProperty("GREENPLUM", "com.pivotal.jdbc.GreenplumDriver");
        dscPropers.setProperty("MEMSQL", "com.mysql.jdbc.Driver");
        dscPropers.setProperty("RDP", "com.rapidsdata.jdbcdriver.Driver");
        dscPropers.setProperty("SPARK", "org.apache.hive.jdbc.HiveDriver");
    }
    private DatasourceFactory() {
        throw new IllegalStateException("DatasourceConnectionFactory Is Utility Class");
    }

}
