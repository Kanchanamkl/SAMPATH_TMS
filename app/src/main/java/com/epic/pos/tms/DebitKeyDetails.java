package com.epic.pos.tms;

import java.util.ArrayList;

public class DebitKeyDetails {
    private String hostName;
    private ArrayList<String> hostKeys = new ArrayList();
    //private String hostKey1;
    //private String hostKey2;

    public DebitKeyDetails() {
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public ArrayList<String> getHostKeys() {
        return this.hostKeys;
    }

    public void setHostKey1(ArrayList<String> hostKeys) {
        this.hostKeys = hostKeys;
    }
}
