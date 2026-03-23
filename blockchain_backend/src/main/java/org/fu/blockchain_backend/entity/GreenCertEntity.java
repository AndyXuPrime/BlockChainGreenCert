package org.fu.blockchain_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor  // 【新增】JPA 必须的无参构造函数
@AllArgsConstructor // 【新增】方便后续代码 new 对象
@Table(name = "green_cert")
public class GreenCertEntity {
    @Id
    @Column(name = "cert_id") // 【建议】显式指定数据库列名，防止大小写映射出问题
    private String certId;

    private Integer issuerId;
    private Integer currentOwnerId;
    private String energyType;
    private Integer amount;
    private Date issueTime;
    private Integer status;
    private String txHash;
}