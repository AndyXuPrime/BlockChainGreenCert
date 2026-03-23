package org.fu.blockchain_backend.entity;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "green_cert")
public class GreenCertEntity {
    @Id
    private String certId;
    private Integer issuerId;
    private Integer currentOwnerId;
    private String energyType;
    private Integer amount;
    private Date issueTime;
    private Integer status;
    private String txHash;
}