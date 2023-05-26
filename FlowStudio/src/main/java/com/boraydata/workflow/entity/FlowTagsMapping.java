package com.boraydata.workflow.entity;


import com.boraydata.workflow.entity.id.FlowTagsMappingId;
import com.boraydata.workflow.entity.id.WorkFlowMappingId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@IdClass(FlowTagsMappingId.class)
@Table(name = "tagsmapping")
public class FlowTagsMapping {

    @Id
    @Column(name = "tagid", nullable = false)
    public Long tagId;

    @Id
    @Column(name = "streamid")
    public String streamId;

    @Id
    @Column(name = "wflowid")
    public Long flowId;

    public FlowTagsMapping(Long tagId, String streamId) {
        this.tagId = tagId;
        this.streamId = streamId;
        this.flowId = -1L;
    }

    public FlowTagsMapping(Long tagId, Long flowId) {
        this.tagId = tagId;
        this.flowId = flowId;
        this.streamId = "null";
    }

    public FlowTagsMapping() {

    }
}
