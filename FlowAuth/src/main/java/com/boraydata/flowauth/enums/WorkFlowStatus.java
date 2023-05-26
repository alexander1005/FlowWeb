package com.boraydata.flowauth.enums;

public enum WorkFlowStatus {
    DRAFT(1),
    PUBLISH(2);

    public static final WorkFlowStatus[] values = WorkFlowStatus.values();

    public int status;

    WorkFlowStatus(int status) {
        this.status = status;
    }

    public static WorkFlowStatus valueOf(Integer i) {
        return values[i - 1];
    }

    public static Integer value(WorkFlowStatus schemaType) {
        return schemaType.ordinal() + 1;
    }
}
