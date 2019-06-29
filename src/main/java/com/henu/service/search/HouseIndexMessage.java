package com.henu.service.search;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息结构体
 */
@Setter
@Getter
public class HouseIndexMessage {

    public static final String INDEX="index";
    public static final String REMOVE="remove";
    public static final int MAX_RETRY=3;

    private Long houseId;
    private String operation;
    private int retry=0;

    /**
     * 默认构造器，防止Jackson序列化失败
     */
    public HouseIndexMessage(){

    }

    public HouseIndexMessage(Long houseId, String operation, int retry) {
        this.houseId = houseId;
        this.operation = operation;
        this.retry = retry;
    }
}
