package org.fu.blockchain_backend.repository;

import org.fu.blockchain_backend.entity.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DegreeRepository extends JpaRepository<Degree, Long>, JpaSpecificationExecutor<Degree> {
    Degree findByIdCardNum(String idCardNum);
}
