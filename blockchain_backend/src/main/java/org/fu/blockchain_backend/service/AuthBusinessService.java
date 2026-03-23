package org.fu.blockchain_backend.service;

import cn.hutool.crypto.SecureUtil; // 导入加密工具
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fu.blockchain_backend.dto.RegisterReqDTO;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthBusinessService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Client client; // 注入 FISCO BCOS SDK 客户端

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterReqDTO dto) {
        // 1. 检查用户名是否已存在
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 【核心修复】在本地生成区块链公私钥对
        CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().generateKeyPair();
        String chainAddress = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();

        // 3. 组装实体对象
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());

        // 【优化】使用 Hutool 进行 MD5 加密，确保存入数据库的不是明文密码
        user.setPassword(SecureUtil.md5(dto.getPassword()));

        user.setCompanyName(dto.getCompanyName());
        user.setRole("CORP");

        // 【修复】将变量名改为 chainAddress
        user.setChainAddress(chainAddress);
        user.setPrivateKey(privateKey); // 存入私钥，后续发交易要用

        // 4. 保存到 MySQL
        userRepository.save(user);
    }
}