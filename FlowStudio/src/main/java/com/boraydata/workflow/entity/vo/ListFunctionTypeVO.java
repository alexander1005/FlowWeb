package com.boraydata.workflow.entity.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListFunctionTypeVO {
    public int number;
    public String value;

    public ListFunctionTypeVO(int number, String value) {
        this.number = number;
        this.value = value;
    }
}