package org.fu.blockchain_backend.controller;

import cn.hutool.crypto.SecureUtil;
import org.fu.blockchain_backend.common.Result;
import org.fu.blockchain_backend.dto.LoginReqDTO;
import org.fu.blockchain_backend.dto.RegisterReqDTO;
import org.fu.blockchain_backend.entity.SysUser;
import org.fu.blockchain_backend.repository.UserRepository;
import org.fu.blockchain_backend.service.AuthBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthBusinessService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1. 登录接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid LoginReqDTO dto) {
        // 1. 根据用户名查数据库
        SysUser user = userRepository.findByUsername(dto.getUsername());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. MD5 密码比对 (将前端传来的明文转成 MD5，和数据库里的密文比对)
        String inputMd5Password = SecureUtil.md5(dto.getPassword());
        if (!inputMd5Password.equalsIgnoreCase(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 登录成功！为了安全，把私钥和密码抹除后再返回给前端
        user.setPassword(null);
        user.setPrivateKey(null);

        return Result.success(user);
    }

    /**
     * 2. 注册接口
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Valid RegisterReqDTO dto) {
        try {
            // 调用自动生成区块链私钥的 Service
            authService.register(dto);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }
}