package com.henu.service.search;

import java.util.List;

import com.henu.service.ServiceMultiResult;
import com.henu.service.ServiceResult;
import com.henu.web.form.RentSearch;

/**
 * 检索接口
 * Created by 瓦力.
 */
public interface ISearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 获取补全建议关键词
     */
    ServiceResult<List<String>> suggest(String prefix);
}
