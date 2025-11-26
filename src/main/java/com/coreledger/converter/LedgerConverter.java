package com.coreledger.converter;

import com.coreledger.dto.ledger.CreateLedgerDTO;
import com.coreledger.entity.Ledger;
import com.coreledger.entity.LedgerItem;
import com.coreledger.entity.PaymentRecord;
import com.coreledger.vo.ledger.LedgerDetailVO;
import com.coreledger.vo.ledger.LedgerListVO;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LedgerConverter {

    /**
     * CreateDTO → Entity (Ledger)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)  // 明细单独处理
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "memo", source = "memo")
    @Mapping(target = "totalAmount", constant = "0")
    @Mapping(target = "paidAmount", constant = "0")
    @Mapping(target = "discountAmount", constant = "0")
    @Mapping(target = "ledgerStatus", expression = "java(com.coreledger.enums.LedgerStatus.IN_PROGRESS)")
    Ledger toEntity(CreateLedgerDTO dto);

    /**
     * CreateDTO.LedgerItemDTO → LedgerItem
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ledgerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "memo", ignore = true)
    @Mapping(source = "skuId", target = "skuId")
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "skuName", target = "skuName")
    @Mapping(source = "attrValueMap", target = "attrValueMap")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    LedgerItem toItemEntity(CreateLedgerDTO.LedgerItemDTO dto);

    /**
     * List<CreateDTO.LedgerItemDTO> → List<LedgerItem>
     */
    List<LedgerItem> toItemEntityList(List<CreateLedgerDTO.LedgerItemDTO> dtos);

    /**
     * UpdateLedgerItemsDTO.AddItemDTO → LedgerItem
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ledgerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "memo", ignore = true)
    LedgerItem toItemEntityFromAddDTO(com.coreledger.dto.ledger.UpdateLedgerItemsDTO.AddItemDTO dto);

    /**
     * List<AddItemDTO> → List<LedgerItem>
     */
    List<LedgerItem> toItemEntityListFromAddDTO(List<com.coreledger.dto.ledger.UpdateLedgerItemsDTO.AddItemDTO> dtos);

    /**
     * Entity → ListVO
     */
    @Mapping(target = "ledgerStatus", expression = "java(entity.getLedgerStatus().getValue())")
    @Mapping(target = "ledgerStatusDesc", expression = "java(entity.getLedgerStatus().getDescription())")
    @Mapping(target = "remainingAmount", expression = "java(entity.getRemainingAmount())")
    @Mapping(target = "customerName", ignore = true)   // 需要通过客户服务查询
    @Mapping(target = "customerPhone", ignore = true)  // 需要通过客户服务查询
    LedgerListVO toListVO(Ledger entity);

    /**
     * Entity → DetailVO
     */
    @Mapping(target = "ledgerStatus", expression = "java(entity.getLedgerStatus().getValue())")
    @Mapping(target = "ledgerStatusDesc", expression = "java(entity.getLedgerStatus().getDescription())")
    @Mapping(target = "remainingAmount", expression = "java(entity.getRemainingAmount())")
    @Mapping(target = "customerName", ignore = true)         // 需要通过客户服务查询
    @Mapping(target = "customerPhone", ignore = true)        // 需要通过客户服务查询
    @Mapping(target = "customerAddress", ignore = true)      // 需要通过客户服务查询
    @Mapping(target = "customerAddressDetail", ignore = true)// 需要通过客户服务查询
    @Mapping(target = "items", ignore = true)                // 明细单独转换
    @Mapping(target = "paymentRecords", ignore = true)       // 支付记录单独查询
    LedgerDetailVO toDetailVO(Ledger entity);

    /**
     * LedgerItem → LedgerItemVO
     */
    @Mapping(source = "skuId", target = "skuId")
    @Mapping(source = "skuName", target = "skuName")
    @Mapping(source = "attrValueMap", target = "attrValueMap")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(target = "subtotal", expression = "java(entity.getSubtotal())")
    LedgerDetailVO.LedgerItemVO toItemVO(LedgerItem entity);

    /**
     * List<LedgerItem> → List<LedgerItemVO>
     */
    List<LedgerDetailVO.LedgerItemVO> toItemVOList(List<LedgerItem> entities);

    /**
     * PaymentRecord → PaymentRecordVO
     */
    @Mapping(source = "amount", target = "amount")
    @Mapping(target = "paymentMethod", expression = "java(entity.getPaymentMethod().getValue())")
    @Mapping(target = "paymentMethodDesc", expression = "java(entity.getPaymentMethod().getDescription())")
    @Mapping(source = "memo", target = "memo")
    @Mapping(source = "createInstant", target = "createInstant")
    LedgerDetailVO.PaymentRecordVO toPaymentRecordVO(PaymentRecord entity);

    /**
     * List<PaymentRecord> → List<PaymentRecordVO>
     */
    List<LedgerDetailVO.PaymentRecordVO> toPaymentRecordVOList(List<PaymentRecord> entities);
}
