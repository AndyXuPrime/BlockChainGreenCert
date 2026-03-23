package org.fu.blockchain_backend.service.impl;

import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple7;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fu.blockchain_backend.config.ContractConfig;
import org.fu.blockchain_backend.contracts.GreenCert; // 你导出的智能合约包装类
import org.fu.blockchain_backend.entity.CertTransferLog;
import org.fu.blockchain_backend.entity.GreenCertEntity;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.repository.CertTransferLogRepository;
import org.fu.blockchain_backend.repository.GreenCertRepository;
import org.fu.blockchain_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;

@Service
public class GreenCertServiceImpl {

    @Autowired
    private GreenCertRepository greenCertRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CertTransferLogRepository transferLogRepository;

    @Autowired
    private Client client;
    @Autowired
    private ContractConfig contractConfig;

    /**
     * 【核心引擎】根据用户实体，动态加载具有该用户私钥签名权限的智能合约对象
     */
    private GreenCert loadContractByUser(SysUser user) {
        if (user.getPrivateKey() == null || user.getPrivateKey().isEmpty()) {
            throw new RuntimeException("该用户缺少区块链私钥，无法发起交易");
        }
        // 从 MySQL 中取出 Hex 格式的私钥，恢复成 KeyPair 对象
        CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().createKeyPair(user.getPrivateKey());
        // 加载智能合约包装类
        return GreenCert.load(contractConfig.getGreenCertAddress(), client, keyPair);
    }

    /**
     * 1. 核发绿证 (Admin 操作)
     */
    @Transactional(rollbackFor = Exception.class)
    public String issueCert(String certId, Integer ownerId, String energyType, Integer amount) {
        // 1. 获取账户信息 (假设数据库里 ID 为 1 的是 Admin)
        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        SysUser owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("接收企业不存在"));

        // 2. 加载合约 (此时合约拥有 Admin 的私钥签名权限)
        GreenCert contract = loadContractByUser(admin);

        // 3. 【核心重构】像调普通 Java 方法一样，调用智能合约发交易！(告别繁琐的 JSON 拼接)
        TransactionReceipt receipt = contract.issueCert(
                certId,
                owner.getChainAddress(),
                energyType,
                BigInteger.valueOf(amount)
        );

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("上链失败，状态码: " + receipt.getStatus() + "，信息: " + receipt.getMessage());
        }

        // 4. 落库 MySQL
        GreenCertEntity certEntity = new GreenCertEntity();
        certEntity.setCertId(certId);
        certEntity.setIssuerId(admin.getId());
        certEntity.setCurrentOwnerId(owner.getId());
        certEntity.setEnergyType(energyType);
        certEntity.setAmount(amount);
        certEntity.setIssueTime(new Date());
        certEntity.setStatus(1); // 1-有效
        certEntity.setTxHash(receipt.getTransactionHash());

        greenCertRepository.save(certEntity); // 使用 JPA 保存

        return receipt.getTransactionHash();
    }

    /**
     * 2. 跨域流转 (企业操作)
     */
    @Transactional(rollbackFor = Exception.class)
    public String transferCert(String certId, Integer fromUserId, Integer toUserId) {
        SysUser fromUser = userRepository.findById(fromUserId).orElseThrow(() -> new RuntimeException("发起方不存在"));
        SysUser toUser = userRepository.findById(toUserId).orElseThrow(() -> new RuntimeException("接收方不存在"));

        // 1. 加载合约 (此时合约拥有卖方 fromUser 的私钥签名权限)
        GreenCert contract = loadContractByUser(fromUser);

        // 2. 调用智能合约流转
        TransactionReceipt receipt = contract.transferCert(certId, toUser.getChainAddress());

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("流转上链失败: " + receipt.getMessage());
        }

        // 3. 更新 MySQL 主表所有权
        GreenCertEntity cert = greenCertRepository.findById(certId).orElseThrow(() -> new RuntimeException("证书不存在"));
        cert.setCurrentOwnerId(toUserId);
        greenCertRepository.save(cert);

        // 4. 记录流转溯源日志 (高分项)
        CertTransferLog log = new CertTransferLog();
        log.setCertId(certId);
        log.setFromOwnerId(fromUserId);
        log.setToOwnerId(toUserId);
        log.setTransferTime(new Date());
        log.setTxHash(receipt.getTransactionHash());
        transferLogRepository.save(log);

        return receipt.getTransactionHash();
    }

    /**
     * 3. 链上防篡改校验 (大作业亮点)
     */
    public boolean verifyCertOnChain(String certId) throws Exception {
        // 1. 查本地 MySQL
        GreenCertEntity localCert = greenCertRepository.findById(certId).orElseThrow(() -> new RuntimeException("本地无此证书"));
        SysUser localOwner = userRepository.findById(localCert.getCurrentOwnerId()).orElseThrow(() -> new RuntimeException("找不到所有者信息"));

        // 2. 查区块链 (调用 getCert 属于 view 查询，不需要消耗 Gas，随便用个账号加载合约即可，比如 Admin)
        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        GreenCert contract = loadContractByUser(admin);

        // 3. 【核心重构】直接获取强类型的 Tuple7 返回值，告别 JSONArray 解析！
        Tuple7<String, String, String, String, BigInteger, BigInteger, Boolean> result = contract.getCert(certId);

        // 4. 提取链上数据进行比对 (根据 ABI：Value3 是 currentOwner，Value7 是 isValid)
        String chainOwnerAddress = result.getValue3();
        Boolean chainIsValid = result.getValue7();

        // 5. 交叉比对
        boolean isOwnerMatch = localOwner.getChainAddress().equalsIgnoreCase(chainOwnerAddress);
        boolean isStatusMatch = (localCert.getStatus() == 1) == chainIsValid;

        return isOwnerMatch && isStatusMatch;
    }
}