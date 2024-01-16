package mall.services;

import mall.proto.BuyLessonRequest;
import mall.proto.BuyLessonResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface WxPayServiceClient {
    BuyLessonResponse buyLesson(BuyLessonRequest buyLessonRequest, String tradeNo);
    WxPayNotificateResponse payScoreCallbackNotification(HttpServletRequest request);
    void start();
}
