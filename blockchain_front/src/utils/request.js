import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8080', // 你的 Spring Boot 后端地址
    timeout: 10000 // 请求超时时间
});

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data;
        // 如果后端返回的 code 是 "0" (成功)
        if (res.code === "0") {
            return res.data || res.msg || "操作成功";
        } else {
            // 业务逻辑错误，抛出给前端 catch
            return Promise.reject(new Error(res.msg || '系统未知错误'));
        }
    },
    error => {
        console.error('网络请求错误: ' + error);
        return Promise.reject(error);
    }
);

export default request;