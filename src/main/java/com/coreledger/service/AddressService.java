package com.coreledger.service;

import com.coreledger.common.mapper.address.AddressConverter;
import com.coreledger.dto.address.AddressCreateDTO;
import com.coreledger.dto.address.AddressQueryDTO;
import com.coreledger.entity.SysAddress;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.SysAddressRepository;
import com.coreledger.vo.address.AddressChainVO;
import com.coreledger.vo.address.AddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址业务服务类
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final SysAddressRepository addressRepository;
    private final AddressConverter addressConverter;

    /**
     * 获取地址详情
     *
     * @param id 地址ID
     * @return 地址VO
     * @throws NotFoundException 当地址不存在时抛出 (BusinessCode.ADDRESS_NOT_FOUND)
     */
    public AddressVO getAddress(Long id) {
        SysAddress address = addressRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException(BusinessCode.ADDRESS_NOT_FOUND));
        
        return addressConverter.toVO(address);
    }

    /**
     * 创建地址
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>parentId为0或null：创建省级地址（level=1）</li>
     *   <li>指定parentId：创建子级地址，level自动为父级+1</li>
     *   <li>自动构建mergerName（完整路径）</li>
     *   <li>层级不能超过5（村级）</li>
     * </ul>
     *
     * @param createDTO 创建请求DTO
     * @return 创建的地址VO
     * @throws NotFoundException 当父级地址不存在时抛出 (BusinessCode.ADDRESS_NOT_FOUND)
     * @throws BusinessException 当层级超过限制时抛出 (BusinessCode.ADDRESS_LEVEL_EXCEEDED)
     */
    @Transactional(rollbackFor = Exception.class)
    public AddressVO createAddress(AddressCreateDTO createDTO) {
        SysAddress newAddress = new SysAddress();
        newAddress.setName(createDTO.getName());

        // 判断是否创建顶级地址（省）
        if (createDTO.getParentId() == null || createDTO.getParentId() == 0L) {
            // 创建省级地址
            newAddress.setParentId(0L);
            newAddress.setLevel(1);
            newAddress.setMergerName(createDTO.getName());
            
            log.info("创建省级地址: {}", createDTO.getName());
        } else {
            // 创建子级地址
            // 1. 查询父级地址
            SysAddress parentAddress = addressRepository.findByIdAndStatus(createDTO.getParentId(), Status.ACTIVE)
                    .orElseThrow(() -> new NotFoundException(BusinessCode.ADDRESS_NOT_FOUND));

            // 2. 计算子级层级
            int childLevel = parentAddress.getLevel() + 1;
            
            // 3. 校验层级不能超过5（村级）
            if (childLevel > 5) {
                throw new BusinessException(BusinessCode.ADDRESS_LEVEL_EXCEEDED);
            }

            // 4. 设置子级地址属性
            newAddress.setParentId(createDTO.getParentId());
            newAddress.setLevel(childLevel);
            
            // 5. 构建完整路径（父级路径 + 当前名称）
            String mergerName = parentAddress.getMergerName() + "-" + createDTO.getName();
            newAddress.setMergerName(mergerName);

            log.info("创建子级地址: 父级ID={}, 父级={}, 子级={}, 层级={}, 完整路径={}", 
                    createDTO.getParentId(), parentAddress.getName(), createDTO.getName(), 
                    childLevel, mergerName);
        }

        // 保存地址
        SysAddress savedAddress = addressRepository.save(newAddress);

        log.info("地址创建成功: ID={}, 名称={}, 层级={}", 
                savedAddress.getId(), savedAddress.getName(), savedAddress.getLevel());

        return addressConverter.toVO(savedAddress);
    }

    /**
     * 查询地址列表
     *
     * <p>优先按父级ID查询，如果父级ID为null则按层级查询</p>
     *
     * @param queryDTO 查询条件DTO
     * @return 地址列表
     */
    public List<AddressVO> listAddresses(AddressQueryDTO queryDTO) {
        List<SysAddress> addresses;

        if (queryDTO.getParentId() != null) {
            // 按父级ID查询子级地址
            addresses = addressRepository.findByParentIdAndStatus(queryDTO.getParentId(), Status.ACTIVE);
            log.debug("按父级ID查询地址, parentId: {}, 结果数量: {}", queryDTO.getParentId(), addresses.size());
        } else if (queryDTO.getLevel() != null) {
            // 按层级查询地址
            addresses = addressRepository.findByLevelAndStatus(queryDTO.getLevel(), Status.ACTIVE);
            log.debug("按层级查询地址, level: {}, 结果数量: {}", queryDTO.getLevel(), addresses.size());
        } else {
            // 默认查询所有顶级地址（省）
            addresses = addressRepository.findByParentIdAndStatus(0L, Status.ACTIVE);
            log.debug("查询顶级地址（省），结果数量: {}", addresses.size());
        }

        return addresses.stream()
                .map(addressConverter::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据父级ID查询子级地址
     *
     * @param parentId 父级ID
     * @return 子级地址列表
     */
    public List<AddressVO> listAddressesByParent(Long parentId) {
        List<SysAddress> addresses = addressRepository.findByParentIdAndStatus(parentId, Status.ACTIVE);
        log.debug("查询子级地址, parentId: {}, 结果数量: {}", parentId, addresses.size());

        return addresses.stream()
                .map(addressConverter::toVO)
                .collect(Collectors.toList());
    }


    /**
     * 根据地址ID向上查询地址链（用于回显地址级联选择器）
     *
     * <p>从指定地址向上递归查询所有父级地址，直到省级</p>
     * <p>返回完整的地址路径：[省, 市, 区, 镇, 村]</p>
     *
     * @param addressId 地址ID
     * @return 地址链VO
     * @throws NotFoundException 当地址不存在时抛出 (BusinessCode.ADDRESS_NOT_FOUND)
     */
    public AddressChainVO getAddressChain(Long addressId) {
        // 1. 查询目标地址
        SysAddress targetAddress = addressRepository.findByIdAndStatus(addressId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException(BusinessCode.ADDRESS_NOT_FOUND));

        // 2. 向上递归查询父级地址
        List<SysAddress> addressChain = new ArrayList<>();
        addressChain.add(targetAddress);
        
        SysAddress currentAddress = targetAddress;
        while (!currentAddress.isTopLevel()) {
            Long parentId = currentAddress.getParentId();
            SysAddress parentAddress = addressRepository.findByIdAndStatus(parentId, Status.ACTIVE)
                    .orElseThrow(() -> new NotFoundException(BusinessCode.ADDRESS_NOT_FOUND));
            
            addressChain.add(parentAddress);
            currentAddress = parentAddress;
        }

        // 3. 反转列表（从省到村的顺序）
        Collections.reverse(addressChain);

        // 4. 构建地址链VO
        AddressChainVO chainVO = new AddressChainVO();
        
        List<Long> addressIds = new ArrayList<>();
        List<String> addressNames = new ArrayList<>();
        List<Integer> addressLevels = new ArrayList<>();
        StringBuilder fullPathBuilder = new StringBuilder();

        for (int i = 0; i < addressChain.size(); i++) {
            SysAddress address = addressChain.get(i);
            addressIds.add(address.getId());
            addressNames.add(address.getName());
            addressLevels.add(address.getLevel());
            
            if (i > 0) {
                fullPathBuilder.append("-");
            }
            fullPathBuilder.append(address.getName());
        }

        chainVO.setAddressIds(addressIds);
        chainVO.setAddressNames(addressNames);
        chainVO.setAddressLevels(addressLevels);
        chainVO.setFullPath(fullPathBuilder.toString());
        chainVO.setTargetAddressId(targetAddress.getId());
        chainVO.setTargetAddressName(targetAddress.getName());

        log.debug("查询地址链成功, 目标地址ID: {}, 地址链长度: {}, 完整路径: {}", 
                addressId, addressChain.size(), chainVO.getFullPath());

        return chainVO;
    }

    /**
     * 获取指定父级地址下的所有子地址ID列表（递归查询所有子孙地址）
     *
     * @param parentAddressId 父级地址ID
     * @return 所有子地址ID列表（包括所有子孙地址）
     */
    public List<Long> getAllChildAddressIds(Long parentAddressId) {
        List<Long> allChildIds = new ArrayList<>();
        
        // 递归查询所有子地址
        collectChildAddressIds(parentAddressId, allChildIds);
        
        log.debug("查询父级地址 {} 下的所有子地址ID, 共 {} 个", parentAddressId, allChildIds.size());
        
        return allChildIds;
    }

    /**
     * 递归收集所有子地址ID
     *
     * @param parentId 父级ID
     * @param resultIds 结果集合
     */
    private void collectChildAddressIds(Long parentId, List<Long> resultIds) {
        // 查询直接子级地址
        List<SysAddress> children = addressRepository.findByParentIdAndStatus(parentId, Status.ACTIVE);
        
        for (SysAddress child : children) {
            // 添加子地址ID
            resultIds.add(child.getId());
            
            // 递归查询孙地址
            collectChildAddressIds(child.getId(), resultIds);
        }
    }
}
