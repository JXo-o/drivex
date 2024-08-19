package com.jxh.drivex.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TradeType {

    REWARD(1, "系统奖励");

    @EnumValue
    private final Integer type;
    private final String content;

    TradeType(Integer type, String content) {
        this.type = type;
        this.content = content;
    }

}
