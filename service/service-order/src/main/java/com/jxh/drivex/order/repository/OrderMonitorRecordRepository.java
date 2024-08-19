package com.jxh.drivex.order.repository;

import com.jxh.drivex.model.entity.order.OrderMonitorRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMonitorRecordRepository extends MongoRepository<OrderMonitorRecord, String> {

}
