package org.fu.blockchain_backend.service.impl;

import cn.hutool.crypto.SecureUtil;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fu.blockchain_backend.dto.RegisterReqDTO;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Client client; // 注入 FISCO BCOS 官方底层客户端

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterReqDTO dto) {
        // 1. 检查用户名是否重复
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 【核心修复】使用 v3.x 语法在本地生成区块链公私钥对
        // 注意：这里调用的是 getKeyPairFactory().generateKeyPair()
        CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().generateKeyPair();
        String chainAddress = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();

        // 3. 组装实体并保存
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());

        // 使用 Hutool 进行 MD5 加密（你在 pom.xml 中已经引入了 hutool）
        user.setPassword(SecureUtil.md5(dto.getPassword()));

        user.setCompanyName(dto.getCompanyName());
        user.setRole("CORP");
        user.setChainAddress(chainAddress);
        user.setPrivateKey(privateKey); // 存入私钥，后续发交易要用来签名

        userRepository.save(user);
    }
}