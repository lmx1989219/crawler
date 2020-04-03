package com.lmx.spider.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 同花顺A股行情
 *
 * @author: lucas
 * @create: 2019-08-21 10:53
 **/
public class TianyanSpider implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(TianyanSpider.class);

    private Site site = Site.me().setRetryTimes(0).setSleepTime(1000);

    @Override
    public void process(Page page) {
        if (page.getUrl().regex("https://www.tianyancha.com/search\\?key=(\\.*)").match()) {
            String entId = page.getHtml()
                    .css("#web-content > div > div.container-left > div.search-block.header-block-container > div.result-list.sv-search-container > div:nth-child(1) > div.search-result-single", "data-id").get();
            if (entId != null) {
                logger.info("entId={}", entId);
                page.addTargetRequest(new Request("https://www.tianyancha.com/company/" + entId));
            }
        } else {
            String entName = page.getHtml().css("#company_web_top > div.box.-company-box.-company-claimed > div.content > div.header > h1").get();
            String owner = page.getHtml().css("#_container_baseInfo > table:nth-child(1) > tbody > tr:nth-child(1) > td.left-col.shadow > div > div:nth-child(1) > div.humancompany > div.name > a", "title").get();
            logger.info("entName={},owner={}", entName, owner);
        }
    }

    @Override
    public Site getSite() {
        site.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        site.addHeader("Accept-Encoding", "gzip, deflate, br");
        site.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        site.addHeader("Cache-Control", "max-age=0");
        site.addHeader("Connection", "keep-alive");
        site.addHeader("Host", "www.tianyancha.com");
        site.addHeader("User-Agent:", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");

        return site;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            startSpider();
        }
    }

    static ExecutorService es = Executors.newFixedThreadPool(2);

    static void startSpider() {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("10.1.1.1", 1999)));
        Spider.create(new TianyanSpider()).addUrl("https://www.tianyancha.com/search?key=%E5%9B%BD%E6%A7%90%E9%87%91%E8%9E%8D")
                .setDownloader(httpClientDownloader).thread(es, 2).run();
    }
}
