package mall.proto;

import java.util.List;

public class LoginResponse {
    private String userId;
    private String avatarUrl;
    private String nickName;
    private double longitude;
    private double latitude;
    private String posName;
    private List<OrganizationInfo> organizationInfos;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public List<OrganizationInfo> getOrganizationInfos() {
        return organizationInfos;
    }

    public void setOrganizationInfos(List<OrganizationInfo> organizationInfos) {
        this.organizationInfos = organizationInfos;
    }
}
