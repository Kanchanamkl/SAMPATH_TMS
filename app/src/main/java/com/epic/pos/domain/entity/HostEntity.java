package com.epic.pos.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.epic.pos.data.db.dbpos.modal.Host;


public class HostEntity  extends CommonListEntity implements Parcelable{

    private boolean isSelected;
    private Host host;
    int hostId;;

    public HostEntity(Host host) {
        this.host = host;
    }

    public HostEntity(int hostId, String name) {
        this.hostId = hostId;
        this.name   = name;
    }

    public int getHostId() {
        return hostId;
    }
    protected HostEntity(Parcel in) {
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HostEntity> CREATOR = new Creator<HostEntity>() {
        @Override
        public HostEntity createFromParcel(Parcel in) {
            return new HostEntity(in);
        }

        @Override
        public HostEntity[] newArray(int size) {
            return new HostEntity[size];
        }
    };

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public Host getHost() {
        return host;
    }
}
