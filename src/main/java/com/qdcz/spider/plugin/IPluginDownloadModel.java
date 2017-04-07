package com.qdcz.spider.plugin;

import com.qdcz.spider.http.Response;

/**
 * @author: YuanYingqiu
 * date:    2017/4/7.
 */
public interface IPluginDownloadModel {
    boolean judge();
    void getResonpse(Response response);
}
