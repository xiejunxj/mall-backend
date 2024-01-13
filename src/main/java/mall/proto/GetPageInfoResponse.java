package mall.proto;

import java.util.List;

public class GetPageInfoResponse {
    private List<OrganizationInfo> organizationInfos;

    private List<BuyUserResponse> buyUserInfos;

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
}
