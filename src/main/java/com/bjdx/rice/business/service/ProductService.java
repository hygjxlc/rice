package com.bjdx.rice.business.service;

import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.dto.product.ProductDTO;
import com.bjdx.rice.business.dto.product.ProductQueryDTO;
import com.bjdx.rice.business.entity.Product;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductService {
    void create(ProductDTO productDTO);

    void update(ProductDTO productDTO);
    void delete(List<Long> ids);
    MyPage<Product> query(ProductQueryDTO customer);

    @Transactional(rollbackFor = Exception.class)
    Map<String, Object> importFromExcel(MultipartFile file) throws IOException;

    Set<String> types();

    Product get(Long id);

    List<DropDownDTO> getAllProducts(String name);

    void downloadTemplate(HttpServletResponse response) throws IOException;
}
