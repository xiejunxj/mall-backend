package mall.proto;

public class BuyLessonResponse {
    String paySign;
    String timeStamp;
    String packageVal;
    String nonceStr;

    int error;

    public String getPaySign() {
        return paySign;
    }

    public void setPaySign(String paySign) {
        this.paySign = paySign;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPackageVal() {
        return packageVal;
    }

    public void setPackageVal(String packageVal) {
        this.packageVal = packageVal;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }
}
