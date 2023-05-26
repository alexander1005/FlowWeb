package com.boraydata.flowregistry.service;

import com.boraydata.flowregistry.entity.vo.DestinationConfigListVO;
import com.boraydata.flowregistry.entity.vo.DestinationListVO;

import java.util.List;

public interface DestinationService extends Service {

    List<DestinationListVO> list();

    String listTemplate(Integer id);

    List<DestinationConfigListVO> listDestinationConfig();

}
