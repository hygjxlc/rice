package com.bjdx.rice.business.exception;

public enum ResponseCode {


    /**
     * http status
     */

    SUCCECC(1000,"成功"),
    FAIL(2000,"失败"),

    HTTP_FAIL(2001,"http请求失败"),
    NOT_GATEWAY_REQUEST(2002,"请从网关访问服务"),
    PARAM_ERROR(2003,"参数错误"),

    //用户
    PASSWORD_RULEE_RROR(3000,"密码规则错误"),
    USERNAME_RULEE_RROR(3001,"账号规则错误"),
    USER_LOCKING(3003,"用户锁定，请五分钟后再试"),
    NO_USER(3004,"用户不存在"),
    USER_STATUS_DISABLE(3005,"该账号已被禁用"),
    WRONG_PASSWORD_OR_USERNAME(3006,"用户名或密码错误"),
    GET_LOGINUSER_ERROR(3007,"获取用户信息异常"),
    USER_HAS_EXIST(3008,"用户名已存在"),
    ONLY_UPDATE_OWNER(3009,"此操作只能修改本人密码"),
    NO_ROLE_AUTH(3010,"无权使用角色"),
    NO_ROLE(3011,"无此角色"),
    NO_MENU_AUTH(3012,"无权使用菜单"),
    NOT_DELETE_OWN_USER(3013,"禁止删除本人账号"),
    ROLE_NAME_HAS_EXIST(3014,"角色名称已存在"),
    ONLY_UPDATE_OWN_ROLE(3015,"只能修改自己创建的角色"),
    NO_MENU(3016,"菜单不存在"),
    NO_PARENT_MENU(3017,"父菜单不存在"),

    NO_MENU_URL_AUTH(3018,"无访问权限"),
    MENU_HAS_EXIST(3019,"菜单名已存在"),
    EXIST_CHILDREN_MENU(3020,"存在子菜单，不可删除"),
    ROLE_HAS_BINGING_USER(3021,"角色已有绑定账号"),
    NO_DELETE_USER_AUTH(3022,"无权删除用户"),
    NO_SELECT_USER_AUTH(3023,"无权查询用户"),
    NO_UPDATE_USER_AUTH(3024,"无权更新用户"),
    USERNAME_OR_PASSWORD_ERROR(3025,"账号或密码错误"),
    NEW_PASSWORD_EQUALS_OLD_PASSWORD(3025,"新密码不能和原密码相同"),
    REPEAT_PASSWORD_ERROR(3025,"二次确认密码输入错误"),
    OLD_PASSWORD_ERROR(3025,"旧密码输入错误"),
    NO_ORG(3026,"部门不存在"),
    ORG_NAME_EXIST(3027,"组织关系名以存在"),
    DELETE_ORG_PARAM_ERROR(3028,"删除组织关系参数错误"),
    DELETE_ORG_FAIL(3028,"删除组织关系失败"),
    ORG_DOES_NOT_EXIST(3029,"人员信息中包含的组织关系不存在"),
    USER_ADMIN_FORBID_ACCOUNT(3030,"禁止操作管理员账号"),
    USER_ADMIN_FORBID_ROLE(3031,"禁止操作管理员角色"),
    ORG_INEXISTENCE(3032,"还未添加组织关系"),
    PARAMS_ERROR(3033,"修改用户参数错误"),
    ORG_INCLUDE_NOT_EXIST(3034,"所选组织包含不存在的组织"),
    FORBIG_GIVE_ROLE_ADMIN(3035,"禁止赋予管理员角色"),
    MOBILE_HAS_EXIST(3036,"手机号已存在"),



    //登陆权限
    NO_TOKEN(4001,"token不存在"),
    INVALID_TOKEN(4002,"您尚未登录或登录信息已过期，请重新登录！"),
    NO_INTERFACE_AUTH(4003,"权限不足"),

