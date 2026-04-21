package com.bjdx.rice.business.service;

import com.alibaba.excel.EasyExcel;
import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.admin.entity.User;
import com.bjdx.rice.admin.service.CurrentUserService;
import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.dto.product.ProductDTO;
import com.bjdx.rice.business.dto.product.ProductQueryDTO;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.excel.ProductImportListener;
import com.bjdx.rice.business.exception.MyException;
import com.bjdx.rice.business.mapper.CustomerProductMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.bjdx.rice.business.utils.ImportUtil.buildResult;

@Service
@Transactional
public class ProductServiceImpl implements ProductService{

    private static final Set<String> TYPES = new HashSet<>(Arrays.asList("大米", "小米", "糯米", "食用油", "面粉", "调料", "食盐","其他"));

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CustomerProductMapper customerProductMapper;

    @Override
    public void create(ProductDTO productDTO) {
        String username = currentUserService.getCurrentUser().getUsername();
        LocalDateTime now = LocalDateTime.now();

        // 商品编号：为空则生成
        if (StringUtils.isBlank(productDTO.getProductCode())) {
            String code = "PROD-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            productDTO.setProductCode(code);
        }

        //新增时商品类别在TYPES中
        if (!TYPES.contains(productDTO.getProductType()))
            throw new MyException("商品类别错误");
        //商品编号不能重复
        Product productCode = productMapper.selectByProductCode(productDTO.getProductCode());
        if (productCode != null)
            throw new MyException("商品编号重复");

        Product product = new Product();
        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setProductType(productDTO.getProductType());
        product.setUnit(productDTO.getUnit());
        product.setPrice(productDTO.getPrice());
        product.setBrand(productDTO.getBrand());
        product.setSpecification(productDTO.getSpecification());
        product.setIndicatorDesc(productDTO.getIndicatorDesc());
        product.setRemarks(productDTO.getRemarks());
        product.setCreatedBy(username);
        product.setCreatedTime(now);
        product.setUpdatedBy(username);
        product.setUpdatedTime(now);
        productMapper.insert(product);

    }

    @Override
    public void update(ProductDTO productDTO) {
        //编辑时商品类别在TYPES中
        if (!TYPES.contains(productDTO.getProductType()))
            throw new MyException("商品类别错误");
        //商品编号除了本身不能重复
        Product productCode = productMapper.selectByProductCode(productDTO.getProductCode());
        if (productCode != null && !productCode.getId().equals(productDTO.getId()))
            throw new MyException("商品编号重复");

        Product product = productMapper.selectByPrimaryKey(productDTO.getId());
        if (product == null)
            throw new MyException("商品不存在");

        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setProductType(productDTO.getProductType());
        product.setUnit(productDTO.getUnit());
        product.setPrice(productDTO.getPrice());
        product.setBrand(productDTO.getBrand());
        product.setSpecification(productDTO.getSpecification());
        product.setIndicatorDesc(productDTO.getIndicatorDesc());
        product.setRemarks(productDTO.getRemarks());
        product.setUpdatedBy(currentUserService.getCurrentUser().getUsername());
        product.setUpdatedTime(LocalDateTime.now());


        productMapper.updateByPrimaryKey(product);

    }

    @Override
    public void delete(List<Long> ids) {
        //判断有没有使用
        try {
            for (Long id : ids) {
                productMapper.deleteByPrimaryKey(id);
            }
        }
        catch (Exception e) {
            throw new MyException("商品正在使用中");
        }
    }

    @Override
    public MyPage<Product> query(ProductQueryDTO customer) {
        MyPage<Product> page = new MyPage<>();
        PageHelper.startPage(customer.getPageNum(), customer.getPageSize());
        List<Product> list = productMapper.query(customer);
        if (list.isEmpty())
            return page;
        page = new MyPage<>(list);
        page.setList( list);
        return page;
    }

    @Override
    public Set<String> types() {
        return TYPES;
    }

