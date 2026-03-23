package org.example.demo.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.constants.ContractConstants;
import org.example.demo.model.bo.GreenCertGetCertInputBO;
import org.example.demo.model.bo.GreenCertIssueCertInputBO;
import org.example.demo.model.bo.GreenCertRevokeCertInputBO;
import org.example.demo.model.bo.GreenCertTransferCertInputBO;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Data
public class GreenCertService {
  @Value("${contract.greenCertAddress}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public TransactionResponse revokeCert(GreenCertRevokeCertInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ContractConstants.GreenCertAbi, "revokeCert", input.toArgs());
  }

  public TransactionResponse issueCert(GreenCertIssueCertInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ContractConstants.GreenCertAbi, "issueCert", input.toArgs());
  }

  public CallResponse getCert(GreenCertGetCertInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ContractConstants.GreenCertAbi, "getCert", input.toArgs());
  }

  public TransactionResponse transferCert(GreenCertTransferCertInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ContractConstants.GreenCertAbi, "transferCert", input.toArgs());
  }

  public CallResponse systemAdmin() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ContractConstants.GreenCertAbi, "systemAdmin", Arrays.asList());
  }
}
