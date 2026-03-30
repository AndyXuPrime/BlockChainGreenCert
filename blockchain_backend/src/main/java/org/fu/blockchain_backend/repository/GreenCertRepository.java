package org.fu.blockchain_backend.repository;

import org.fu.blockchain_backend.entity.GreenCertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreenCertRepository extends JpaRepository<GreenCertEntity, String> {
    java.util.List<GreenCertEntity> findByCurrentOwnerId(Integer currentOwnerId);
}