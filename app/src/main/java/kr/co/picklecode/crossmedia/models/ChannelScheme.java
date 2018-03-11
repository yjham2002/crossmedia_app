package kr.co.picklecode.crossmedia.models;

/**
 * Created by HP on 2018-03-10.
 */

public class ChannelScheme {

    private int id;
    private String crawlUrl;
    private String title;
    private int order;

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

}
