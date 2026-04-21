package com.bjdx.rice.business.service;

import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductReqDTO;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductResDTO;
import com.bjdx.rice.business.entity.CustomerProduct;
import com.bjdx.rice.business.exception.MyException;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.CustomerProductMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.github.pagehelper.PageHelper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.bjdx.rice.business.utils.ImportUtil.buildResult;

@Service
public class CustomerProductServiceImpl implements CustomerProductService{
    @Autowired
    private CustomerProductMapper customerProductMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CustomerInfoMapper customerInfoMapper;

    // 在 CustomerProductServiceImpl 类中定义
    private static final Map<String, Set<String>> COLUMN_KEYWORDS = new HashMap<>();

    static {
        // 商品名称相关关键词
        Set<String> productNameKeywords = new HashSet<>(Arrays.asList("品种名称","食品名称"));
        COLUMN_KEYWORDS.put("productName", productNameKeywords);

        // 单价
        Set<String> priceKeywords = new HashSet<>(Arrays.asList(
                "单价", "价格", "单价(元)(*)",
                "中标价格(元)", "中标价", "中标价格",
                "投标价格(元)", "投标价", "投标价格"
        ));
        COLUMN_KEYWORDS.put("price", priceKeywords);


        // 单位
        Set<String> unitKeywords = new HashSet<>(Arrays.asList("单位", "商品单位"));
        COLUMN_KEYWORDS.put("unit", unitKeywords);

    }

