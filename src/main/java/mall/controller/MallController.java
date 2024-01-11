package mall.controller;
;
import mall.cache.BuyerCache;
import mall.models.*;
import mall.proto.*;
import mall.services.WxLoginResponse;
import mall.services.WxPayNotificateResponse;
import mall.services.WxPayServiceClient;
import mall.services.WxServiceClient;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static mall.cache.OrganizationInfoCache.OrgCacheEntity;

@RestController
@RequestMapping("/api/v1")
@Validated
@ComponentScan(basePackageClasses = {mall.services.WxServiceClient.class,
        mall.services.WxPayServiceClient.class, UserRepository.class})
public class MallController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository repository;

    private final WxServiceClient wxServiceClient;

    private final WxPayServiceClient wxPayServiceClient;

    private BuyerCache buyerCache;

    @Value("${mall.avatar-path}")
    private String avatarStorePath;

    @Value("${mall.server-base-url}")
    private String serverRootUrl;

    @Value("${mall.http-dir}")
    private String httpDir;

    @Value("${mall.rewardNum}")
    private int rewardNum;

    @Value("${mall.lessonPrice}")
    private int lessonPrice;

    public MallController(UserRepository repository, WxServiceClient wxServiceClient,
                          WxPayServiceClient wxPayServiceClient, BuyerCache buyerCache) {

        this.repository = repository;
        this.wxServiceClient = wxServiceClient;
        this.wxPayServiceClient = wxPayServiceClient;
        this.buyerCache = buyerCache;
        this.wxPayServiceClient.start();
        this.buyerCache.Start();
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

    public int getRewardNum() {
        return rewardNum;
    }

    public void setRewardNum(int rewardNum) {
        this.rewardNum = rewardNum;
    }

    public int getLessonPrice() {
        return lessonPrice;
    }

    public void setLessonPrice(int lessonPrice) {
        this.lessonPrice = lessonPrice;
    }

    private User getLoginUser(String openId, String sessionKey) {
        Optional<User> user = repository.findByOpenId(openId);
        logger.debug("Req open id {}", openId);
        if (!user.isPresent())
        {
            User my = new User();
            my.setOpenId(openId);
            my.setLoginSession(sessionKey);
            try {
                User ss = repository.save(my);
            } catch (DuplicateKeyException e) {
                logger.info("openId duplicate in login {} {}", e.getCause(), e.getMessage());
                return null;
            } catch (Exception e) {
                logger.error("Unnormal exception in login {} {}", e.getCause(), e.getMessage());
                return null;
            }
            return my;
        } else {
            return user.get();
        }
    }
    @GetMapping("/mallLogin")
    public ResponseEntity<Object> login(@RequestParam("openid") String openid) {
        WxLoginResponse rsp =  wxServiceClient.getWxLoginInfo(openid);
        if (rsp == null || (rsp.getErrmsg() != null && !rsp.getErrmsg().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            LoginResponse body = new LoginResponse();
//            List<BuyUserResponse> buyUsers = getBuyersInfo();
//            body.setBuyUserInfo(buyUsers);
            List<OrganizationInfo> orgs = getOrgInfo();
            body.setOrganizationInfos(orgs);
            User user = getLoginUser(rsp.getOpenid(), rsp.getSession_key());
            if (user == null) {
                return new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            body.setAvatarUrl(user.getAvatarUrl());
            body.setNickName(user.getNickName());
            body.setUserId(user.getOpenId());
            body.setLatitude(user.getLatitude());
            body.setLongitude(user.getLongitude());
            body.setPosName(user.getPosName());
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        }
    }

    public List<BuyUserResponse> getBuyersInfo() {
        List<BuyUserResponse> bodies = new ArrayList<>();
        Iterator<Map.Entry<String, Buyer>> iterator = this.buyerCache.getBuyerCacheEntity().entrySet().iterator();
        double money = this.getLessonPrice()/100.0;
        while(iterator.hasNext()){
            Map.Entry<String, Buyer> entry = iterator.next();
            BuyUserResponse rsp = new BuyUserResponse();
            rsp.setDate(entry.getValue().getDate());
            rsp.setMoney(money);
            rsp.setIconUrl(entry.getValue().getIconUrl());
            rsp.setUserName(entry.getValue().getUserName());
            bodies.add(rsp);
        }
        return bodies;
    }

    @GetMapping("/mallGetBuyers")
    public ResponseEntity<List<BuyUserResponse>> getBuyers() {
        List<BuyUserResponse> bodies = getBuyersInfo();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(bodies, headers, HttpStatus.OK);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/uploadFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadAvatar(@RequestParam(value = "avatarPath") MultipartFile file,
                                               @RequestParam(value = "uId") String uId) throws IOException {
        Optional<User> old = repository.findByOpenId(uId);
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", uId);
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            String oriName = file.getOriginalFilename();
            String extension = oriName.substring(oriName.lastIndexOf("."));
            String newName = this.avatarStorePath + "/" + uId + extension;
            FileUtils.writeByteArrayToFile(new File(newName), file.getBytes());
            UploadAvatarResponse body = new UploadAvatarResponse();
            body.setAvatarUrl(this.serverRootUrl + this.httpDir + "/" + uId + extension);
            old.get().setAvatarUrl(this.serverRootUrl + this.httpDir + "/" + uId + extension);
            repository.save(old.get());
            logger.info("Req success user {} avatarUrl {}", uId,
                    this.serverRootUrl + this.httpDir + "/" + uId + extension);
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mallUserUpdateNick")
    public ResponseEntity<Object> updateUserNick(@RequestBody UpdateUserRequest user) {
        Optional<User> old = repository.findByOpenId(user.getUserId());
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", user.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            if (!user.getNickName().isEmpty()) {
                old.get().setNickName(user.getNickName());
                repository.save(old.get());
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    @PostMapping("/mallUserUpdateLoc")
    public ResponseEntity<Object> updateUserLoc(@RequestBody UpdateUserRequest user) {
        Optional<User> old = repository.findByOpenId(user.getUserId());
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", user.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            old.get().setLongitude(user.getLongitude());
            old.get().setLatitude(user.getLatitude());
            old.get().setPosName(user.getPosName());
            repository.save(old.get());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    public List<OrganizationInfo> getOrgInfo() {
        List<OrganizationInfo> bodies = new ArrayList<>();
        Iterator<Map.Entry<Long, OrganizationInfo>> iterator = OrgCacheEntity.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Long, OrganizationInfo> entry = iterator.next();
            bodies.add(entry.getValue());
        }
        return bodies;
    }
    @RequestMapping("/getvalue")
    public ResponseEntity<Object> getValue() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        UploadAvatarResponse body = new UploadAvatarResponse();

        String url = this.serverRootUrl + this.httpDir ;
        logger.info("1111111111 {} {}", url, OrgCacheEntity.size());
        Long id = 1L;
        OrganizationInfo orgInfo = OrgCacheEntity.get(id);
        body.setAvatarUrl(orgInfo.getDesc());
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @PostMapping("/mallUserBuy")
    public ResponseEntity<Object> buyLessonLoc(@RequestBody BuyLessonRequest buyRequest) {
        Optional<User> old = repository.findByOpenId(buyRequest.getUserId());
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", buyRequest.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        BuyLessonResponse res = wxPayServiceClient.buyLesson(buyRequest);
        if (res == null) {
            res = new BuyLessonResponse();
            res.setError(-1);
        } else {
            res.setError(0);
        }
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
    }

    @PostMapping("/mallUserBuyNotify")
    public ResponseEntity buyLessonNotify(HttpServletRequest request) {
        WxPayNotificateResponse payInfo = this.wxPayServiceClient.payScoreCallbackNotification(request);
        if (payInfo == null) {
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        Optional<User> user = this.repository.findByOpenId(payInfo.getPayer().getOpenid());
        if (!user.isPresent()) {
            logger.error("buyLessonNotify user not exist {}", payInfo.getPayer().getOpenid());
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        user.get().setBuyTime(payInfo.getAttach().substring(0, 10));
        String originUser = "";
        if (payInfo.getAttach().length() > 11) {
            originUser = payInfo.getAttach().substring(11);
        }
        logger.info("User pay success openId {} originUser {}", user.get().getOpenId(), originUser);
        this.repository.save(user.get());
        if (!originUser.isEmpty()) {
            Optional<User> originUserInfo = this.repository.findByOpenId(originUser);
            if (!originUserInfo.isPresent()) {
                logger.error("buyLessonNotify user {} originalUser not exist {}", payInfo.getPayer().getOpenid(),
                        originUser);
            } else {
                int moneySum = originUserInfo.get().getMoneySum() + this.getRewardNum();
                originUserInfo.get().setMoneySum(moneySum);
                this.repository.save(originUserInfo.get());
            }
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/mallUserGetQrCode")
    public ResponseEntity<Object> getQrCode(@RequestBody GetQrcodeRequest user) {
        Optional<User> old = repository.findByOpenId(user.getUserId());
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", user.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            if (old.get().getQrUrl() == null)
            {
                GetQrCodeResponse body = new GetQrCodeResponse();
                body.setErrcode(0);
                body.setUrl(old.get().getQrUrl());
                return new ResponseEntity<>(body, headers, HttpStatus.OK);
            }
            GetQrCodeResponse body = wxServiceClient.getQrCode(user);
            if (body.getErrcode() == 0) {
                old.get().setQrUrl(body.getUrl());
                this.repository.save(old.get());
            }
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        }
    }

}
