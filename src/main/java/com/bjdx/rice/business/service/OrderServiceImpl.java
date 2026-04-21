package com.bjdx.rice.business.service;

import com.bjdx.rice.admin.service.CurrentUserService;
import com.bjdx.rice.business.dto.HttpResponseDTO;
import com.bjdx.rice.business.dto.order.*;
import com.bjdx.rice.business.entity.*;
import com.bjdx.rice.business.mapper.*;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import com.bjdx.rice.business.service.vector.impl.VectorSearchServiceImpl;
import com.bjdx.rice.business.utils.HttpUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import com.alibaba.fastjson.JSONObject;
import com.bjdx.rice.business.exception.MyException;
import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.excel.SalesOrderHeader;
import com.bjdx.rice.business.utils.ChatUtils;
import com.bjdx.rice.business.utils.NumberToChineseUtil;
import com.github.pagehelper.PageHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ymh
 * @date 2025/12/13 16:28
 */
@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Autowired
    private CustomerInfoMapper customerInfoMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CustomerProductService customerProductService;
    
    @Autowired
    private VectorSearchService vectorSearchService;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private LogMapper logMapper;
    @Value("${yongyou.url}")
    String yongyouUrl;

    @Value("${chatmodel.apiKey}")
    String apiKey;

    @Value("${chatmodel.url}")
    String chatModelUrl;

    @Value("${chatmodel.model}")
    String chatModelName;

    @Value("${vector.search.similarity-threshold:0.5}")
    private float vectorSimilarityThreshold;

    @Override
    @Transactional
    public Orders createOrder(CreateOrderRequest request) {
        // 创建订单主表
        Orders order = new Orders();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerId(request.getCustomerId());
        order.setPhone(request.getPhone());
        order.setContacts(request.getContacts());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setOrderAt(new Date());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setRemark(request.getRemark());
        order.setMark(OrderMark.正常);

        //判断订单类型是否在enum中
        // 设置订单类型
        if (StringUtils.isNotBlank(request.getOrderType())) {
            try {
                OrderType orderType = OrderType.valueOf(request.getOrderType());
                order.setOrderType(orderType);
            } catch (IllegalArgumentException e) {
                order.setOrderType(OrderType.电子订单); // 默认设置为电子订单
            }
        } else {
            order.setOrderType(OrderType.电子订单); // 默认设置为电子订单
        }

        order.setStatus(OrderStatus.待处理); // 默认状态为待处理

        // 生成订单编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        // 这里简化处理，实际应查询数据库获取序号
        String orderNo = "ORD" + dateStr;
        order.setOrderNo(orderNo);

        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        // 保存订单主表（此处省略实际的repository调用）
        ordersMapper.insert(order);

        // 处理订单明细
        List<OrderItems> orderItems = request.getItems().stream().map(itemDto -> {
            OrderItems item = new OrderItems();
            item.setOrderId(order.getId()); // 关联订单ID
            item.setProductName(itemDto.getProductName());
            item.setProductId(itemDto.getProductId());
            item.setSpecification(itemDto.getSpecification());
            item.setQuantity(itemDto.getQuantity());
            item.setUnit(itemDto.getUnit());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setProductNo(itemDto.getProductNo());

            // 计算小计
            double subtotal = itemDto.getQuantity() * itemDto.getUnitPrice();
            item.setSubtotal(subtotal);

            item.setCreatedAt(new Date());
            return item;
        }).collect(Collectors.toList());

        // 保存订单明细（此处省略实际的repository调用）
        orderItemsMapper.insertList(orderItems);

        return order;
    }

    @Transactional
    @Override
    public Orders updateOrder(UpdateOrderRequest request) {
        Long id = request.getId();
        // 查询订单是否存在
        Orders order = ordersMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new MyException("订单不存在");
        }

        // 更新订单信息
        order.setCustomerName(request.getCustomerName());
        order.setCustomerId(request.getCustomerId());
        order.setPhone(request.getPhone());
        order.setContacts(request.getContacts());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setOrderAt(order.getOrderAt());
        order.setMark(order.getMark());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setRemark(request.getRemark());

    // 设置订单类型
        if (StringUtils.isNotBlank(request.getOrderType())) {
            try {
                OrderType orderType = OrderType.valueOf(request.getOrderType());
                order.setOrderType(orderType);
            } catch (IllegalArgumentException e) {
                order.setOrderType(OrderType.电子订单); // 默认设置为电子订单
            }
        } else {
            order.setOrderType(OrderType.电子订单); // 默认设置为电子订单
        }

        order.setUpdatedAt(new Date());

        // 更新订单主表
        ordersMapper.updateByPrimaryKey(order);

        // 删除原有订单明细
        orderItemsMapper.deleteByOrderId(id);

        // 重新插入新的订单明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OrderItems> orderItems = request.getItems().stream().map(itemDto -> {
                OrderItems item = new OrderItems();
                item.setOrderId(id); // 关联订单ID
                item.setProductName(itemDto.getProductName());
                item.setProductId(itemDto.getProductId());
                item.setSpecification(itemDto.getSpecification());
                item.setQuantity(itemDto.getQuantity());
                item.setUnit(itemDto.getUnit());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setProductNo(itemDto.getProductNo());

                // 计算小计
                double subtotal = itemDto.getQuantity() * itemDto.getUnitPrice();
                item.setSubtotal(subtotal);

                item.setCreatedAt(new Date());
                return item;
            }).collect(Collectors.toList());

            orderItemsMapper.insertList(orderItems);
        }

        return order;
    }

    @Transactional
    @Override
    public void deleteOrder(Long id) {
        // 删除订单主表
        ordersMapper.deleteByPrimaryKey(id);

        // 删除订单明细
        orderItemsMapper.deleteByOrderId(id);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) {
        // 查询订单主表
        Orders order = ordersMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new MyException("订单不存在");
        }

        // 查询订单明细
        List<OrderItems> orderItems = orderItemsMapper.selectByOrderId(id);

        // 组装返回结果
        return OrderDetailResponse.from(order, orderItems);
    }

    @Override
    public MyPage<OrderPageResponse> queryOrderList(OrderPageQueryRequest request) {
        MyPage<OrderPageResponse> page = new MyPage<>();
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<OrderPageResponse> list = ordersMapper.queryOrderList(request);
        if (list.isEmpty())
        {
            return page;
        }
        page = new MyPage<>(list);
        //组织数据
        for (OrderPageResponse response : list)
        {

            List<OrderItems> orderItems = orderItemsMapper.selectByOrderId(response.getId());
            List<OrderPageResponse.ProductInfo> products = getProductInfos(orderItems);
            response.setProducts(products);
            //计算总金额
            double totalAmount = orderItems.stream().mapToDouble(OrderItems::getSubtotal).sum();
            response.setTotalAmount(totalAmount);
            response.setProductCount(orderItems.size());
        }
        page.setList(list);
        return page;
    }

    @Override
    public void finishOrder(Long id) {
        ordersMapper.finishOrder(id);
    }

    @Override
    public CreateOrderRequest orderRecognition(MultipartFile file, String orderType) {
        // 通用订单识别接口 - 优先使用前端传入的订单类型，否则由AI识别
        final String finalOrderType = StringUtils.isNotBlank(orderType) ? orderType : "";
        CreateOrderRequest request = new CreateOrderRequest();
        
        // 1. 接口调用日志
        logger.info("【订单识别】OrderServiceImpl.orderRecognition() 接口被调用，传入订单类型：{}，文件名：{}，文件大小：{} bytes",
                orderType, file.getOriginalFilename(), file.getSize());
        
        //检查文件是否是图片
        List<String> imageTypes = Arrays.asList("jpg", "jpeg", "png", "gif");
        String fileName = file.getOriginalFilename();
        if (fileName != null && !fileName.isEmpty()) {
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if(!imageTypes.contains(fileExtension)){
                throw new MyException("只可上传以下类型的文件:"+String.join(",",imageTypes));
            }
        }

        String userQuestionStr = "帮我根据图片内容生成json，按照下面格式生成。public class CreateOrderRequest implements Serializable {\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "    @ApiModelProperty(\"客户名称\")\n" +
                "    private String customerName;\n" +
                "\n" +
                "    @ApiModelProperty(\"联系人\")\n" +
                "    private String contacts;\n" +
                "\n" +
                "    @ApiModelProperty(\"联系电话\")\n" +
                "    private String phone;\n" +
                "\n" +
                "    @ApiModelProperty(\"配送地址\")\n" +
                "    private String deliveryAddress;\n" +
                "\n" +
                "    @ApiModelProperty(\"计划交期\")\n" +
                "    private Date deliveryDate;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单备注\")\n" +
                "    private String remark;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单类型: 电子订单, 手工订单, 微信订单\")\n" +
                "    private String orderType;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单商品明细\")\n" +
                "    private List<OrderItemDto> items;\n" +
                "}public class OrderItemDto implements Serializable {\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "    @ApiModelProperty(\"商品编号\")\n" +
                "    private String productNo;\n" +
                "\n" +
                "    @ApiModelProperty(\"商品名称\")\n" +
                "    private String productName;\n" +
                "    \n" +
                "    @ApiModelProperty(\"规格（如：5kg/袋）\")\n" +
                "    private String specification;\n" +
                "    \n" +
                "    @ApiModelProperty(\"数量\")\n" +
                "    private Double quantity;\n" +
                "    \n" +
                "    @ApiModelProperty(\"单位（如：袋、件、箱）\")\n" +
                "    private String unit;\n" +
                "    \n" +
                "    @ApiModelProperty(\"单价（元）\")\n" +
                "    private Double unitPrice;\n" +
                "}\n" +
                "以json的格式返回给我，只允许输出json不允许输出其他内容";
        String aiAnswer = getAiAnswer(file, userQuestionStr);

        if(aiAnswer.startsWith("```json")) {
            aiAnswer =aiAnswer.substring(7,aiAnswer.length()-3);
        }
        JSONObject jsonObject = JSONObject.parseObject(aiAnswer);

        //将json对象转换成实体类
        request = JSONObject.toJavaObject(jsonObject, CreateOrderRequest.class);
        
        // 通用接口：完全由前端传入的订单类型决定，不再依赖AI识别
        if (StringUtils.isNotBlank(finalOrderType)) {
            request.setOrderType(finalOrderType);
            logger.info("【订单识别】使用前端传入订单类型：{}", finalOrderType);
        } else {
            // 前端未传入，默认电子订单
            request.setOrderType("电子订单");
            logger.warn("【订单识别】前端未传入订单类型，默认设置为电子订单");
        }

        //找到对应的客户
        CustomerInfo customerInfo = customerInfoMapper.getCustomerByName(request.getCustomerName());
        //为空则再进行一次模糊匹配
        if (Objects.isNull(customerInfo))
        {
            String customerName = extractCoreCustomerName(request.getCustomerName());
            customerInfo = customerInfoMapper.getCustomerByName(customerName);
        }
        if (Objects.nonNull(customerInfo)) {
            request.setCustomerId(customerInfo.getId());
            request.setCustomerName(customerInfo.getUnitName());
            request.setContacts(customerInfo.getContactPerson());
            request.setPhone(customerInfo.getContactPhone());
            request.setDeliveryAddress(customerInfo.getAddress());

        }
        //还是找不到就返回一个默认客户
        else {
            request.setCustomerId(1L);
            request.setCustomerName(request.getCustomerName());
            request.setContacts(request.getContacts());
            request.setPhone(request.getPhone());
            request.setDeliveryAddress(request.getDeliveryAddress());

        }

        //找到对应产品
        for (OrderItemDto item : request.getItems())
        {
            Product product = productMapper.getProductByName(item.getProductName());
            if (Objects.nonNull( product)) {
                item.setProductId(product.getId());
                item.setProductName(product.getProductName());
                item.setProductNo(product.getProductCode());
                item.setSpecification(product.getSpecification());
                item.setUnit(product.getUnit());

                String question = "商品名称是"+item.getProductName()+", 单位是"+item.getUnit()+",规格是"+ item.getSpecification()+"，数量需要多少，只返回数量";
                String aiAnswer1 = getAiAnswer(file, question);
                try {
                    item.setQuantity(Double.valueOf(aiAnswer1));
                }
                catch (Exception ignored)
                {
                }
                if(Objects.nonNull(item.getUnitPrice()))
                {
                    continue;
                }

                //通过客户产品表获取单价
                BigDecimal price = customerProductService.getPrice(customerInfo.getId(), item.getProductId());
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                    // 按product_id未找到，尝试按商品名称查询
                    price = customerProductService.getPriceByProductName(customerInfo.getId(), item.getProductName());
                }
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    item.setUnitPrice(price.doubleValue());
                }
            }
            else {
                //没找到设置默认产品
                item.setProductId(1L);
                item.setProductName(item.getProductName());
                item.setProductNo(item.getProductNo());
                item.setSpecification(item.getSpecification());
                item.setUnit(item.getUnit());
                item.setUnitPrice(item.getUnitPrice());
                item.setQuantity(item.getQuantity());
            }
        }

        return request;
    }

    private String getAiAnswer(MultipartFile file, String userQuestionStr) {

        String imageBase64 = null;
        try {
            imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new MyException("图片处理异常",e);
        }
        List<Map>messages = new ArrayList<>();
        Map imageUrlMap = new HashMap();
        imageUrlMap.put("url",String.format("data:image/jpeg;base64,%s", imageBase64));

        List<Map> imagesMapList = new ArrayList<>();
        Map imagesMap = new HashMap();
        imagesMap.put("type","image_url");
        imagesMap.put("image_url",imageUrlMap);

        Map userQuestionMap = new HashMap();
        userQuestionMap.put("type", "text");
        userQuestionMap.put("text", userQuestionStr);
        imagesMapList.add(imagesMap);
        imagesMapList.add(userQuestionMap);

        Map userMsg = new HashMap();
        userMsg.put("role", "user");
        userMsg.put("content", imagesMapList);
        messages.add(userMsg);

        Map inputParam = new HashMap();
        inputParam.put("model", chatModelName);
        inputParam.put("messages", messages);


        String result = ChatUtils.call(chatModelUrl, apiKey, chatModelName, inputParam, null);
        JSONObject json = JSONObject.parseObject(result);
        return ChatUtils.getAiAnswer(json);
    }

    /**
     * 纯文本AI调用（无图片），用于语义匹配等场景
     */
    private String getAiTextAnswer(String userQuestionStr) {
        List<Map> messages = new ArrayList<>();

        // 系统提示词
        Map systemMsg = new HashMap();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个商品匹配助手，根据用户输入的商品名称，从候选商品列表中选择语义最匹配的商品。只返回序号，不要返回其他内容。");
        messages.add(systemMsg);

        // 用户提问
        Map userContent = new HashMap();
        userContent.put("type", "text");
        userContent.put("text", userQuestionStr);

        List<Map> contentList = new ArrayList<>();
        contentList.add(userContent);

        Map userMsg = new HashMap();
        userMsg.put("role", "user");
        userMsg.put("content", contentList);
        messages.add(userMsg);

        Map inputParam = new HashMap();
        inputParam.put("model", chatModelName);
        inputParam.put("messages", messages);

        String result = ChatUtils.call(chatModelUrl, apiKey, chatModelName, inputParam, null);
        JSONObject json = JSONObject.parseObject(result);
        return ChatUtils.getAiAnswer(json);
    }

    /**
     * AI语义匹配：将候选商品列表发送给AI，让AI选择语义最匹配的商品
     * @param inputName 用户输入的商品名称
     * @param candidates 候选商品列表（LCS搜索结果）
     * @return AI选择的最佳候选索引（0-based），-1表示AI无法判断
     */
    private int aiSemanticMatch(String inputName, List<VectorSearchService.VectorSearchResult> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return -1;
        }
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("用户要购买的商品是：").append(inputName).append("\n\n");
            prompt.append("请从以下候选商品中选择与用户需求语义最匹配的商品：\n");
            for (int i = 0; i < candidates.size(); i++) {
                prompt.append(i + 1).append(". ").append(candidates.get(i).getName()).append("\n");
            }
            prompt.append("\n请只返回最匹配商品的序号（1-" + candidates.size() + "），如果没有语义相关的商品，返回0。");

            logger.info("【订单识别】[AI语义匹配] 发送AI请求：\n{}", prompt.toString());
            long startTime = System.currentTimeMillis();
            String aiAnswer = getAiTextAnswer(prompt.toString());
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("【订单识别】[AI语义匹配] AI返回：'{}'，耗时：{}ms", aiAnswer, elapsed);

            // 解析AI返回的序号
            aiAnswer = aiAnswer.trim();
            // 提取数字
            String numStr = aiAnswer.replaceAll("[^0-9]", "");
            if (numStr.isEmpty()) {
                logger.warn("【订单识别】[AI语义匹配] AI返回无法解析为数字：'{}'", aiAnswer);
                return -1;
            }
            int idx = Integer.parseInt(numStr);
            if (idx == 0) {
                logger.info("【订单识别】[AI语义匹配] AI判断无语义相关商品");
                return -1;
            }
            if (idx < 1 || idx > candidates.size()) {
                logger.warn("【订单识别】[AI语义匹配] AI返回序号{}超出范围[1,{}]", idx, candidates.size());
                return -1;
            }
            return idx - 1; // 转为0-based
        } catch (Exception e) {
            logger.error("【订单识别】[AI语义匹配] AI调用失败：{}", e.getMessage());
            return -1;
        }
    }

    @Override
    public void export(HttpServletResponse response, Long id) throws IOException {
        OrderDetailResponse orderDetail = getOrderDetail(id);
        List<OrderItems> itemsList = orderDetail.getItems();

        // 构造头部
        SalesOrderHeader header = new SalesOrderHeader();
        header.setDocumentDate(DateFormatUtils.format(orderDetail.getOrderAt(), "yyyy-MM-dd"));
        header.setDocumentNo(orderDetail.getOrderNo());
        header.setPreparedBy("");
        header.setCustomer("");
        header.setAccountTaxNo("");
        header.setHandler("");
        header.setSummary("");

        // 计算汇总
        double totalQuantity = 0.0;
        double totalDiscountedAmount = 0.0;
        double totalTaxIncludedAmount = 0.0;

        // 创建 workbook
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("销售订单");

            // === 样式定义 ===
            // 标题样式
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // 左对齐
            CellStyle leftStyle = workbook.createCellStyle();
            leftStyle.setAlignment(HorizontalAlignment.LEFT);

            // 右对齐
            CellStyle rightStyle = workbook.createCellStyle();
            rightStyle.setAlignment(HorizontalAlignment.RIGHT);

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 合计加粗右对齐
            CellStyle boldRight = workbook.createCellStyle();
            boldRight.setAlignment(HorizontalAlignment.RIGHT);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldRight.setFont(boldFont);

            // === 第0行：标题 ===
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("销售订单");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // === 第1行 ===
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("单据日期：" + header.getDocumentDate());
            row1.getCell(0).setCellStyle(leftStyle);
            row1.createCell(3).setCellValue("单据编号：" + header.getDocumentNo());
            row1.getCell(3).setCellStyle(leftStyle);
            row1.createCell(8).setCellValue("制单人：" + header.getPreparedBy());
            row1.getCell(8).setCellStyle(leftStyle);

            // === 第2行 ===
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("往来单位：" + (header.getCustomer() == null ? "" : header.getCustomer()));
            row2.getCell(0).setCellStyle(leftStyle);
            row2.createCell(3).setCellValue("账号税号：" + (header.getAccountTaxNo() == null ? "" : header.getAccountTaxNo()));
            row2.getCell(3).setCellStyle(leftStyle);
            row2.createCell(8).setCellValue("经手人：" + (header.getHandler() == null ? "" : header.getHandler()));
            row2.getCell(8).setCellStyle(leftStyle);

            // === 第3行：单据摘要 ===
            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("单据摘要：" + (header.getSummary() == null ? "" : header.getSummary()));
            row3.getCell(0).setCellStyle(leftStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 10));

            // === 第4行：表头 ===
            String[] headers = {"行号", "商品编码", "商品名称", "单位", "数量", "单价", "金额", "折扣", "折后金额", "税率", "含税金额"};
            Row headerRow = sheet.createRow(4);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 设置列宽
            sheet.setColumnWidth(0, 5 * 256);
            sheet.setColumnWidth(1, 12 * 256);
            sheet.setColumnWidth(2, 20 * 256);
            sheet.setColumnWidth(3, 6 * 256);
            sheet.setColumnWidth(4, 8 * 256);
            sheet.setColumnWidth(5, 10 * 256);
            sheet.setColumnWidth(6, 12 * 256);
            sheet.setColumnWidth(7, 8 * 256);
            sheet.setColumnWidth(8, 12 * 256);
            sheet.setColumnWidth(9, 8 * 256);
            sheet.setColumnWidth(10, 15 * 256);

            // === 第5行开始：商品明细 ===
            int currentRowIdx = 5;
            for (int i = 0; i < itemsList.size(); i++) {
                OrderItems item = itemsList.get(i);
                Row dataRow = sheet.createRow(currentRowIdx++);

                dataRow.createCell(0).setCellValue(i + 1); // 行号
                dataRow.createCell(1).setCellValue(item.getProductNo()); // 商品编码
                dataRow.createCell(2).setCellValue(item.getProductName()); // 商品名称
                dataRow.createCell(3).setCellValue(item.getUnit()); // 单位
                dataRow.createCell(4).setCellValue(item.getQuantity()); // 数量
                dataRow.createCell(5).setCellValue(item.getUnitPrice()); // 单价
                dataRow.createCell(6).setCellValue(item.getSubtotal()); // 金额
                dataRow.createCell(7).setCellValue(0.0); // 折扣
                dataRow.createCell(8).setCellValue(item.getSubtotal()); // 折后金额
                dataRow.createCell(9).setCellValue(0.0); // 税率
                dataRow.createCell(10).setCellValue(item.getSubtotal()); // 含税金额

                // 累加
                totalQuantity += item.getQuantity();
                totalDiscountedAmount += item.getSubtotal();
                totalTaxIncludedAmount += item.getSubtotal();
            }

            // === 汇总部分（紧跟在数据后）===
            int firstSummaryRow = currentRowIdx; // 此时 currentRowIdx = 5 + items.size()

            // 页小计
            Row subtotalRow = sheet.createRow(firstSummaryRow);
            subtotalRow.createCell(0).setCellValue("页小计：");
            subtotalRow.getCell(0).setCellStyle(leftStyle);
            subtotalRow.createCell(4).setCellValue(totalQuantity);
            subtotalRow.createCell(8).setCellValue(totalDiscountedAmount);
            subtotalRow.createCell(10).setCellValue(totalTaxIncludedAmount);

            // 合计
            Row totalRow = sheet.createRow(firstSummaryRow + 1);
            totalRow.createCell(0).setCellValue("合计：");
            totalRow.getCell(0).setCellStyle(leftStyle);
            totalRow.createCell(4).setCellValue(totalQuantity);
            totalRow.createCell(8).setCellValue(totalDiscountedAmount);
            totalRow.createCell(10).setCellValue(totalTaxIncludedAmount);

            // 金额大写
            Row bigAmountRow = sheet.createRow(firstSummaryRow + 2);
            bigAmountRow.createCell(10).setCellValue("金额大写：" + NumberToChineseUtil.number2Chinese(totalTaxIncludedAmount));
            bigAmountRow.getCell(10).setCellStyle(rightStyle);

            // 企业信息
            Row companyRow = sheet.createRow(firstSummaryRow + 4);
            companyRow.createCell(0).setCellValue("企业名称：常州常金农业发展有限公司");
            companyRow.getCell(0).setCellStyle(leftStyle);

            Row contactRow = sheet.createRow(firstSummaryRow + 5);
            contactRow.createCell(0).setCellValue("电话：051983882479");
            contactRow.createCell(4).setCellValue("传真：051983882479");
            contactRow.getCell(0).setCellStyle(leftStyle);
            contactRow.getCell(4).setCellStyle(leftStyle);

            Row addressRow = sheet.createRow(firstSummaryRow + 6);
            addressRow.createCell(0).setCellValue("地 址：常州市钟楼区枫香路60号");
            addressRow.getCell(0).setCellStyle(leftStyle);

            // 写出
            workbook.write(bos);

            // 响应
            String fileName = URLEncoder.encode(orderDetail.getOrderNo(), StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            response.getOutputStream().write(bos.toByteArray());
        }
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request) {
        // 检查订单状态是否在 enum 中
        if (StringUtils.isNotBlank(request.getStatus())) {
            try {
                OrderStatus.valueOf(request.getStatus()); // 验证状态是否为有效枚举值
                ordersMapper.updateOrderStatus(request.getId(), request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new MyException("订单状态错误");
            }
        } else {
            throw new MyException("订单状态错误");
        }
    }
    @Override
    @Transactional
    public void syncToYongyou(Long id) {
        OrderDetailResponse orderDetail = getOrderDetail(id);

        if (orderDetail.getStatus().equals(OrderStatus.待处理.name())) {
            throw new MyException("订单待处理，不能同步");
        }
        if (orderDetail.getStatus().equals(OrderStatus.已完成.name())) {
            throw new MyException("订单已完成，不能同步");
        }
        // 转换成用友接口需要的格式
        YongyouSyncRequest yongyouRequest = convertToYongyouFormat(orderDetail);


        try {
            HttpResponseDTO response = HttpUtil.doPost(yongyouUrl, "POST", yongyouRequest, null, null);

            if (response.getHttpCode() == 200) {
                // 解析返回的JSON数据
                String responseData = response.getResponseData();
                JSONObject jsonResponse = JSONObject.parseObject(responseData);

                // 检查返回码和消息
                Integer code = jsonResponse.getInteger("code");
                String msg = jsonResponse.getString("msg");

                if (Objects.equals(msg,"成功")) {
                    // 返回体格式正确且表示成功，更新订单状态为已结束
                    ordersMapper.finishOrder(id);
                } else {
                    // 返回体格式不正确或表示失败
                    throw new MyException("同步到用友系统失败，返回信息: code=" + code + ", msg=" + msg);
                }
            } else {
                throw new MyException("同步到用友系统失败，HTTP状态码: " + response.getHttpCode() +
                        ", 响应内容: " + response.getResponseData());
            }
        } catch (Exception e) {
            throw new MyException("订单 " +orderDetail.getOrderNo()+" 同步到用友系统异常: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void batchSync(List<Long> ids) {
        for (Long id : ids)
        {
            syncToYongyou(id);
        }
    }

    @Override
    public CreateOrderRequest electronicOrderRecognition(MultipartFile file, String orderType) {
        // 电子单识别接口 - 优先使用前端传入的订单类型，否则默认为电子订单
        final String finalOrderType = StringUtils.isNotBlank(orderType) ? orderType : "电子订单";
        CreateOrderRequest request = new CreateOrderRequest();
        
        // 1. 接口调用日志
        logger.info("【订单识别】OrderServiceImpl.electronicOrderRecognition() 接口被调用，传入订单类型：{}，最终订单类型：{}，文件名：{}，文件大小：{} bytes",
                orderType, finalOrderType, file.getOriginalFilename(), file.getSize());
        //检查文件是否是图片
        List<String> imageTypes = Arrays.asList("jpg", "jpeg", "png", "gif");
        String fileName = file.getOriginalFilename();
        if (fileName != null && !fileName.isEmpty()) {
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if(!imageTypes.contains(fileExtension)){
                logger.error("【订单识别】文件格式不支持：{}，只允许：{}", fileExtension, imageTypes);
                throw new MyException("只可上传以下类型的文件:"+String.join(",",imageTypes));
            }
            logger.info("【订单识别】文件格式校验通过：{}", fileExtension);
        }

        // 2. AI识别请求日志
        logger.info("【订单识别】开始调用AI服务进行图片识别...");
        String userQuestionStr = "帮我根据图片内容生成json，按照下面格式生成。public class CreateOrderRequest implements Serializable {\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "    @ApiModelProperty(\"客户名称\")\n" +
                "    private String customerName;\n" +
                "\n" +
                "    @ApiModelProperty(\"联系人\")\n" +
                "    private String contacts;\n" +
                "\n" +
                "    @ApiModelProperty(\"联系电话\")\n" +
                "    private String phone;\n" +
                "\n" +
                "    @ApiModelProperty(\"配送地址\")\n" +
                "    private String deliveryAddress;\n" +
                "\n" +
                "    @ApiModelProperty(\"计划交期\")\n" +
                "    private Date deliveryDate;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单备注\")\n" +
                "    private String remark;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单类型: 电子订单, 手工订单, 微信订单\")\n" +
                "    private String orderType;\n" +
                "\n" +
                "    @ApiModelProperty(\"订单商品明细\")\n" +
                "    private List<OrderItemDto> items;\n" +
                "}public class OrderItemDto implements Serializable {\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "    @ApiModelProperty(\"商品编号\")\n" +
                "    private String productNo;\n" +
                "\n" +
                "    @ApiModelProperty(\"商品名称\")\n" +
                "    private String productName;\n" +
                "    \n" +
                "    @ApiModelProperty(\"规格（如：5kg/袋）\")\n" +
                "    private String specification;\n" +
                "    \n" +
                "    @ApiModelProperty(\"数量\")\n" +
                "    private Double quantity;\n" +
                "    \n" +
                "    @ApiModelProperty(\"单位（如：袋、件、箱）\")\n" +
                "    private String unit;\n" +
                "    \n" +
                "    @ApiModelProperty(\"单价（元）\")\n" +
                "    private Double unitPrice;\n" +
                "}\n" +
                "以json的格式返回给我，数量字段必须填充正确的数值，只允许输出json不允许输出其他内容";
        
        long aiStartTime = System.currentTimeMillis();
        String aiAnswer = getAiAnswer(file, userQuestionStr);
        long aiEndTime = System.currentTimeMillis();
        
        // 3. AI识别结果日志
        logger.info("【订单识别】AI服务调用完成，耗时：{} ms", aiEndTime - aiStartTime);
        logger.debug("【订单识别】AI原始返回：{}", aiAnswer);

        if(aiAnswer.startsWith("```json")) {
            aiAnswer =aiAnswer.substring(7,aiAnswer.length()-3);
            logger.debug("【订单识别】去除markdown标记后的AI返回");
        }
        
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(aiAnswer);
            logger.info("【订单识别】AI返回JSON解析成功");
        } catch (Exception e) {
            logger.error("【订单识别】AI返回JSON解析失败，返回内容：{}", aiAnswer, e);
            throw new MyException("AI识别结果解析失败，请重试");
        }

        //将json对象转换成实体类
        request = JSONObject.toJavaObject(jsonObject, CreateOrderRequest.class);
        
        // 电子单识别接口：完全由前端传入的订单类型决定，不再依赖AI识别
        if (StringUtils.isNotBlank(finalOrderType)) {
            request.setOrderType(finalOrderType);
            logger.info("【订单识别】使用前端传入订单类型：{}", finalOrderType);
        } else {
            // 前端未传入，默认电子订单
            request.setOrderType("电子订单");
            logger.warn("【订单识别】前端未传入订单类型，默认设置为电子订单");
        }
        
        // 4. AI识别结果详情日志
        logger.info("【订单识别】AI识别结果 - 客户名称：{}，订单类型：{}，商品数量：{}", 
                request.getCustomerName(), 
                request.getOrderType(),
                request.getItems() != null ? request.getItems().size() : 0);
        
        if (request.getItems() != null) {
            for (int i = 0; i < request.getItems().size(); i++) {
                OrderItemDto item = request.getItems().get(i);
                logger.info("【订单识别】商品{} - 名称：{}，编号：{}，数量：{}，单价：{}", 
                        i + 1, item.getProductName(), item.getProductNo(), 
                        item.getQuantity(), item.getUnitPrice());
            }
        }

        // 5. 客户和商品匹配流程
        // 微信单需要进行客户和商品匹配，电子单跳过此流程
        boolean isElectronicOrder = "电子订单".equals(request.getOrderType());
        
        if (isElectronicOrder) {
            // 电子单：跳过商品匹配，但需要匹配客户以获取customerId（价格查询需要）
            logger.info("【订单识别】订单类型为电子订单，跳过商品匹配流程，仅执行客户匹配");
            
            // 5.1 电子订单客户匹配
            logger.info("【订单识别】开始电子订单客户匹配，AI识别客户名称：'{}'", request.getCustomerName());
            CustomerInfo customerInfo = customerInfoMapper.getCustomerByName(request.getCustomerName());
            
            // 为空则再进行一次模糊匹配
            if (Objects.isNull(customerInfo)) {
                String customerName = extractCoreCustomerName(request.getCustomerName());
                if (!customerName.equals(request.getCustomerName())) {
                    logger.info("【订单识别】SQL精确匹配未找到客户，尝试截取后名称：'{}'", customerName);
                    customerInfo = customerInfoMapper.getCustomerByName(customerName);
                }
            }
            
            // 为空则再进行向量搜索匹配
            if (Objects.isNull(customerInfo) && vectorSearchService != null && vectorSearchService.isAvailable()) {
                logger.info("【订单识别】SQL匹配未找到客户，尝试向量搜索");
                VectorSearchService.VectorSearchResult vsResult = vectorSearchService.searchCustomer(
                        request.getCustomerName(), 3, vectorSimilarityThreshold);
                if (vsResult != null && vsResult.isSuccess()) {
                    customerInfo = (CustomerInfo) vsResult.getEntity();
                    logger.info("【订单识别】向量搜索找到客户：'{}'（ID：{}，相似度：{}）", 
                            vsResult.getName(), vsResult.getId(), String.format("%.4f", vsResult.getScore()));
                } else {
                    logger.warn("【订单识别】向量搜索未找到匹配客户");
                }
            }
            
            if (Objects.nonNull(customerInfo)) {
                request.setCustomerId(customerInfo.getId());
                request.setCustomerName(customerInfo.getUnitName());
                request.setContacts(customerInfo.getContactPerson());
                request.setPhone(customerInfo.getContactPhone());
                request.setDeliveryAddress(customerInfo.getAddress());
                logger.info("【订单识别】电子订单客户匹配成功：{}（ID：{}）", 
                        customerInfo.getUnitName(), customerInfo.getId());
            } else {
                logger.warn("【订单识别】电子订单客户匹配失败，customerId为空，三级价格查询策略1/2可能无法生效");
            }
        } else {
            // 微信单/手工单：进行客户和商品匹配
            logger.info("【订单识别】订单类型为{}，执行客户和商品匹配流程", request.getOrderType());
            
            // 5.1 客户匹配
            logger.info("【订单识别】开始客户匹配，AI识别客户名称：'{}'", request.getCustomerName());
            CustomerInfo customerInfo = customerInfoMapper.getCustomerByName(request.getCustomerName());
            
            // 为空则再进行一次模糊匹配
            if (Objects.isNull(customerInfo)) {
                logger.info("【订单识别】SQL精确匹配未找到客户，尝试截取特殊字符后匹配");
                String customerName = extractCoreCustomerName(request.getCustomerName());
                if (!customerName.equals(request.getCustomerName())) {
                    logger.info("【订单识别】截取客户核心名称：'{}' -> '{}'", request.getCustomerName(), customerName);
                }
                customerInfo = customerInfoMapper.getCustomerByName(customerName);
                
                if (Objects.isNull(customerInfo)) {
                    logger.info("【订单识别】截取后匹配仍未找到客户，尝试向量搜索");
                    // 尝试向量搜索
                    if (vectorSearchService != null && vectorSearchService.isAvailable()) {
                        VectorSearchService.VectorSearchResult vsResult = vectorSearchService.searchCustomer(
                                request.getCustomerName(), 3, vectorSimilarityThreshold);
                        if (vsResult != null && vsResult.isSuccess()) {
                            customerInfo = (CustomerInfo) vsResult.getEntity();
                            logger.info("【订单识别】向量搜索找到客户：'{}'（ID：{}，相似度：{}）", 
                                    vsResult.getName(), vsResult.getId(), String.format("%.4f", vsResult.getScore()));
                        } else {
                            logger.warn("【订单识别】向量搜索未找到匹配客户");
                        }
                    }
                }
            } else {
                logger.info("【订单识别】SQL精确匹配找到客户：'{}'（ID：{}）", 
                        customerInfo.getUnitName(), customerInfo.getId());
            }
            
            if (Objects.nonNull(customerInfo)) {
                request.setCustomerId(customerInfo.getId());
                request.setCustomerName(customerInfo.getUnitName());
                request.setContacts(customerInfo.getContactPerson());
                request.setPhone(customerInfo.getContactPhone());
                request.setDeliveryAddress(customerInfo.getAddress());
                logger.info("【订单识别】客户匹配成功：{}（ID：{}，联系人：{}，电话：{}）", 
                        customerInfo.getUnitName(), customerInfo.getId(),
                        customerInfo.getContactPerson(), customerInfo.getContactPhone());
            } else {
                // 使用默认客户
                request.setCustomerId(1L);
                logger.warn("【订单识别】客户匹配失败，使用默认客户（ID：1）");
            }
            
            // 5.2 商品匹配
            if (request.getItems() != null) {
                logger.info("【订单识别】开始商品匹配，共{}个商品", request.getItems().size());
                int matchCount = 0;
                int failCount = 0;
                
                for (int i = 0; i < request.getItems().size(); i++) {
                    OrderItemDto item = request.getItems().get(i);
                    logger.info("【订单识别】匹配商品{}/{}：'{}'", i + 1, request.getItems().size(), item.getProductName());
                    
                    // 三层商品匹配策略：SQL精确 → SQL LIKE子串 → LCS+规格二次区分
                    Product product = null;
                    String matchMethod = "未匹配";
                    String inputProductName = item.getProductName();
                    boolean isShortInput = inputProductName != null && inputProductName.length() <= 2;
                    
                    if (isShortInput) {
                        logger.info("【订单识别】商品名'{}'为短输入（≤2字），跳过SQL LIKE，提高匹配阈值防止误匹配", inputProductName);
                    }
                    
                    // 第1层：SQL精确匹配（商品名完全相等）
                    product = productMapper.getProductByExactName(inputProductName);
                    if (Objects.nonNull(product)) {
                        matchMethod = "SQL精确";
                        logger.info("【订单识别】[第1层-SQL精确] 匹配到商品：'{}'（ID：{}）", 
                                product.getProductName(), product.getId());
                    }
                    
                    // 第2层：SQL LIKE子串匹配（原始名称是商品名的子串）
                    // 短输入（≤2字）跳过此层，防止“油”匹配到“油条专用粉”等误匹配
                    if (Objects.isNull(product) && !isShortInput) {
                        List<Product> likeProducts = productMapper.getProductsByNameSubstring(inputProductName);
                        if (likeProducts != null && likeProducts.size() == 1) {
                            product = likeProducts.get(0);
                            matchMethod = "SQL LIKE(唯一)";
                            logger.info("【订单识别】[第2层-SQL LIKE] 匹配到唯一商品：'{}'（ID：{}）", 
                                    product.getProductName(), product.getId());
                        } else if (likeProducts != null && likeProducts.size() > 1) {
                            // 多个LIKE结果，需要通过规格区分或降级到LCS
                            logger.info("【订单识别】[第2层-SQL LIKE] 找到{}个匹配商品，需要规格区分", likeProducts.size());
                            // 尝试规格区分
                            Double inputSpecValue = VectorSearchServiceImpl.extractSpecValue(inputProductName);
                            if (inputSpecValue != null) {
                                // 用户输入有规格，在LIKE结果中找规格最接近的
                                double minSpecDiff = Double.MAX_VALUE;
                                for (Product p : likeProducts) {
                                    Double pSpecValue = VectorSearchServiceImpl.extractSpecValue(p.getProductName());
                                    if (pSpecValue != null) {
                                        double diff = Math.abs(inputSpecValue - pSpecValue);
                                        if (diff < minSpecDiff) {
                                            minSpecDiff = diff;
                                            product = p;
                                        }
                                    }
                                }
                                if (product != null) {
                                    matchMethod = "SQL LIKE+规格区分";
                                    logger.info("【订单识别】[第2层-SQL LIKE+规格区分] 匹配到商品：'{}'（ID：{}）", 
                                            product.getProductName(), product.getId());
                                }
                            }
                            // LIKE多个结果但无法规格区分，降级到LCS
                        }
                    }
                    
                    // 第3层：LCS比率匹配 + 规格二次区分 / AI语义判断
                    if (Objects.isNull(product) && vectorSearchService != null && vectorSearchService.isAvailable()) {
                        float lcsThreshold = isShortInput ? 0.4f : 0.5f;
                        logger.info("【订单识别】[第3层-LCS匹配] 尝试LCS匹配商品'{}'，阈值：{}", inputProductName, String.format("%.2f", lcsThreshold));
                        try {
                            // 短输入时取更多候选用于AI语义判断
                            int candidateTopK = isShortInput ? 5 : 3;
                            List<VectorSearchService.VectorSearchResult> candidates = 
                                    vectorSearchService.searchProductCandidates(inputProductName, candidateTopK, lcsThreshold);
                                                
                            if (candidates.isEmpty() && !isShortInput) {
                                // 降低阈值重试（仅非短输入时降低）
                                logger.info("【订单识别】[第3层-LCS匹配] 商品'{}'在阈值0.50未找到匹配，降低至0.40重试",
                                        inputProductName);
                                lcsThreshold = 0.4f;
                                candidates = vectorSearchService.searchProductCandidates(
                                        inputProductName, candidateTopK, lcsThreshold);
                            }
                                                
                            if (!candidates.isEmpty()) {
                                if (isShortInput) {
                                    // 短输入：交给AI做语义判断，而不是依赖LCS比率
                                    // LCS比率对短输入不可靠（如“油”LCS=1.0匹配“油条专用粉”）
                                    logger.info("【订单识别】[第4层-AI语义匹配] 短输入'{}'有{}个LCS候选，交由AI语义判断",
                                            inputProductName, candidates.size());
                                    int aiChoice = aiSemanticMatch(inputProductName, candidates);
                                    if (aiChoice >= 0) {
                                        VectorSearchService.VectorSearchResult aiResult = candidates.get(aiChoice);
                                        product = (Product) aiResult.getEntity();
                                        matchMethod = "AI语义匹配";
                                        logger.info("【订单识别】[第4层-AI语义匹配] AI选择商品：'{}'（ID：{}，LCS比率：{}）", 
                                                aiResult.getName(), aiResult.getId(), 
                                                String.format("%.4f", aiResult.getScore()));
                                    } else {
                                        logger.warn("【订单识别】[第4层-AI语义匹配] AI判断无语义相关商品，短输入'{}'匹配失败", inputProductName);
                                    }
                                } else {
                                    // 非短输入：规格二次区分
                                    VectorSearchService.VectorSearchResult bestResult = 
                                            VectorSearchServiceImpl.resolveSpecTiebreak(
                                                    inputProductName, candidates, 0.15f);
                                    if (bestResult != null && bestResult.isSuccess()) {
                                        // LCS比率偏低时（<0.6），匹配置信度不足，交由AI二次确认
                                        if (bestResult.getScore() < 0.6f) {
                                            logger.info("【订单识别】[第3层-LCS匹配] LCS比率{}<0.6，匹配置信度不足，交由AI语义判断",
                                                    String.format("%.4f", bestResult.getScore()));
                                            int aiChoice = aiSemanticMatch(inputProductName, candidates);
                                            if (aiChoice >= 0) {
                                                bestResult = candidates.get(aiChoice);
                                                matchMethod = "LCS+AI语义确认";
                                                logger.info("【订单识别】[第4层-AI语义确认] AI修正匹配：'{}'（ID：{}，LCS比率：{}）",
                                                        bestResult.getName(), bestResult.getId(),
                                                        String.format("%.4f", bestResult.getScore()));
                                            } else {
                                                // AI判断无相关商品，不匹配
                                                bestResult = null;
                                                logger.info("【订单识别】[第4层-AI语义确认] AI判断无语义相关商品，放弃LCS匹配");
                                            }
                                        }
                                        if (bestResult != null && bestResult.isSuccess()) {
                                            product = (Product) bestResult.getEntity();
                                            if ("LCS+AI语义确认".equals(matchMethod)) {
                                                // already logged
                                            } else {
                                                matchMethod = "LCS+规格区分";
                                                logger.info("【订单识别】[第3层-LCS+规格区分] 匹配到商品：'{}'（ID：{}，LCS比率：{}，阈值：{}）", 
                                                        bestResult.getName(), bestResult.getId(), 
                                                        String.format("%.4f", bestResult.getScore()), 
                                                        String.format("%.2f", lcsThreshold));
                                            }
                                        }
                                    }
                                }
                            } else {
                                logger.warn("【订单识别】[第3层-LCS匹配] 商品'{}'未找到匹配（阈值：{}）", 
                                        inputProductName, String.format("%.2f", lcsThreshold));
                            }
                        } catch (Exception e) {
                            logger.error("【订单识别】[第3层-LCS匹配] 匹配失败：{}", e.getMessage());
                        }
                    }
                    
                    if (Objects.nonNull(product)) {
                        item.setProductId(product.getId());
                        item.setProductName(product.getProductName());
                        item.setProductNo(product.getProductCode());
                        item.setSpecification(product.getSpecification());
                        item.setUnit(product.getUnit());
                        
                        // 获取价格
                        if (Objects.nonNull(customerInfo)) {
                            BigDecimal price = customerProductService.getPrice(customerInfo.getId(), item.getProductId());
                            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                                // 按product_id未找到，尝试按商品名称查询
                                price = customerProductService.getPriceByProductName(customerInfo.getId(), item.getProductName());
                            }
                            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                                item.setUnitPrice(price.doubleValue());
                                logger.info("【订单识别】获取客户专属价格：{} = {}元", 
                                        product.getProductName(), price);
                            } else {
                                // 使用基础价格
                                BigDecimal basePrice = productMapper.getPrice(item.getProductId());
                                if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
                                    item.setUnitPrice(basePrice.doubleValue());
                                    logger.info("【订单识别】获取基础价格：{} = {}元", 
                                            product.getProductName(), basePrice);
                                } else {
                                    logger.warn("【订单识别】商品'{}'无价格信息", product.getProductName());
                                }
                            }
                        }
                        matchCount++;
                        logger.info("【订单识别】商品匹配成功：{}（ID：{}，编号：{}，规格：{}，单价：{}）", 
                                product.getProductName(), product.getId(), 
                                product.getProductCode(), product.getSpecification(),
                                item.getUnitPrice());
                    } else {
                        // 使用默认商品
                        item.setProductId(1L);
                        failCount++;
                        logger.warn("【订单识别】商品'{}'匹配失败，使用默认商品（ID：1）", item.getProductName());
                    }
                }
                logger.info("【订单识别】商品匹配完成：成功{}个，失败{}个", matchCount, failCount);
            }
        }
        
        // 6. 价格检查与补充（所有订单类型都需要）
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            logger.info("【订单识别】开始检查商品价格，共{}个商品", request.getItems().size());
            int priceFixedCount = 0;
            
            for (OrderItemDto item : request.getItems()) {
                // 检查价格是否为null或0
                if (item.getUnitPrice() == null || item.getUnitPrice() <= 0) {
                    
                    if (isElectronicOrder) {
                        // ========== 电子订单：三级价格查询策略 ==========
                        logger.info("【订单识别】电子订单商品'{}'价格缺失，启动三级查询策略", item.getProductName());
                        
                        boolean priceFound = false;
                        
                        // 策略1/2需要customerId，无则直接跳到策略3
                        if (request.getCustomerId() != null) {
                            // 策略1：customer_product 表 customer_id + product_name 精确查询
                            CustomerProduct cpExact = customerProductService.findPriceExactMatch(
                                    request.getCustomerId(), item.getProductId(), item.getProductName());
                        if (cpExact != null && cpExact.getPrice() != null 
                                && cpExact.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                            item.setUnitPrice(cpExact.getPrice().doubleValue());
                            priceFixedCount++;
                            priceFound = true;
                            logger.info("【订单识别】[策略1-精确匹配] 商品'{}'找到价格：{}元，" +
                                    "customer_product记录ID：{}，product_name：{}，结束时间：{}",
                                    item.getProductName(), cpExact.getPrice(),
                                    cpExact.getId(), cpExact.getProductName(), cpExact.getEndTime());
                        }
                        
                        // 策略2：customer_product 表 customer_id + product_id 精确 + product_name 核心中文名模糊查询
                        if (!priceFound) {
                            String coreName = extractCoreProductName(item.getProductName());
                            logger.info("【订单识别】[策略2-模糊匹配] 商品'{}'提取核心名称：'{}'，开始模糊查询",
                                    item.getProductName(), coreName);
                            CustomerProduct cpFuzzy = customerProductService.findPriceFuzzyMatch(
                                    request.getCustomerId(), item.getProductId(), coreName);
                            if (cpFuzzy != null && cpFuzzy.getPrice() != null
                                    && cpFuzzy.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                                item.setUnitPrice(cpFuzzy.getPrice().doubleValue());
                                priceFixedCount++;
                                priceFound = true;
                                logger.info("【订单识别】[策略2-模糊匹配] 商品'{}'找到价格：{}元，" +
                                        "customer_product记录ID：{}，product_name：{}，结束时间：{}",
                                        item.getProductName(), cpFuzzy.getPrice(),
                                        cpFuzzy.getId(), cpFuzzy.getProductName(), cpFuzzy.getEndTime());
                            }
                        }
                        } else {
                            logger.info("【订单识别】电子订单客户ID为空，跳过策略1/2，直接使用向量搜索");
                        }
                        
                        // 策略3：LCS比率算法匹配商品（最长公共子序列，区分度高于N-Gram余弦）
                        if (!priceFound) {
                            logger.info("【订单识别】[策略3-LCS匹配] 商品'{}'前两个策略均未找到价格，尝试LCS匹配",
                                    item.getProductName());
                            if (vectorSearchService != null && vectorSearchService.isAvailable()) {
                                try {
                                    float lcsThreshold = 0.5f;
                                    VectorSearchService.VectorSearchResult vsResult = vectorSearchService.searchProduct(
                                            item.getProductName(), 1, lcsThreshold);
                                    
                                    if (vsResult == null || !vsResult.isSuccess()) {
                                        logger.info("【订单识别】[策略3-LCS匹配] 商品'{}'在阈值0.50未找到匹配，降低至0.40重试",
                                                item.getProductName());
                                        lcsThreshold = 0.4f;
                                        vsResult = vectorSearchService.searchProduct(
                                                item.getProductName(), 1, lcsThreshold);
                                    }
                                    
                                    if (vsResult != null && vsResult.isSuccess()) {
                                        BigDecimal basePrice = productMapper.getPrice(vsResult.getId());
                                        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
                                            item.setUnitPrice(basePrice.doubleValue());
                                            priceFixedCount++;
                                            priceFound = true;
                                            logger.info("【订单识别】[策略3-LCS匹配] 商品'{}'找到价格：{}元，" +
                                                    "匹配商品：{}（阈值：{}，LCS比率：{}）",
                                                    item.getProductName(), basePrice,
                                                    vsResult.getName(), String.format("%.2f", lcsThreshold), String.format("%.2f", vsResult.getScore()));
                                        } else {
                                            logger.warn("【订单识别】[策略3-LCS匹配] 找到商品'{}'，但基础价格为空",
                                                    item.getProductName());
                                        }
                                    } else {
                                        logger.warn("【订单识别】[策略3-LCS匹配] 商品'{}'未找到匹配结果" +
                                                "（已尝试阈值0.50和0.40）",
                                                item.getProductName());
                                    }
                                } catch (Exception e) {
                                    logger.error("【订单识别】[策略3-LCS匹配] 失败：{}", e.getMessage());
                                }
                            } else {
                                logger.warn("【订单识别】[策略3-LCS匹配] 向量搜索服务不可用");
                            }
                        }
                        
                        if (!priceFound) {
                            logger.warn("【订单识别】电子订单商品'{}'三级查询策略均未找到价格", item.getProductName());
                        }
                        
                    } else {
                        // 微信/手工订单：使用LCS比率算法+规格二次区分匹配商品查找价格
                        logger.info("【订单识别】商品'{}'价格缺失或为0，尝试通过LCS+规格区分查找价格", item.getProductName());
                        
                        if (vectorSearchService != null && vectorSearchService.isAvailable()) {
                            try {
                                float lcsThreshold = 0.5f;
                                List<VectorSearchService.VectorSearchResult> candidates = 
                                        vectorSearchService.searchProductCandidates(item.getProductName(), 3, lcsThreshold);
                                
                                if (candidates.isEmpty()) {
                                    logger.info("【订单识别】商品'{}'在阈值0.50未找到匹配，降低至0.40重试",
                                            item.getProductName());
                                    lcsThreshold = 0.4f;
                                    candidates = vectorSearchService.searchProductCandidates(
                                            item.getProductName(), 3, lcsThreshold);
                                }
                                
                                if (!candidates.isEmpty()) {
                                    VectorSearchService.VectorSearchResult bestResult = 
                                            VectorSearchServiceImpl.resolveSpecTiebreak(
                                                    item.getProductName(), candidates, 0.15f);
                                    if (bestResult != null && bestResult.isSuccess()) {
                                        BigDecimal basePrice = productMapper.getPrice(bestResult.getId());
                                        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
                                            item.setUnitPrice(basePrice.doubleValue());
                                            priceFixedCount++;
                                            logger.info("【订单识别】通过LCS+规格区分为商品'{}'找到价格：{}，匹配商品：{}（阈值：{}，LCS比率：{}）",
                                                    item.getProductName(), basePrice, bestResult.getName(), String.format("%.2f", lcsThreshold), String.format("%.4f", bestResult.getScore()));
                                        } else {
                                            logger.warn("【订单识别】LCS+规格区分匹配找到商品'{}'，但基础价格为空", bestResult.getName());
                                        }
                                    }
                                } else {
                                    logger.warn("【订单识别】LCS匹配未找到商品'{}'的匹配结果（已尝试阈值0.50和0.40）",
                                            item.getProductName());
                                }
                            } catch (Exception e) {
                                logger.error("【订单识别】LCS匹配查找价格失败：{}", e.getMessage());
                            }
                        } else {
                            logger.warn("【订单识别】向量搜索服务不可用，无法查找价格");
                        }
                    }
                }
            }
            
            logger.info("【订单识别】价格检查完成，共补充{}个商品的价格", priceFixedCount);
            
            // 将仍然为null的单价设置为默认值0.0
            int nullPriceCount = 0;
            for (OrderItemDto item : request.getItems()) {
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(0.0);
                    nullPriceCount++;
                    logger.warn("【订单识别】商品'{}'价格匹配失败，设置默认价格为0.0", item.getProductName());
                }
            }
            if (nullPriceCount > 0) {
                logger.warn("【订单识别】共{}个商品价格设置为默认值0.0", nullPriceCount);
            }
        }

        // 7. 记录最终识别结果（存入数据库前的真实数据）
        logger.info("【订单识别】识别完成，最终数据 - 客户ID：{}，客户名称：{}，订单类型：{}，商品数量：{}",
                request.getCustomerId(),
                request.getCustomerName(),
                request.getOrderType(),
                request.getItems() != null ? request.getItems().size() : 0);
        
        if (request.getItems() != null) {
            for (int i = 0; i < request.getItems().size(); i++) {
                OrderItemDto item = request.getItems().get(i);
                logger.info("【订单识别】最终商品{} - 商品ID：{}，名称：{}，编号：{}，规格：{}，数量：{}，单位：{}，单价：{}",
                        i + 1, 
                        item.getProductId(),
                        item.getProductName(), 
                        item.getProductNo(),
                        item.getSpecification(),
                        item.getQuantity(), 
                        item.getUnit(),
                        item.getUnitPrice());
            }
        }

        //记录操作日志
        Log log = new Log();
        log.setType("订单识别");
        log.setContent(request.toString());
        log.setCreateUser(currentUserService.getCurrentNickname());
        log.setCreateTime(new Date());
        logMapper.insert(log);
        logger.info("【订单识别】识别完成，操作日志已记录（订单数据待前端确认后通过创建接口写入数据库）");
        return request;
    }

    /**
     * 提取商品核心中文名称
     * 从商品名称中去除规格、数量、包装等信息，只保留核心中文品名
     *
     * 处理模式示例：
     *   "5kg常金香软米"              → "常金香软米"
     *   "1.9L*6海天蒸鱼豉油"         → "海天蒸鱼豉油"
     *   "多力葵花油(5L/桶)"          → "多力葵花油"
     *   "福临门非转基因一级大豆油（5L/桶）" → "福临门非转基因一级大豆油"
     *   "常金薏米330g/袋"           → "常金薏米"
     *   "(B)黑米散装/1*10KG"         → "黑米散装"
     *   "250g祈福大米"              → "祈福大米"
     *   "2.5kg常金软米真空装（红袋）"    → "常金软米真空装"
     *   "5L*4鲁花压榨一级花生油"       → "鲁花压榨一级花生油"
     *
     * @param productName 原始商品名称
     * @return 核心商品中文名称
     */
    private String extractCoreProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return productName;
        }
        
        String coreName = productName.trim();
        
        // 1. 去除前缀括号标记，如 "(B)"、"（A）"
        coreName = coreName.replaceAll("^[（\\(][^）\\)]*[）\\)]\\s*", "");
        
        // 2. 去除中间/后缀的括号规格信息，如 "(5L/桶)"、"（5L*4桶/箱）"、"（红袋）"
        coreName = coreName.replaceAll("[（\\(][^）\\)]*[）\\)]", "");
        
        // 3. 去除 "/" 及之后的包装/规格信息，如 "/袋"、"/桶"、"/1*10KG"
        int slashIndex = coreName.indexOf('/');
        if (slashIndex > 0) {
            coreName = coreName.substring(0, slashIndex).trim();
        }
        
        // 4. 去除 数字*数字*单位 模式，如 "1.9L*6"、"5L*4"、"2.2kg*6"、"500ml*12"
        coreName = coreName.replaceAll("\\d+\\.?\\d*\\s*[a-zA-Z]+\\s*\\*\\s*\\d+", "");
        
        // 5. 去除前缀的 数字+单位 模式，如 "5kg"、"1.9L"、"250g"、"500ml"、"2200g"
        //    仅去除开头的规格，避免误删中间的规格
        coreName = coreName.replaceAll("^\\d+\\.?\\d*\\s*[a-zA-Z]+\\s*", "");
        
        // 6. 去除后缀的 数字+单位 模式，如 "330g"、"500g"、"1kg"、"25Kg"
        //    仅去除尾部的规格
        coreName = coreName.replaceAll("\\s*\\d+\\.?\\d*\\s*[a-zA-Z]+\\s*$", "");
        
        // 7. 提取中文字符序列及紧随其后的数字，去除残留的英文、特殊符号
        //    保留中文+数字组合（如“1.9升”中的中文“升”前的数字也应保留在核心名中，
        //    但前5步已去除前置规格，此步主要处理残留问题）
        StringBuilder resultBuilder = new StringBuilder();
        for (int idx = 0; idx < coreName.length(); idx++) {
            char c = coreName.charAt(idx);
            if (isChineseChar(c)) {
                resultBuilder.append(c);
            } else if (Character.isDigit(c) || c == '.') {
                // 保留中文字符后面的数字和点号（如“升1.9”中的1.9）
                // 但如果前面没有中文字符，说明是残留规格数字，跳过
                if (resultBuilder.length() > 0) {
                    resultBuilder.append(c);
                }
            }
        }
        String result = resultBuilder.toString().trim();
        
        // 8. 如果提取结果为空，返回原始名称（降级处理）
        if (result.isEmpty()) {
            return productName.trim();
        }
        
        return result;
    }

    /**
     * 提取客户核心名称
     * 截取特殊字符之前的部分，如 "江苏省省级机关(省重点)" → "江苏省省级机关"
     *
     * @param customerName 原始客户名称
     * @return 核心客户名称
     */
    private String extractCoreCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return customerName;
        }
        
        String coreName = customerName.trim();
        String[] specialChars = {"(", "（", "[", "【", "<", "《", "-", "—", "_", "|"};
        
        for (String specialChar : specialChars) {
            if (coreName.contains(specialChar)) {
                coreName = coreName.substring(0, coreName.indexOf(specialChar)).trim();
                break;
            }
        }
        
        return coreName;
    }

    /**
     * 判断字符是否为中文字符（含中文标点）
     */
    private boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B;
    }

    /**
     * 计算两个字符串的最长公共子序列(LCS)的绝对长度
     * 用于短输入守卫：当LCS绝对长度太短时（如只有1个字“米”），说明匹配不可靠
     */
    private int computeLCSLength(String text1, String text2) {
        if (text1 == null || text1.isEmpty() || text2 == null || text2.isEmpty()) {
            return 0;
        }
        int m = text1.length();
        int n = text2.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = prev[j - 1] + 1;
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
            java.util.Arrays.fill(curr, 0);
        }
        return prev[n];
    }

    @Override
    public void syncToYongyou(YongyouSyncRequest request) {
        // empName 可以使用联系人或默认值
        request.setEmpName("李颖华");
        // storName 使用客户配送地址
        request.setStorName("成品库");

        try {
            HttpResponseDTO response = HttpUtil.doPost(yongyouUrl, "POST", request, null, null);

            if (response.getHttpCode() == 200) {
                // 解析返回的JSON数据
                String responseData = response.getResponseData();
                JSONObject jsonResponse = JSONObject.parseObject(responseData);

                // 检查返回码和消息
                Integer code = jsonResponse.getInteger("code");
                String msg = jsonResponse.getString("msg");

                if (!Objects.equals(msg,"成功")) {
                    // 返回体格式不正确或表示失败
                    throw new MyException("同步到用友系统失败，返回信息: code=" + code + ", msg=" + msg);
                }
                // 订单同步日志
                Log log = new Log();
                log.setType("订单同步");
                log.setContent(request.toString());
                log.setCreateUser(currentUserService.getCurrentNickname());
                log.setCreateTime(new Date());
                logMapper.insert(log);
            } else {
                throw new MyException("同步到用友系统失败，HTTP状态码: " + response.getHttpCode() +
                        ", 响应内容: " + response.getResponseData());
            }
        } catch (Exception e) {
            throw new MyException("订单  同步到用友系统异常: " + e.getMessage(), e);
        }
    }

    /**
     * 将订单详情转换为用友接口格式
     * @param orderDetail 订单详情
     * @return 用友接口请求对象
     */
    private YongyouSyncRequest convertToYongyouFormat(OrderDetailResponse orderDetail) {
        YongyouSyncRequest request = new YongyouSyncRequest();

        // supperName 是客户名称
        request.setSupperName(orderDetail.getCustomerName());

        // empName 可以使用联系人或默认值
        request.setEmpName("李颖华");

        // remark 使用订单备注
        request.setRemark(StringUtils.defaultString(orderDetail.getRemark(), ""));

        // storName 使用客户配送地址
        request.setStorName("成品库");

        // 转换明细项
        List<YongyouSyncRequest.YongyouDetail> details = new ArrayList<>();
        if (orderDetail.getItems() != null && !orderDetail.getItems().isEmpty()) {
            for (OrderItems item : orderDetail.getItems()) {
                YongyouSyncRequest.YongyouDetail detail = new YongyouSyncRequest.YongyouDetail();

                // 仓库名称（同主表）
                detail.setStorName(request.getStorName());

                // 商品名称
                detail.setProd_Name(item.getProductName());

                // 商品编号（这里用商品ID，如果需要其他编号可以调整）
                detail.setProd_Number(item.getProductId() != null ? item.getProductId().intValue() : 0);

                // 商品单位
                detail.setProd_DW(item.getUnit());

                // 商品价格（单价）
                detail.setProd_Price(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0);

                details.add(detail);
            }
        }

        request.setDetails(details);
        return request;
    }

    private List<OrderPageResponse.ProductInfo> getProductInfos(List<OrderItems> orderItems) {
        List<OrderPageResponse.ProductInfo> products = new ArrayList<>(orderItems.size());
        for (OrderItems item : orderItems)
        {
            OrderPageResponse.ProductInfo productInfo = new OrderPageResponse.ProductInfo();
            productInfo.setProductName(item.getProductName());
            productInfo.setSpecification(item.getSpecification());
            productInfo.setQuantity(item.getQuantity());
            productInfo.setUnit(item.getUnit());
            products.add(productInfo);
        }
        return products;
    }


}
