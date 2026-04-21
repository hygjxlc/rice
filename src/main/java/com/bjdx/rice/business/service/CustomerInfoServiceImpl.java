package com.bjdx.rice.business.service;

import com.alibaba.excel.EasyExcel;
import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.admin.entity.User;
import com.bjdx.rice.admin.service.CurrentUserService;
import com.bjdx.rice.business.dto.customer.CustomerReqDTO;
import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.excel.CustomerImportListener;
import com.bjdx.rice.business.exception.MyException;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.time.ZoneId;
import java.util.stream.Collectors;

import static com.bjdx.rice.business.utils.ImportUtil.buildResult;

@Service
public class CustomerInfoServiceImpl implements CustomerInfoService {
    @Autowired
    private CustomerInfoMapper customerInfoMapper;
    @Autowired
    private CurrentUserService currentUserService;


    @Override
    public void create(CustomerInfo customer) {
        //单位名称不能重复
        CustomerInfo customerInfo = customerInfoMapper.selectByUnitName(customer.getUnitName());
        if (customerInfo != null)
            throw new MyException("客户名称不能重复");
        //单位编号为空则自动生成
        if (StringUtils.isEmpty(customer.getUnitCode())) {
            customer.setUnitCode(generateUnitCode());
        }
        User currentUser = currentUserService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customer.setCreatedBy(currentUser.getNickname());
        customer.setUpdatedBy(currentUser.getNickname());
        customerInfoMapper.insert( customer);
    }

    @Override
    public void update(CustomerInfo customer) {
        //单位名称不能重复
        CustomerInfo customerInfoOld = customerInfoMapper.selectByUnitName(customer.getUnitName());
        if (customerInfoOld != null
                && !customerInfoOld.getUnitName().equals(customer.getUnitName())) {
            throw new MyException("客户名称不能重复");
        }
        CustomerInfo customerInfo = customerInfoMapper.selectByPrimaryKey(customer.getId());
        User currentUser = currentUserService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        customerInfo.setUpdatedAt(now);
        customerInfo.setUpdatedBy(currentUser.getNickname());
        customerInfo.setUnitCode(customer.getUnitCode());
        customerInfo.setUnitName(customer.getUnitName());
        customerInfo.setDisabledFlag(customer.getDisabledFlag());
        customerInfo.setUnitAlias(customer.getUnitAlias());
        customerInfo.setContactPerson(customer.getContactPerson());
        customerInfo.setContactPhone(customer.getContactPhone());
        customerInfo.setRegion(customer.getRegion());
        customerInfo.setAddress(customer.getAddress());
        customerInfo.setMobilePhone(customer.getMobilePhone());
        customerInfo.setRemarks(customer.getRemarks());
        customerInfoMapper.updateByPrimaryKey(customerInfo);
    }

    @Override
    public void delete(Long id) {
        try {
            customerInfoMapper.deleteByPrimaryKey(id);
        }
        catch (Exception e) {
            throw new MyException("删除客户失败");
        }
    }

    @Override
    public MyPage<CustomerInfo> query(CustomerReqDTO customer) {
        MyPage<CustomerInfo> page = new MyPage<>();
        PageHelper.startPage(customer.getPageNum(), customer.getPageSize());
        List<CustomerInfo> list = customerInfoMapper.query(customer);
        if (list.isEmpty())
            return page;
        page = new MyPage<>(list);
        page.setList( list);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException("文件不能为空");
        }

        // 1. 使用 EasyExcel 解析
        CustomerImportListener listener = new CustomerImportListener();
        try {
            EasyExcel.read(file.getInputStream(), listener).sheet().doRead();
        } catch (MyException e) {
            throw e; // 表头缺失等致命错误
        } catch (Exception e) {
            throw new MyException("Excel 解析失败: " + e.getMessage());
        }

        List<CustomerInfo> customers = listener.getCustomers();
        List<String> parseErrors = listener.getErrors();

        if (customers.isEmpty() && parseErrors.isEmpty()) {
            return buildResult(0, 0, 0, Collections.emptyList());
        }

        // 2. 填充公共字段并处理单位编号
        User currentUser = currentUserService.getCurrentUser();
        String nickname = currentUser.getNickname();
        LocalDateTime now = LocalDateTime.now();

        for (CustomerInfo c : customers) {
            // 单位编号为空则自动生成
            if (StringUtils.isBlank(c.getUnitCode())) {
                c.setUnitCode(generateUnitCode());
            }

            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            c.setCreatedBy(nickname);
            c.setUpdatedBy(nickname);
        }

