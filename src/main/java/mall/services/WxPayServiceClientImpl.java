package mall.services;


import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import mall.proto.BuyLessonRequest;
import mall.proto.BuyLessonResponse;
import mall.utils.ApiV3Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
public class WxPayServiceClientImpl implements WxPayServiceClient {
    @Value("${mall.merchantId}")
    private String merchantId;

    @Value("${mall.payApiClientKey}")
    private String privateKeyPath;

    @Value("${mall.merchantSerial}")
    private String merchantSerialNumber;

    @Value("${mall.apiV3Key}")
    private String apiV3Key;

    @Value("${mall.appid}")
    private String appId;

    @Value("${mall.lessonPrice}")
    private int lessonPrice;

    @Value("${mall.payDesc}")
    private String payDesc;

    @Value("${mall.notifyUrl}")
    private String notifyUrl;

    private JsapiServiceExtension service;
    public WxPayServiceClientImpl() {
    }

    @Override
    public void start() {
        Config config = new RSAAutoCertificateConfig.Builder().
                merchantId(this.merchantId).privateKeyFromPath(this.privateKeyPath)
                .merchantSerialNumber(this.merchantSerialNumber)
                .apiV3Key(this.apiV3Key)
                .build();
        this.service = new JsapiServiceExtension.Builder()
                .config(config)
                .signType("RSA") // 不填默认为RSA
                .build();
    }
    private Logger logger = LoggerFactory.getLogger(getClass());

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getMerchantSerialNumber() {
        return merchantSerialNumber;
    }

    public void setMerchantSerialNumber(String merchantSerialNumber) {
        this.merchantSerialNumber = merchantSerialNumber;
    }

    public String getApiV3Key() {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getLessonPrice() {
        return lessonPrice;
    }

    public void setLessonPrice(int lessonPrice) {
        this.lessonPrice = lessonPrice;
    }

    public String getPayDesc() {
        return payDesc;
    }

    public void setPayDesc(String payDesc) {
        this.payDesc = payDesc;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    private PrepayWithRequestPaymentResponse prepayWithRequestPayment(BuyLessonRequest buyLessonRequest) {
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(this.lessonPrice);
        request.setAmount(amount);
        request.setAppid(this.appId);
        request.setMchid(this.merchantId);
        request.setDescription(this.payDesc);
        request.setNotifyUrl(this.notifyUrl);
        String secStr = String.valueOf(System.currentTimeMillis()/1000);
        request.setOutTradeNo(secStr);
        request.setAttach(secStr + ":" + buyLessonRequest.getOriginUserId());
        Payer payer = new Payer();
        payer.setOpenid(buyLessonRequest.getUserId());
        request.setPayer(payer);
        return service.prepayWithRequestPayment(request);
    }

    @Override
    public BuyLessonResponse buyLesson(BuyLessonRequest buyLessonRequest) {
        try {
            PrepayWithRequestPaymentResponse response = prepayWithRequestPayment(buyLessonRequest);
            BuyLessonResponse res = new BuyLessonResponse();
            res.setPaySign(response.getPaySign());
            res.setTimeStamp(response.getTimeStamp());
            res.setNonceStr(response.getNonceStr());
            res.setPackageVal(response.getPackageVal());
            return res;
        } catch (HttpException e) { // 发送HTTP请求失败
            logger.warn("send api fail 1, {} {}", e.getMessage(), e.getCause());
            // 调用e.getHttpRequest()获取请求打印日志或上报监控，更多方法见HttpException定义
        } catch (ServiceException e) { // 服务返回状态小于200或大于等于300，例如500
            logger.warn("send api fail 2, {} {}", e.getMessage(), e.getCause());
            // 调用e.getResponseBody()获取返回体打印日志或上报监控，更多方法见ServiceException定义
        } catch (MalformedMessageException e) { // 服务返回成功，返回体类型不合法，或者解析返回体失败
            logger.warn("send api fail 3, {} {}", e.getMessage(), e.getCause());
            // 调用e.getMessage()获取信息打印日志或上报监控，更多方法见MalformedMessageException定义
        }
        return null;
    }

    @Override
    public WxPayNotificateResponse payScoreCallbackNotification(HttpServletRequest request) {
        try {
            ServletInputStream servletInputStream = request.getInputStream();
            int contentLength = request.getContentLength();
            byte[] callBackInBytes = new byte[contentLength];
            servletInputStream.read(callBackInBytes, 0, contentLength);
            String callBackIn = new String(callBackInBytes, "UTF-8");
            // 模拟确认订单回调通知API
//            String callBackIn = "{\"id\":\"123\",\"create_time\":\"2020-11-02T16:31:35+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"PAYSCORE.USER_CONFIRM\",\"summary\":\"微信支付分服务订单用户已确认\",\"resource\":{\"original_type\":\"payscore\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"1111111111==\",\"associated_data\":\"payscore\",\"nonce\":\"12321321\"}}";
            // 模拟支付成功回调通知API
//            String callBackIn = "{\"id\":\"123\",\"create_time\":\"2020-11-02T16:31:35+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"PAYSCORE.USER_PAID\",\"summary\":\"微信支付分服务订单支付成功\",\"resource\":{\"original_type\":\"payscore\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"1111111111==\",\"associated_data\":\"payscore\",\"nonce\":\"12321321\"}}";

            logger.info("【微信支付分免密支付回调】：" + callBackIn);

            JSONObject notifyIn = JSONObject.parseObject(callBackIn);
            if (notifyIn == null) {
                logger.error("参数不正确，反序列化失败");
                return null;
            }

            //解密回调信息
            JSONObject resource = notifyIn.getJSONObject("resource");
            byte[] key = (this.apiV3Key).getBytes("UTF-8");
            ApiV3Util aesUtil = new ApiV3Util(key);
            String decryptToString = aesUtil.decryptToString(resource.getString("associated_data").getBytes("UTF-8"), resource.getString("nonce").getBytes("UTF-8"), resource.getString("ciphertext"));

            if (StringUtils.isEmpty(decryptToString)) {
                return null;
            }
            logger.info("【支付分支付回调解密结果：】" + decryptToString);
            WxPayNotificateResponse payIn = JSONObject.parseObject(decryptToString, WxPayNotificateResponse.class);
            if (payIn == null) {
                logger.error("参数不正确，反序列化失败");
                return null;
            }
            if (payIn.getTrade_state() == "SUCCESS") {
                logger.info(payIn.print());
                return payIn;
            }
            return null;
        } catch (Exception e) {
            logger.error("微信支付回调处理异常，" + e.toString());
            return null;
        }
    }
}