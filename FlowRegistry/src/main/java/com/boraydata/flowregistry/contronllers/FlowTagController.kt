package com.boraydata.flowregistry.contronllers

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.utils.ErrorUtils
import com.boraydata.flowregistry.entity.FlowTag
import com.boraydata.flowregistry.entity.FlowTagsMapping
import com.boraydata.flowregistry.entity.dto.FlowTagDTO
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateFlowDTO
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateWorkFlowDTO
import com.boraydata.flowregistry.service.impl.FlowTagServiceImpl
import com.boraydata.flowregistry.utils.*
import org.springframework.web.bind.annotation.*

@RequestMapping("/streams")
@RestController
class FlowTagController(private val flowTagService: FlowTagServiceImpl) {

    @GetMapping("/listTags")
    fun listTags(): Response<Iterable<FlowTag>> {
        return try {
            successWithData(flowTagService.listTags())
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }

    @GetMapping("/listTagsMapping")
    fun listTagsMapping(): Response<Iterable<FlowTagsMapping>> {
        return try {
            successWithData(flowTagService.listTagsMapping())
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }


    @PostMapping("/addTag")
    fun addTags(@RequestBody flowTagDTO: FlowTagDTO): Response<String> {
        return try {
            val addTags = flowTagService.addTags(flowTagDTO)
            if (addTags == null)
                success()
            else
                fail(addTags)
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }

    @PostMapping("/updateFlowTag")
    fun updateFlowTag(@RequestBody flowTagUpdateFlowDTO: FlowTagUpdateFlowDTO): Response<String> {
        return try {
            val updateFlowTag = flowTagService.updateFlowTag(flowTagUpdateFlowDTO)
            if (updateFlowTag.contains("ERROR"))
                return failWithData(ErrorCode.SUBMIT_FAILED.code, updateFlowTag)
            successWithData(updateFlowTag)
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }



    @PostMapping("/updateWorkFlowTag")
    fun updateWorkFlowTag(@RequestBody flowTagUpdateWorkFlowDTO: FlowTagUpdateWorkFlowDTO): Response<String> {
        return try {
            val updateFlowTag = flowTagService.updateWorkFlowTag(flowTagUpdateWorkFlowDTO)
            successWithData(updateFlowTag)
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }

    @DeleteMapping("/deleteTag")
    fun deleteTag(
        @RequestHeader(value = "tag") tagName: String
    ): Response<String> {
        return try {
            flowTagService.deleteTag(tagName)
            success()
        } catch (e: Throwable) {
            failWithData(ErrorCode.SUBMIT_FAILED.code, ErrorUtils.getErrorMessage(e))
        }
    }
}