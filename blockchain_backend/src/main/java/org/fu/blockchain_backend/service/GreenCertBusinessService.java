package org.fu.blockchain_backend.service;

import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fu.blockchain_backend.config.ContractConfig;
import org.fu.blockchain_backend.contracts.GreenCert;
import org.fu.blockchain_backend.entity.GreenCertEntity;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.repository.GreenCertRepository;
import org.fu.blockchain_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;

@Service
public class GreenCertBusinessService {

    @Autowired
    private Client client;
    @Autowired
    private ContractConfig contractConfig;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GreenCertRepository greenCertRepository;

    /**
     * 获取加载了特定用户私钥的智能合约对象
     */
    private GreenCert loadContractByUser(SysUser user) {
        // 从数据库取出该用户的私钥，恢复成 KeyPair 对象
        CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().createKeyPair(user.getPrivateKey());
        // 加载智能合约
        return GreenCert.load(contractConfig.getGreenCertAddress(), client, keyPair);
    }

    @Transactional(rollbackFor = Exception.class)
    public String issueCert(String certId, Integer ownerId, String energyType, Integer amount) {
        // 1. 获取 Admin 账户 (假设 ID 为 1)
        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        SysUser owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("接收企业不存在"));

        // 2. 加载合约 (使用 Admin 的身份)
        GreenCert contract = loadContractByUser(admin);

        // 3. 发送交易上链
        TransactionReceipt receipt = contract.issueCert(certId, owner.getChainAddress(), energyType, BigInteger.valueOf(amount));

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("上链失败，状态码: " + receipt.getStatus());
        }

        // 4. 落库 MySQL
        GreenCertEntity certEntity = new GreenCertEntity();
        certEntity.setCertId(certId);
        certEntity.setIssuerId(admin.getId());
        certEntity.setCurrentOwnerId(owner.getId());
        certEntity.setEnergyType(energyType);
        certEntity.setAmount(amount);
        certEntity.setIssueTime(new Date());
        certEntity.setStatus(1);
        certEntity.setTxHash(receipt.getTransactionHash());
        greenCertRepository.save(certEntity);

        return receipt.getTransactionHash();
    }

    // transferCert 逻辑同理，只需 loadContractByUser(fromUser)，然后调用 contract.transferCert()
}