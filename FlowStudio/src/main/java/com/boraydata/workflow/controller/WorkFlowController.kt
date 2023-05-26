package com.boraydata.workflow.controller

import com.boraydata.flowauth.enums.TagConstants
import com.boraydata.workflow.entity.State
import com.boraydata.workflow.utils.Response
import com.boraydata.workflow.entity.dto.WorkFlowDTO
import com.boraydata.workflow.entity.dto.WorkFlowDeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUndeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUpdateDTO
import com.boraydata.workflow.entity.vo.*
import com.boraydata.workflow.service.DistributedLockService
import com.boraydata.workflow.service.IWorkFlowService
import com.boraydata.workflow.utils.UserUtils.currentUser
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletResponse

@RequestMapping("/studio")
@RestController
class WorkFlowController(
    private val workflowService: IWorkFlowService,
    private val distributedLockService: DistributedLockService
) {

    @PostMapping("/deployWorkFlowById")
    fun deployWorkFlow(@RequestParam(value = "id") workflowID: Long): Response<String> {
        return workflowService.deploy(workflowID)
    }

    @PostMapping("/deployWorkFlow")
    fun deployWorkFlowByName(@RequestBody workFlowDeployDTO: WorkFlowDeployDTO): Response<WorkFlowDeployVO> {
        var lock = false;
        while (!lock) {
            lock = distributedLockService.tryLock("${TagConstants.deploy.name}-${currentUser().id}")
            TimeUnit.SECONDS.sleep(5)
        }
        return try {
            workflowService.deployWorkFlow(workFlowDeployDTO)
        } finally {
            distributedLockService.unlock("${TagConstants.deploy.name}-${currentUser().id}")
        }
    }

    @PostMapping("/createWorkFlow")
    fun createWorkFlow(@RequestBody workFlowDTO: WorkFlowDTO): Response<WorkFlowVO> {
        return workflowService.createWorkFlow(workFlowDTO)
    }

    @DeleteMapping("/deleteWorkFlowById")
    fun deleteWorkFlow(@RequestParam(value = "id") workflowID: Long): Response<String> {
        return workflowService.delete(workflowID)
    }

    @DeleteMapping("/deleteWorkFlow")
    fun deleteWorkFlowByName(@RequestHeader(value = "flow_name") workflowName: String): Response<String> {
        return workflowService.deleteWorkFlow(workflowName)
    }

//    @PostMapping("/shutdownWorkFlow")
//    fun shutdownWorkFlow(@RequestBody workFlowDeployDTO: WorkFlowDeployDTO): Response<String> {
//        return workflowService.shutdownWorkFlow(workFlowDeployDTO.flowName)
//    }

    @PostMapping("/killWorkFlow")
    fun killWorkFlow(@RequestBody workFlowDeployDTO: WorkFlowDeployDTO): Response<String> {
        return workflowService.killWorkFlow(workFlowDeployDTO.flowName)
    }

    @PostMapping("/undeployWorkFlow")
    fun undeployWorkFlow(@RequestBody workFlowUndeployDTO: WorkFlowUndeployDTO): Response<Any> {
        return workflowService.undeployWorkFlow(workFlowUndeployDTO)
    }

    @GetMapping("/listDeployedWorkFlow")
    fun listDeployedWorkFlow(): Response<List<WorkFlowInstListVO>> {
        return workflowService.listDeployedWorkFlow()
    }

    @GetMapping("/listWorkFlow")
    fun listWorkFlow(): Response<List<WorkFlowListVO>> {
        return workflowService.listWorkFlow()
    }

    @GetMapping("/describeWorkFlow")
    fun describeStream(@RequestHeader(value = "flow_name") workflowName: String): Response<WorkFlowListVO> {
        return workflowService.describeWorkFlow(workflowName)
    }

    @PostMapping("/updateWorkFlow")
    fun updateWorkFlow(@RequestBody workFlowUpdateDTO: WorkFlowUpdateDTO): Response<WorkFlowVO> {
        return workflowService.updateWorkFlow(workFlowUpdateDTO)
    }

    @GetMapping("/getStatus")
    fun getStatus(@RequestHeader(value = "flow_name") workflowName: String): Response<State> {
        return workflowService.getStatus(workflowName)
    }

    @GetMapping("/getDeployedWorkFlowLog")
    fun getDeployedWorkFlowLog(@RequestHeader(value = "flow_name") workflowName: String): Response<WorkFlowLogVO> {
        return workflowService.getDeployedWorkFlowLog(workflowName)
    }

    @GetMapping("/getDeployedWorkFlowLogText")
    fun getDeployedWorkFlowLogText(@RequestHeader(value = "flow_name") workflowName: String): Response<String> {
        return workflowService.getDeployedWorkFlowLogText(workflowName)
    }

    @GetMapping("/getDeployedWorkFlowLogTextIn")
    fun getDeployedWorkFlowLogTextIn(@RequestHeader(value = "flow_name") workflowName: String): String? {
        return workflowService.getDeployedWorkFlowLogTextIn(workflowName)
    }

    @GetMapping("/getDeployedWorkFlowLogFile")
    fun getDeployedWorkFlowLogFile(response: HttpServletResponse, @RequestHeader(value = "flow_name") workflowName: String) {
        workflowService.getDeployedWorkFlowLogFile(response, workflowName)
    }

    @GetMapping("/getFlowCount")
    fun getFlowCount(): Response<Int> {
        return workflowService.getFlowCount()
    }

    @GetMapping("/getWorkflowCount")
    fun getWorkflowCount(): Response<Int> {
        return workflowService.getWorkflowCount()
    }

    @GetMapping("/getDeployedWorkflowCount")
    fun getDeployedWorkflowCount(): Response<Int> {
        return workflowService.getDeployedWorkflowCount()
    }
}