        // 3. 批量查询现有数据，避免逐条查询
        Set<String> unitNames = customers.stream()
                .map(CustomerInfo::getUnitName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, CustomerInfo> existingMap = new HashMap<>();
        if (!unitNames.isEmpty()) {
            try {
                // 批量查询所有单位名称对应的客户信息
                List<CustomerInfo> existingCustomers = customerInfoMapper.selectByUnitNames(new ArrayList<>(unitNames));
                for (CustomerInfo existing : existingCustomers) {
                    existingMap.put(existing.getUnitName(), existing);
                }
            } catch (Exception e) {
                parseErrors.add("批量查询现有客户数据失败: " + e.getMessage());
            }
        }

        // 4. 分离新增和更新的数据
        List<CustomerInfo> toInsert = new ArrayList<>();
        List<CustomerInfo> toUpdate = new ArrayList<>();

        // 根据批量查询结果判断是新增还是更新
        for (CustomerInfo customer : customers) {
            try {
                CustomerInfo existing = existingMap.get(customer.getUnitName());
                if (existing != null) {
                    // 存在则更新
                    customer.setId(existing.getId());
                    // 保留原有的单位编码，避免更新时改变编码
                    customer.setUnitCode(existing.getUnitCode());
                    toUpdate.add(customer);
                } else {
                    // 不存在则新增
                    toInsert.add(customer);
                }
            } catch (Exception e) {
                parseErrors.add("处理客户 [" + customer.getUnitName() + "] 时发生错误: " + e.getMessage());
            }
        }

        // 5. 执行批量操作
        int total = customers.size();
        int inserted = 0;
        int updated = 0;
        List<String> operationErrors = new ArrayList<>();

        // 批量插入
        if (!toInsert.isEmpty()) {
            try {
                int batchSize = 500;
                for (int i = 0; i < toInsert.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toInsert.size());
                    List<CustomerInfo> batch = toInsert.subList(i, end);
                    customerInfoMapper.insertBatch(batch);
                    inserted += batch.size();
                }
            } catch (Exception e) {
                operationErrors.add("批量插入失败: " + e.getMessage());
            }
        }

        // 批量更新
        if (!toUpdate.isEmpty()) {
            try {
                int batchSize = 500;
                for (int i = 0; i < toUpdate.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toUpdate.size());
                    List<CustomerInfo> batch = toUpdate.subList(i, end);
                    customerInfoMapper.updateBatch(batch);
                    updated += batch.size();
                }
            } catch (Exception e) {
                operationErrors.add("批量更新失败: " + e.getMessage());
            }
        }

        int success = inserted + updated;
        int fail = total - success;
        List<String> allErrors = new ArrayList<>(parseErrors);
        allErrors.addAll(operationErrors);

        return buildResult(total, success, fail, allErrors);
    }



    @Override
    public List<DropDownDTO> getAllCustomers(String name) {
        return customerInfoMapper.getAllCustomers(name);
    }


    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> EXAMPLE_ROW = Arrays.asList(
                "20260123001",
                "常金",
                "否",
                "常金",
                "黄先生",
                "13300000000",
                "江苏",
                "江苏省xxxx",
                "13300000000",
                "xxxx",
                "2026-12-20"  // 建议使用标准日期格式，避免歧义
        );
        List<String> HEADERS = Arrays.asList(
                "单位编码", "单位名称", "停用标志", "单位别名", "联系人",
                "联系电话", "地区", "联系地址", "移动电话", "备注", "建档日期"
        );


        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户导入模板");

            // 表头样式（加粗）
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 写入示例行
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < EXAMPLE_ROW.size(); i++) {
                exampleRow.createCell(i).setCellValue(EXAMPLE_ROW.get(i));
            }

            // 设置列宽（单位：1/256 字符宽度）
            int[] widths = {
                    20 * 256, // 单位编码
                    25 * 256, // 单位名称
                    10 * 256, // 停用标志
                    20 * 256, // 单位别名
                    15 * 256, // 联系人
                    18 * 256, // 联系电话
                    15 * 256, // 地区
                    35 * 256, // 联系地址
                    18 * 256, // 移动电话
                    25 * 256, // 备注
                    15 * 256  // 建档日期
            };

            for (int i = 0; i < widths.length && i < HEADERS.size(); i++) {
                sheet.setColumnWidth(i, widths[i]);
            }

            // 设置响应头
            String fileName = "客户导入模板.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            try (ServletOutputStream out = response.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }
        }
    }


    // 生成单位编码
    private String generateUnitCode() {
        return "CUST"+System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }



    private List<CustomerInfo> parseExcel(MultipartFile file) {
        List<CustomerInfo> list = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            for (int i = firstRowNum + 1; i <= lastRowNum; i++) { // 跳过标题行
                Row row = sheet.getRow(i);
                if (row == null) continue;

                CustomerInfo customer = new CustomerInfo();

                // 单位编码
                String unitCode = getCellValueAsString(row.getCell(0));
                if (StringUtils.isBlank(unitCode)) continue; // 空行跳过
                customer.setUnitCode(unitCode);

                // 单位名称
                customer.setUnitName(getCellValueAsString(row.getCell(1)));

                // 停用标志
                String disabledStr = getCellValueAsString(row.getCell(2));
                customer.setDisabledFlag("是".equals(disabledStr) ?
                        CustomerInfo.DisabledFlag.是 : CustomerInfo.DisabledFlag.否);

                // 其他字段
                customer.setUnitAlias(getCellValueAsString(row.getCell(3)));
                customer.setContactPerson(getCellValueAsString(row.getCell(4)));
                customer.setContactPhone(getCellValueAsString(row.getCell(5)));
                customer.setRegion(getCellValueAsString(row.getCell(6)));
                customer.setAddress(getCellValueAsString(row.getCell(7)));
                customer.setMobilePhone(getCellValueAsString(row.getCell(8)));
                customer.setRemarks(getCellValueAsString(row.getCell(9)));

                // 注意：createdAt/createdBy 等由 create() 方法自动填充，此处不设

                list.add(customer);
            }
        } catch (Exception e) {
            throw new MyException("解析Excel失败: " + e.getMessage());
        }
        return list;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime().toString();
                } else {
                    // 防止科学计数法，如电话号码变成 1.33E10
                    cell.setCellType(CellType.STRING);
                    return cell.getStringCellValue().trim();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
