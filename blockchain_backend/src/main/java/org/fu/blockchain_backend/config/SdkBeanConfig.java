package org.fu.blockchain_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.model.ConfigProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.util.Map;

@Configuration
@Slf4j
public class SdkBeanConfig {

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private BcosConfig bcosConfig;

    @Bean
    public Client client() throws Exception {
        ConfigProperty property = new ConfigProperty();
        configNetwork(property);
        configCryptoMaterial(property);

        // 1. 初始化配置选项
        ConfigOption configOption = new ConfigOption(property);

        // 2. 根据 group0 创建底层连接客户端
        Client client = new BcosSDK(configOption).getClient(systemConfig.getGroupId());

        // 3. 测试连接是否成功（获取当前区块高度）
        BigInteger blockNumber = client.getBlockNumber().getBlockNumber();
        log.info("🎉 区块链底层节点连接成功！当前区块高度: {}", blockNumber);

        // 【核心修复】彻底删除了 configCryptoKeyPair(client) 的调用
        // 全局 Client 不再绑定任何私钥，私钥将在 Service 业务层动态加载

        return client;
    }

    public void configNetwork(ConfigProperty configProperty){
        Map peers = bcosConfig.getNetwork();
        configProperty.setNetwork(peers);
    }

    public void configCryptoMaterial(ConfigProperty configProperty){
        Map<String, Object> cryptoMaterials = bcosConfig.getCryptoMaterial();
        configProperty.setCryptoMaterial(cryptoMaterials);
    }
}