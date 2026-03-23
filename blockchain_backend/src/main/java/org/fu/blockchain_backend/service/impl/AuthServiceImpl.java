package org.fu.blockchain_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.mapper.SysUserMapper;
import org.fu.blockchain_backend.util.WeBASEUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Transactional(rollbackFor = Exception.class)
    public void register(SysUser user) {
        // 1. 检查用户名是否重复
        if (sysUserMapper.selectCount(new QueryWrapper<SysUser>().eq("username", user.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 调用 WeBASE 生成区块链身份 (使用 username 作为 signUserId)
        String chainAddress = WeBASEUtil.createAccount(user.getUsername());
        user.setChainAddress(chainAddress);

        // 3. 保存到 MySQL
        user.setRole("CORP"); // 默认注册为企业
        sysUserMapper.insert(user);
    }

    // login 方法略 (查库比对密码即可)
}