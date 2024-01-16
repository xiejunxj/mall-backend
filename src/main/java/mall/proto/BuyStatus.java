package mall.proto;

public enum BuyStatus {
    BUY_NONE(0), BUY_DONE(1);
    private int value;
    private BuyStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
