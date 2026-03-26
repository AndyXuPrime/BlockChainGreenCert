package org.fu.blockchain_backend.service;

import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple7;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fu.blockchain_backend.config.ContractConfig;
import org.fu.blockchain_backend.contracts.GreenCert;
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
public class GreenCertBusinessService {

    @Autowired
    private Client client;
    @Autowired
    private ContractConfig contractConfig;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GreenCertRepository greenCertRepository;
    @Autowired
    private CertTransferLogRepository transferLogRepository;

    private GreenCert loadContractByUser(SysUser user) {
        if (user.getPrivateKey() == null || user.getPrivateKey().isEmpty()) {
            throw new RuntimeException("用户缺少区块链私钥");
        }
        CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().createKeyPair(user.getPrivateKey());
        return GreenCert.load(contractConfig.getGreenCertAddress(), client, keyPair);
    }

    @Transactional(rollbackFor = Exception.class)
    public String issueCert(String certId, Integer ownerId, String energyType, Integer amount) {
        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        SysUser owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("接收企业不存在"));

        GreenCert contract = loadContractByUser(admin);
        TransactionReceipt receipt = contract.issueCert(certId, owner.getChainAddress(), energyType, BigInteger.valueOf(amount));

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("上链失败: " + receipt.getMessage());
        }

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

    @Transactional(rollbackFor = Exception.class)
    public String transferCert(String certId, Integer fromUserId, Integer toUserId) {
        SysUser fromUser = userRepository.findById(fromUserId).orElseThrow(() -> new RuntimeException("发起方不存在"));
        SysUser toUser = userRepository.findById(toUserId).orElseThrow(() -> new RuntimeException("接收方不存在"));

        GreenCert contract = loadContractByUser(fromUser);
        TransactionReceipt receipt = contract.transferCert(certId, toUser.getChainAddress());

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("流转上链失败: " + receipt.getMessage());
        }

        GreenCertEntity cert = greenCertRepository.findById(certId).orElseThrow(() -> new RuntimeException("证书不存在"));
        cert.setCurrentOwnerId(toUserId);
        greenCertRepository.save(cert);

        CertTransferLog log = new CertTransferLog();
        log.setCertId(certId);
        log.setFromOwnerId(fromUserId);
        log.setToOwnerId(toUserId);
        log.setTransferTime(new Date());
        log.setTxHash(receipt.getTransactionHash());
        transferLogRepository.save(log);

        return receipt.getTransactionHash();
    }

    @Transactional(rollbackFor = Exception.class)
    public void revokeCert(String certId) {
        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        GreenCert contract = loadContractByUser(admin);

        TransactionReceipt receipt = contract.revokeCert(certId);
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("作废失败: " + receipt.getMessage());
        }

        GreenCertEntity cert = greenCertRepository.findById(certId).orElseThrow(() -> new RuntimeException("证书不存在"));
        cert.setStatus(2);
        greenCertRepository.save(cert);
    }

    public boolean verifyCertOnChain(String certId) throws Exception {
        GreenCertEntity localCert = greenCertRepository.findById(certId).orElseThrow(() -> new RuntimeException("本地无此证书"));
        SysUser localOwner = userRepository.findById(localCert.getCurrentOwnerId()).orElseThrow(() -> new RuntimeException("找不到所有者信息"));

        SysUser admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("找不到Admin账户"));
        GreenCert contract = loadContractByUser(admin);

        Tuple7<String, String, String, String, BigInteger, BigInteger, Boolean> result = contract.getCert(certId);

        String chainOwnerAddress = result.getValue3();
        Boolean chainIsValid = result.getValue7();

        boolean isOwnerMatch = localOwner.getChainAddress().equalsIgnoreCase(chainOwnerAddress);
        boolean isStatusMatch = (localCert.getStatus() == 1) == chainIsValid;

        return isOwnerMatch && isStatusMatch;
    }
}