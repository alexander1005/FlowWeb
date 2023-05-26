package com.boraydata.flowregistry.service.impl;

import com.boraydata.flowregistry.dao.DestinationDAO;
import com.boraydata.flowregistry.entity.Destination;
import com.boraydata.flowregistry.entity.vo.DestinationConfigListVO;
import com.boraydata.flowregistry.entity.vo.DestinationListVO;
import com.boraydata.flowregistry.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DestinationServiceImpl implements DestinationService {

    @Autowired
    private DestinationDAO destinationDAO;

    @Override
    public List<DestinationListVO> list() {
        Iterable<Destination> all = destinationDAO.findAll();
        return StreamSupport.stream(all.spliterator(), true).map(d ->
                new DestinationListVO(d.getId(), d.getName())).collect(Collectors.toList());
    }

    @Override
    public String listTemplate(Integer id) {
        Optional<Destination> destination = destinationDAO.findById(id);
        return destination.map(Destination::getTemplate).orElse(null);
    }

    @Override
    public List<DestinationConfigListVO> listDestinationConfig() {
        Iterable<Destination> all = destinationDAO.findAll();
        return StreamSupport.stream(all.spliterator(), true).map(d ->
                new DestinationConfigListVO(d.getId(), d.getName(), d.getTemplate(), d.getTitle())).collect(Collectors.toList());
    }
}
