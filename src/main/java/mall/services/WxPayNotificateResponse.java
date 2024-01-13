package mall.services;

public class WxPayNotificateResponse {
    String transaction_id;
    String mchid;

    String out_trade_no;
    public class Amount {
        Long payer_total;
        Long total;
        String currency;
        String payer_currency;

        public Long getPayer_total() {
            return payer_total;
        }

        public void setPayer_total(Long payer_total) {
            this.payer_total = payer_total;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getPayer_currency() {
            return payer_currency;
        }

        public void setPayer_currency(String payer_currency) {
            this.payer_currency = payer_currency;
        }
    }
    Amount amount;
    String trade_state;
    String trade_state_desc;
    String bank_type;
    String attach;
    String success_time;

    Payer payer;
    public class Payer {
        String openid;

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getTrade_state() {
        return trade_state;
    }

    public void setTrade_state(String trade_state) {
        this.trade_state = trade_state;
    }

    public String getTrade_state_desc() {
        return trade_state_desc;
    }

    public void setTrade_state_desc(String trade_state_desc) {
        this.trade_state_desc = trade_state_desc;
    }

    public String getBank_type() {
        return bank_type;
    }

    public void setBank_type(String bank_type) {
        this.bank_type = bank_type;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }


    public String getSuccess_time() {
        return success_time;
    }

    public void setSuccess_time(String success_time) {
        this.success_time = success_time;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String print() {
        String s = "trade_state: " + this.getTrade_state() + ", trade_state_desc: " + this.getTrade_state_desc()
                + ", transaction_id: " + this.getTransaction_id() + ", attach: " + this.getAttach()
                + ", mchId: " + this.getMchid() + ", bank_type: " + this.getBank_type()
                + ", success_time: " + this.getSuccess_time() + ", open_id: " + this.getPayer().getOpenid()
                + ", Amount: total " + this.getAmount().getTotal() + " " + this.getAmount().getCurrency()
                + ", payer_total: " + this.getAmount().getPayer_total() + " " + this.getAmount().getPayer_currency()
                + ", out_trade_no: " + this.getOut_trade_no();
        return s;
    }
}


