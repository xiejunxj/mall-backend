package mall.controller;

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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static mall.cache.OrganizationInfoCache.OrgCacheEntity;

@RestController
@RequestMapping("/api/v1")
@Validated
@ComponentScan(basePackageClasses = {mall.services.WxServiceClient.class,
        mall.services.WxPayServiceClient.class, UserRepository.class})
public class MallController{

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository repository;

    private final ProjectRepository projectRepository;

    private final WxServiceClient wxServiceClient;

    private final WxPayServiceClient wxPayServiceClient;

    private List<BuyUserResponse> buyers;

    @Value("${mall.projectId}")
    private int projectId;

    private Long projectUid;

    public Long getProjectUid() {
        return projectUid;
    }

    public void setProjectUid(Long projectUid) {
        this.projectUid = projectUid;
    }

    @Value("${mall.merchantId}")
    private String merchantId;

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

    private int scanNum;
    private int active;

    public MallController(UserRepository repository, ProjectRepository projectRepository, WxServiceClient wxServiceClient,
                          WxPayServiceClient wxPayServiceClient) {

        this.repository = repository;
        this.projectRepository = projectRepository;
        this.wxServiceClient = wxServiceClient;
        this.wxPayServiceClient = wxPayServiceClient;
        this.wxPayServiceClient.start();
        this.buyers = new ArrayList<>();
    }

    @PostConstruct
    public void Init() {
        Optional<Project> project = this.projectRepository.findByProjId(this.projectId);
        if (!project.isPresent()) {
            logger.error("ProjectId not in the table {}", this.projectId);
            System.exit(-1);
        }
        this.setScanNum(project.get().getScanNum());
        this.setActive(project.get().getValid());
        this.setProjectUid(project.get().getId());
        List<User> userList = this.repository.findByBuyStatus(BuyStatus.BUY_DONE.getValue());
        double money = this.lessonPrice/100.0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (User user : userList) {
            BuyUserResponse response = new BuyUserResponse();
            response.setUserName(user.getNickName());
            response.setMoney(money);
            response.setIconUrl(user.getAvatarUrl());
            long buyTime = Long.parseLong(user.getBuyTime());
            response.setDate(sdf.format(new Date(buyTime)));
            this.buyers.add(response);
        }
    }

    @PreDestroy
    public void preDestroy(){
        logger.info("PreDestroy>>>>");
        Optional<Project> project = this.projectRepository.findByProjId(this.projectId);
        if (project.isPresent()) {
            project.get().setScanNum(this.getScanNum());
            this.projectRepository.saveAndFlush(project.get());
        }
        logger.info("MallController auto save info when exit {}", this.getScanNum());
    }
    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getScanNum() {
        return scanNum;
    }

    public void setScanNum(int scanNum) {
        this.scanNum = scanNum;
    }

