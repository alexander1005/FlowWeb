package com.boraydata.flowregistry.entity.dto;

public class ApplyCertsDTO {
    Integer days;
    Boolean renew;

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Boolean getRenew() {
        return renew;
    }

    public void setRenew(Boolean renew) {
        this.renew = renew;
    }
}
