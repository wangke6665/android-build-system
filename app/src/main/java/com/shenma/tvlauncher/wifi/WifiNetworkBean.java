package com.shenma.tvlauncher.wifi;

public class WifiNetworkBean {
    private String MACAddr;
    private String bssid;
    private String dns;
    private String dns2;
    private String gateway;
    private String ip;
    private int linkspeed;
    private int mRssi;
    private String mask;
    private String ssid;

    public int getmRssi() {
        return this.mRssi;
    }

    public void setmRssi(int mRssi) {
        this.mRssi = mRssi;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getLinkspeed() {
        return this.linkspeed;
    }

    public void setLinkspeed(int linkspeed) {
        this.linkspeed = linkspeed;
    }

    public String getMACAddr() {
        return this.MACAddr;
    }

    public void setMACAddr(String mACAddr) {
        this.MACAddr = mACAddr;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGateway() {
        return this.gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getDns() {
        return this.dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getDns2() {
        return this.dns2;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }
}
