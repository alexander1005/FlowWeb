package com.boraydata.flowregistry.contronllers

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.enums.TagConstants
import com.boraydata.flowregistry.entity.dto.*
import com.boraydata.flowregistry.entity.vo.*
import com.boraydata.flowregistry.service.impl.DistributedLockServiceImpl
import com.boraydata.flowregistry.service.impl.FlowDefServiceImpl
import com.boraydata.flowregistry.utils.*
import com.boraydata.flowregistry.utils.UserUtils.currentUser
import org.springframework.core.io.FileSystemResource
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletResponse

@RequestMapping("/streams")
@RestController
class FlowDefController(
    private val flowDefService: FlowDefServiceImpl,
    private val distributedLockService: DistributedLockServiceImpl
) {

    @GetMapping("/list")
    fun listStreams(): Response<Iterable<FlowDefList>> {
        val listStreams = flowDefService.listStreams()
        return successWithData(listStreams)
    }

    @GetMapping("/listStreamFull")
    fun listStreamFull(): Response<Iterable<FlowDefListFull>> {
        val listStreams = flowDefService.listStreamFull()
        return successWithData(listStreams)
    }

    @GetMapping("/describe/{streamName}")
    fun describeStream(@PathVariable("streamName") streamName: String): Response<Any> {
        val flowDefVO = flowDefService.describeStream(streamName)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @GetMapping("/describeFull/{streamName}")
    fun describeStreamFull(@PathVariable("streamName") streamName: String): Response<FlowDefListFull> {
        val flowDefVO = flowDefService.describeStreamFull(streamName)
        return successWithData(flowDefVO)
    }

    @PostMapping("/create")
    fun createStream(@RequestBody flowDefDTO: FlowDefDTO): Response<Any> {
        val flowDefVO = flowDefService.createStream(flowDefDTO)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @PostMapping("/update")
    fun updateStream(@RequestBody flowDefUpdateDTO: FlowDefUpdateDTO): Response<Any> {
        val flowDefVO = flowDefService.updateStream(flowDefUpdateDTO)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @DeleteMapping("/deleteAll")
    fun deleteAllStreams(): Response<String> {
        val flowDefVO = flowDefService.deleteAll()
        return if (flowDefVO == null) success()
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @DeleteMapping("/delete")
    fun deleteStream(@RequestHeader("stream_name") streamName: String): Response<String> {
        val flowDefVO = flowDefService.deleteByStreamName(streamName)
        return if (flowDefVO == null) success()
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @PostMapping("/updateShardCount")
    fun updateShardCount(@RequestBody flowDefShardsDTO: FlowDefShardsDTO): Response<Any> {
        val flowDefVO = flowDefService.updateShardCount(flowDefShardsDTO)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @PostMapping("/updateRetention")
    fun updateRetention(@RequestBody flowDefRetentionDTO: FlowDefRetentionDTO): Response<Any> {
        val flowDefVO = flowDefService.updateRetention(flowDefRetentionDTO)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @PostMapping("/updateStatus")
    fun updateStatus(@RequestBody flowDefStatusDTO: FlowDefStatusDTO): Response<Any> {
        val flowDefVO = flowDefService.updateStatus(flowDefStatusDTO)
        return if (flowDefVO.stateCode) successWithData(flowDefVO)
        else failWithData(ErrorCode.SUBMIT_FAILED.code, "ERROR: " + flowDefVO.errorMessage)
    }

    @PostMapping("/applyCerts")
    fun applyCerts(@RequestBody applyCertsDTO: ApplyCertsDTO): Response<String> {
        var lock = false
        while (!lock) {
            lock = distributedLockService.tryLock("${TagConstants.applyCerts.name}-${currentUser().id}")
            if (!lock)
                TimeUnit.SECONDS.sleep(5)
        }
        return try {
            val applyCerts = flowDefService.applyCerts(applyCertsDTO)
            if (applyCerts.errorMessage == null) successWithData(applyCerts.clientPfx)
            else fail(applyCerts.errorMessage)
        } finally {
            distributedLockService.unlock("${TagConstants.applyCerts.name}-${currentUser().id}")
        }
    }

    @GetMapping("/applyCertsToFile")
    fun applyCertsToFile(response: HttpServletResponse, @RequestHeader("days") days: Int, @RequestHeader("renew") renew: Boolean) {
        var lock = false
        while (!lock) {
            lock = distributedLockService.tryLock("${TagConstants.applyCerts.name}-${currentUser().id}")
            if (!lock)
                TimeUnit.SECONDS.sleep(5)
        }
        try {
            flowDefService.applyCertsToFile(response, days, renew)
        } finally {
            distributedLockService.unlock("${TagConstants.applyCerts.name}-${currentUser().id}")
        }
    }

    @GetMapping("/listFormat")
    fun listFormat(): Response<Iterable<ListFormatVO>> {
        val listFormat = flowDefService.listFormat()
        return successWithData(listFormat)
    }

    @GetMapping("/listSqlTypes")
    fun listSqlTypes(): Response<Iterable<String>> {
        val listSqlTypes = flowDefService.listSqlTypes()
        return successWithData(listSqlTypes)
    }

    @GetMapping("/listStreamType")
    fun listStreamType(): Response<Iterable<ListStreamTypeVO>> {
        val listSqlTypes = flowDefService.listStreamType()
        return successWithData(listSqlTypes)
    }


    @GetMapping("/listCompression")
    fun listCompression(): Response<List<ListCompressionVO>> {
        val listCompression = flowDefService.listCompression()
        return successWithData(listCompression)
    }

    @GetMapping("/listEncryption")
    fun listEncryption(): Response<List<ListEncryptionVO>> {
        val listEncryption = flowDefService.listEncryption()
        return successWithData(listEncryption)
    }

    @GetMapping("/listFlowStatus")
    fun listFlowStatus(): Response<List<ListFlowDefStatusVO>> {
        val listFlowDefStatusVO = flowDefService.listFlowDefStatus()
        return successWithData(listFlowDefStatusVO)
    }

}
