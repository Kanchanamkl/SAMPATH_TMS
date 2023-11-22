package com.epic.pos.domain.entity;

import com.epic.pos.data.db.dbpos.modal.Merchant;

public class MerchantEntity{

    private boolean isSelected;
    private Merchant merchant;

    public MerchantEntity(Merchant merchant) {
        this.merchant = merchant;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Merchant getMerchant() {
        return merchant;
    }
}