    public void AddScanNum() {
        this.scanNum += 1;
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

    @GetMapping("/mallLogin")
    public ResponseEntity<Object> login(@RequestParam("code") String code) {
        WxLoginResponse rsp =  wxServiceClient.getWxLoginInfo(code);
        if (rsp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            Optional<User> user = repository.findByOpenId(rsp.getOpenid());
            logger.debug("Req open id {}", rsp.getOpenid());
            if (!user.isPresent()) {
                User my = new User();
                my.setOpenId(rsp.getOpenid());
                my.setLoginSession(rsp.getSession_key());
                try {
                    User ss = repository.save(my);
                } catch (DuplicateKeyException e) {
                    logger.info("openId duplicate in login {} {}", e.getCause(), e.getMessage());
                } catch (Exception e) {
                    logger.error("Unnormal exception in login {} {}", e.getCause(), e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            LoginResponse body = new LoginResponse();
            body.setOpenId(rsp.getOpenid());
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        }
    }


    @GetMapping("/mallGetPageInfo")
    public ResponseEntity<GetPageInfoResponse> getetPageInfo(@RequestParam(value = "needOrg") int needOrg) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        GetPageInfoResponse body = new GetPageInfoResponse();
        if (!this.buyers.isEmpty()) {
            body.setBuyUserInfos(this.buyers);
        }
        body.setBuyerNum(this.buyers.size());
        if (needOrg == 1) {
            List<OrganizationInfo> orgs = getOrgInfo();
            body.setOrganizationInfos(orgs);
        }
        this.AddScanNum();
        body.setScanNum(this.getScanNum());
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
    /**
     * 上传文件
     *
     * @param file 文件
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/mallUpdateUser", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUser(@RequestParam(value = "avatarPath") MultipartFile file,
                                               @RequestParam(value = "uId") String uId,
                                               @RequestParam(value = "nickName") String nickName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        if (uId.isEmpty() || nickName.isEmpty()) {
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<User> user = this.repository.findByOpenId(uId);
            if (!user.isPresent()) {
                logger.error("Req updateUser fail user {} nickName {}", uId, nickName);
                return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
            }
            String oriName = file.getOriginalFilename();
            String extension = oriName.substring(oriName.lastIndexOf("."));
            String newName = this.avatarStorePath + "/" + uId + extension;
            FileUtils.writeByteArrayToFile(new File(newName), file.getBytes());
            UploadAvatarResponse body = new UploadAvatarResponse();
            String avatarUrl = this.serverRootUrl + this.httpDir + "/" + uId + extension;
            body.setAvatarUrl(avatarUrl);
            user.get().setAvatarUrl(avatarUrl);
            user.get().setNickName(nickName);
            this.repository.save(user.get());
            logger.info("Req updateUser success user {} avatarUrl {} nickName {}", uId,
                    avatarUrl, nickName);
            return new ResponseEntity<>(body, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
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

    @PostMapping("/mallUserBuy")
    public ResponseEntity<Object> buyLessonLoc(@RequestBody BuyLessonRequest buyRequest) {
        if (buyRequest.getLessons().isEmpty() || buyRequest.getChildAge() == 0 || buyRequest.getChildPhone().isEmpty()) {
            logger.warn("Request not valid {} {} {} {} {} {}", buyRequest.getUserId(),
                    buyRequest.getLessons(), buyRequest.getChildAge(), buyRequest.getChildName(),
                    buyRequest.getChildPhone(), buyRequest.getOriginUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Optional<User> old = repository.findByOpenId(buyRequest.getUserId());
        if (!old.isPresent()) {
            logger.warn("There is no such user but request buy {}", buyRequest.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        if (old.get().getBuyStatus() == BuyStatus.BUY_DONE.getValue()) {
            BuyLessonResponse res = new BuyLessonResponse();
            res.setError(1);
            return  new ResponseEntity<>(res, headers, HttpStatus.OK);
        }
        String tradeNo = String.valueOf(System.currentTimeMillis());
        BuyLessonResponse res = wxPayServiceClient.buyLesson(buyRequest, tradeNo);
        old.get().setWxBuyNonce(res.getNonceStr());
        if (res == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!buyRequest.getOriginUserId().isEmpty()) {
            Optional<User> originalUser = repository.findByOpenId(buyRequest.getOriginUserId());
            if (!originalUser.isPresent()) {
                logger.warn("There is no original user and skip {}", buyRequest.getOriginUserId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            old.get().setBuyOrigin(originalUser.get().getOpenId());
        }
        old.get().setWxBuyNonce(res.getNonceStr());
        old.get().setChildAge(buyRequest.getChildAge());
        old.get().setChildPhone(buyRequest.getChildPhone());
        old.get().setChildName(buyRequest.getChildName());
        old.get().setBuyLessons(buyRequest.getLessons());
        old.get().setBuyOrigin(buyRequest.getOriginUserId());
        old.get().setBuyTime(tradeNo);
        try {
            this.repository.save(old.get());
        } catch (Exception e) {
            logger.error("save user info to db fail {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        res.setError(0);
        logger.info("wx req order openid {} originUser {} lessons {} phone {} childName {} age {} tradeNo {} nonce {}" +
                        " package {} paysign {} stamp {}",
                buyRequest.getUserId(), buyRequest.getOriginUserId(), buyRequest.getLessons(),
                buyRequest.getChildPhone(), buyRequest.getChildName(), buyRequest.getChildAge(),
                tradeNo, res.getNonceStr(), res.getPackageVal(), res.getPaySign(), res.getTimeStamp());
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
    }

    @PostMapping("/mallUserBuyNotify")
    public ResponseEntity buyLessonNotify(HttpServletRequest request) {
        WxPayNotificateResponse payInfo = this.wxPayServiceClient.payScoreCallbackNotification(request);
        if (payInfo == null) {
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        if (!payInfo.getMchid().equals(this.merchantId)) {
            logger.warn("notify mchantId is not right {} {}", payInfo.getMchid(), this.merchantId);
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        Optional<User> user = this.repository.findByOpenId(payInfo.getPayer().getOpenid());
        if (!user.isPresent()) {
            logger.error("buyLessonNotify user not exist {}", payInfo.getPayer().getOpenid());
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        String[] attachStr = payInfo.getAttach().split(":");
        if (attachStr.length != 3) {
            logger.error("buyLessonNotify attachStr error {}", payInfo.print());
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        String lessons = attachStr[0];
        String originUser = attachStr[1];
        String phoneNumber = attachStr[2];
        if (!lessons.equals(user.get().getBuyLessons())
                || !phoneNumber.equals(user.get().getChildPhone())) {
            logger.warn("notify info is not same with req info rL {} L {} rP {}  p {}",
                    user.get().getBuyLessons(), lessons, user.get().getChildPhone(), phoneNumber);
        }
        try {
            if (!user.get().getBuyTime().equals(payInfo.getOut_trade_no())) {
                logger.error("there is no such trade no in this user openid {} tradeNo {} realTradeNo {} " +
                                "wx_transaction_id {} attachStr {} mchId {}",
                        user.get().getOpenId(), payInfo.getOut_trade_no(), user.get().getBuyTime(),
                        payInfo.getTransaction_id(), payInfo.getAttach(), payInfo.getMchid());
                return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
            }
            if (user.get().getBuyStatus() == BuyStatus.BUY_DONE.getValue()) {
                if (!user.get().getBuyLessons().equals(lessons) || user.get().getChildPhone().equals(phoneNumber)) {
                    logger.error("done buy with different lessons or phoneNumber. phoneNumber {} real phoneNumber {} " +
                            "lessons {} real lessons {} transaction_id {} tradeNo {} openid {}",
                            phoneNumber, user.get().getChildPhone(), lessons, user.get().getBuyLessons(),
                            payInfo.getTransaction_id(), payInfo.getOut_trade_no(), user.get().getOpenId());
                }
                return new ResponseEntity(HttpStatus.OK);
            }
            user.get().setWxTransactionId(payInfo.getTransaction_id());
            user.get().setBuyStatus(BuyStatus.BUY_DONE.getValue());
            this.repository.save(user.get());
            logger.info("User pay success openId {} originUser {} attach {}", user.get().getOpenId(), originUser,
                    payInfo.getAttach());
            try {
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
            } catch (Exception er) {
                logger.error("Need change by superuser. Original user {} fail to add money by user {} buy",
                        originUser, payInfo.getPayer().getOpenid());
            }
        } catch (Exception e) {
            logger.error("deal notify error {} {}", e.getMessage(), payInfo.print());
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
        BuyUserResponse buyUserResponse = new BuyUserResponse();
        buyUserResponse.setUserName(user.get().getNickName());
        buyUserResponse.setIconUrl(user.get().getAvatarUrl());
        double money = (double)this.lessonPrice/100.0;
        buyUserResponse.setMoney(money);
        long buyTime = Long.parseLong(payInfo.getOut_trade_no());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        buyUserResponse.setDate(sdf.format(new Date(buyTime)));
        this.buyers.add(buyUserResponse);
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
            if (old.get().getQrUrl() != null)
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

    @GetMapping("/mallUserGetMyOrder")
    public ResponseEntity<Object> getMyOrder(@RequestParam("userId") String userId) {
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Optional<User> old = repository.findByOpenId(userId);
        if (!old.isPresent()) {
            logger.warn("There is no such user but request update {}", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            GetMyOrder order = new GetMyOrder();
            if (old.get().getBuyTime() != null && !old.get().getBuyTime().isEmpty()) {
                double money = (double)this.lessonPrice/100.0;
                long orderTime = Long.parseLong(old.get().getBuyTime());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                order.setOrderTime(sdf.format(new Date(orderTime)));
                order.setMoney(String.valueOf(money));
            } else {
                order.setOrderTime("-");
                order.setMoney("-");
            }
            return new ResponseEntity<>(order, headers, HttpStatus.OK);
        }
    }

}
