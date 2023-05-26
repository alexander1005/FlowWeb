package com.boraydata.workflow.service.impl

import com.boraydata.common.FlowConstants
import com.boraydata.common.utils.FunctionDefConfigChecker
import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.utils.ErrorUtils
import com.boraydata.workflow.WorkFlowClient
import com.boraydata.workflow.dao.FunctionConfigDAO
import com.boraydata.workflow.dao.FunctionDefDAO
import com.boraydata.workflow.dao.WorkFlowDAO
import com.boraydata.workflow.dao.WorkFlowInstDAO
import com.boraydata.workflow.entity.FunctionDef
import com.boraydata.workflow.entity.dto.FunctionDefDTO
import com.boraydata.workflow.entity.id.FunctionDefId
import com.boraydata.workflow.entity.vo.FunctionConfigVO
import com.boraydata.workflow.entity.vo.FunctionDefListVO
import com.boraydata.workflow.entity.vo.ListFunctionTypeVO
import com.boraydata.workflow.entity.vo.WorkflowFunctionListVO
import com.boraydata.workflow.service.IFunctionDefService
import com.boraydata.workflow.utils.*
import com.boraydata.workflow.utils.ClientUtil.*
import com.boraydata.workflow.utils.UserUtils.currentUser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FSDataOutputStream
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.streams.toList


