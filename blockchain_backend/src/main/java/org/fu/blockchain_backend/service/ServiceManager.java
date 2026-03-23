package org.example.demo.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.config.ContractConfig;
import org.example.demo.config.SystemConfig;
import org.fisco.bcos.sdk.v3.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
public class ServiceManager {
  @Autowired
  private SystemConfig systemConfig;

  @Autowired
  private ContractConfig contractConfig;

  @Autowired
  private Client client;

  List<String> hexPrivateKeyList;

  @PostConstruct
  public void init() {
    hexPrivateKeyList = Arrays.asList(this.systemConfig.getHexPrivateKey().split(","));
  }

  /**
   * @notice: must use @Qualifier("GreenCertService") with @Autowired to get this Bean
   */
  @Bean("GreenCertService")
  public Map<String, GreenCertService> initGreenCertServiceManager() throws Exception {
    Map<String, GreenCertService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
    for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
    	String privateKey = this.hexPrivateKeyList.get(i);
    	if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
    		privateKey = privateKey.substring(2);
    	}
    	if (privateKey.isEmpty()) {
    		continue;
    	}
    	org.fisco.bcos.sdk.v3.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.v3.crypto.CryptoSuite(this.client.getCryptoType());
    	org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
    	String userAddress = cryptoKeyPair.getAddress();
    	log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
    	GreenCertService greenCertService = new GreenCertService();
    	greenCertService.setAddress(this.contractConfig.getGreenCertAddress());
    	greenCertService.setClient(this.client);
    	org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor txProcessor = 
    		org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
    	greenCertService.setTxProcessor(txProcessor);
    	serviceMap.put(userAddress, greenCertService);
    }
    log.info("++++++++GreenCertService map:{}", serviceMap);
    return serviceMap;
  }
}
