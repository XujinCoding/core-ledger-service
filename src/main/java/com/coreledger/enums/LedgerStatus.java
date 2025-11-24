package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 账本状态枚举
 *
 * <p>状态说明:</p>
 * <ul>
 *   <li><b>IN_PROGRESS (进行中)</b>: 账单创建，未收款，在主页面可查询</li>
 *   <li><b>PARTIAL (部分缴费)</b>: 已收部分款项，继续收款，在主页面可查询</li>
 *   <li><b>CLEARED (已结清)</b>: 完全缴费或抹零结清，不在主页面显示</li>
 *   <li><b>ON_CREDIT (赊账中)</b>: 客户赊账，暂不催收，不在主页面显示</li>
 *   <li><b>CLOSED (已关闭)</b>: 作废的账单</li>
 * </ul>
 *
 * <p>主页面查询规则: 只显示 IN_PROGRESS 和 PARTIAL 状态的账单</p>
 * <p>客户详情页: 显示该客户所有状态的账单（包括赊账和已结清）</p>
 * <p>状态转换规则请参考: docs/LEDGER_STATUS_TRANSITIONS.md</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum LedgerStatus implements BaseEnum {

    /** 进行中 */
    IN_PROGRESS(1, "进行中"),

    /** 部分缴费 */
    PARTIAL(2, "部分缴费"),

    /** 已结清 */
    CLEARED(3, "已结清"),

    /** 赊账中 */
    ON_CREDIT(4, "赊账中"),

    /** 已关闭 */
    CLOSED(5, "已关闭");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    LedgerStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 IN_PROGRESS
     */
    public static LedgerStatus fromValue(int value) {
        for (LedgerStatus status : LedgerStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return IN_PROGRESS;
    }

    /**
     * 判断是否为活跃状态（在主页面可查询）
     * 主页面只显示"正在处理中"的账单
     *
     * @return true 如果状态为 IN_PROGRESS 或 PARTIAL
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == PARTIAL;
    }

    /**
     * 判断是否可以接收支付
     * 赊账中的账单也可以接收支付（赊账后来缴费）
     *
     * @return true 如果状态为 IN_PROGRESS、PARTIAL 或 ON_CREDIT
     */
    public boolean canReceivePayment() {
        return this == IN_PROGRESS || this == PARTIAL || this == ON_CREDIT;
    }

    /**
     * 判断是否可以转为赊账
     *
     * @return true 如果状态为 IN_PROGRESS 或 PARTIAL
     */
    public boolean canMoveToCredit() {
        return this == IN_PROGRESS || this == PARTIAL;
    }

    /**
     * 判断是否可以抹零结清（管理员权限）
     *
     * @return true 如果状态为 IN_PROGRESS、PARTIAL 或 ON_CREDIT
     */
    public boolean canDiscountSettle() {
        return this == IN_PROGRESS || this == PARTIAL || this == ON_CREDIT;
    }

    /**
     * 判断是否为终态（不可再变更）
     *
     * @return true 如果状态为 CLEARED 或 CLOSED
     */
    public boolean isFinalState() {
        return this == CLEARED || this == CLOSED;
    }
}
