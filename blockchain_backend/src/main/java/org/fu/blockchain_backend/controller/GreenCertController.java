package org.fu.blockchain_backend.controller;

import org.fu.blockchain_backend.common.Result;
import org.fu.blockchain_backend.dto.IssueReqDTO;
import org.fu.blockchain_backend.dto.TransferReqDTO;
import org.fu.blockchain_backend.service.GreenCertBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin // 允许 Vue 前端跨域调用
@RestController
@RequestMapping("/api/cert")
public class GreenCertController {

    @Autowired
    private GreenCertBusinessService certService;

    /**
     * 1. 核发接口 (你已经调通了这个)
     */
    @PostMapping("/issue")
    public Result issue(@RequestBody @Valid IssueReqDTO req) {
        try {
            String txHash = certService.issueCert(req.getCertId(), req.getOwnerId(), req.getEnergyType(), req.getAmount());
            return Result.success("核发成功，交易哈希: " + txHash);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 2. 流转接口
     */
    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid TransferReqDTO req) {
        try {
            String txHash = certService.transferCert(req.getCertId(), req.getFromUserId(), req.getToUserId());
            return Result.success("流转成功，交易哈希: " + txHash);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 3. 防篡改核验接口 (你刚才 404 的就是这个)
     */
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

    /**
     * 4. 强制作废接口 (补充一个，方便你完整演示)
     */
    @PostMapping("/revoke")
    public Result revoke(@RequestParam String certId) {
        try {
            certService.revokeCert(certId);
            return Result.success("作废成功，该证书已失效。");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}