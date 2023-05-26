package com.boraydata.workflow.service.impl

import com.boraydata.common.FlowConstants
import com.boraydata.flowauth.constants.SymbolConstants
import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.enums.FlowDefStatus
import com.boraydata.flowauth.enums.WorkFlowStatus
import com.boraydata.flowauth.utils.ErrorUtils
import com.boraydata.flowauth.utils.ValidateUtils
import com.boraydata.workflow.WorkFlowClient
import com.boraydata.workflow.dao.*
import com.boraydata.workflow.entity.*
import com.boraydata.workflow.entity.dto.WorkFlowDTO
import com.boraydata.workflow.entity.dto.WorkFlowDeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUndeployDTO
import com.boraydata.workflow.entity.dto.WorkFlowUpdateDTO
import com.boraydata.workflow.entity.vo.*
import com.boraydata.workflow.exceptions.ApplicationChangedException
import com.boraydata.workflow.service.IWorkFlowService
import com.boraydata.workflow.utils.*
import com.boraydata.workflow.utils.ClientUtil.*
import com.boraydata.workflow.utils.UserUtils.currentUser
import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.Path
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

@Service
class WorkFlowServiceImpl(
    private val workFlowDAO: WorkFlowDAO,
    private val workFlowInstDAO: WorkFlowInstDAO,
    private val functionDefDAO: FunctionDefDAO,
    private val flowDefDAO: FlowDefDAO,
    private val workFlowMappingDAO: WorkFlowMappingDAO,
    private val dataSource: DataSource,
    private val flowTagDAO: FlowTagDAO,
    private val flowTagsMappingDAO: FlowTagsMappingDAO
) : IWorkFlowService {
    private val log = LoggerFactory.getLogger(IWorkFlowService::class.java.name)
    private val lock = ReentrantLock()

    override fun deploy(workflowID: Long): Response<String> {
        return try {
            if (workFlowInstDAO.findByFlowNameAndAccountId(
                    workFlowDAO.findById(workflowID).get().flowName,
                    currentUser().id
                ) != null
            )
                return fail("ERROR: Workflow has been deploy, Modification and deletion operations are not allowed")
            val workFlow = workFlowDAO.findById(workflowID)
            if (!workFlow.isPresent) return fail(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflowID is not present")
            val appId = client().deploy(
                workflowID,
                currentUser().id,
                workFlow.get().flowName,
                Optional.of(
                    getProperties(
                        workFlow.get().masterMemory,
                        workFlow.get().workerMemory,
                        workFlow.get().workerSlots
                    )
                )
            )
            val workFlowInstId = StringBuilder()
            workFlowInstId.append(workflowID).append(SymbolConstants.LINE).append(appId)
            workFlowInstDAO.save(
                toWorkFlowInst(
                    workFlowInstId.toString(),
                    workFlow.get().accountId,
                    workFlow.get().flowName
                )
            )
            successWithData(appId)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun deployWorkFlow(workFlowDeployDTO: WorkFlowDeployDTO): Response<WorkFlowDeployVO> {
        lock.lock()
        return try {
            val userId = currentUser().id
            val workflowName = workFlowDeployDTO.flowName
            if (workFlowInstDAO.findByFlowNameAndAccountId(workflowName, userId) != null)
                return failWithData(
                    ErrorCode.SUBMIT_FAILED.code,
                    "ERROR: Workflow has been deploy. Not allowed to deploy again"
                )
            val workFlow = workFlowDAO.findByFlowNameAndAccountId(workflowName, userId)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflowName is not present")
            if (workFlow.status == WorkFlowStatus.DRAFT.status) {
                workFlowConfigChecker(dataSource).validate(workFlow.flowCfg, userId)
                workFlow.status = WorkFlowStatus.PUBLISH.status
            }
            val streams = workFlowMappingDAO.findByFlowId(workFlow.flowId)
            streams.map {
                val flow = flowDefDAO.findByStreamNameAndAccountId(it.streamId, userId)
                if (flow!=null && FlowDefStatus.DISABLE.status == flow.status)
                    return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: ${flow.streamName} is disable")
            }
            workflowParameterSetting(
                workFlowDeployDTO.masterMemory,
                workFlowDeployDTO.workerMemory,
                workFlowDeployDTO.workerSlots,
                workFlow
            )
            val appId = client().deploy(
                workFlow.flowId,
                userId,
                workflowName,
                Optional.of(getProperties(workFlow.masterMemory, workFlow.workerMemory, workFlow.workerSlots))
            )
            val workFlowInstId = StringBuilder()
            workFlowInstId.append(workFlow.flowId).append(SymbolConstants.LINE).append(appId)
            workFlowInstDAO.save(toWorkFlowInst(workFlowInstId.toString(), userId, workflowName))
            val streamNames = WorkFlowClient.parseWorkflowStreams(workFlow.flowCfg)
            streamNames.stream().forEach {
                val workFlowMapping = WorkFlowMapping()
                val flowDef = flowDefDAO.findByStreamNameAndAccountId(it, currentUser().id)
                if (flowDef != null) {
                    workFlowMapping.streamId = flowDef.streamId
                    workFlowMapping.flowId = workFlow.flowId
                    workFlowMappingDAO.save(workFlowMapping)
                    flowDefDAO.save(setStatus(flowDef, FlowDefStatus.ACTIVE))
                }
            }
            val workFlowDeployVO = WorkFlowDeployVO()
            workFlowDeployVO.applicationId = appId
            workFlowDeployVO.state = getStatusFromClient(workflowName)
            successWithData(workFlowDeployVO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            if (errorMessage.contains("DeployWorkFlow interface call failed with error code")) {
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflow status is not publish")
            }
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        } finally {
            lock.unlock()
        }
    }

    override fun createWorkFlow(workFlowDTO: WorkFlowDTO): Response<WorkFlowVO> {
        return try {
            if (workFlowDAO.findByFlowNameAndAccountId(workFlowDTO.flowName, currentUser().id) != null)
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflow already exists")
            if (!ValidateUtils.validateName(workFlowDTO.flowName))
                return failWithData(
                    ErrorCode.SUBMIT_FAILED.code,
                    "ERROR: flowName only English characters, numbers, and symbols (_,-) are allowed."
                )
            FlowConstants.COMPRESSION_TYPES
            return workFlowDTOCreateToWorkFlow(workFlowDTO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun updateWorkFlow(workFlowUpdateDTO: WorkFlowUpdateDTO): Response<WorkFlowVO> {
        return try {
            val userId = currentUser().id
            val workflow = workFlowDAO.findByFlowNameAndAccountId(workFlowUpdateDTO.flowName, userId)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: update workflow is not exist")
            // 更新前检查是否正在运行
            if (workFlowInstDAO.findByFlowNameAndAccountId(workflow.flowName, userId) != null)
                return failWithData(ErrorCode.SUBMIT_FAILED.code,
                    "ERROR: Workflow has been deploy, Modification and deletion operations are not allowed")
            return workFlowDTOUpdateToWorkFlow(workflow, workFlowUpdateDTO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun delete(workflowID: Long): Response<String> {
        return try {
            if (workFlowInstDAO.findByFlowNameAndAccountId(
                    workFlowDAO.findById(workflowID).get().flowName,
                    currentUser().id
                ) != null
            )
                return fail("ERROR: Workflow has been deploy, Modification and deletion operations are not allowed")
            workFlowDAO.deleteById(workflowID)
            success()
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun deleteWorkFlow(workflowName: String): Response<String> {
        return try {
            val userId = currentUser().id
            val workflow = workFlowDAO.findByFlowNameAndAccountId(workflowName, userId)
                ?: return fail("ERROR: workflowName is not present")
            // 删除前检查是否正在运行
            if (workFlowInstDAO.findByFlowNameAndAccountId(workflowName, userId) != null)
                return fail("ERROR: Workflow has been deploy, Modification and deletion operations are not allowed")
            deleteWorkFlowRepo(workflowName)
            // 删除该work flow所定义的function
            functionDefDAO.deleteByWorkFlowIdAndAccountId(workflow.flowId, userId)
            workFlowDAO.deleteByFlowNameAndAccountId(workflowName, userId)
            workFlowMappingDAO.deleteByFlowId(workflow.flowId)
            success()
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

//    override fun shutdownWorkFlow(flowName: String): Response<String> {
//        return try {
//            val workflow = workFlowDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
//                ?: return fail("ERROR: workflowName is not present")
//            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(workflow.flowName, currentUser().id)
//                ?: return fail("ERROR: deployed workflow is not exist")
//            shutdown(workflow, workFlowInst)
//            success()
//        } catch (e: Throwable) {
//            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $e")
//        }
//    }

    override fun killWorkFlow(flowName: String): Response<String> {
        return try {
            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
                ?: return fail("ERROR: deployed workflow is not exist")
            client().kill(getApplicationId(workFlowInst.wFlowInstId))
            success()
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun undeployWorkFlow(workFlowUndeployDTO: WorkFlowUndeployDTO): Response<Any> {
        return try {
            val workflow = workFlowDAO.findByFlowNameAndAccountId(workFlowUndeployDTO.flowName, currentUser().id)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflowName is not present")
            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(workflow.flowName, currentUser().id)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: deployed workflow is not exist")
            val shutdown = shutdown(workflow, workFlowInst)
//            client().kill(getApplicationId(workFlowInst.wFlowInstId))
            val deleteByFlowNameAndAccountId =
                workFlowInstDAO.deleteByFlowNameAndAccountId(workFlowUndeployDTO.flowName, currentUser().id)
            if (deleteByFlowNameAndAccountId == 0)
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: deployed workflow is not exist")
            successWithData(shutdown)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun listDeployedWorkFlow(): Response<List<WorkFlowInstListVO>> {
        return try {
            val workFlowInst = workFlowInstDAO.findAllByAccountId(currentUser().id)
            val workFlowInstListVO = workFlowInst.map {
                val workflowInstVO = WorkFlowInstListVO()
                workflowInstVO.wFlowInstId = it.wFlowInstId
                workflowInstVO.flowName = it.flowName
                workflowInstVO.startTime = it.startTime
                workflowInstVO.status = getStatusFromClient(it.flowName)
                workflowInstVO
            }
            successWithData(workFlowInstListVO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun listWorkFlow(): Response<List<WorkFlowListVO>> {
        return try {
            val workFlows = workFlowDAO.findAllByAccountId(currentUser().id)
            val workFlowsListVO = workFlows.map {
                val workFlowsVO = WorkFlowListVO()
                workFlowsVO.flowId = it.flowId
                workFlowsVO.flowName = it.flowName
                workFlowsVO.flowCfg = it.flowCfg
                workFlowsVO.updateTime = it.updateTime
                workFlowsVO.createTime = it.createTime
                workFlowsVO.state = getStatusFromClient(it.flowName)
                workFlowsVO.status = WorkFlowStatus.valueOf(it.status).name
                workFlowsVO.masterMemory = it.masterMemory
                workFlowsVO.workerMemory = it.workerMemory
                workFlowsVO.workerSlots = it.workerSlots
                workFlowsVO.flows =
                    workFlowMappingDAO.findByFlowId(it.flowId).map {
                        val flow = flowDefDAO.findById(it.streamId)
                        if (flow.isPresent)
                            flow.get().streamName
                        else
                            null
                    }.filterNotNull()
                workFlowsVO.flowDesc = it.flowDesc
                // tag
                val flowTagsMapping = flowTagsMappingDAO.findByFlowId(it.flowId)
                val tagList: MutableList<String> = ArrayList()
                flowTagsMapping.forEach {
                    if (it != null) {
                        val flowTag = flowTagDAO.findByTagIdAndAccountID(it.tagId, currentUser().id)
                        if (flowTag != null)
                            tagList.add(flowTag.name)
                    }
                }
                workFlowsVO.tag = tagList

                workFlowsVO
            }
            successWithData(workFlowsListVO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun describeWorkFlow(workflowName: String): Response<WorkFlowListVO> {
        return try {
            val workflow = workFlowDAO.findByFlowNameAndAccountId(workflowName, currentUser().id)
            if (workflow == null)
                successWithData(null)
            val workFlowsVO = WorkFlowListVO()
            workFlowsVO.flowId = workflow.flowId
            workFlowsVO.flowName = workflow.flowName
            workFlowsVO.flowCfg = workflow.flowCfg
            workFlowsVO.updateTime = workflow.updateTime
            workFlowsVO.createTime = workflow.createTime
            workFlowsVO.state = getStatusFromClient(workflow.flowName)
            workFlowsVO.status = WorkFlowStatus.valueOf(workflow.status).name
            workFlowsVO.masterMemory = workflow.masterMemory
            workFlowsVO.workerMemory = workflow.workerMemory
            workFlowsVO.workerSlots = workflow.workerSlots
//            workFlowsVO.flows =
//                workFlowMappingDAO.findByFlowId(workflow.flowId).map { flowDefDAO.findById(it.streamId).get().streamName }
            workFlowsVO.flows =
                workFlowMappingDAO.findByFlowId(workflow.flowId).map {
                    val flow = flowDefDAO.findById(it.streamId)
                    if (flow.isPresent)
                        flow.get().streamName
                    else
                        null
                }.filterNotNull()
            workFlowsVO.flowDesc = workflow.flowDesc

//            val flowTagsMapping = flowTagsMappingDAO.findByFlowId(workflow.flowId)
//            if (flowTagsMapping != null) {
//                val flowTag = flowTagDAO.findByTagIdAndAccountID(flowTagsMapping.tagId, workflow.accountId)
//                if (flowTag != null) workFlowsVO.tag = flowTag.name else workFlowsVO.tag = null
//            } else workFlowsVO.tag = null

            // tag
            val flowTagsMapping = flowTagsMappingDAO.findByFlowId(workflow.flowId)
            val tagList: MutableList<String> = ArrayList()
            flowTagsMapping.forEach {
                if (it != null) {
                    val flowTag = flowTagDAO.findByTagIdAndAccountID(it.tagId, currentUser().id)
                    if (flowTag != null)
                        tagList.add(flowTag.name)
                }
            }
            workFlowsVO.tag = tagList

            successWithData(workFlowsVO)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            successWithData(null)
        }
    }

    override fun getStatus(workflowName: String): Response<State> {
        return try {
            val data = getStatusFromClient(workflowName)
            if (data != null) successWithData(data)
            else successWithData(null)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun getDeployedWorkFlowLog(flowName: String): Response<WorkFlowLogVO> {
        return try {
            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: deployed workflow is not exist")
            val deployedWorkFlowLog = client().getDeployedWorkFlowLog(getApplicationId(workFlowInst.wFlowInstId))
//            val logCollection = deployedWorkFlowLog.keys.stream().map { WorkFlowLogVO(it, deployedWorkFlowLog[it]) }
//                .collect(Collectors.toList())
            val data = WorkFlowLogVO(deployedWorkFlowLog)
            return successWithData(data)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun getDeployedWorkFlowLogText(flowName: String): Response<String> {
        val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
            ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: deployed workflow is not exist")
        val applicationId = getApplicationId(workFlowInst.wFlowInstId)
        val yarnLogText = getYarnLogText(applicationId)

        if (yarnLogText != null && yarnLogText.trim().isNotEmpty()) {
            log.info("success return log")
            return successWithData(yarnLogText)
        }
        studioNodes().forEach {
            log.info(it)
            val auth = "http://$it/api/studio/getDeployedWorkFlowLogTextIn"
            val accessToken = HttpUtils.getAccessToken()
            log.info(accessToken)
            if (accessToken != null) {
                log.info("requesting log")
                val t1 = System.currentTimeMillis()
                val data = HttpUtils.getRequestForLog(auth, accessToken, flowName)
                if (data != null) {
                    log.info("get request log, use " + (System.currentTimeMillis() - t1) + "ms")
                    if (data.trim().isNotEmpty())
                        return successWithData(data)
                }
            }
        }
        log.info("Failed to read logs")
        return fail("Failed to read logs")
    }

//    override fun getDeployedWorkFlowLogText(flowName: String): Response<String> {
//        return try {
//            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
//                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: deployed workflow is not exist")
//            val deployedWorkFlowLog = client().getDeployedWorkFlowLogText(getApplicationId(workFlowInst.wFlowInstId))
//
//            if (deployedWorkFlowLog.trim().isNotEmpty()) {
//                log.info("success return log")
//                return successWithData(deployedWorkFlowLog)
//            }
//            studioNodes().forEach {
//                log.info(it)
//                val auth = "http://$it/studio/getDeployedWorkFlowLogTextIn"
//                val accessToken = HttpUtils.getAccessToken()
//                log.info(accessToken)
//                if (accessToken != null) {
//                    val t1 = System.currentTimeMillis();
//                    val data = HttpUtils.getRequestForLog(auth, accessToken, flowName)
//                    if (data != null) {
//                        log.info("get request log, use "+ (System.currentTimeMillis() - t1) + "ms")
//                        if (data.trim().isNotEmpty())
//                            return successWithData(data)
//                    }
//                }
//            }
//            return fail("Failed to read logs")
//        } catch (e: Throwable) {
//            log.error("ERROR", "ERROR: ", e)
//            val errorMessage = ErrorUtils.getErrorMessage(e)
//            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
//        }
//    }

    override fun getDeployedWorkFlowLogTextIn(flowName: String): String? {
        return try {
            val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
                ?: return null
            val applicationId = getApplicationId(workFlowInst.wFlowInstId)
            val yarnLogText = getYarnLogText(applicationId) ?: return null
            if (yarnLogText.trim().isEmpty())
                return null
            log.info("success return log")
            yarnLogText
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            null
        }
    }

    // stream , find container
    override fun getDeployedWorkFlowLogFile(response: HttpServletResponse, flowName: String): File? {
        lock.lock()
        var file: File? = null
        return try {
            file = getYarnLogFile(flowName)
            if (file == null)
                return null
            response.reset()
            response.contentType = "application/octet-stream"
            response.characterEncoding = "utf-8"
            response.setHeader("Content-Disposition", "attachment;filename=${file.name}")
            fileToRequest(file, response)
            file
        } catch (e: Exception) {
            log.error("ERROR", "ERROR: ", e)
            null
        } finally {
            file?.delete()
            lock.unlock()
        }
    }

    override fun getFlowCount(): Response<Int> {
        val flows = flowDefDAO.findAllByAccountId(currentUser().id)
        return successWithData(flows.count())
    }

    override fun getWorkflowCount(): Response<Int> {
        val workflows = workFlowDAO.findAllByAccountId(currentUser().id)
        return successWithData(workflows.count())
    }

    override fun getDeployedWorkflowCount(): Response<Int> {
        val deployedWorkflows = workFlowInstDAO.findAllByAccountId(currentUser().id)
        return successWithData(deployedWorkflows.count())
    }

    private fun getStatusFromClient(flowName: String): State? {
        val userId = currentUser().id
        val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, userId) ?: return null
        val applicationId = workFlowInst.wFlowInstId.split(SymbolConstants.LINE)[1]
        return try {
            client().getStatus(applicationId)
        } catch (e: ApplicationChangedException) {
            workFlowInstDAO.deleteByFlowNameAndAccountId(flowName, userId)
            null
        }
    }

    private fun workflowToVOSuccess(workflow: WorkFlow): WorkFlowVO {
        val workFlowVO = WorkFlowVO()
        workFlowVO.accountId = workflow.accountId
        workFlowVO.flowName = workflow.flowName
        workFlowVO.flowCfg = workflow.flowCfg
        workFlowVO.createTime = workflow.createTime
        workFlowVO.updateTime = workflow.updateTime
        return workFlowVO
    }

    private fun workFlowDTOCreateToWorkFlow(workFlowDTO: WorkFlowDTO): Response<WorkFlowVO> {
        val workflow = WorkFlow()
        val accountID = currentUser().id
        return try {
            workflow.accountId = accountID
            workflow.flowName = workFlowDTO.flowName
            workflow.flowCfg = workFlowDTO.flowCfg.toJSONString()
            workflow.status = WorkFlowStatus.DRAFT.status
            workflow.createTime = Date(System.currentTimeMillis())
            workflow.updateTime = Date(System.currentTimeMillis())
            workflow.masterMemory = 1024
            workflow.workerMemory = 1024
            workflow.workerSlots = 1
            if (workFlowDTO.flowDesc != null)
                workflow.flowDesc = workFlowDTO.flowDesc
            val wflow = workFlowDAO.save(workflow)
            if (workFlowDTO.tag != null) {
                Arrays.stream(workFlowDTO.tag.split(SymbolConstants.COMMA).toTypedArray()).forEach { t: String? ->
                    var tag = flowTagDAO.findByNameAndAccountID(t, accountID)
                    if (tag == null) {
                        val newTag = FlowTag()
                        newTag.name = t
                        newTag.accountID = accountID
                        tag = flowTagDAO.save(newTag)
                    }
                    flowTagsMappingDAO.save(FlowTagsMapping(tag.tagId, wflow.flowId))
                }
            }
            successWithData(workflowToVOSuccess(wflow))
        } catch (e: Throwable) {
            val errorMessage = ErrorUtils.getErrorMessage(e)
            log.error("ERROR", "ERROR: ", e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    private fun workFlowDTOUpdateToWorkFlow(
        workflow: WorkFlow,
        workFlowUpdateDTO: WorkFlowUpdateDTO
    ): Response<WorkFlowVO> {
        return try {
            if (workFlowUpdateDTO.flowCfg != null)
                workflow.flowCfg = workFlowUpdateDTO.flowCfg.toJSONString()
            if (workFlowUpdateDTO.status != null)
                workflow.status = workFlowUpdateDTO.status
            if (workFlowUpdateDTO.flowDesc != null)
                workflow.flowDesc = workFlowUpdateDTO.flowDesc
            if (workflow.status != WorkFlowStatus.DRAFT.status) {
                workFlowConfigChecker(dataSource).validate(workflow.flowCfg, currentUser().id)
                val streamNames = WorkFlowClient.parseWorkflowStreams(workflow.flowCfg)
                streamNames.stream().forEach {
                    val workFlowMapping = WorkFlowMapping()
                    val flowDef = flowDefDAO.findByStreamNameAndAccountId(it, currentUser().id)
                    if (flowDef != null) {
                        workFlowMapping.streamId = flowDef.streamId
                        workFlowMapping.flowId = workflow.flowId
                        workFlowMappingDAO.save(workFlowMapping)
                    }
                }
            } else { // DARFT
                workflowParameterSetting(
                    workFlowUpdateDTO.masterMemory,
                    workFlowUpdateDTO.workerMemory,
                    workFlowUpdateDTO.workerSlots,
                    workflow
                )
                workFlowMappingDAO.deleteByFlowId(workflow.flowId)
            }
            workflow.updateTime = Date(System.currentTimeMillis())
            workFlowDAO.save(workflow)
            successWithData(workflowToVOSuccess(workflow))
        } catch (e: Throwable) {
            val errorMessage = ErrorUtils.getErrorMessage(e)
            log.error("ERROR", "ERROR: ", e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    private fun toWorkFlowInst(wFlowInstId: String, accountId: Long, flowName: String): WorkFlowInst {
        val workFlowInst = WorkFlowInst()
        workFlowInst.wFlowInstId = wFlowInstId
        workFlowInst.accountId = accountId
        workFlowInst.flowName = flowName
        workFlowInst.startTime = Date(System.currentTimeMillis())
        return workFlowInst
    }

    private fun setStatus(flowDef: FlowDef, flowDefStatus: FlowDefStatus): FlowDef {
        flowDef.status = flowDefStatus.status
        return flowDef
    }

    // 删除目录
    private fun deleteWorkFlowRepo(workflowName: String) {
        val fileSystem = fileSystem()
        fileSystem.delete(
            Path(
                repository(WorkFlowClient.getUserBaseRepository(hdfsUrl(), currentUser().id), workflowName)
            ), true
        )
        fileSystem.close()
    }

    private fun shutdown(workflow: WorkFlow, workFlowInst: WorkFlowInst): List<String> {
        val shutdown = client().shutdown(getApplicationId(workFlowInst.wFlowInstId))
        val workflowOfStreams = workFlowMappingDAO.findByFlowId(workflow.flowId)
        workflowOfStreams.forEach {
//            val streams = workFlowMappingDAO.findByStreamId(it.streamId)
//            if (streams.size == 0)
            val flow = flowDefDAO.findById(it.streamId)
            if (flow.isPresent) {
                flowDefDAO.save(setStatus(flow.get(), FlowDefStatus.ENABLE))
            }
        }

        if (workflow.status != WorkFlowStatus.PUBLISH.status) { // DARFT
            workFlowMappingDAO.deleteByFlowId(workflow.flowId)
        }
        return shutdown
    }

    private fun getApplicationId(wFlowInstId: String): String {
        return wFlowInstId.split(SymbolConstants.LINE)[1]
    }

    private fun getProperties(masterMemory: Int, workerMemory: Int, workerSlots: Int): Properties {
        val properties = Properties()
        properties.setProperty("MasterMemSize", masterMemory.toString())
        properties.setProperty("WorkerMemSize", workerMemory.toString())
        properties.setProperty("WorkerSlots", workerSlots.toString())
        return properties
    }

    private fun workflowParameterSetting(
        masterMemory: Int?,
        workerMemory: Int?,
        workerSlots: Int?,
        workFlow: WorkFlow
    ) {
        if (masterMemory != null)
            workFlow.masterMemory = masterMemory
        if (workerMemory != null)
            workFlow.workerMemory = workerMemory
        if (workerSlots != null)
            workFlow.workerSlots = workerSlots
    }

    private fun fileToRequest(file: File, response: HttpServletResponse) {
        response.setContentLength(file.length().toInt())
        BufferedInputStream(FileInputStream(file)).use { bis ->
            val buff = ByteArray(1024)
            val os: OutputStream = response.outputStream
            var i: Int
            while (bis.read(buff).also { i = it } != -1) {
                os.write(buff, 0, i)
                os.flush()
            }
        }
    }

    private fun byteToFile(bytes: ByteArray, path: String): File? {
        return try {
            val localFile = File(path)
            if (!localFile.exists()) {
                localFile.createNewFile()
            }
            val os: OutputStream = FileOutputStream(localFile)
            os.write(bytes)
            os.close()
            localFile
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun inputStreamToString(inputStream: InputStream): String {
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream, "utf8"))
//        var line: String?
//        val stringBuilder = java.lang.StringBuilder()
//        while (bufferedReader.readLine().also { line = it } != null) {
//            stringBuilder.append("\n")
//            stringBuilder.append(line)
//            log.info(line)
//        }
//        return stringBuilder.toString()
        try {
            val text: String = IOUtils.toString(inputStream, "utf8")
            return text
        } finally {
            IOUtils.closeQuietly(inputStream)
        }

    }

//    private fun getAllNodesLogText(flowName: String): String? {
//        val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
//            ?: return null
//        val applicationId = getApplicationId(workFlowInst.wFlowInstId)
//        val yarnLogText = getYarnLogText(applicationId)
//
//        if (yarnLogText != null && yarnLogText.trim().isNotEmpty()) {
//            log.info("success return log")
//            return yarnLogText
//        }
//        studioNodes().forEach {
//            log.info(it)
//            val auth = "http://$it/api/studio/getDeployedWorkFlowLogTextIn"
//            val accessToken = HttpUtils.getAccessToken()
//            log.info(accessToken)
//            if (accessToken != null) {
//                log.info("requesting log")
//                val t1 = System.currentTimeMillis()
//                val data = HttpUtils.getRequestForLog(auth, accessToken, flowName)
//                if (data != null) {
//                    log.info("get request log, use " + (System.currentTimeMillis() - t1) + "ms")
//                    if (data.trim().isNotEmpty())
//                        return data
//                }
//            }
//        }
//        return null
//    }

    private fun getYarnLogFile(flowName: String): File? {
        val workFlowInst = workFlowInstDAO.findByFlowNameAndAccountId(flowName, currentUser().id)
            ?: return null
        val applicationId = getApplicationId(workFlowInst.wFlowInstId)
        val yarnCmd = "yarn logs -applicationId $applicationId > $applicationId.log"
        val cmd = arrayOf(
            "/bin/sh",
            "-c",
            yarnCmd
        )
        var file: File? = null
        try {
            val process = Runtime.getRuntime().exec(cmd)
            if (process.waitFor() == 0) {
                file = File("$applicationId.log")
                if (file.exists()) {
                    return file
                }
            }
            studioNodes().forEach {
                log.info(it)
                val auth = "http://$it/api/studio/getDeployedWorkFlowLogTextIn"
                val accessToken = HttpUtils.getAccessToken()
                log.info(accessToken)
                if (accessToken != null) {
                    log.info("requesting log")
                    val t1 = System.currentTimeMillis()
                    val data = HttpUtils.getRequestForLog(auth, accessToken, flowName)
                    if (data != null) {
                        log.info("get request log, use " + (System.currentTimeMillis() - t1) + "ms")
                        if (data.trim().isNotEmpty()) {
                            return byteToFile(data.toByteArray(), "$applicationId.log")
                        }
                    }
                }
            }
            return null
        } finally {
            file?.delete()
        }
    }

    private fun getYarnLogText(applicationId: String): String? {
        val yarnCmd = "yarn logs -applicationId $applicationId > $applicationId.log"
        val cmd = arrayOf(
            "/bin/sh",
            "-c",
            yarnCmd
        )
        var file: File? = null
        try {
            val process = Runtime.getRuntime().exec(cmd)
            if (process.waitFor() == 0) {
                file = File("$applicationId.log")
                if (file.exists()) {
                    val bytes = Files.readAllBytes(Paths.get(file.absolutePath))
                    val string = String(bytes, StandardCharsets.UTF_8)
                    return string
                }
            }
            log.info("null")
            return null
        } finally {
            file?.delete()
        }
    }


}