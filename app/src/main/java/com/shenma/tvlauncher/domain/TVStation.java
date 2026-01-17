package com.shenma.tvlauncher.domain;

import java.util.List;

public class TVStation {
    private String code;
    private List<TVStationInfo> data;
    private String msg;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<TVStationInfo> getData() {
        return this.data;
    }

    public void setData(List<TVStationInfo> data) {
        this.data = data;
    }
}
