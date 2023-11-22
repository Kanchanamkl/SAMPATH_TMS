package com.epic.pos.ui.common.merchant;

public enum MerchantType {

    ALL(0), SALE_SUPPORT_MERCHANTS(1), INSTALLMENT_SUPPORT_MERCHANTS(2);

    public int val;

    MerchantType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static MerchantType valueOf(int val){
        if (val == SALE_SUPPORT_MERCHANTS.getVal()){
            return SALE_SUPPORT_MERCHANTS;
        }else if (val == INSTALLMENT_SUPPORT_MERCHANTS.getVal()){
            return INSTALLMENT_SUPPORT_MERCHANTS;
        }else {
            return ALL;
        }
    }
}
