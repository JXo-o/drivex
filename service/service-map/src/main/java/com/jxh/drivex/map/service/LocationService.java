package com.jxh.drivex.map.service;

import com.jxh.drivex.model.form.map.OrderServiceLocationForm;
import com.jxh.drivex.model.form.map.SearchNearByDriverForm;
import com.jxh.drivex.model.form.map.UpdateDriverLocationForm;
import com.jxh.drivex.model.form.map.UpdateOrderLocationForm;
import com.jxh.drivex.model.vo.map.NearByDriverVo;
import com.jxh.drivex.model.vo.map.OrderLocationVo;
import com.jxh.drivex.model.vo.map.OrderServiceLastLocationVo;

import java.util.List;

public interface LocationService {

    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    Boolean removeDriverLocation(Long driverId);

    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);

    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);

    OrderLocationVo getCacheOrderLocation(Long orderId);

    Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList);

    OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId);
}
