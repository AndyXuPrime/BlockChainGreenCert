package org.fu.blockchain_backend.service;

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
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 【路线B核心】：直接使用 SDK 在本地生成区块链公私钥对！不需要请求 WeBASE！
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        String address = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // 实际应加密
        user.setCompanyName(dto.getCompanyName());
        user.setRole("CORP");
        user.setChainAddress(address);
        user.setPrivateKey(privateKey); // 存入私钥，后续发交易要用

        userRepository.save(user);
    }
}