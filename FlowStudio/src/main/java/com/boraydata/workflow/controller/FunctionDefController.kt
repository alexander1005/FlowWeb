package com.boraydata.workflow.controller

import com.boraydata.workflow.utils.Response
import com.boraydata.workflow.entity.FunctionDef
import com.boraydata.workflow.entity.dto.FunctionDefDTO
import com.boraydata.workflow.entity.vo.FunctionConfigVO
import com.boraydata.workflow.entity.vo.FunctionDefListVO
import com.boraydata.workflow.entity.vo.ListFunctionTypeVO
import com.boraydata.workflow.entity.vo.WorkflowFunctionListVO
import com.boraydata.workflow.service.IFunctionDefService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/studio")
@RestController
class FunctionDefController(private val functionDefService: IFunctionDefService) {

    @GetMapping("/listFunction")
    fun listFunction(): Response<List<FunctionDefListVO>> {
        return functionDefService.listFunction()
    }

    @GetMapping("/listFunctionType")
    fun listFunctionType(): Response<List<ListFunctionTypeVO>> {
        return functionDefService.listFunctionType()
    }

    @GetMapping("/listRedisUdf")
    fun listRedisUdf(@RequestHeader(value = "flow_name") workflowName: String): Response<List<String>> {
        return functionDefService.listRedisUdf(workflowName)
    }

    @PostMapping("/createFunction")
    fun createFunction(@RequestBody functionDefDTO: FunctionDefDTO): Response<FunctionDef> {
        return functionDefService.createFunction(functionDefDTO)
    }

    @PostMapping("/updateFunction")
    fun updateFunction(@RequestBody functionDefDTO: FunctionDefDTO): Response<FunctionDef> {
        return functionDefService.updateFunction(functionDefDTO)
    }

    @PostMapping("/uploadingFunctionFile")
    fun uploadingFunctionFile(@RequestParam("functionFile") file: MultipartFile): Response<String> {
        return functionDefService.uploadingFunctionFile(file)
    }

    @DeleteMapping("/deleteFunction")
    fun deleteFunction(@RequestHeader(value = "flow_name") workflowName: String, @RequestHeader(value = "fun_name") functionName: String): Response<String> {
        return functionDefService.deleteFunction(workflowName, functionName)
    }

    @GetMapping("/listFunctionConfig")
    fun listFunctionConfig(): Response<Iterable<FunctionConfigVO>> {
        return functionDefService.listFunctionConfig()
    }

    @GetMapping("/getJarUdfCounts")
    fun getJarUdfCounts(@RequestHeader(value = "jar_name") jarName: String): Response<Int> {
        return functionDefService.getJarUdfCounts(jarName)
    }

    @GetMapping("/listWorkflowFunction")
    fun listWorkflowFunction(@RequestHeader(value = "flow_name") workflowName: String): Response<Iterable<WorkflowFunctionListVO>> {
        return functionDefService.listWorkflowFunction(workflowName)
    }

}