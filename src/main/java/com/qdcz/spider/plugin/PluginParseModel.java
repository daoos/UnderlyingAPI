package com.qdcz.spider.plugin;


public interface PluginParseModel {
    boolean judge();

    void getResult(ParseResult parseResult);
}