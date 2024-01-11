package mall.cache;

import mall.models.Buyer;
import mall.models.BuyerRepository;
import mall.models.User;
import mall.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BuyerCache {
    /**
     * 键值对集合
     */

    private final Map<String, Buyer> buyerCacheEntity;

    @Autowired
    private final BuyerRepository repository;

    public Map<String, Buyer> getBuyerCacheEntity() {
        return buyerCacheEntity;
    }

    public BuyerCache(BuyerRepository repository) {
        this.repository = repository;
        this.buyerCacheEntity = new HashMap<>();
    }

    public void Start() {
        List<Buyer> buyerList =  this.repository.findAll();
        for (Buyer buyer : buyerList) {
            this.buyerCacheEntity.put(buyer.getOpenId(), buyer);
        }
    }
//        public UpdateBuyerCache(User user, String dateStr) {
//            Buyer buyer = new Buyer();
//            buyer.setIconUrl(user.getAvatarUrl());
//            buyer.setUserName(user.getNickName());
//            buyer.setDate(dateStr);
//            try {
//                this.repository.save(buyer);
//            } catch (Exception e) {
//
//            }
//        }

}
