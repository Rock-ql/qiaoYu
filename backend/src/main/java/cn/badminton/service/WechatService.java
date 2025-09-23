package cn.badminton.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信API集成服务
 * 负责与微信API的交互，包括授权登录、获取用户信息等
 * 
 * 作者: xiaolei
 */
@Service
public class WechatService {
    
    private static final Logger logger = LoggerFactory.getLogger(WechatService.class);
    
    @Value("${wechat.appid:}")
    private String appId;
    
    @Value("${wechat.secret:}")
    private String appSecret;
    
    @Value("${wechat.miniprogram.appid:}")
    private String miniAppId;
    
    @Value("${wechat.miniprogram.secret:}")
    private String miniAppSecret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过授权码获取access_token
     */
    public String getAccessToken(String code) {
        logger.info("获取微信access_token，授权码: {}", code);
        
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code
            );
            
            String response = restTemplate.getForObject(url, String.class);
            logger.debug("微信API响应: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.get("errmsg").asText();
                logger.error("获取access_token失败，错误码: {}, 错误信息: {}", errcode, errmsg);
                throw new RuntimeException("微信授权失败: " + errmsg);
            }
            
            String accessToken = jsonNode.get("access_token").asText();
            logger.info("获取access_token成功");
            return accessToken;
            
        } catch (Exception e) {
            logger.error("获取微信access_token异常，授权码: {}, 错误信息: {}", code, e.getMessage(), e);
            throw new RuntimeException("获取微信授权令牌失败", e);
        }
    }

    /**
     * 通过access_token获取用户信息
     */
    public WechatUserInfo getUserInfo(String accessToken, String openId) {
        logger.info("获取微信用户信息，openId: {}", openId);
        
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken, openId
            );
            
            String response = restTemplate.getForObject(url, String.class);
            logger.debug("微信用户信息API响应: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.get("errmsg").asText();
                logger.error("获取用户信息失败，错误码: {}, 错误信息: {}", errcode, errmsg);
                throw new RuntimeException("获取微信用户信息失败: " + errmsg);
            }
            
            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenId(jsonNode.get("openid").asText());
            userInfo.setNickname(jsonNode.get("nickname").asText());
            userInfo.setAvatarUrl(jsonNode.get("headimgurl").asText());
            userInfo.setUnionId(jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : null);
            
            logger.info("获取微信用户信息成功，昵称: {}", userInfo.getNickname());
            return userInfo;
            
        } catch (Exception e) {
            logger.error("获取微信用户信息异常，openId: {}, 错误信息: {}", openId, e.getMessage(), e);
            throw new RuntimeException("获取微信用户信息失败", e);
        }
    }

    /**
     * 通过授权码直接获取用户信息
     */
    public WechatUserInfo getUserInfoByCode(String code) {
        logger.info("通过授权码获取微信用户信息");
        
        try {
            // 获取access_token和openid
            String tokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code
            );
            
            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);
            JsonNode tokenNode = objectMapper.readTree(tokenResponse);
            
            if (tokenNode.has("errcode")) {
                int errcode = tokenNode.get("errcode").asInt();
                String errmsg = tokenNode.get("errmsg").asText();
                logger.error("获取access_token失败，错误码: {}, 错误信息: {}", errcode, errmsg);
                return null;
            }
            
            String accessToken = tokenNode.get("access_token").asText();
            String openId = tokenNode.get("openid").asText();
            
            // 获取用户信息
            return getUserInfo(accessToken, openId);
            
        } catch (Exception e) {
            logger.error("通过授权码获取微信用户信息异常，错误信息: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 小程序登录，获取session_key和openid
     */
    public Map<String, String> getMiniProgramSession(String code) {
        logger.info("小程序登录，获取session信息");
        
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                miniAppId, miniAppSecret, code
            );
            
            String response = restTemplate.getForObject(url, String.class);
            logger.debug("小程序登录API响应: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.get("errmsg").asText();
                logger.error("小程序登录失败，错误码: {}, 错误信息: {}", errcode, errmsg);
                return null;
            }
            
            Map<String, String> sessionInfo = new HashMap<>();
            sessionInfo.put("openid", jsonNode.get("openid").asText());
            sessionInfo.put("session_key", jsonNode.get("session_key").asText());
            if (jsonNode.has("unionid")) {
                sessionInfo.put("unionid", jsonNode.get("unionid").asText());
            }
            
            logger.info("小程序登录成功");
            return sessionInfo;
            
        } catch (Exception e) {
            logger.error("小程序登录异常，错误信息: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解密小程序用户信息
     */
    public WechatUserInfo getMiniProgramUserInfo(String code, String encryptedData, String iv) {
        logger.info("解密小程序用户信息");
        
        try {
            // 获取session信息
            Map<String, String> sessionInfo = getMiniProgramSession(code);
            if (sessionInfo == null) {
                logger.error("获取小程序session信息失败");
                return null;
            }
            
            String sessionKey = sessionInfo.get("session_key");
            String openId = sessionInfo.get("openid");
            String unionId = sessionInfo.get("unionid");
            
            // 如果有加密数据，进行解密
            if (encryptedData != null && iv != null && !encryptedData.isEmpty() && !iv.isEmpty()) {
                String decryptedData = decrypt(encryptedData, sessionKey, iv);
                JsonNode userNode = objectMapper.readTree(decryptedData);
                
                WechatUserInfo userInfo = new WechatUserInfo();
                userInfo.setOpenId(userNode.get("openId").asText());
                userInfo.setNickname(userNode.get("nickName").asText());
                userInfo.setAvatarUrl(userNode.get("avatarUrl").asText());
                userInfo.setUnionId(userNode.has("unionId") ? userNode.get("unionId").asText() : unionId);
                
                return userInfo;
            } else {
                // 没有加密数据，只返回基本信息
                WechatUserInfo userInfo = new WechatUserInfo();
                userInfo.setOpenId(openId);
                userInfo.setUnionId(unionId);
                userInfo.setNickname("微信用户");
                userInfo.setAvatarUrl("");
                
                return userInfo;
            }
            
        } catch (Exception e) {
            logger.error("解密小程序用户信息异常，错误信息: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES解密
     */
    private String decrypt(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedData);
            byte[] key = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
            
        } catch (Exception e) {
            logger.error("AES解密失败，错误信息: {}", e.getMessage(), e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 微信用户信息
     */
    public static class WechatUserInfo {
        private String openId;
        private String unionId;
        private String nickname;
        private String avatarUrl;

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getUnionId() {
            return unionId;
        }

        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        @Override
        public String toString() {
            return "WechatUserInfo{" +
                    "openId='" + openId + '\'' +
                    ", unionId='" + unionId + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", avatarUrl='" + avatarUrl + '\'' +
                    '}';
        }
    }
}