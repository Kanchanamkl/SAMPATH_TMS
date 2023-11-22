package com.epic.pos.ui.settings.tms;

import com.epic.pos.tms.DebitKeyDetails;

import java.util.ArrayList;

public class DebitKey {
    private ArrayList<DebitKeyDetails> issuerKeyProfiles = new ArrayList();

    public DebitKey() {
    }

    public ArrayList<DebitKeyDetails> getIssuerKeyProfiles() {
        return this.issuerKeyProfiles;
    }

    public void setIssuerKeyProfiles(ArrayList<DebitKeyDetails> issuerKeyProfiles) {
        this.issuerKeyProfiles = issuerKeyProfiles;
    }
}
