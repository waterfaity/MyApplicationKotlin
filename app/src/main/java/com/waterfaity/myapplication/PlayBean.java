package com.waterfaity.myapplication;

public class PlayBean implements MusicPlayService.MusicBean {

    public PlayBean(String path) {
        this.path = path;
    }

    private String path;
    @Override
    public String getMp3UrlOrPath() {
        return path;
    }
}
