package com.henu.web.controlle.admin;

import com.henu.base.ApiDataTableResponse;
import com.henu.base.ApiResponse;
import com.henu.entity.SupportAddress;
import com.henu.service.IAddressService;
import com.henu.service.IHouseService;
import com.henu.service.ServiceMultiResult;
import com.henu.service.ServiceResult;
import com.henu.web.dto.HouseDTO;
import com.henu.web.dto.SupportAddressDTO;
import com.henu.web.form.DatatableSearch;
import com.henu.web.form.HouseForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;

    @GetMapping("/admin/center")
    public String adminCenterPage() {
        return "admin/center";
    }

    @GetMapping("/admin/welcome")
    public String adminWelComePage() {
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    /**
     * 新增房源功能页
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        String fileName = file.getOriginalFilename();
        File target = new File("D:\\DataSpace\\xunwu-project\\tmp\\" + fileName);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
        return ApiResponse.ofSuccess(null);
    }

    @PostMapping("admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }
//        if(houseForm.getPhotos()==null||houseForm.getCover()==null){
//            System.out.println("没有图片");
//            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"必须上传图片");
//        }

        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }
        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
    }

    /**
     * 房源列表页
     */
    @GetMapping("admin/house/list")
    public String houseListPage(){
        return "admin/house-list";
    }
    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody){
      ServiceMultiResult<HouseDTO> result= houseService.adminQuery(searchBody);
      ApiDataTableResponse response=new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
      response.setData(result.getResult());
      response.setRecordFiltered(result.getTotal());
      response.setRecordsTotal(result.getTotal());
      response.setDraw(searchBody.getDraw());
        return response;
    }
}
