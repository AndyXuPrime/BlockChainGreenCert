# 🌱 基于区块链的绿证核发与跨域流转系统 (Green Certificate System)

[![Java Version](https://img.shields.io/badge/Java-1.8%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/SpringBoot-2.6.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue3](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)](https://vuejs.org/)
[![FISCO BCOS](https://img.shields.io/badge/FISCO_BCOS-3.x-108EE9.svg)](https://fisco-bcos-doc.readthedocs.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

> 一个面向“双碳”目标的现代化绿色电力证书（绿证）管理平台。本项目采用**“链上链下协同（双引擎）”**架构设计，结合 Spring Boot 与 FISCO BCOS 联盟链，实现了绿证的权威核发、跨域流转、数据溯源与防篡改交叉核验。
>
> 💡 **特别说明**：本项目附带详尽的《新手避坑指南》，非常适合作为高校学生、初学者入门 Web3 全栈开发的学习参考项目。

## ✨ 核心特性

- 🏗️ **双引擎架构** - MySQL 负责关系明细与高性能查询，区块链负责核心资产确权与防篡改，完美契合企业级 Web3 落地场景。
- 🔐 **私钥后端托管** - 摒弃繁琐的浏览器钱包插件，采用 Java SDK 直连底层节点，在内存中动态加载私钥签名，实现无感知的 Web3 交互。
- 🛡️ **交叉防篡改核验** - 独创的“链上链下比对”机制，一键识别本地数据库是否遭到黑客篡改，彰显区块链信任价值。
- 📜 **不可篡改的溯源** - 绿证流转采用“状态追加”与“逻辑删除”模式，保留完整的资产流转生命周期日志。
- 📱 **现代化 UI 交互** - Vue 3 + Element Plus 构建的响应式控制台，包含炫酷的能量粒子登录动效与严格的角色路由隔离。

## 🏗️ 系统架构设计

本项目拒绝“纯链上”或“纯链下”的极端方案，采用**链上链下协同架构**。

```mermaid
graph TB
    subgraph "前端层 (Web2 体验)"
        A["Vue 3 前端界面<br/>发起核发/流转/核验请求"]
    end

    subgraph "业务调度层 (桥梁)"
        B["Spring Boot 后端<br/>业务逻辑与权限校验"]
        C(("动态提取私钥<br/>构建交易并签名"))
    end

    subgraph "持久化与共识层 (双引擎)"
        D[("MySQL 数据库<br/>存储: 账号密码、企业明细<br/>绿证列表、流转日志")]
        E[("FISCO BCOS 联盟链<br/>存储: 资产归属权、电量<br/>状态(isValid)、交易哈希")]
    end

    A <-->|HTTP/JSON| B
    B --> C
    B <-->|JPA/SQL| D
    C <-->|Java SDK / Channel 协议| E
    
    style A fill:#e3f2fd,stroke:#1565c0
    style B fill:#fff2cc,stroke:#d6b656
    style C fill:#fff2cc,stroke:#d6b656,stroke-dasharray: 5 5
    style D fill:#dae8fc,stroke:#6c8ebf
    style E fill:#d5e8d4,stroke:#82b366
```

## 📊 核心业务流程

### 1. 绿证跨域流转流程 (企业间点对点交易)

```mermaid
sequenceDiagram
    participant CorpA as 卖方企业 (CorpA)
    participant SB as Spring Boot 后端
    participant DB as MySQL 数据库
    participant Chain as FISCO BCOS 区块链

    CorpA->>SB: 发起流转请求 (指定买家与证书ID)
    SB->>DB: 校验 CorpA 是否为当前所有者
    DB-->>SB: 校验通过，提取 CorpA 私钥
    SB->>SB: 使用 CorpA 私钥对交易进行数字签名
    SB->>Chain: 发送 transferCert 交易
    Chain->>Chain: 智能合约校验权限与证书状态
    Chain-->>SB: 交易共识成功，返回 TxHash
    SB->>DB: 更新绿证归属，写入流转溯源日志(附带TxHash)
    DB-->>SB: 落库成功
    SB-->>CorpA: 提示流转成功
```

### 2. 防篡改交叉核验流程 (系统高光时刻)

```mermaid
sequenceDiagram
    participant User as 监管方/公众
    participant SB as Spring Boot 后端
    participant DB as MySQL 数据库
    participant Chain as FISCO BCOS 区块链

    User->>SB: 请求核验证书 (ID: GC-001)
    SB->>DB: 查询本地数据库记录
    DB-->>SB: 返回本地所有者 (如: CorpB)
    SB->>Chain: 调用 getCert 查询链上真实状态 (不消耗Gas)
    Chain-->>SB: 返回链上所有者 (如: CorpB)
    SB->>SB: 交叉比对 (本地数据 == 链上数据 ?)
    alt 比对一致
        SB-->>User: 🟢 验证通过！数据真实有效
    else 数据库被黑客篡改
        SB-->>User: 🔴 警告！本地数据已被篡改，以链上为准！
    end
```

## 🚀 快速开始

### 1. 环境准备
- **JDK:** 1.8 或 21 (已在 `pom.xml` 中适配 Lombok 版本)
- **Node.js:** v16+
- **数据库:** MySQL 8.0+
- **区块链:** 运行中的 FISCO BCOS 3.x 节点 (需开放 20200 Channel 端口)

### 2. 数据库初始化
创建名为 `green_cert_db` 的数据库，并执行以下 SQL 初始化测试账号：
```sql
-- Spring Boot 启动时会自动建表，只需插入初始数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `role`, `company_name`, `chain_address`) VALUES 
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', '国家能源局', '替换为你的Admin 0x地址'),
(2, 'corpa', 'e10adc3949ba59abbe56e057f20f883e', 'CORP', '内蒙古风电集团', '替换为你的CorpA 0x地址'),
(3, 'corpb', 'e10adc3949ba59abbe56e057f20f883e', 'CORP', '深圳腾讯数据中心', '替换为你的CorpB 0x地址');
-- 注意：实际开发中，请在数据库中补全 Admin 的 private_key 字段（纯 Hex 格式）
```

### 3. 区块链证书配置
将虚拟机的底层节点证书（`ca.crt`, `sdk.crt`, `sdk.key`）拷贝至后端的 `src/main/resources/conf` 目录下。

### 4. 启动后端 (Spring Boot)
1. 修改 `application.properties` 中的 MySQL 账号密码及区块链节点 IP。
2. 运行 `BlockchainBackendApplication.java`。
3. 看到控制台输出 `🎉 区块链底层节点连接成功！当前区块高度: X` 即为启动成功。

### 5. 启动前端 (Vue 3 + Vite)
```bash
cd blockchain_front
npm install
npm run dev
```
访问 `http://localhost:5173` 即可进入系统。

## 🗄️ 核心数据库设计

本系统严格遵循“链上存资产，链下存关系”的原则：

1. **`sys_user` (用户表):** 桥接 Web2 与 Web3。包含 `chain_address` (区块链 0x 地址) 和 `private_key` (托管私钥，用于后端静默签名)。
2. **`green_cert` (绿证主表):** 映射智能合约结构体，包含 `status` (0-未上链, 1-有效, 2-已作废) 和 `tx_hash` (上链凭据)。
3. **`cert_transfer_log` (流转溯源表):** 记录证书的每一次流转历史（A -> B -> C），为前端提供毫秒级的溯源时间轴查询。

## 💣 附录：新手避坑指南 (Blood & Tears)

刚接触区块链开发？不要被底层报错吓倒。以下是本项目开发过程中总结的“排雷手册”：

### 坑 1：区块链到底能不能改数据？
*   **误区：** 以为智能合约里能写 `DELETE` 语句物理删除发错的绿证。
*   **正解：** 引入**“逻辑删除（Logical Deletion）”**设计模式。在合约中增加 `bool isValid` 字段，作废时将其置为 `false`，并在流转时拦截无效证书。区块链的本质是“状态追加”，一切错误纠正都会留下不可篡改的审计轨迹。

### 坑 2：Java 启动报 `NumberFormatException: For input string: "M" under radix 16`
*   **病因：** 底层 Java SDK 加载私钥时崩溃。
*   **解药：** SDK 需要的是纯粹的 **64 位 Hex（十六进制）字符串**。绝对不能把带有 `-----BEGIN PRIVATE KEY-----` 或包含字母 `M` 的 PEM 文件原文直接塞进数据库或配置中。

### 坑 3：FISCO BCOS 3.x 报 `The group not exist, groupID: 1`
*   **病因：** 找不到群组，通常是因为照抄了旧版教程。
*   **解药：** 版本差异坑。FISCO BCOS 2.x 的默认群组是数字 `1`，而 **3.x 版本的默认群组名是字符串 `"group0"`**。

### 坑 4：前端请求报 `CORS policy` (跨域拦截)
*   **解药：** 这不是区块链的问题，是纯粹的 Web 安全机制。在 Spring Boot 中添加 `CorsConfig` 全局配置类，放行前端的 `5173` 或 `8888` 端口即可。

### 坑 5：Java 启动报 `Connection Refused` 或 `Timeout`
*   **解药：** 检查虚拟机 IP 是否改变；在 Ubuntu 终端运行 `ps -ef | grep fisco` 确认节点存活；检查虚拟机防火墙是否放行了 `20200` (Channel 协议) 端口。

## 🛠️ 技术栈

<div align="left">

![Java](https://img.shields.io/badge/Java-1.8%2B-00ADD8?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.6.13-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![FISCO BCOS](https://img.shields.io/badge/FISCO_BCOS_SDK-3.6.0-108EE9?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
<br>
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-Build_Tool-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element_Plus-UI_Library-409EFF?style=for-the-badge&logo=element&logoColor=white)

</div>

## 🙏 致谢

- 感谢 [FISCO BCOS](https://github.com/FISCO-BCOS/FISCO-BCOS) 开源社区提供的强大联盟链底层支持。
- 感谢 [WeBankBlockchain](https://github.com/WeBankBlockchain) 提供的相关开发工具链。
- 感谢 [BlockChainEduSys](https://github.com/the-bule-sea/BlockChainEduSys_2025) 项目在初期业务逻辑设计上提供的灵感参考。

---
<div align="center">

**如果这个项目帮助你理解了区块链全栈开发，请给一个 ⭐️ Star！**

</div>