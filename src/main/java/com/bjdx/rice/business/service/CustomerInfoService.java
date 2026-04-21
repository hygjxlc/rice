package com.bjdx.rice.business.service;

import com.bjdx.rice.business.dto.customer.CustomerReqDTO;
import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.entity.CustomerInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.bjdx.rice.admin.dto.DropDownDTO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CustomerInfoService {


    void create(@Valid CustomerInfo customer);

    void update(@Valid CustomerInfo customer);
    void delete(Long id);
    MyPage<CustomerInfo> query(CustomerReqDTO customer);

    @Transactional(rollbackFor = Exception.class)
    Map<String, Object> importFromExcel(MultipartFile file);

    List<DropDownDTO> getAllCustomers(String name);

    void downloadTemplate(HttpServletResponse response) throws IOException;
}
