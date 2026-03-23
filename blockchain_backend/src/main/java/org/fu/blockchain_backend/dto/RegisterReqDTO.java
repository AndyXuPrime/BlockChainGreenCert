package org.fu.blockchain_backend.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterReqDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "企业名称不能为空")
    private String companyName;
}