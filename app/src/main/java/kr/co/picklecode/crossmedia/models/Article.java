package kr.co.picklecode.crossmedia.models;

/**
 * Created by HP on 2018-01-11.
 */

public class Article {

    private int id;
    private int type;
    private String title;
    private String content;
    private String repPath;
    private String imgPath;
    private String regDate;
    private String uptDate;

    public Article() {}

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
        return "Article{" +
                "id=" + id +
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
