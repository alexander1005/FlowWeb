package com.boraydata.flowregistry.contronllers

import com.boraydata.flowregistry.entity.dto.DestinationTemplateDTO
import com.boraydata.flowregistry.entity.vo.DestinationConfigListVO
import com.boraydata.flowregistry.entity.vo.DestinationListVO
import com.boraydata.flowregistry.service.impl.DestinationServiceImpl
import com.boraydata.flowregistry.utils.Response
import com.boraydata.flowregistry.utils.successWithData
import org.springframework.web.bind.annotation.*

@RequestMapping("/streams")
@RestController
class DestinationController(private val destinationServiceImpl: DestinationServiceImpl) {

    @GetMapping("/listDestination")
    fun listDestination(): Response<List<DestinationListVO>> {
        val list = destinationServiceImpl.list()
        return successWithData(list)
    }

    @PostMapping("/listTemplate")
    fun listTemplate(@RequestBody destinationTemplateDTO: DestinationTemplateDTO): Response<String> {
        val listTemplate = destinationServiceImpl.listTemplate(destinationTemplateDTO.id)
        return successWithData(listTemplate)
    }

    @GetMapping("/listDestinationConfig")
    fun listDestinationConfig(): Response<List<DestinationConfigListVO>> {
        val list = destinationServiceImpl.listDestinationConfig()
        return successWithData(list)
    }
}