    //minio
    MINIO_CONNECT_FAIL(5001,"minio连接失败"),
    MINIO_CREATE_BUCKET_FAIL(5002,"minio创建桶失败"),
    MINIO_PUT_FAIL(5003,"minio上传文件失败"),
    MINIO_GET_FAIL(5004,"minio下载文件失败"),
    MINIO_FILE_NOT_EXIST(5005,"文件不存在"),
    MINIO_DELETE_FAIL(5006,"minio删除文件失败"),
    MINIO_TYPE_ERROR(5007,"type值错误"),

    //视图
    VIEW_HAS_EXIST(6001,"视图已存在"),
    NO_VIEW(6002,"视图不存在"),
    VIEW_COUNT_TOO_LARGE(6003,"视图数量不可超过20"),
    VIEW_NAME_HAS_EXIST(6004,"视图名称重复"),
    USER_CONFIG_NOT_DEFINITE(6005,"未指定视图用户配置，视图id不可为空"),
    NOT_EXIST_TABLE_TYPE_DATA(6006,"未找到该表类型的字段配置"),
    NO_TABLE_USER_CONFIG(6006,"未找到该表类型的字段配置"),
    NO_VIEW_AUTH(6007,"无查看视图权限"),

    // 基础数据
    QUERY_DATA_EMPTY(7001,"数据不存在,请输入正确的参数!"),
    DATA_IS_EXIST(7002,"该编号重复,请重新输入!"),
    WORK_SEQUENCE_EXCEL_FAIL(7003,"导出工序excel失败"),
    PROCESS_ROUTE_EXCEL_FAIL(7004,"导出工序excel失败"),
    SALARY_CONFIG_EXCEL_FAIL(7005,"导出绩效工资配置excel失败"),
    QUERY_ORG_FAIL(7006,"查询部门表数据失败"),
    DELETE_WORK_SEQUENCE_FAIL(7007,"该工序在被工艺路线使用,请勿删除!"),
    DELETE_PROCESS_ROUTE_FAIL(7008,"该工艺路线在被产品使用,请勿删除!"),

    NO_CUSTOMER(7100,"客户不存在"),

    // 库存管理
    QUERY_STOCK_EMPTY(8001,"数据不存在,请输入正确的参数!"),
    STOCK_IS_EXIST(8002,"该编号重复,请重新输入!"),
    STOCK_DETAIL_EXCEL_FAIL(8003,"导出库存收发明细excel失败"),
    STOCK_SUNMMARY_EXCEL_FAIL(8004,"导出进销汇总excel失败"),
    STOCK_BALANCE_EXCEL_FAIL(8005,"导出库存余额excel失败"),
    STOCK_INF0_DETAIL_EXCEL_FAIL(8006,"导出库存余额明细excel失败"),

    // 自定义管理
    CUSTOM_CONFIGURATION_FAIL(9001,"自定义配置字段类型参数错误!"),

    //生产
    DELETE_FAIL(10001,"删除失败!"),

    //公式 11000
    EXPRESSION_ERROR(11000,"公式错误!"),
    EXPRESSION_FIELD_NOT_EXIST(11001,"公式引用字段不存在!"),


    //自定义事件
    EVENT_NOT_EXIST(12000,"事件不存在"),
    EVENT_NO_TRIGGER_FORM(12001,"触发表单不存在"),
    EVENT_REPEAT_NAME(12010,"事件名称重复"),

    //空间
    SPACE_NAME_HAS_EXIST(13000,"空间名称重复"),
    SPACE_CONTAINS_ANALYSIS_PROJECT(13001,"空间包含分析项目无法删除"),
    SPACE_CONTAINS_ANALYSIS_REPORT(13002,"空间包含分析报告无法删除"),
    SPACE_CONTAINS_DATA_SOURCE(13003,"空间包含数据源无法删除"),

    //分析项目
    ANALYSIS_PROJECT_NAME_HAS_EXIST(14000,"分析项目名称重复")
    ;
    public int code;
    public String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
