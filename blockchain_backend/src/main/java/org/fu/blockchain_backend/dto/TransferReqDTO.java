package org.fu.blockchain_backend.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TransferReqDTO {
    @NotBlank(message = "证书编号不能为空")
    private String certId;
    @NotNull(message = "发起方ID不能为空")
    private Integer fromUserId;
    @NotNull(message = "接收方ID不能为空")
    private Integer toUserId;
}