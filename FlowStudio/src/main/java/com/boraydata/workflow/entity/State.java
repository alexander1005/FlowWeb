package com.boraydata.workflow.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class State {
    private String yarnStartTime;
    private String yarnFinishTime;
    private String yarnState;
    private String yarnFinalState;
    private String jobState;
    private List<JobState> jobStates;
}
