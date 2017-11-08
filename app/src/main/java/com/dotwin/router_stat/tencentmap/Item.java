package com.dotwin.router_stat.tencentmap;

import com.dotwin.router_stat.R;

/**
 * Created by ff135 on 2017/9/21.
 */

public class Item {
    private int tvAddr= R.drawable.addr;
    private int tvgou;
    private String name;
    private String addr;
    private double lon;
    private double lat;
    private String cityName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return "Item{" +
                "tvAddr=" + tvAddr +
                ", tvgou=" + tvgou +
                ", name='" + name + '\'' +
                ", addr='" + addr + '\'' +
                ", lon=" + lon +
                ", lat=" + lat +
                ", cityName='" + cityName + '\'' +
                '}';
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getTvAddr() {
        return tvAddr;
    }

    public void setTvAddr(int tvAddr) {
        this.tvAddr = tvAddr;
    }

    public int getTvgou() {
        return tvgou;
    }

    public void setTvgou(int tvgou) {
        this.tvgou = tvgou;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
