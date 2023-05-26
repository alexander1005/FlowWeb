package com.boraydata.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
public class JobState {
    private String jobName;
    private String jobState;

    public JobState(String jobName, String jobState) {
        this.jobName = jobName;
        this.jobState = jobState;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobState() {
        return jobState;
    }

    public void setJobState(String jobState) {
        this.jobState = jobState;
    }
}
