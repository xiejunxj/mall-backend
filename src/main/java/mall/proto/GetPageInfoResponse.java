package mall.proto;

import java.util.List;

public class GetPageInfoResponse {
    private List<OrganizationInfo> organizationInfos;

    private List<BuyUserResponse> buyUserInfos;

    private int scanNum;

    private int buyerNum;

    public List<OrganizationInfo> getOrganizationInfos() {
        return organizationInfos;
    }

    public void setOrganizationInfos(List<OrganizationInfo> organizationInfos) {
        this.organizationInfos = organizationInfos;
    }

    public List<BuyUserResponse> getBuyUserInfos() {
        return buyUserInfos;
    }

    public void setBuyUserInfos(List<BuyUserResponse> buyUserInfos) {
        this.buyUserInfos = buyUserInfos;
    }

    public int getScanNum() {
        return scanNum;
    }

    public void setScanNum(int scanNum) {
        this.scanNum = scanNum;
    }

    public int getBuyerNum() {
        return buyerNum;
    }

    public void setBuyerNum(int buyerNum) {
        this.buyerNum = buyerNum;
    }
}
