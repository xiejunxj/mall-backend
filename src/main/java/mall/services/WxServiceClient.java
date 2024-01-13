package mall.services;

import mall.proto.GetQrCodeResponse;
import mall.proto.GetQrcodeRequest;

public interface WxServiceClient {
    WxLoginResponse getWxLoginInfo(String code);
    GetQrCodeResponse getQrCode(GetQrcodeRequest request);
}
