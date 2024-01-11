package mall.services;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import mall.proto.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class WxServiceClientImpl implements WxServiceClient{
    private final WebClient webClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mall.appid}")
    private String appid;

    @Value("${mall.secretkey}")
    private String secretkey;

    @Value("${mall.avatar-path}")
    private String avatarStorePath;

    @Value("${mall.server-base-url}")
    private String serverRootUrl;

    @Value("${mall.http-dir}")
    private String httpDir;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public String getAvatarStorePath() {
        return avatarStorePath;
    }

    public void setAvatarStorePath(String avatarStorePath) {
        this.avatarStorePath = avatarStorePath;
    }

    public String getServerRootUrl() {
        return serverRootUrl;
    }

    public void setServerRootUrl(String serverRootUrl) {
        this.serverRootUrl = serverRootUrl;
    }

    public String getHttpDir() {
        return httpDir;
    }

    public void setHttpDir(String httpDir) {
        this.httpDir = httpDir;
    }
    public WxServiceClientImpl(WebClient.Builder webClientBuilder) {
          this.webClient = webClientBuilder.baseUrl("https://api.weixin.qq.com/")
                  .build();}
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public WxLoginResponse getWxLoginInfo(String openId) {
        String url = "/sns/jscode2session";
        MultiValueMap<String, String> body;
        logger.info("getLoginInfo {}", openId);
        try {
            String rspStr = webClient.get().
                    uri(uriBuilder -> uriBuilder.path(url)
                            .queryParam("appid", this.appid)
                            .queryParam("secret", this.secretkey)
                            .queryParam("js_code", openId)
                            .queryParam("grant_type", "authorization_code").build())
                    .header("Content-Type", "application/json;charset=utf-8")
                    .retrieve().bodyToMono(String.class).timeout(Duration.ofSeconds(3)).block();
            WxLoginResponse rsp = objectMapper.readValue(rspStr, WxLoginResponse.class);
            logger.info("Request wx success {} {} {} {} {}",rsp.getErrcode(), rsp.getErrmsg(), rsp.getSession_key(),
                    rsp.getUnionid(), rsp.getOpenid());
            return rsp;
        } catch (Exception err) {
            logger.info("Request wx error with openid {} err {}", openId, err.getMessage());
        }
        return null;
    }


    private String getAccessToken(){
        String accessToken = null;
        String token_url = "/cgi-bin/token";
        try {
            String rspStr = webClient.get().
                    uri(uriBuilder -> uriBuilder.path(token_url)
                            .queryParam("appid", this.appid)
                            .queryParam("secret", this.secretkey)
                            .queryParam("grant_type", "client_credential").build())
                    .header("Content-Type", "application/json;charset=utf-8")
                    .retrieve().bodyToMono(String.class).timeout(Duration.ofSeconds(3)).block();
            logger.info("Request wx token str {}",rspStr);
            JSONObject token = JSON.parseObject(rspStr);
            accessToken = token.getString("access_token");
            if (StringUtils.isEmpty(accessToken)) {
                logger.error("Request wx token fail");
                return null;
            }
            return accessToken;
        } catch (Exception err) {
            logger.info("Request wx token error with err {}", err.getMessage());
        }
        return null;
    }
    public GetQrCodeResponse getQrCode(GetQrcodeRequest request) {
        GetQrCodeResponse rspQr = new GetQrCodeResponse();
        String accessTokenStr = getAccessToken();
        if (accessTokenStr == null) {
            rspQr.setErrcode(-1);
            return rspQr;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            restTemplate.setRequestFactory(requestFactory);
            Map<String, Object> param = new HashMap<>(3);
            param.put("scene", request.getUserId());
            param.put("page", "pages/index/index");
            param.put("width", 247);
            String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessTokenStr;
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(url, JSON.toJSONString(param), byte[].class);
            byte[] qrBytes = responseEntity.getBody();
            String path = this.getAvatarStorePath() + "/qr_" +  request.getUserId() + ".png";
            FileUtils.writeByteArrayToFile(new File(path), qrBytes);
            rspQr.setUrl(this.serverRootUrl + this.httpDir + "/qr_" + request.getUserId() + ".png");
        } catch (Exception e) {
            rspQr.setErrcode(-1);
            logger.error("req QrCode error {} {}", request.getUserId(), e.getMessage());
        }
        return rspQr;
    }
}
