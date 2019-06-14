package com.henu.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Datatables 响应结构
 */
@Getter
@Setter
public class ApiDataTableResponse extends ApiResponse{
    private int draw;
    private long recordsTotal;
    private long recordFiltered;

    public ApiDataTableResponse(ApiResponse.Status status) {
        this(status.getCode(),status.getStandardMessage(),null);
    }

    public ApiDataTableResponse(int code, String mes, Object data) {
        super(code, mes, data);
    }
}
