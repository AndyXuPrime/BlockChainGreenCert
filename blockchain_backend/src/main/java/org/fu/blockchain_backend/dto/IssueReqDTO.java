package org.fu.blockchain_backend.dto;
import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class IssueReqDTO {
    @NotBlank(message = "证书编号不能为空")
    private String certId;
    @NotNull(message = "接收企业ID不能为空")
    private Integer ownerId;
    @NotBlank(message = "能源类型不能为空")
    private String energyType;
    @NotNull(message = "电量不能为空")
    @Min(value = 1, message = "电量必须大于0")
    private Integer amount;
}