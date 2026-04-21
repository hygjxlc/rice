package com.bjdx.rice.business.controller;

import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductReqDTO;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductResDTO;
import com.bjdx.rice.business.entity.CustomerProduct;
import com.bjdx.rice.business.service.CustomerProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;


@Api(tags = "招标")
@RestController
@RequestMapping("/customerProduct")
public class CustomerProductController {
    @Resource
    private CustomerProductService customerProductService;


    /**
     * 新建招标
     */
    @PostMapping("/add")
    @ApiOperation("新建招标")
    public ResponseObj createBid(@RequestBody CustomerProduct dto) {
        customerProductService.createBid(dto);
        return ResponseObj.success();
    }

    @PostMapping("/edit")
    @ApiOperation("修改招标")
    public ResponseObj editBid(@RequestBody CustomerProduct dto) {
        customerProductService.editBid(dto);
        return ResponseObj.success();
    }

    @PostMapping("/list")
    @ApiOperation("查询客户产品列表")
    public ResponseObj<CustomerProductResDTO> list(@RequestBody CustomerProductReqDTO dto) {
        return ResponseObj.success().put(customerProductService.list(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation("查询客户产品")
    public ResponseObj<CustomerProductResDTO> get(@PathVariable Long id) {
        return ResponseObj.success().put(customerProductService.get(id));
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除客户产品")
    public ResponseObj delete(@PathVariable Long id) {
        customerProductService.delete(id);
        return ResponseObj.success();
    }

    @PostMapping("/import")
    @ApiOperation("导入")
    public ResponseObj importCustomers(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = customerProductService.importFromExcel(file);
        return ResponseObj.success().put( result);
    }

    @PostMapping("/getPrice")
    @ApiOperation("获取价格")
    public ResponseObj getPrice(@RequestParam("customerId")Long customerId, @RequestParam("productId") Long productId) {
        return ResponseObj.success().put(customerProductService.getPrice(customerId, productId));
    }

}
