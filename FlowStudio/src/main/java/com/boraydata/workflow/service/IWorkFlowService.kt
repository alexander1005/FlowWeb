package com.boraydata.workflow.service

import com.boraydata.workflow.entity.State
import com.boraydata.workflow.utils.Response
import com.boraydata.workflow.entity.dto.WorkFlowDTO
import com.boraydata.workflow.entity.dto.WorkFlowDeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUndeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUpdateDTO
import com.boraydata.workflow.entity.vo.*
import java.io.File
import javax.servlet.http.HttpServletResponse

interface IWorkFlowService {

    fun deploy(workflowID: Long): Response<String>

    fun deployWorkFlow(workFlowDeployDTO: WorkFlowDeployDTO): Response<WorkFlowDeployVO>

    fun createWorkFlow(workFlowDTO: WorkFlowDTO): Response<WorkFlowVO>

    fun delete(workflowID: Long): Response<String>

    fun deleteWorkFlow(workflowName: String): Response<String>

//    fun shutdownWorkFlow(flowName: String): Response<String>

    fun killWorkFlow(flowName: String): Response<String>

    fun undeployWorkFlow(workFlowUndeployDTO: WorkFlowUndeployDTO): Response<Any>

    fun listDeployedWorkFlow(): Response<List<WorkFlowInstListVO>>

    fun listWorkFlow(): Response<List<WorkFlowListVO>>

    fun describeWorkFlow(workflowName: String): Response<WorkFlowListVO>

    fun updateWorkFlow(workFlowUpdateDTO: WorkFlowUpdateDTO): Response<WorkFlowVO>

    fun getStatus(workflowName: String): Response<State>

    fun getDeployedWorkFlowLog(flowName: String): Response<WorkFlowLogVO>

    fun getDeployedWorkFlowLogText(flowName: String): Response<String>

    fun getDeployedWorkFlowLogTextIn(flowName: String): String?

    fun getDeployedWorkFlowLogFile(response: HttpServletResponse, flowName: String): File?

    fun getFlowCount(): Response<Int>

    fun getWorkflowCount(): Response<Int>

    fun getDeployedWorkflowCount(): Response<Int>

}