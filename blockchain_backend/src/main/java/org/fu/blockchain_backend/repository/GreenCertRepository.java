package org.fu.blockchain_backend.repository;
import org.fu.blockchain_backend.entity.GreenCertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenCertRepository extends JpaRepository<GreenCertEntity, String> {
}