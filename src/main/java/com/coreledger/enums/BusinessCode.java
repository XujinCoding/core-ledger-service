package com.coreledger.enums;

import lombok.Getter;

/**
 * 统一错误码枚举
 *
 * <p>错误码范围划分:</p>
 * <ul>
 *   <li><b>200</b>: 成功</li>
 *   <li><b>400-499</b>: 客户端错误</li>
 *   <li><b>500-599</b>: 服务器错误</li>
 *   <li><b>1000-1999</b>: 客户模块</li>
 *   <li><b>2000-2999</b>: 账本模块</li>
 *   <li><b>3000-3999</b>: 商品模块</li>
 *   <li><b>4000-4999</b>: 支付模块</li>
 *   <li><b>5000-5999</b>: 权限模块</li>
 *   <li><b>9000-9999</b>: 系统错误</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum BusinessCode {

    // ==================== 通用错误码 (200-599) ====================
    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 参数错误 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未登录 */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 无权限 */
    FORBIDDEN(403, "无权限访问"),

    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),

    /** 服务器错误 */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // ==================== 客户模块 (1000-1999) ====================
    /** 客户不存在 */
    CUSTOMER_NOT_FOUND(1001, "客户不存在"),

    /** 手机号已存在 */
    CUSTOMER_PHONE_EXISTS(1002, "手机号已被注册"),

    /** 客户已被删除 */
    CUSTOMER_DELETED(1003, "客户已被删除"),

    /** 客户已被删除 */
    CUSTOMER_BIND_EXISTS(1004, "已绑定该商户"),

    // ==================== 账本模块 (2000-2999) ====================
    /** 账本不存在 */
    LEDGER_NOT_FOUND(2001, "账本不存在"),

    /** 账本状态不允许此操作 */
    LEDGER_STATUS_NOT_ALLOWED(2002, "账本状态不允许此操作"),

    /** 支付金额超过应收金额 */
    LEDGER_PAYMENT_EXCEED(2003, "支付金额超过应收金额"),

    /** 账本已结清 */
    LEDGER_ALREADY_CLEARED(2004, "账本已结清，无法继续操作"),

    /** 账本已关闭 */
    LEDGER_ALREADY_CLOSED(2005, "账本已关闭，无法继续操作"),

    /** 账本明细为空 */
    LEDGER_ITEMS_EMPTY(2006, "账本明细不能为空"),

    /** 账本明细不存在 */
    LEDGER_ITEM_NOT_FOUND(2007, "账本明细不存在"),

    /** 账本明细不属于该账单 */
    LEDGER_ITEM_NOT_BELONG(2008, "账单明细不属于该账单"),

    /** 账本明细已删除 */
    LEDGER_ITEM_DELETED(2009, "账单明细已删除，无法修改"),

    /** 支付方式必填 */
    LEDGER_PAYMENT_METHOD_REQUIRED(2010, "有支付金额时支付方式不能为空"),

    /** 账本已有支付记录 */
    LEDGER_HAS_PAYMENT(2011, "账本已有支付记录，无法关闭"),

    // ==================== 商品模块 (3000-3999) ====================
    /** 商品不存在 */
    PRODUCT_NOT_FOUND(3001, "商品不存在"),

    /** 商品分类不存在 */
    PRODUCT_CATEGORY_NOT_FOUND(3002, "商品分类不存在"),

    /** 商品分类层级超限 */
    PRODUCT_CATEGORY_LEVEL_EXCEED(3003, "商品分类层级不能超过5级"),

    /** 商品分类下存在子分类 */
    PRODUCT_CATEGORY_HAS_CHILDREN(3004, "该分类下存在子分类，无法删除"),

    /** 商品分类下存在商品 */
    PRODUCT_CATEGORY_HAS_PRODUCTS(3005, "该分类下存在商品，无法删除"),

    /** 商品属性不存在 */
    PRODUCT_ATTR_NOT_FOUND(3006, "商品属性不存在"),

    /** 商品属性值不存在 */
    PRODUCT_ATTR_VALUE_NOT_FOUND(3007, "商品属性值不存在"),

    /** 商品属性值被SKU使用 */
    PRODUCT_ATTR_VALUE_IN_USE(3008, "该属性值正在被SKU使用，无法删除"),

    /** 商品属性被SKU使用 */
    PRODUCT_ATTR_IN_USE(3009, "该属性正在被SKU使用，无法删除"),

    /** SKU不存在 */
    PRODUCT_SKU_NOT_FOUND(3010, "SKU不存在"),

    /** SKU未定价 */
    PRODUCT_SKU_NOT_PRICED(3011, "SKU未定价，无法使用"),

    /** SKU价格无效 */
    PRODUCT_SKU_PRICE_INVALID(3012, "SKU价格必须大于0"),

    /** 商品无属性 */
    PRODUCT_NO_ATTRS(3013, "商品没有属性，无法生成SKU"),

    /** 商品SKU数量过多 */
    PRODUCT_SKU_TOO_MANY(3014, "商品SKU数量过多，请减少属性或属性值"),

    // ==================== 支付模块 (4000-4999) ====================
    /** 支付记录不存在 */
    PAYMENT_RECORD_NOT_FOUND(4001, "支付记录不存在"),

    /** 支付金额无效 */
    PAYMENT_AMOUNT_INVALID(4002, "支付金额必须大于0"),

    /** 支付方式无效 */
    PAYMENT_METHOD_INVALID(4003, "支付方式无效"),

    // ==================== 权限模块 (5000-5999) ====================
    /** 仅管理员可操作 */
    ADMIN_ONLY(5001, "该操作仅限管理员执行"),

    /** 用户不存在 */
    USER_NOT_FOUND(5002, "用户不存在"),

    /** 用户名或密码错误 */
    USER_CREDENTIALS_INVALID(5003, "用户名或密码错误"),

    /** 用户已被禁用 */
    USER_DISABLED(5004, "用户已被禁用"),

    /** 微信登录失败 */
    WECHAT_LOGIN_FAILED(5005, "微信登录失败"),

    /** 微信API调用失败 */
    WECHAT_API_ERROR(5006, "微信API调用失败"),

    /** 手机号已被绑定 */
    PHONE_ALREADY_BOUND(5007, "该手机号已被其他账号绑定"),

    /** 微信数据解密失败 */
    WECHAT_DECRYPT_FAILED(5016, "微信数据解密失败"),

    /** 用户名已被使用 */
    USERNAME_ALREADY_USED(5009, "该用户名已被使用"),

    /** 微信账号已注册 */
    WECHAT_ALREADY_REGISTERED(5010, "该微信账号已注册"),

    /** 商户不存在 */
    MERCHANT_NOT_FOUND(5011, "商户不存在"),

    /** 客户已绑定 */
    CUSTOMER_ALREADY_BOUND(5012, "客户已绑定该商户"),

    /** 权限不足 */
    UNAUTHORIZED_OPERATION(5013, "权限不足"),

    /** 未选择商户 */
    MERCHANT_NOT_SELECTED(5017, "请先选择商户"),

    /** 客户已注册 */
    CUSTOMER_ALREADY_REGISTERED(5015, "该用户已注册为客户"),

    /** 功能未实现 */
    NOT_IMPLEMENTED(5008, "功能未实现"),

    /** 无效参数 */
    INVALID_PARAMETER(5014, "无效参数"),

    // ==================== 地址模块 (6000-6999) ====================
    /** 地址不存在 */
    ADDRESS_NOT_FOUND(6001, "地址不存在"),

    /** 地址层级错误 */
    ADDRESS_LEVEL_INVALID(6002, "地址层级错误"),

    /** 必须选择村级地址 */
    ADDRESS_MUST_BE_VILLAGE(6003, "客户地址必须选择到村级"),

    /** 地址层级超限 */
    ADDRESS_LEVEL_EXCEEDED(6004, "地址层级不能超过5级（村级）"),

    // ==================== 系统错误 (9000-9999) ====================
    /** 乐观锁冲突 */
    OPTIMISTIC_LOCK_CONFLICT(9001, "数据已被其他用户修改，请刷新后重试"),

    /** 数据库错误 */
    DATABASE_ERROR(9002, "数据库操作失败"),

    /** 数据校验失败 */
    VALIDATION_ERROR(9003, "数据校验失败"),

    /** 业务逻辑错误 */
    BUSINESS_ERROR(9004, "业务逻辑错误"),

    /** 未知错误 */
    UNKNOWN_ERROR(9999, "未知错误");

    /** 错误码 */
    private final int code;

    /** 错误描述 */
    private final String message;

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误描述
     */
    BusinessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取枚举对象
     *
     * @param code 错误码
     * @return 枚举对象，未找到时返回 UNKNOWN_ERROR
     */
    public static BusinessCode fromCode(int code) {
        for (BusinessCode businessCode : BusinessCode.values()) {
            if (businessCode.code == code) {
                return businessCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
