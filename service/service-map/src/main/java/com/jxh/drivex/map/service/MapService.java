package com.jxh.drivex.map.service;

import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;

public interface MapService {

    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);
}
