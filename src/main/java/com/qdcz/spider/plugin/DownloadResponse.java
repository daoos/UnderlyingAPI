package com.qdcz.spider.plugin;

/**
 * @author: YuanYingqiu
 * date:    2017/4/7.
 */
public class DownloadResponse {
    private String url;
    private int statusCode;
    private boolean urlMatched;
    private boolean isSucceed;
    private byte[] htmlContent;
    private String charset;
    private String searchWord;
    private int page;//需要下载的页码
    private int totalPage;//所有的页码
    private boolean toSearch;//是否要去搜索
    private boolean toTurnPage;



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isUrlMatched() {
        return urlMatched;
    }

    public void setUrlMatched(boolean urlMatched) {
        this.urlMatched = urlMatched;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public void setSucceed(boolean succeed) {
        isSucceed = succeed;
    }

    public byte[] getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(byte[] htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }


    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public boolean isToSearch() {
        return toSearch;
    }

    public void setToSearch(boolean toSearch) {
        this.toSearch = toSearch;
    }


    public boolean isToTurnPage() {
        return toTurnPage;
    }

    public void setToTurnPage(boolean toTurnPage) {
        this.toTurnPage = toTurnPage;
    }
}
