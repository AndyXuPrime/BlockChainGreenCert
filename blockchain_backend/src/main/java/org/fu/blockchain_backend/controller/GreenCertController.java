package org.fu.blockchain_backend.controller;

import org.fu.blockchain_backend.common.Result;
import org.fu.blockchain_backend.dto.IssueReqDTO;
import org.fu.blockchain_backend.service.GreenCertBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/cert")
public class GreenCertController {

    @Autowired
    private GreenCertBusinessService certService;

    @PostMapping("/issue")
    public Result issue(@RequestBody @Valid IssueReqDTO req) {
        try {
            String txHash = certService.issueCert(req.getCertId(), req.getOwnerId(), req.getEnergyType(), req.getAmount());
            return Result.success("核发成功，交易哈希: " + txHash);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}