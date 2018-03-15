package kr.co.picklecode.crossmedia.models;

/**
 * Created by HP on 2018-03-10.
 */

public class ChannelScheme {

    private int id;
    private String crawlUrl;
    private String title;
    private int order;
    private int cg_min;
    private int cg_max;
    private int cg_range;
    private int cg_cur;

    public int getCg_min() {
        return cg_min;
    }

    public void setCg_min(int cg_min) {
        this.cg_min = cg_min;
    }

    public int getCg_max() {
        return cg_max;
    }

    public void setCg_max(int cg_max) {
        this.cg_max = cg_max;
    }

    public int getCg_range() {
        return cg_range;
    }

    public void setCg_range(int cg_range) {
        this.cg_range = cg_range;
    }

    public int getCg_cur() {
        return cg_cur;
    }

    public void setCg_cur(int cg_cur) {
        this.cg_cur = cg_cur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCrawlUrl() {
        return crawlUrl;
    }

    public void setCrawlUrl(String crawlUrl) {
        this.crawlUrl = crawlUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "ChannelScheme{" +
                "id=" + id +
                ", crawlUrl='" + crawlUrl + '\'' +
                ", title='" + title + '\'' +
                ", order=" + order +
                ", cg_min=" + cg_min +
                ", cg_max=" + cg_max +
                ", cg_range=" + cg_range +
                ", cg_cur=" + cg_cur +
                '}';
    }

}
