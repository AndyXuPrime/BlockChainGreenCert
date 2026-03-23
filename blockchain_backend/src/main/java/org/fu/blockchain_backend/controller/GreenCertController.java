package org.fu.blockchain_backend.controller;

import org.fu.blockchain_backend.common.Result;
import org.fu.blockchain_backend.dto.IssueReqDTO;
import org.fu.blockchain_backend.dto.TransferReqDTO;
import org.fu.blockchain_backend.service.impl.GreenCertServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin // 允许 Vue 前端跨域调用
@RestController
@RequestMapping("/api/cert")
public class GreenCertController {

    @Autowired
    private GreenCertServiceImpl certService;

    @PostMapping("/issue")
    public Result issue(@RequestBody IssueReqDTO req) {
        try {
            String txHash = certService.issueCert(req.getCertId(), req.getOwnerId(), req.getEnergyType(), req.getAmount());
            return Result.success("核发成功，交易哈希: " + txHash);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public Result transfer(@RequestBody TransferReqDTO req) {
        try {
            // 实际开发中，fromUserId 应该从登录 Session/Token 中获取，防止越权
            String txHash = certService.transferCert(req.getCertId(), req.getFromUserId(), req.getToUserId());
            return Result.success("流转成功，交易哈希: " + txHash);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/verify/{certId}")
    public Result verify(@PathVariable String certId) {
        try {
            boolean isValid = certService.verifyCertOnChain(certId);
            if (isValid) {
                return Result.success("✅ 链上核验通过！数据真实有效未被篡改。");
            } else {
                return Result.error("❌ 警告：本地数据与区块链底层账本不一致，疑似被篡改！");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}