    @Override
    public BigDecimal getPrice(Long customerId, Long productId) {
        BigDecimal price = customerProductMapper.getPrice(customerId, productId);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = BigDecimal.ZERO;
        }
        return price;
    }

    @Override
    public BigDecimal getPriceByProductName(Long customerId, String productName) {
        BigDecimal price = customerProductMapper.getPriceByProductName(customerId, productName);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = BigDecimal.ZERO;
        }
        return price;
    }

    @Override
    public CustomerProduct findPriceExactMatch(Long customerId, Long productId, String productName) {
        return customerProductMapper.findPriceExactMatch(customerId, productId, productName);
    }

    @Override
    public CustomerProduct findPriceFuzzyMatch(Long customerId, Long productId, String coreProductName) {
        return customerProductMapper.findPriceFuzzyMatch(customerId, productId, coreProductName);
    }

    @Override
    public void createBid(CustomerProduct dto) {
        customerProductMapper.insert(dto);
    }

    @Override
    public void editBid(CustomerProduct dto) {
        customerProductMapper.updateByPrimaryKey(dto);
    }

    @Override
    public MyPage<CustomerProductResDTO> list(CustomerProductReqDTO dto) {
        MyPage<CustomerProductResDTO> page = new MyPage<>();
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<CustomerProductResDTO> list = customerProductMapper.list(dto);
        if (list.isEmpty())
            return page;
        page = new MyPage<>(list);
        page.setList( list);
        return page;
    }

    @Override
    public CustomerProductResDTO get(Long id) {
        return customerProductMapper.get(id);
    }

    @Override
    public void delete(Long id) {
        customerProductMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException("文件不能为空");
        }

        // 解析 Excel，识别需要删除和更新的数据
        ParseResult parseResult = parseExcelWithDeleteLogic(file);
        List<CustomerProduct> validList = parseResult.getValidRecords();
        List<String> errors = parseResult.getErrors();
        Set<String> deleteKeys = parseResult.getDeleteKeys(); // 需要删除的数据键集合

        if (validList.isEmpty() && deleteKeys.isEmpty() && errors.isEmpty()) {
            return buildResult(0, 0, 0, Collections.emptyList());
        }

        int batchSize = 500;
        int totalProcessed = parseResult.getTotalProcessed();
        int inserted = 0;
        int updated = 0;
        int deleted = 0;
        List<String> operationErrors = new ArrayList<>();

        // 1. 处理需要删除的数据 - 优化为批量删除
        if (!deleteKeys.isEmpty()) {
            try {
                // 批量删除：按客户ID分组，减少数据库交互次数
                Map<Long, List<Long>> customerIdToProductIds = new HashMap<>();

                // 按客户ID分组产品ID
                for (String deleteKey : deleteKeys) {
                    String[] parts = deleteKey.split("_");
                    Long customerId = Long.parseLong(parts[0]);
                    Long productId = Long.parseLong(parts[1]);
                    customerIdToProductIds.computeIfAbsent(customerId, k -> new ArrayList<>()).add(productId);
                }

                // 批量删除每个客户的相关产品
                for (Map.Entry<Long, List<Long>> entry : customerIdToProductIds.entrySet()) {
                    Long customerId = entry.getKey();
                    List<Long> productIds = entry.getValue();
                    customerProductMapper.deleteByCustomerAndProductIds(customerId, productIds);
                    deleted += productIds.size();
                }
            } catch (Exception e) {
                operationErrors.add("批量删除失败: " + e.getMessage());
            }
        }

        // 2. 处理有效的新增和更新数据
        if (!validList.isEmpty()) {
            // 批量查询现有数据
            Map<String, CustomerProduct> existingMap = new HashMap<>();
            try {
                Set<Long> customerIds = validList.stream()
                        .map(CustomerProduct::getCustomerId)
                        .collect(Collectors.toSet());
                Set<Long> productIds = validList.stream()
                        .map(CustomerProduct::getProductId)
                        .collect(Collectors.toSet());

                List<CustomerProduct> existingRecords = customerProductMapper.selectByCustomerAndProductIds(
                        new ArrayList<>(customerIds),
                        new ArrayList<>(productIds)
                );

                for (CustomerProduct existing : existingRecords) {
                    String key = existing.getCustomerId() + "_" + existing.getProductId();
                    existingMap.put(key, existing);
                }
            } catch (Exception e) {
                errors.add("查询现有客户产品数据失败: " + e.getMessage());
            }

            // 分离新增和更新的数据
            List<CustomerProduct> toInsert = new ArrayList<>();
            List<CustomerProduct> toUpdate = new ArrayList<>();

            for (CustomerProduct customerProduct : validList) {
                try {
                    String key = customerProduct.getCustomerId() + "_" + customerProduct.getProductId();
                    CustomerProduct existing = existingMap.get(key);

                    if (existing != null) {
                        // 存在则更新
                        customerProduct.setId(existing.getId());
                        toUpdate.add(customerProduct);
                    } else {
                        // 不存在则新增
                        toInsert.add(customerProduct);
                    }
                } catch (Exception e) {
                    errors.add("处理客户产品 [客户ID:" + customerProduct.getCustomerId() +
                            ", 产品ID:" + customerProduct.getProductId() + "] 时发生错误: " + e.getMessage());
                }
            }

            // 批量插入
            if (!toInsert.isEmpty()) {
                try {
                    for (int i = 0; i < toInsert.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, toInsert.size());
                        List<CustomerProduct> batch = toInsert.subList(i, end);
                        customerProductMapper.insertBatch(batch);
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
                        List<CustomerProduct> batch = toUpdate.subList(i, end);
                        customerProductMapper.updateBatch(batch);
                        updated += batch.size();
                    }
                } catch (Exception e) {
                    operationErrors.add("批量更新失败: " + e.getMessage());
                }
            }
        }

        int successCount = inserted + updated;
        int failCount = totalProcessed - successCount;
        List<String> msg = new ArrayList<>(errors);
        //msg第一行插入消息
        msg.add(0, "成功导入" + successCount + "条数据，失败" + failCount + "条数据。 \n"
        +"新增"+inserted+"条数据,"+"更新"+updated+"条数据,"+"删除"+deleted+"条数据。 \n");
        msg.addAll(operationErrors);
        
        return buildResult(totalProcessed, successCount, failCount, msg);
    }





    private List<CustomerProduct> parseExcel(MultipartFile file) {
        List<CustomerProduct> list = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            // === 1. 解析客户名称（第0行，A列）===
            Row customerNameRow = sheet.getRow(firstRowNum);
            if (customerNameRow == null) {
                throw new MyException("第1行缺失，无法读取客户名称");
            }
            String customerText = getCellValueAsString(customerNameRow.getCell(0));
            if (customerText == null || customerText.trim().isEmpty()) {
                throw new MyException("客户名称不能为空");
            }
            String[] split = customerText.split("-");
            String customerName = split[0].trim();
            Long customerId = customerInfoMapper.getIdByName(customerName);
            if (Objects.isNull(customerId) || customerId <= 0) {
                throw new MyException("客户不存在: " + customerName);
            }

            // === 2. 解析时间范围（第1行，A列）===
            Row timeRow = sheet.getRow(firstRowNum + 1);
            if (timeRow == null) {
                throw new MyException("第2行缺失，无法读取价格有效期");
            }
            String timeText = getCellValueAsString(timeRow.getCell(0));
            String startTimeStr = extractDateTime(timeText, 1);
            String endTimeStr = extractDateTime(timeText, 2);
            if (startTimeStr == null || endTimeStr == null) {
                throw new MyException("时间格式错误，未能解析开始/结束时间: " + timeText);
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // === 3. 解析标题行（第2行，即 firstRowNum + 2）===
            Row headerRow = sheet.getRow(firstRowNum + 2);
            if (headerRow == null) {
                throw new MyException("第3行（标题行）缺失");
            }
            Map<String, Integer> colIndexMap = resolveColumnIndexes(headerRow);

            Integer productNameCol = colIndexMap.get("productName");
            Integer priceCol = colIndexMap.get("price");

            // === 4. 遍历数据行（从第3行开始，即 firstRowNum + 3）===
            for (int i = firstRowNum + 3; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                // 商品名称
                String productName = getCellValueAsString(row.getCell(productNameCol));
                if (productName == null || productName.trim().isEmpty()) {
                    throw new MyException("第" + (i + 1) + "行：商品名称不能为空");
                }
                productName = productName.trim();

                Long productId = productMapper.getIdByName(productName);
                if (Objects.isNull(productId) || productId <= 0) {
                    throw new MyException("第" + (i + 1) + "行：商品不存在 - " + productName);
                }

                // 单价
                String priceStr = getCellValueAsString(row.getCell(priceCol));
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    throw new MyException("第" + (i + 1) + "行：单价不能为空");
                }
                priceStr = priceStr.trim();

                BigDecimal price;
                try {
                    price = new BigDecimal(priceStr);
                } catch (NumberFormatException e) {
                    throw new MyException("第" + (i + 1) + "行：单价格式错误 - " + priceStr);
                }

                // 构建实体
                CustomerProduct customer = new CustomerProduct();
                customer.setProductId(productId);
                customer.setCustomerId(customerId);
                customer.setPrice(price);
                customer.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
                customer.setEndTime(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));

                list.add(customer);
            }

        } catch (MyException e) {
            throw e; // 业务异常直接抛出
        } catch (Exception e) {
            throw new MyException("解析Excel失败: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * 根据标题行解析各字段对应的列索引
     */
    private Map<String, Integer> resolveColumnIndexes(Row headerRow) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        int lastCellNum = headerRow.getLastCellNum();
        if (lastCellNum <= 0) {
            throw new MyException("标题行为空");
        }

        for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
            String header = getCellValueAsString(headerRow.getCell(colIndex));
            if (header == null || header.isEmpty()) continue;

            for (Map.Entry<String, Set<String>> entry : COLUMN_KEYWORDS.entrySet()) {
                if (entry.getValue().contains(header)) {
                    columnIndexMap.putIfAbsent(entry.getKey(), colIndex);
                    break;
                }
            }
        }

        // 必填字段校验
        if (!columnIndexMap.containsKey("productName")) {
            throw new MyException("未找到商品名称列，请确保包含以下任一列名：" + COLUMN_KEYWORDS.get("productName"));
        }
        if (!columnIndexMap.containsKey("price")) {
            throw new MyException("未找到单价列，请确保包含以下任一列名：" + COLUMN_KEYWORDS.get("price"));
        }

        return columnIndexMap;
    }

    /**
     * 判断一行是否为空
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !getCellValueAsString(cell).isEmpty()) {
                return false;
            }
        }
        return true;
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

    /**
     * 从文本中提取日期时间
     * @param text 包含日期时间的文本
     * @param index 要提取第几个日期时间 (1表示第一个, 2表示第二个)
     * @return 提取到的日期时间字符串，如果不存在则返回null
     */
    private String extractDateTime(String text, int index) {
        // 正则表达式匹配 yyyy-MM-dd HH:mm:ss 格式的日期时间
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
            if (count == index) {
                return matcher.group();
            }
        }
        return null; // 没有找到指定位置的日期时间
    }

    // 内部类用于封装解析结果
    private static class ParseResult {
        private final List<CustomerProduct> validRecords;
        private final List<String> errors;
        private final int totalProcessed;
        private final Set<String> deleteKeys; // 需要删除的数据键集合 (customerId_productId格式)

        public ParseResult(List<CustomerProduct> validRecords, List<String> errors,
                           int totalProcessed, Set<String> deleteKeys) {
            this.validRecords = validRecords;
            this.errors = errors;
            this.totalProcessed = totalProcessed;
            this.deleteKeys = deleteKeys;
        }

        public List<CustomerProduct> getValidRecords() { return validRecords; }
        public List<String> getErrors() { return errors; }
        public int getTotalProcessed() { return totalProcessed; }
        public Set<String> getDeleteKeys() { return deleteKeys; }
    }

    private ParseResult parseExcelWithDeleteLogic(MultipartFile file) {
        List<CustomerProduct> validRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> deleteKeys = new HashSet<>();
        int totalProcessed = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            // === 1. 解析客户名称（第0行，A列）===
            Row customerNameRow = sheet.getRow(firstRowNum);
            if (customerNameRow == null) {
                throw new MyException("第1行缺失，无法读取客户名称");
            }
            String customerText = getCellValueAsString(customerNameRow.getCell(0));
            if (customerText == null || customerText.trim().isEmpty()) {
                throw new MyException("客户名称不能为空");
            }
            String[] split = customerText.split("-");
            String customerName = split[0].trim();
            Long customerId = customerInfoMapper.getIdByName(customerName);
            if (Objects.isNull(customerId) || customerId <= 0) {
                throw new MyException("客户不存在: " + customerName);
            }

            // === 2. 解析时间范围（第1行，A列）===
            Row timeRow = sheet.getRow(firstRowNum + 1);
            if (timeRow == null) {
                throw new MyException("第2行缺失，无法读取价格有效期");
            }
            String timeText = getCellValueAsString(timeRow.getCell(0));
            String startTimeStr = extractDateTime(timeText, 1);
            String endTimeStr = extractDateTime(timeText, 2);
            if (startTimeStr == null || endTimeStr == null) {
                throw new MyException("时间格式错误，未能解析开始/结束时间: " + timeText);
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // === 3. 解析标题行（第2行，即 firstRowNum + 2）===
            Row headerRow = sheet.getRow(firstRowNum + 2);
            if (headerRow == null) {
                throw new MyException("第3行（标题行）缺失");
            }
            Map<String, Integer> colIndexMap = resolveColumnIndexes(headerRow);

            Integer productNameCol = colIndexMap.get("productName");
            Integer priceCol = colIndexMap.get("price");

            // === 4. 收集所有商品名称，尝试批量查询匹配商品ID ===
            Map<String, Long> productNameToIdMap = new HashMap<>();
            Set<String> productNames = new HashSet<>();

            // 第一遍遍历：收集所有商品名称
            for (int i = firstRowNum + 3; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                String productName = getCellValueAsString(row.getCell(productNameCol));
                if (productName != null && !productName.trim().isEmpty()) {
                    productNames.add(productName.trim());
                }
            }

            // 尝试批量查询匹配的商品ID（非必须，匹配不到也继续）
            if (!productNames.isEmpty()) {
                try {
                    List<Map<String, Object>> productResults = productMapper.getIdsByNames(new ArrayList<>(productNames));
                    for (Map<String, Object> result : productResults) {
                        String name = (String) result.get("productName");
                        Long id = (Long) result.get("id");
                        if (name != null && id != null && id > 0) {
                            productNameToIdMap.put(name, id);
                        }
                    }
                } catch (Exception e) {
                    errors.add("批量查询商品ID失败: " + e.getMessage());
                }
            }

            // === 5. 第二遍遍历：处理具体数据 ===
            for (int i = firstRowNum + 3; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalProcessed++;

                // 商品名称
                String productName = getCellValueAsString(row.getCell(productNameCol));
                if (productName == null || productName.trim().isEmpty()) {
                    errors.add("第" + (i + 1) + "行：商品名称不能为空，已跳过");
                    continue;
                }
                productName = productName.trim();

                // 尝试获取商品ID（精确匹配），匹配不到则置为null
                Long productId = productNameToIdMap.get(productName);
                if (productId == null || productId <= 0) {
                    productId = null; // 未匹配到商品，product_id 置空
                }

                // 关键逻辑：价格为空时，标记为需要删除的数据
                String priceStr = getCellValueAsString(row.getCell(priceCol));
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    if (productId != null) {
                        // 有productId，可以按客户+商品ID删除
                        String deleteKey = customerId + "_" + productId;
                        deleteKeys.add(deleteKey);
                        errors.add("第" + (i + 1) + "行：单价为空，将删除该招标信息 [" +
                                customerName + " - " + productName + "]");
                    } else {
                        // 无productId，无法定位删除，仅记录提示
                        errors.add("第" + (i + 1) + "行：单价为空且未匹配到商品ID，无法删除 [" +
                                customerName + " - " + productName + "]");
                    }
                    continue;
                }
                priceStr = priceStr.trim();

                BigDecimal price;
                try {
                    price = new BigDecimal(priceStr);
                } catch (NumberFormatException e) {
                    errors.add("第" + (i + 1) + "行：单价格式错误 - " + priceStr + "，已跳过");
                    continue;
                }

                // 构建实体
                CustomerProduct customer = new CustomerProduct();
                customer.setProductId(productId);
                customer.setProductName(productName); // 直接存入Excel原始商品名
                customer.setCustomerId(customerId);
                customer.setPrice(price);
                customer.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
                customer.setEndTime(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));

                validRecords.add(customer);
            }

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("解析Excel失败: " + e.getMessage(), e);
        }

        return new ParseResult(validRecords, errors, totalProcessed, deleteKeys);
    }

}
