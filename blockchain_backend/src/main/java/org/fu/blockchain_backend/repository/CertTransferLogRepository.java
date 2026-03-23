package org.fu.blockchain_backend.repository;

import org.fu.blockchain_backend.entity.CertTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertTransferLogRepository extends JpaRepository<CertTransferLog, Integer> {
}