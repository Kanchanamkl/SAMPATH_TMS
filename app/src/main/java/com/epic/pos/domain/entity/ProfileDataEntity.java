package com.epic.pos.domain.entity;

import java.util.HashMap;
import java.util.List;

public class ProfileDataEntity {
    private HashMap<String,String> configProfile;
    private List<HostEntry> mainHostTids;
    private List<HostEntry> sharedHostTids;

    public List<HostEntry> getMainHostTids() {
        return mainHostTids;
    }

    public List<HostEntry> getSharedHostTids() {
        return sharedHostTids;
    }

    public HashMap<String, String> getConfigProfile() {
        return configProfile;
    }
}
