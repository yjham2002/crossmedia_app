package kr.co.picklecode.crossmedia.models;

/**
 * Created by HP on 2018-03-30.
 */

public class MediaRaw {
    private Article parent;
    private int cg_id;
    private int ch_id;
    private int type;
    private String title = "";
    private String content = "";
    private String repPath = "";
    private String imgPath = "";
    private String regDate = "";
    private String uptDate = "";

    public MediaRaw() {}

    public Article getParent() {
        return parent;
    }

    public void setParent(Article parent) {
        this.parent = parent;
    }

    public int getCg_id() {
        return cg_id;
    }

    public void setCg_id(int cg_id) {
        this.cg_id = cg_id;
    }

    public int getCh_id() {
        return ch_id;
    }

    public void setCh_id(int ch_id) {
        this.ch_id = ch_id;
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

    public String getRepPath() {
        return repPath;
    }

    public void setRepPath(String repPath) {
        this.repPath = repPath;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getUptDate() {
        return uptDate;
    }

    public void setUptDate(String uptDate) {
        this.uptDate = uptDate;
    }

    @Override
    public String toString() {
        return "MediaRaw{" +
                "cg_id=" + cg_id +
                ", ch_id=" + ch_id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", repPath='" + repPath + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", regDate='" + regDate + '\'' +
                ", uptDate='" + uptDate + '\'' +
                '}';
    }
}