    @Override
    public Product get(Long id) {
        Product product = productMapper.selectByPrimaryKey(id);
        if (Objects.isNull(product))
            throw new MyException("商品不存在");
        return product;
    }

    @Override
    public List<DropDownDTO> getAllProducts(String name) {

        return productMapper.getAllProducts(name);
    }


    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            List<String> EXAMPLE_ROW = Arrays.asList(
                    "PROD-20260122001",
                    "优质粳米（一级）",
                    "大米类",
                    "/",
                    "公斤",
                    "3.4",
                    "常金",
                    "常金",
                    "颗粒饱满、质地坚硬、色泽自然、香味浓郁。",
                    "备注"
            );
            List<String> HEADERS = Arrays.asList(
                    "商品编号", "商品名称", "商品类型","规格", "单位", "价格", "品牌", "品牌范围", "指标说明","备注"
            );

            Sheet sheet = workbook.createSheet("商品导入模板");

            // 创建表头样式
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

            // 写入示例行（作为填写示例）
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < EXAMPLE_ROW.size(); i++) {
                exampleRow.createCell(i).setCellValue(EXAMPLE_ROW.get(i));
            }

            // 设置列宽（单位：1/256 字符宽度）
            int[] columnWidths = {25 * 256, 30 * 256, 15 * 256, 10 * 256, 10 * 256, 15 * 256, 15 * 256, 40 * 256};
            for (int i = 0; i < columnWidths.length && i < HEADERS.size(); i++) {
                sheet.setColumnWidth(i, columnWidths[i]);
            }

            // 设置响应
            String fileName = "商品导入模板.xlsx";
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException("文件不能为空");
        }

        // 1. 使用 EasyExcel 解析
        ProductImportListener listener = new ProductImportListener();
        try {
            EasyExcel.read(file.getInputStream(), listener).sheet().doRead();
        } catch (MyException e) {
            throw e; // 表头缺失等致命错误
        } catch (Exception e) {
            throw new MyException("Excel 解析失败: " + e.getMessage());
        }

        List<Product> products = listener.getProducts();
        List<String> parseErrors = listener.getErrors();

        // 如果解析阶段就有错误，直接返回
        if (!parseErrors.isEmpty()) {
            return buildResult(products.size(), 0, products.size(), parseErrors);
        }

        if (products.isEmpty()) {
            return buildResult(0, 0, 0, Collections.emptyList());
        }

        // 2. 获取当前用户
        User currentUser = currentUserService.getCurrentUser();
        String nickname = currentUser.getNickname();
        LocalDateTime now = LocalDateTime.now();

        // 3. 填充公共字段 & 类型映射
        for (Product p : products) {
            // 商品编号：为空则生成
            if (StringUtils.isBlank(p.getProductCode())) {
                String code = "PROD-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                        "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                p.setProductCode(code);
            }

            // 商品类别映射与校验
            String originalType = p.getProductType();
            String mappedType = mapAndValidateProductType(originalType);
            p.setProductType(mappedType);

            p.setCreatedBy(nickname);
            p.setUpdatedBy(nickname);
            p.setCreatedTime(now);
            p.setUpdatedTime(now);
        }

        // 4. 校验并分离新增和更新的数据
        List<String> validationErrors = validateForImportWithUpdate(products);

        // 如果校验后没有有效数据，直接返回
        if (products.isEmpty() && !validationErrors.isEmpty()) {
            return buildResult(products.size(), 0, products.size(), validationErrors);
        }

        // 5. 分离需要新增和更新的数据
        List<Product> toInsert = new ArrayList<>();
        List<Product> toUpdate = new ArrayList<>();

        // 根据商品编号是否存在来分类
        separateInsertAndUpdateData(products, toInsert, toUpdate);

        // 6. 批量操作
        int batchSize = 500;
        int total = products.size();
        int inserted = 0;
        int updated = 0;
        List<String> operationErrors = new ArrayList<>();

        // 批量插入
        if (!toInsert.isEmpty()) {
            try {
                for (int i = 0; i < toInsert.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toInsert.size());
                    List<Product> batch = toInsert.subList(i, end);
                    productMapper.insertBatch(batch);
                    inserted += batch.size();
                }
            } catch (Exception e) {
                operationErrors.add("批量插入失败: " + e.getMessage());
            }
        }

        // 批量更新
        if (!toUpdate.isEmpty()) {
            try {
                for (int i = 0; i < toUpdate.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toUpdate.size());
                    List<Product> batch = toUpdate.subList(i, end);
                    productMapper.updateBatch(batch);
                    updated += batch.size();
                }
            } catch (Exception e) {
                operationErrors.add("批量更新失败: " + e.getMessage());
            }
        }

        int success = inserted + updated;
        int fail = total - success;
        List<String> msg = new ArrayList<>(parseErrors);
        //msg第一行插入消息
        msg.add(0, "成功导入" + success + "条数据，失败" + fail + "条数据。 \n"
                +" 新增"+inserted+"条数据,"+"更新"+updated+"条数据。 \n");
        msg.addAll(operationErrors);
        msg.addAll(validationErrors);
        msg.addAll(operationErrors);


        return buildResult(total, success, fail, msg);
    }

    /**
     * 校验导入数据并准备更新操作
     * 区分哪些是新增，哪些是更新
     */
    private List<String> validateForImportWithUpdate(List<Product> list) {
        Set<String> importingCodes = new HashSet<>();
        List<String> errors = new ArrayList<>();
        List<Product> validProducts = new ArrayList<>();

        for (Product p : list) {
            if (StringUtils.isBlank(p.getProductCode())) {
                errors.add("存在空的商品编号（系统应自动生成，此处异常）");
                continue;
            }
            if (StringUtils.isBlank(p.getProductName())) {
                errors.add("商品编号 [" + p.getProductCode() + "] 存在空的商品名称");
                continue;
            }

            // 检查本次导入内部是否重复
            if (!importingCodes.add(p.getProductCode())) {
                errors.add("导入文件中商品编号重复，已跳过: " + p.getProductCode());
                continue;
            }

            validProducts.add(p);
        }

        // 将过滤后的有效商品列表放回原列表
        list.clear();
        list.addAll(validProducts);

        return errors;
    }

    /**
     * 分离需要插入和更新的数据
     */
    private void separateInsertAndUpdateData(List<Product> products, List<Product> toInsert, List<Product> toUpdate) {
        if (products.isEmpty()) {
            return;
        }

        // 提取所有商品编号
        List<String> productCodes = products.stream()
                .map(Product::getProductCode)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 查询数据库中已存在的商品编号
        List<String> existingCodes = productMapper.selectProductCodesIn(productCodes);

        // 转换为Set便于快速查找
        Set<String> existingCodeSet = new HashSet<>(existingCodes);

        // 分类处理
        for (Product product : products) {
            if (existingCodeSet.contains(product.getProductCode())) {
                // 数据库中已存在，需要更新
                toUpdate.add(product);
            } else {
                // 数据库中不存在，需要新增
                toInsert.add(product);
            }
        }
    }


    /**
     * 将 Excel 中的“食品销售类型”映射为标准商品类别，并校验合法性
     */
    private String mapAndValidateProductType(String saleType) {
        if (saleType == null) {
            throw new MyException("商品类别不能为空");
        }
        saleType = saleType.trim();

        switch (saleType) {
            case "食用油类":
                return "食用油";
            case "食盐类":
                return "食盐";
            case "调味品类":
                return "调料";
            case "大米类":
                return "大米";
            case "大米":
            case "小米":
            case "糯米":
            case "面粉":
            case "食用油":
            case "调料":
            case "食盐":
                return saleType; // 已是标准值
            default:
                //如果saletype存在TYPES中的任一一个值就返回
                for (String type : TYPES)
                {
                    if (saleType.contains(type))
                    {
                        return type;
                    }
                }
                return "其他";
        }
    }



}