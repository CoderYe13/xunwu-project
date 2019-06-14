package com.henu.service;

import com.henu.web.dto.HouseDTO;
import com.henu.web.form.DatatableSearch;
import com.henu.web.form.HouseForm;

/**
 * 房屋管理服务接口
 */
public interface IHouseService {
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);
}
