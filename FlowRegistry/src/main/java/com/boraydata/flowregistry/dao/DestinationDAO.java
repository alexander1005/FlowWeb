package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.Destination;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationDAO extends PagingAndSortingRepository<Destination, Integer> {

}
