package com.coreledger.dto.ledger;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单明细 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class LedgerItemDTO {

    /** 明细ID（null表示新增，不为null表示修改） */
    private Long id;

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100")
    private String productName;

    /** SKU ID */
    private Long skuId;

    /** SKU名称 */
    @Size(max = 150, message = "SKU名称长度不能超过150")
    private String skuName;

    /** 单价 */
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    private BigDecimal price;

    /** 数量 */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    /** 小计金额（price × quantity）*/
    @NotNull(message = "小计金额不能为空")
    @DecimalMin(value = "0.01", message = "小计金额必须大于0")
    private BigDecimal amount;
}
