package org.fu.blockchain_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 绿证流转溯源日志实体类
 * 对应数据库表：cert_transfer_log
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cert_transfer_log")
public class CertTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 被流转的证书编号 (对应合约中的 certId)
     */
    @Column(name = "cert_id", nullable = false)
    private String certId;

    /**
     * 原所有者用户ID (对应 sys_user 表的 id)
     */
    @Column(name = "from_owner_id", nullable = false)
    private Integer fromOwnerId;

    /**
     * 新所有者用户ID (买方，对应 sys_user 表的 id)
     */
    @Column(name = "to_owner_id", nullable = false)
    private Integer toOwnerId;

    /**
     * 发生流转的系统时间
     */
    @Column(name = "transfer_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date transferTime;

    /**
     * 本次流转上链后返回的交易哈希 (TxHash)
     * 极其重要：这是链下数据与区块链底层证据的唯一关联键
     */
    @Column(name = "tx_hash", unique = true, nullable = false)
    private String txHash;
}