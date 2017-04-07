package com.qdcz.spider.plugin;


/**
 * @author: YuanYingqiu
 * date:    2017/4/7.
 */
public interface IPluginDownloadModel {
    boolean judge();

    void getResonpse(DownloadResponse response);

}
