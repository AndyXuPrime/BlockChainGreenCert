package org.fu.blockchain_backend.entity;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "sys_user")
public class SysUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String companyName;
    private String chainAddress;
    private String privateKey; // 路线B必须存私钥用于签名
}