@Service
class FunctionDefServiceImpl(
    private val functionDefDAO: FunctionDefDAO,
    private val workFlowDAO: WorkFlowDAO,
    private val workFlowInstDAO: WorkFlowInstDAO,
    private val functionConfigDAO: FunctionConfigDAO
) : IFunctionDefService {
    private val log = LoggerFactory.getLogger(IFunctionDefService::class.java.name)

    override fun listFunction(): Response<List<FunctionDefListVO>> {
        val func = functionDefDAO.findAll()
        val functionDefListVO = func.map {
            val functionConfig = functionConfigDAO.findByFunctionName(it.functionName)
            val funcVO = FunctionDefListVO()
            funcVO.accountId = it.accountId
            funcVO.workFlowId = it.workFlowId
            funcVO.functionName = it.functionName
            funcVO.functionType = it.functionType
            funcVO.functionClass = it.functionClass
            funcVO.functionArgs = it.functionArgs
            funcVO.functionVersion = it.functionVersion
            funcVO.functionLocation = it.functionLocation
            funcVO.createTime = it.createTime
            funcVO.updateTime = it.updateTime
            funcVO.fDesc = it.fDesc
            if (functionConfig != null) {
                funcVO.description = functionConfig.description
                funcVO.fuiconfig = functionConfig.fuiconfig
            }
            funcVO
        }
        return successWithData(functionDefListVO)
    }

    override fun listFunctionConfig(): Response<Iterable<FunctionConfigVO>> {
        val data = functionConfigDAO.findAll().map {
            val functionConfigVO = FunctionConfigVO()
            functionConfigVO.functionId = it.functionId
            functionConfigVO.functionName = it.functionName
            functionConfigVO.functionType = it.functionType
            functionConfigVO.description = it.description
            functionConfigVO.fuiconfig = it.fuiconfig
            functionConfigVO
        }
        return successWithData(data)
    }

    override fun getJarUdfCounts(jarName: String): Response<Int> {
        val count = functionDefDAO.findByFunctionLocationAndAccountId(jarName, currentUser().id).count()
        return successWithData(count)
    }

    override fun listWorkflowFunction(workflowName: String): Response<Iterable<WorkflowFunctionListVO>> {
        return try {
            val accountId = currentUser().id
            val flowId = workFlowDAO.findByFlowNameAndAccountId(workflowName, accountId).flowId
            val map = functionDefDAO.findByWorkFlowIdAndAccountId(flowId, accountId).map {
                val listVo = WorkflowFunctionListVO()
                listVo.accountId = it.accountId
                listVo.workFlowId = it.workFlowId
                listVo.functionName = it.functionName
                listVo.functionType = it.functionType
                listVo.functionClass = it.functionClass
                listVo.functionArgs = it.functionArgs
                listVo.functionVersion = it.functionVersion
                listVo.functionLocation = it.functionLocation
                listVo.createTime = it.createTime
                listVo.updateTime = it.updateTime
                listVo.iconType = "i_udf"
                listVo.fDesc = it.fDesc
                listVo
            }
            successWithData(map)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun listFunctionType(): Response<List<ListFunctionTypeVO>> {
        val functionTypes = FlowConstants.FUNCTION_TYPE.entries.stream()
            .map { ListFunctionTypeVO(it.key, it.value) }.toList()
        return successWithData(functionTypes)
    }

    override fun listRedisUdf(workFlowName: String): Response<List<String>> {
        val userId = currentUser().id
        val workflow = workFlowDAO.findByFlowNameAndAccountId(workFlowName, userId)
            ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is not exist")
        val func = functionDefDAO.findByWorkFlowIdAndAccountId(workflow.flowId, userId)
        val redisName = ArrayList<String>()
        func.map {
            if (it.functionType == 3) {
                redisName.add(it.functionName)
            }
        }
        return successWithData(redisName)
    }

    override fun createFunction(functionDefDTO: FunctionDefDTO): Response<FunctionDef> {
        return try {
            val workflow = workFlowDAO.findByFlowNameAndAccountId(functionDefDTO.workFlowName, currentUser().id)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is not exist")
//            if (workflow.status == WorkFlowStatus.PUBLISH.status)
//                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is PUBLISH")
            if (!FunctionDefConfigChecker.getInstance().validateFunctionType(functionDefDTO.functionType))
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: functionType invalid")
            val functionDef = FunctionDef()
            functionDef.accountId = currentUser().id
            functionDef.workFlowId = workflow.flowId
            functionDef.functionName = functionDefDTO.functionName
            functionDef.functionClass = functionDefDTO.functionClass
            if (functionDefDTO.functionArgs != null)
                functionDef.functionArgs = functionDefDTO.functionArgs
            functionDef.functionVersion = functionDefDTO.functionVersion
            functionDef.createTime = Date(System.currentTimeMillis())
            functionDef.updateTime = Date(System.currentTimeMillis())
            if (functionDefDTO.fDesc != null)
                functionDef.fDesc = functionDefDTO.fDesc
            functionDef.functionType = functionDefDTO.functionType

            val functionLocationBytes = Base64.getDecoder().decode(functionDefDTO.functionLocation)
            putFile(workflow.flowName, functionLocationBytes, functionDefDTO.functionFileName)
            functionDef.functionLocation = functionDefDTO.functionFileName
            val filePath = getFile(functionLocationBytes, functionDefDTO.functionFileName)
            if (!FunctionDefConfigChecker.getInstance().validate(
                    filePath,
                    functionDef.functionClass,
                    functionDefDTO.functionType
                )
            ) {
                rmFile(filePath)
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: Validation fails. Class does not implement or extends according to the Developer Guide.")
            } else {
                rmFile(filePath)
            }

            val save = functionDefDAO.save(functionDef)
            successWithData(save)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun updateFunction(functionDefDTO: FunctionDefDTO): Response<FunctionDef> {
        return try {
            val userId = currentUser().id
            val workflow = workFlowDAO.findByFlowNameAndAccountId(functionDefDTO.workFlowName, userId)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workflowName not exist")
            if (workFlowInstDAO.findByFlowNameAndAccountId(workflow.flowName, userId) != null)
                return failWithData(ErrorCode.SUBMIT_FAILED.code,
                    "ERROR: Workflow has been deploy, Modification and deletion operations are not allowed")
            val functionDef = functionDefDAO.findByFunctionNameAndAccountIdAndWorkFlowId(
                functionDefDTO.functionName, userId, workflow.flowId)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: functionName not exist")

            if (functionDefDTO.functionClass != null)
                functionDef.functionClass = functionDefDTO.functionClass
//            if (functionDefDTO.functionArgs != null)
//                functionDef.functionArgs = functionDefDTO.functionArgs
            if (functionDefDTO.functionVersion != null)
                functionDef.functionVersion = functionDefDTO.functionVersion
            if (functionDefDTO.fDesc != null)
                functionDef.fDesc = functionDefDTO.fDesc
//            if (functionDefDTO.functionType != null) {
//                if (!FunctionDefConfigChecker.getInstance().validateFunctionType(functionDefDTO.functionType))
//                    return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: functionType invalid")
//                functionDef.functionType = functionDefDTO.functionType
//            }
            if (functionDefDTO.functionLocation != null && functionDefDTO.functionFileName != null) {
                functionDef.functionLocation = functionDefDTO.functionFileName
                val functionLocationBytes = Base64.getDecoder().decode(functionDefDTO.functionLocation)
                putFile(workflow.flowName, functionLocationBytes, functionDefDTO.functionFileName)
                functionDef.functionLocation = functionDefDTO.functionFileName
                val filePath = getFile(functionLocationBytes, functionDefDTO.functionFileName)
                if (!FunctionDefConfigChecker.getInstance().validate(
                        filePath,
                        functionDef.functionClass,
                        functionDefDTO.functionType
                    )
                ) {
                    rmFile(filePath)
                    return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: Validation fails. Class does not implement or extends according to the Developer Guide.")
                } else {
                    rmFile(filePath)
                }
            }

//            else if (functionDefDTO.functionFileName != null) {
//                if (!validate(
//                        functionDef.functionLocation,
//                        functionDefDTO.functionFileName,
//                        functionDef.functionClass,
//                        functionDef.functionType)) {
//                    val functionLocationBytes = Base64.getDecoder().decode(functionDefDTO.functionLocation)
//                    putFile(workflow.flowName, functionLocationBytes, functionDefDTO.functionFileName)
//                    functionDef.functionLocation = functionDefDTO.functionFileName
//                    return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: Validation fails")
//                }
//            }

            functionDef.updateTime = Date(System.currentTimeMillis())

            val save = functionDefDAO.save(functionDef)
            successWithData(save)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    private fun validate(
        functionLocation: String,
        functionFileName: String,
        functionClass: String,
        functionType: Int
    ): Boolean {
        val functionLocationBytes = Base64.getDecoder().decode(functionLocation)
        val filePath = getFile(functionLocationBytes, functionFileName)
        return if (!FunctionDefConfigChecker.getInstance().validate(
                filePath,
                functionClass,
                functionType
            )
        ) {
            rmFile(filePath)
            false
        } else {
            rmFile(filePath)
            true
        }
    }

//    override fun createFunctionByMultipart(functionDefDTO: FunctionDefDTO, file: MultipartFile): Response<FunctionDef> {
//        return try {
//            val workflow = workFlowDAO.findByFlowNameAndAccountId(functionDefDTO.workFlowName, currentUser().id)
//                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is not exist")
//            if (workflow.status == WorkFlowStatus.PUBLISH.status)
//                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is PUBLISH")
//            val functionDef = FunctionDef()
//            functionDef.accountId = currentUser().id
//            functionDef.workFlowId = workflow.flowId
//            functionDef.functionName = functionDefDTO.functionName
//            functionDef.functionClass = functionDefDTO.functionClass
//            functionDef.functionArgs = functionDefDTO.functionArgs
//            functionDef.functionVersion = functionDefDTO.functionVersion
////            val functionLocationBytes = Base64.getDecoder().decode(functionDefDTO.functionLocation)
//            val functionLocationBytes = file.bytes
//            functionDef.functionLocation =
//                putFile(workflow.flowName, functionLocationBytes, functionDefDTO.functionFileName)
//            functionDef.createTime = Date(System.currentTimeMillis())
//            functionDef.updateTime = Date(System.currentTimeMillis())
//            if (!FunctionDefConfigChecker.getInstance().validate(
//                    getFile(functionLocationBytes, functionDefDTO.functionFileName),
//                    functionDef.functionClass,
//                    functionDefDTO.functionType
//                )
//            ) {
//                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: functionType is not validate")
//            }
//            functionDef.functionType = functionDefDTO.functionType
//            val save = functionDefDAO.save(functionDef)
//            successWithData(save)
//        } catch (e: Throwable) {
//            log.error(e.toString())
//            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $e")
//        }
//    }

    override fun uploadingFunctionFile(file: MultipartFile): Response<String> {
        return try {
            val fileBinaryStr = Base64.getEncoder().encodeToString(file.bytes)
            successWithData(fileBinaryStr)
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    override fun deleteFunction(workflowName: String, functionName: String): Response<String> {
        return try {
            val workflow = workFlowDAO.findByFlowNameAndAccountId(workflowName, currentUser().id)
                ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is not exist")
//            if (workflow.status == WorkFlowStatus.PUBLISH.status)
//                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: workFlowName is PUBLISH")
            val functionDefId = FunctionDefId()
            functionDefId.accountId = currentUser().id
            functionDefId.workFlowId = workflow.flowId
            functionDefId.functionName = functionName
            val functionDef = functionDefDAO.findById(functionDefId)
            if (!functionDef.isPresent)
                return failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: functionName is not exist")
            rmFile(workflowName, functionDef.get().functionLocation)
            functionDefDAO.deleteById(functionDefId)
            return success()
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            val errorMessage = ErrorUtils.getErrorMessage(e)
            failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: $errorMessage")
        }
    }

    private fun putFile(workflowName: String, functionLocationBytes: ByteArray, functionFileName: String) {
        try {
            val hdfsUrl = hdfsUrl()
            val fileSystem = fileSystem()
            val repositoryBk = WorkFlowClient.getUserBaseRepository(hdfsUrl, currentUser().id)
            val repositoryBkPath = Path(repositoryBk)
            val repository = repository(repositoryBk, workflowName)
            val repositoryPath = Path(repository)
            val udfJars = repository(repository, "udfjars")
            val udfJarsPath = Path(udfJars)

            if (!fileSystem.exists(repositoryBkPath))
                fileSystem.mkdirs(repositoryBkPath)
            if (!fileSystem.exists(repositoryPath))
                fileSystem.mkdirs(repositoryPath)
            if (!fileSystem.exists(udfJarsPath)) {
                fileSystem.mkdirs(udfJarsPath)
            } else {
                fileSystem.delete(udfJarsPath, true)
                fileSystem.mkdirs(udfJarsPath)
            }

            val filePath = Path(repository(udfJars, functionFileName))

            val out: FSDataOutputStream = fileSystem.create(filePath)
            out.write(functionLocationBytes)
            out.close()
            fileSystem.close()
        } catch (e: Throwable) {
            log.error("ERROR", "ERROR: ", e)
            throw Exception(e)
        }
    }

    private fun rmFile(workflowName: String, functionLocation: String) {
        val hdfsUrl = hdfsUrl()
        val fs = fileSystem()
        val repositoryBk = WorkFlowClient.getUserBaseRepository(hdfsUrl, currentUser().id)
        val repository = repository(repositoryBk, workflowName)
        val udfJars = repository(repository, "udfjars")
        val filePath = Path(repository(udfJars, functionLocation))
        fs.delete(filePath, true)
        fs.close()
    }

    private fun getFile(functionLocationBytes: ByteArray, functionFileName: String): String {
        val filePath = "/tmp/$functionFileName"
        val file = File(filePath)
        var output: OutputStream? = null
        var bufferedOutput: BufferedOutputStream? = null
        try {
            output = FileOutputStream(file)
            bufferedOutput = BufferedOutputStream(output)
            bufferedOutput.write(functionLocationBytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (null != bufferedOutput) {
                try {
                    bufferedOutput.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return "file://$filePath"
    }

    private fun rmFile(filePath: String) {
        val file = File(filePath.replace("file://", ""))
        if (file.exists())
            file.delete()
    }
}