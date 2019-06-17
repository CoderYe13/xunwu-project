package com.henu.service;

import com.henu.web.dto.HouseDTO;
import com.henu.web.form.DatatableSearch;
import com.henu.web.form.HouseForm;

/**
 * 房屋管理服务接口
 */
public interface IHouseService {
    /**
     * 新增
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    /**
     * 修改
     * @param houseForm
     * @return
     */
    ServiceResult update(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    /**
     * 查询完整房屋信息
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompletaOne(Long id);

    /**
     * 移除图片
     * @param id
     * @return
     */
    ServiceResult removePhoto(Long id);

    /**
     * 更新封面接口
     * @param coverId
     * @param targetId
     * @return
     */
    ServiceResult updateCover(Long coverId, Long targetId);

    /**
     * 增加标签接口
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult addTag(Long houseId, String tag);

    /**
     * 移除标签接口
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult removeTag(Long houseId, String tag);

    /**
     * 更新房源zhuant
     * @param id
     * @param status
     * @return
     */
    ServiceResult updateStatus(Long id,int status);
}
