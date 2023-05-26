package com.boraydata.flowauth.enums;

public enum FlowDefStatus {

    ENABLE(1),
    DISABLE(2),
    ACTIVE(3),
    DELETING(4);

    public static final FlowDefStatus[] values = FlowDefStatus.values();

    public int status;

    FlowDefStatus(int status) {
        this.status = status;
    }

    public static FlowDefStatus valueOf(Integer i) {

        try {
            return values[i - 1];
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer value(FlowDefStatus schemaType) {
        return schemaType.ordinal() + 1;
    }
}
