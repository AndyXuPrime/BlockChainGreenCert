# 🌱 GreenCert-BlockChain (基于区块链的绿证核发与跨域流转系统)

[![Java Version](https://img.shields.io/badge/Java-1.8%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/SpringBoot-2.6.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue3](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)](https://vuejs.org/)
[![FISCO BCOS](https://img.shields.io/badge/FISCO_BCOS-3.x-108EE9.svg)](https://fisco-bcos-doc.readthedocs.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 💡 **写给初学者的话：**
> 如果你是刚接触 Web3 和联盟链开发的同学，请不要被复杂的密码学名词吓倒。本项目专门为初学者打造，采用**“链上存证 + 链下计算”**的双引擎架构。文档末尾附带了详尽的**《新手排雷避坑指南》**，帮你扫清环境配置与底层调用的所有障碍，让你轻松跑通人生的第一个企业级区块链全栈项目！

一个面向国家“双碳”战略的现代化绿色电力证书（绿证）管理平台。系统利用智能合约实现绿证资产的确权与流转，结合关系型数据库提升业务查询性能，彻底解决传统绿证交易中数据易篡改、跨域互信难、存在“双重计算”等痛点。

## ✨ 核心特性

- 🏗️ **双引擎架构** - MySQL 负责关系明细与高性能检索，区块链负责核心资产确权，兼顾 Web2 的性能与 Web3 的信任。
- 🔐 **私钥后端托管** - 摒弃繁琐的浏览器钱包插件，采用 Java SDK 直连底层节点，在后端内存中动态加载私钥签名，实现用户无感知的极简交互。
- 🛡️ **交叉防篡改核验** - 独创“链上链下比对”机制，一键精准识别本地数据库是否遭到黑客篡改。
- 📜 **逻辑删除与容错** - 针对区块链不可物理删除的特性，引入 `isValid` 状态机，实现绿证的异常作废与完整审计轨迹保留。
- 📱 **现代化 UI 交互** - Vue 3 + Element Plus 构建，包含能量粒子登录动效与严格的 RBAC 角色路由隔离（监管端/企业端）。

## 系统架构设计

本项目拒绝极端的“纯链上”方案，采用务实的链上链下协同架构：

```mermaid
graph TB
    subgraph "前端层 (Web2 交互体验)"
        A[Vue 3 前端界面<br/>发起核发/流转/核验请求]
    end

    subgraph "业务调度层 (双向桥梁)"
        B[Spring Boot 后端<br/>业务逻辑与权限校验]
        C((动态提取私钥<br/>构建交易并离线签名))
    end

    subgraph "持久化与共识层 (双引擎协同)"
        D[(MySQL 数据库)<br/>存储: 账号密码、企业明细<br/>绿证列表、流转日志]
        E[(FISCO BCOS 联盟链)<br/>存储: 资产归属权、电量<br/>状态(isValid)、交易哈希]
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

##  核心业务流程

### 绿证跨域流转与交叉核验流程

```mermaid
sequenceDiagram
    participant CorpA as 卖方企业 (CorpA)
    participant SB as Spring Boot 后端
    participant DB as MySQL 数据库
    participant Chain as FISCO BCOS 区块链
    participant Public as 监管方/买方

    CorpA->>SB: 发起流转请求 (指定买家与证书ID)
    SB->>DB: 校验 CorpA 是否为当前所有者
    DB-->>SB: 校验通过，提取 CorpA 托管私钥
    SB->>Chain: 使用私钥签名，调用 transferCert 上链
    Chain->>Chain: 智能合约校验权限与证书状态 (isValid)
    Chain-->>SB: 交易共识成功，返回 TxHash
    SB->>DB: 更新绿证归属，写入流转溯源日志
    SB-->>CorpA: 提示流转成功

    Note over Public,Chain: 防篡改交叉核验阶段
    Public->>SB: 请求核验证书真伪
    SB->>DB: 查询本地数据库记录 (获取 Owner)
    SB->>Chain: 调用 getCert 查询链上真实状态 (不消耗Gas)
    Chain-->>SB: 返回链上 Owner 与状态
    SB->>SB: 交叉比对 (本地数据 == 链上数据 ?)
    alt 比对一致
        SB-->>Public: 🟢 验证通过！数据真实有效
    else 数据库被篡改
        SB-->>Public: 🔴 警告！本地数据已被篡改，以链上为准！
    end
```

## 快速开始 (保姆级运行指南)

### 1. 环境要求
- **JDK:** 1.8 或 21 (已适配高版本 Lombok)
- **Node.js:** v16+
- **数据库:** MySQL 8.0+
- **区块链:** FISCO BCOS 3.x (推荐使用 WeBASE-Front 一键搭建)

### 2. 区块链底层准备
1. 启动你的 FISCO BCOS 3.x 节点。
2. 将节点生成的三个证书文件（`ca.crt`, `sdk.crt`, `sdk.key`）拷贝到后端项目的 `src/main/resources/conf` 目录下。
3. 在 WeBASE 中部署 `GreenCert.sol` 智能合约，并记录下**合约地址**。

### 3. 数据库初始化
1. 创建数据库 `green_cert_db`。
2. 运行项目提供的 `init.sql` 脚本，初始化 3 个测试账号（Admin, CorpA, CorpB）。
   > ⚠️ **注意：** 请务必将 SQL 中的 `chain_address` 和 `private_key` 替换为你自己在底层生成的真实 0x 地址和 64 位 Hex 私钥！

### 4. 启动后端 (Spring Boot)
1. 修改 `application.properties`：
    - 填入你的 MySQL 账号密码。
    - 修改 `network.peers[0]` 为你虚拟机的 IP 和 20200 端口。
    - 修改 `contract.greenCertAddress` 为你刚才部署的合约地址。
2. 运行 `BlockchainBackendApplication.java`。
3. 看到控制台输出 `🎉 区块链底层节点连接成功！` 即为启动成功。

### 5. 启动前端 (Vue 3)
```bash
cd blockchain_front
npm install
npm run dev
```
访问 `http://localhost:5173`，使用测试账号登录即可体验。

##  核心数据库设计

| 表名 | 功能描述 | 核心字段说明 |
| :--- | :--- | :--- |
| `sys_user` | 用户表 | 桥接 Web2 与 Web3。包含 `chain_address` (区块链地址) 和 `private_key` (托管私钥)。 |
| `green_cert` | 绿证主表 | 映射智能合约结构体。包含 `status` (有效/作废) 和 `tx_hash` (上链凭据)。 |
| `cert_transfer_log` | 流转溯源表 | 记录证书的每一次流转历史（A -> B -> C），提供毫秒级的溯源时间轴查询。 |

##  附录：新手排雷避坑指南 (Blood & Tears)

在开发本项目时，我们踩过无数的坑。如果你在运行或二次开发时遇到报错，请先查阅此指南：

*   **坑 1：Java 启动报 `NumberFormatException: For input string: "M" under radix 16`**
    *   **病因：** 底层 Java SDK 加载私钥时崩溃。
    *   **解药：** SDK 需要的是纯粹的 **64 位 Hex（十六进制）字符串**。绝对不能把带有 `-----BEGIN PRIVATE KEY-----` 或包含字母 `M` 的 PEM 文件原文直接塞进数据库！请写个测试类用 `keyPair.getHexPrivateKey()` 提取纯净私钥。
*   **坑 2：FISCO BCOS 3.x 报 `The group not exist, groupID: 1`**
    *   **病因：** 找不到群组，通常是因为照抄了网上的旧版教程。
    *   **解药：** 版本差异坑。FISCO BCOS 2.x 的默认群组是数字 `1`，而 **3.x 版本的默认群组名是字符串 `"group0"`**。请检查你的配置文件和代码参数。
*   **坑 3：前端请求报 `CORS policy` (跨域拦截)**
    *   **病因：** 浏览器控制台红字，前端连不上后端。
    *   **解药：** 这不是区块链的问题，是纯粹的 Web 安全机制。后端已配置 `CorsConfig`，请确保前端请求的 `baseURL` 端口与后端一致，且不要用 `file:///` 协议直接打开 HTML。
*   **坑 4：区块链数据发错了怎么“删除”？**
    *   **病因：** 试图在智能合约里写 `delete` 语句。
    *   **解药：** 区块链物理上不可篡改！必须使用**“逻辑删除”**。本项目的智能合约中设计了 `isValid` 字段，发现错误时由 Admin 调用 `revokeCert` 将其置为 `false`，保留完整的纠错审计轨迹。

##  联系与讨论
如果你在运行项目中遇到任何问题，或者对“双引擎架构”有更好的建议，欢迎提交 [Issues](https://github.com/AndyXuPrime/BlockChainGreenCert/issues) 或 Pull Requests！

---
<div align="center">

**如果这个项目帮助你理解了区块链全栈开发，请给一个 ⭐️ Star！**

</div>