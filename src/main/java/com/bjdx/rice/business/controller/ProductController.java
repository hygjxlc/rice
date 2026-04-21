package com.bjdx.rice.business.controller;


import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.dto.product.ProductDTO;
import com.bjdx.rice.business.dto.product.ProductQueryDTO;
import com.bjdx.rice.business.entity.CustomerProduct;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "产品管理")
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    @ApiOperation("创建产品")
    public ResponseObj create(@RequestBody ProductDTO dto) {
        productService.create(dto);
        return ResponseObj.success();
    }

    @PostMapping("/update")
    @ApiOperation("更新产品")
    public ResponseObj update(@RequestBody ProductDTO dto) {
        productService.update(dto);
        return ResponseObj.success();
    }

    @PostMapping("/delete")
    @ApiOperation("删除产品")
    public ResponseObj delete(@RequestBody List<Long> ids) {
        productService.delete(ids);
        return ResponseObj.success();
    }

    @GetMapping("/get/{id}")
    @ApiOperation("查询产品")
    public ResponseObj<Product> get(@PathVariable Long id) {
        return ResponseObj.success().put(productService.get(id));
    }


    @PostMapping("/list")
    @ApiOperation("查询产品列表")
    public ResponseObj list(@RequestBody ProductQueryDTO query) {
        return ResponseObj.success().put(productService.query(query));
    }

    @GetMapping("/type")
    @ApiOperation("查询商品类别")
    public ResponseObj types() {
        return ResponseObj.success().put(productService.types());
    }

    @PostMapping("/import")
    @ApiOperation("导入商品")
    public ResponseObj importCustomers(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = productService.importFromExcel(file);
        return ResponseObj.success().put( result);
    }

    @GetMapping("/template")
    @ApiOperation("导入模板下载")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        productService.downloadTemplate(response);
    }

    @GetMapping("/all/{name}")
    @ApiOperation("查询所有商品")
    public ResponseObj<DropDownDTO> all(@PathVariable String name) {
        return ResponseObj.success().put(productService.getAllProducts(name));
    }
}