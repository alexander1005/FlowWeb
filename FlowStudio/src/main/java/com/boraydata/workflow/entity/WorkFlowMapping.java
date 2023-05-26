package com.boraydata.workflow.entity;

import com.boraydata.workflow.entity.id.WorkFlowMappingId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(WorkFlowMappingId.class)
@Table(name = "wfmapping")
public class WorkFlowMapping implements Serializable {
    @Id
    @Column(name = "streamid", nullable = false)
    public String streamId;

    @Id
    @Column(name = "wflowid", nullable = false)
    public Long flowId;
}
