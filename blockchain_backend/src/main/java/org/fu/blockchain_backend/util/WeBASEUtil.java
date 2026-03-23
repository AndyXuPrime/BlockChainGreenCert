package org.fu.blockchain_backend.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.fu.blockchain_backend.config.WeBASEConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeBASEUtil {

    /**
     * 1. 动态在 WeBASE 中创建私钥账户
     * @param signUserId 用户名 (使用 sys_user 表的 username)
     * @return 生成的区块链 0x 地址
     */
    public static String createAccount(String signUserId) {
        String url = WeBASEConfig.WEBASE_URL + "/privateKey";
        Map<String, Object> param = new HashMap<>();
        param.put("type", 2);
        param.put("appId", "green_cert_app");
        param.put("signUserId", signUserId);

        String res = HttpUtil.post(url, JSONUtil.toJsonStr(param));
        JSONObject resJson = JSONUtil.parseObj(res);

        if (resJson.getInt("code") == 0) {
            return resJson.getJSONObject("data").getStr("address");
        } else if (resJson.getInt("code") == 303001) {
            throw new RuntimeException("该用户名在区块链底层已存在，请更换！");
        } else {
            throw new RuntimeException("生成区块链账户失败: " + resJson.getStr("errorMessage"));
        }
    }

    /**
     * 2. 调用智能合约 (通用方法)
     * @param signUserId 谁发起交易 (签名者)
     * @param funcName 函数名
     * @param funcParam 参数列表
     * @return WeBASE 返回的 data 对象
     */
    public static JSONObject sendTransaction(String signUserId, String funcName, List<Object> funcParam) {
        String url = WeBASEConfig.WEBASE_URL + "/trans/handle";
        Map<String, Object> param = new HashMap<>();
        param.put("groupId", WeBASEConfig.GROUP_ID);
        param.put("user", signUserId);
        param.put("contractName", WeBASEConfig.CONTRACT_NAME);
        param.put("contractAddress", WeBASEConfig.CONTRACT_ADDRESS);
        param.put("funcName", funcName);
        param.put("funcParam", funcParam);

        String res = HttpUtil.post(url, JSONUtil.toJsonStr(param));
        JSONObject resJson = JSONUtil.parseObj(res);

        if (resJson.getInt("code") != 0) {
            throw new RuntimeException("WeBASE调用失败: " + resJson.getStr("errorMessage"));
        }

        JSONObject data = resJson.getJSONObject("data");
        if (!"0x0".equals(data.getStr("status"))) {
            throw new RuntimeException("智能合约执行被拒绝 (Status: " + data.getStr("status") + ")");
        }
        return data;
    }
}