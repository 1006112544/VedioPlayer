package com.daobao.asus.vedioplayer.domain;

/**
 * Created by ASUS on 2017/6/28.
 * 一个视频或音频的类
 */

public class MedioItem {
    private String  name;
    private long duration;
    private long size;
    private String data;
    private String artist;
    private boolean IsMusic;

    public boolean isMusic() {
        return IsMusic;
    }

    public void setMusic(boolean music) {
        IsMusic = music;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "MedioItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
