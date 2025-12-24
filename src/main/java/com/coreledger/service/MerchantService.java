package com.coreledger.service;

import com.coreledger.common.mapper.merchant.MerchantConverter;
import com.coreledger.dto.auth.MerchantRegisterDTO;
import com.coreledger.dto.merchant.UpdateMerchantDTO;
import com.coreledger.entity.Merchant;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.MerchantRepository;
import com.coreledger.repository.SysAddressRepository;
import com.coreledger.vo.merchant.MerchantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 商户服务
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantConverter merchantConverter;
    private final SysAddressRepository addressRepository;

    /**
     * 创建商户
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantVO createMerchant(MerchantRegisterDTO dto, Long ownerUserId) {
        // 1. 生成商户编号
        String merchantNo = generateMerchantNo();
        
        // 2. 生成邀请码
        String inviteCode = generateInviteCode();
        
        // 3. 创建商户
        Merchant merchant = new Merchant();
        merchant.setCode(merchantNo);
        merchant.setName(dto.getMerchantName());
        merchant.setOwnerUserId(ownerUserId);
        merchant.setInviteCode(inviteCode);
        merchant.setPhone(dto.getPhone());
        merchant.setAddressId(dto.getAddressId());
        merchant.setAddressDetail(dto.getAddressDetail());
        merchant.setStatus(Status.ACTIVE);  // 默认启用
        
        merchant = merchantRepository.save(merchant);
        log.info("创建商户成功: merchantId={}, merchantNo={}", merchant.getId(), merchantNo);
        
        return toVOWithAddressPath(merchant);
    }

    /**
     * 根据邀请码查询商户
     */
    public Optional<Merchant> findByInviteCode(String inviteCode) {
        return merchantRepository.findByInviteCode(inviteCode);
    }

    /**
     * 根据ID查询商户
     */
    public Optional<Merchant> findById(Long merchantId) {
        return merchantRepository.findById(merchantId);
    }

    /**
     * 查询用户的所有商户
     */
    public List<Merchant> findByOwnerUserId(Long ownerUserId) {
        return merchantRepository.findByOwnerUserIdAndStatus(ownerUserId, Status.ACTIVE);
    }

    /**
     * 生成商户编号
     * 格式: M_yyyyMMddHHmmss_随机3位
     */
    private String generateMerchantNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%03d", new Random().nextInt(1000));
        return "M_" + timestamp + "_" + random;
    }

    /**
     * 生成邀请码
     * 格式: 6位随机字符（大小写字母+数字）
     */
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 更新商户信息
     *
     * @param merchantId 商户ID
     * @param dto 更新信息
     * @return 更新后的商户VO
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantVO updateMerchant(Long merchantId, UpdateMerchantDTO dto) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            merchant.setName(dto.getName());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            merchant.setPhone(dto.getPhone());
        }
        if (dto.getAddressId() != null) {
            merchant.setAddressId(dto.getAddressId());
        }
        if (dto.getAddressDetail() != null) {
            merchant.setAddressDetail(dto.getAddressDetail());
        }

        merchant = merchantRepository.save(merchant);
        log.info("更新商户信息成功: merchantId={}", merchantId);
        return toVOWithAddressPath(merchant);
    }

    /**
     * 根据ID获取商户VO
     *
     * @param merchantId 商户ID
     * @return 商户VO
     */
    public MerchantVO getMerchantVO(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));
        return toVOWithAddressPath(merchant);
    }

    /**
     * 转换为VO并设置地址路径
     *
     * @param merchant 商户实体
     * @return 商户VO
     */
    private MerchantVO toVOWithAddressPath(Merchant merchant) {
        MerchantVO vo = merchantConverter.toVO(merchant);

        // 查询地址信息并设置完整路径
        addressRepository.findById(merchant.getAddressId()).ifPresent(address -> {
            vo.setAddressPath(address.getMergerName());
        });

        return vo;
    }
}
