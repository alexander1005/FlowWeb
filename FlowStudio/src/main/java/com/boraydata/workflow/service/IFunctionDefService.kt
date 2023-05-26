package com.boraydata.workflow.service

import com.boraydata.workflow.utils.Response
import com.boraydata.workflow.entity.FunctionDef
import com.boraydata.workflow.entity.dto.FunctionDefDTO
import com.boraydata.workflow.entity.vo.FunctionConfigVO
import com.boraydata.workflow.entity.vo.FunctionDefListVO
import com.boraydata.workflow.entity.vo.ListFunctionTypeVO
import com.boraydata.workflow.entity.vo.WorkflowFunctionListVO
import org.springframework.web.multipart.MultipartFile

interface IFunctionDefService {

    fun listFunction(): Response<List<FunctionDefListVO>>

    fun listFunctionType(): Response<List<ListFunctionTypeVO>>

    fun listRedisUdf(workFlowName: String): Response<List<String>>

    fun createFunction(functionDefDTO: FunctionDefDTO): Response<FunctionDef>

    fun updateFunction(functionDefDTO: FunctionDefDTO): Response<FunctionDef>

//    fun createFunctionByMultipart(functionDefDTO: FunctionDefDTO, file: MultipartFile): Response<FunctionDef>

    fun uploadingFunctionFile(file: MultipartFile): Response<String>

    fun deleteFunction(workflowName: String, functionName: String): Response<String>

    fun listFunctionConfig(): Response<Iterable<FunctionConfigVO>>

    fun getJarUdfCounts(jarName: String): Response<Int>

    fun listWorkflowFunction(workflowName: String): Response<Iterable<WorkflowFunctionListVO>>

}