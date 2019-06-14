package com.henu.web.controlle.house;

import com.henu.base.ApiResponse;
import com.henu.service.ServiceMultiResult;
import com.henu.service.IAddressService;
import com.henu.service.ServiceResult;
import com.henu.web.dto.SubwayDTO;
import com.henu.web.dto.SubwayStationDTO;
import com.henu.web.dto.SupportAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HouseController {
    @Autowired
    private IAddressService addressService;

    /**
     * 自动补全接口
     */
//    @GetMapping("rent/house/autocomplete")
//    @ResponseBody
//    public ApiResponse autocomplete(@RequestParam(value = "prefix") String prefix) {
//
//        if (prefix.isEmpty()) {
//            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
//        }
//        ServiceResult<List<String>> result = this.searchService.suggest(prefix);
//        return ApiResponse.ofSuccess(result.getResult());
//    }
    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result=addressService.findAllCities();
       if(result.getResultSize()==0){
           return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
       }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
        if (subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStationDTO> stationDTOS = addressService.findAllStationBySubway(subwayId);
        if (stationDTOS.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(stationDTOS);
    }

}
