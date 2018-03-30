package kr.co.picklecode.crossmedia.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-01-11.
 */

public class Article {

    private int id;
    private int type;
    private String title = "";
    private String content = "";
    private String imgPath = "";
    private int cg_max;
    private int cg_min;
    private int cg_range;
    private int cg_current;

    public int getCg_max() {
        return cg_max;
    }

    public void setCg_max(int cg_max) {
        this.cg_max = cg_max;
    }

    public int getCg_min() {
        return cg_min;
    }

    public void setCg_min(int cg_min) {
        this.cg_min = cg_min;
    }

    public int getCg_range() {
        return cg_range;
    }

    public void setCg_range(int cg_range) {
        this.cg_range = cg_range;
    }

    public int getCg_current() {
        return cg_current;
    }

    public void setCg_current(int cg_current) {
        this.cg_current = cg_current;
    }

    private List<MediaRaw> mediaRaws = new ArrayList<>();

    public Article() {}

    public List<MediaRaw> getMediaRaws() {
        return mediaRaws;
    }

    public void setMediaRaws(List<MediaRaw> mediaRaws) {
        this.mediaRaws = mediaRaws;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
