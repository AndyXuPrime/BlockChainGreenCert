package org.fu.blockchain_backend.repository;
import org.fu.blockchain_backend.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<SysUser, Integer> {
    SysUser findByUsername(String username);
}