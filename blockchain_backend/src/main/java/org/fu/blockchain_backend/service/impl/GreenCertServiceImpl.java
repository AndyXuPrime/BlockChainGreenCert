package org.fu.blockchain_backend.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.fu.blockchain_backend.entity.CertTransferLog;
import org.fu.blockchain_backend.entity.GreenCert;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.mapper.CertTransferLogMapper;
import org.fu.blockchain_backend.mapper.GreenCertMapper;
import org.fu.blockchain_backend.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

@Service
public class GreenCertServiceImpl {

    @Autowired
    private GreenCertMapper greenCertMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CertTransferLogMapper transferLogMapper;

    /**
     * 1. 核发绿证 (Admin 操作)
     */
    @Transactional(rollbackFor = Exception.class)
    public String issueCert(String certId, Integer ownerId, String energyType, Integer amount) {
        // 1. 查出接收企业的链上地址
        SysUser owner = sysUserMapper.selectById(ownerId);
        if (owner == null) throw new RuntimeException("接收企业不存在");

        // 2. 调用 WeBASE 上链 (使用 admin 的身份签名)
        JSONObject txData = WeBASEUtil.sendTransaction(
                WeBASEConfig.ADMIN_SIGN_USER,
                "issueCert",
                Arrays.asList(certId, owner.getChainAddress(), energyType, amount)
        );
        String txHash = txData.getStr("transactionHash");

        // 3. 上链成功后，写入 MySQL
        GreenCert cert = new GreenCert();
        cert.setCertId(certId);
        cert.setIssuerId(1); // 假设 Admin 的 ID 是 1
        cert.setCurrentOwnerId(ownerId);
        cert.setEnergyType(energyType);
        cert.setAmount(amount);
        cert.setIssueTime(new Date());
        cert.setStatus(1); // 1-有效
        cert.setTxHash(txHash);
        greenCertMapper.insert(cert);

        return txHash;
    }

    /**
     * 2. 跨域流转 (企业操作)
     */
    @Transactional(rollbackFor = Exception.class)
    public String transferCert(String certId, Integer fromUserId, Integer toUserId) {
        SysUser fromUser = sysUserMapper.selectById(fromUserId);
        SysUser toUser = sysUserMapper.selectById(toUserId);

        // 1. 调用 WeBASE 上链 (使用卖方的 username 作为身份签名)
        JSONObject txData = WeBASEUtil.sendTransaction(
                fromUser.getUsername(),
                "transferCert",
                Arrays.asList(certId, toUser.getChainAddress())
        );
        String txHash = txData.getStr("transactionHash");

        // 2. 更新主表所有权
        GreenCert cert = greenCertMapper.selectById(certId);
        cert.setCurrentOwnerId(toUserId);
        greenCertMapper.updateById(cert);

        // 3. 记录流转溯源日志 (高分项)
        CertTransferLog log = new CertTransferLog();
        log.setCertId(certId);
        log.setFromOwnerId(fromUserId);
        log.setToOwnerId(toUserId);
        log.setTransferTime(new Date());
        log.setTxHash(txHash);
        transferLogMapper.insert(log);

        return txHash;
    }

    /**
     * 3. 链上防篡改校验 (大作业亮点)
     */
    public boolean verifyCertOnChain(String certId) {
        // 1. 查本地 MySQL
        GreenCert localCert = greenCertMapper.selectById(certId);
        if (localCert == null) throw new RuntimeException("本地无此证书");
        SysUser localOwner = sysUserMapper.selectById(localCert.getCurrentOwnerId());

        // 2. 查区块链 (调用 getCert，不需要消耗 Gas，用 admin 身份查即可)
        JSONObject txData = WeBASEUtil.sendTransaction(WeBASEConfig.ADMIN_SIGN_USER, "getCert", Arrays.asList(certId));
        JSONArray constantResult = txData.getJSONArray("constantResult");

        // 3. 提取链上数据进行比对 (根据你的 ABI，索引 2 是 owner地址，索引 6 是 isValid)
        String chainOwnerAddress = constantResult.getStr(2);
        boolean chainIsValid = constantResult.getBool(6);

        // 4. 交叉比对
        boolean isOwnerMatch = localOwner.getChainAddress().equalsIgnoreCase(chainOwnerAddress);
        boolean isStatusMatch = (localCert.getStatus() == 1) == chainIsValid;

        return isOwnerMatch && isStatusMatch;
    }
}