package com.bjdx.rice.business.controller;


import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.business.dto.customer.CustomerReqDTO;
import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.service.CustomerInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@Api(tags = "客户管理")
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerInfoService customerService;

    @PostMapping("/create")
    @ApiOperation("创建客户")
    public ResponseObj create(@Valid @RequestBody CustomerInfo customer) {
        customerService.create(customer);
        return ResponseObj.success();
    }

    @PostMapping("/edit")
    @ApiOperation("编辑客户")
    public ResponseObj edit(@Valid @RequestBody CustomerInfo customer) {
        customerService.update(customer);
        return ResponseObj.success();
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除")
    public ResponseObj delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseObj.success();
    }

    @PostMapping("/query")
    @ApiOperation("列表查询客户")
    public ResponseObj<CustomerInfo> query(@RequestBody CustomerReqDTO customer) {
        return ResponseObj.success().put(customerService.query(customer));
    }

    @PostMapping("/import")
    @ApiOperation("导入客户")
    public ResponseObj importCustomers(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = customerService.importFromExcel(file);
        return ResponseObj.success().put( result);
    }

    @GetMapping("/template")
    @ApiOperation("导入模板下载")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        customerService.downloadTemplate(response);
    }

    @GetMapping("/all/{name}")
    @ApiOperation("获取所有客户")
    public ResponseObj<DropDownDTO> getAllCustomers(@PathVariable("name") String name) {
        return ResponseObj.success().put(customerService.getAllCustomers(name));
    }
}