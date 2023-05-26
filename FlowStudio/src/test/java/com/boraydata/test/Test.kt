package com.boraydata.test

import com.boraydata.common.FlowConstants
import com.boraydata.workflow.entity.vo.ListFunctionTypeVO
import kotlin.streams.toList
import org.junit.jupiter.api.Test


class Test {

    @Test
    fun test() {
//        val functionTypes = FlowConstants.FUNCTION_TYPE.entries.stream()
//            .map { ListFunctionTypeVO(it.key, it.value) }.toList()
        val functionTypes = emptyList<ListFunctionTypeVO>().toMutableList()
        for ((k,v) in FlowConstants.FUNCTION_TYPE) {
            functionTypes.add(ListFunctionTypeVO(k, v))
        }
        functionTypes.forEach { println(it.number) }
    }
}