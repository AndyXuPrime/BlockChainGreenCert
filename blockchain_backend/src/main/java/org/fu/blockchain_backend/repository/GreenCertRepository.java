package org.fu.blockchain_backend.repository;

import org.fu.blockchain_backend.entity.GreenCertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 【新增】显式声明这是一个仓库组件
public interface GreenCertRepository extends JpaRepository<GreenCertEntity, String> {
    // 可以在这里加一个查询方法，以后展示“我的绿证”时很有用
    java.util.List<GreenCertEntity> findByCurrentOwnerId(Integer ownerId);
}