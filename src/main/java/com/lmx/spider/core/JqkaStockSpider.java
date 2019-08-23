package com.lmx.spider.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lmx.spider.core.driver.ChromeDriverMgr;
import com.lmx.spider.core.persist.DBconsumer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 同花顺A股行情
 *
 * @author: lucas
 * @create: 2019-08-21 10:53
 **/
public class JqkaStockSpider implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(JqkaStockSpider.class);

    private Site site = Site.me().setRetryTimes(0).setSleepTime(1000);

    @Override
    public void process(Page page) {
        List<Map<String, String>> stockList = Lists.newArrayList();
        for (int i = 1; i <= 20; i++) {
            String code = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[2]/a/text()").get();
            String name = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[3]/a/text()").get();
            String price = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[4]/text()").get();
            String upDownRatio = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[5]/text()").get();
            String transferRatio = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[8]/text()").get();
            String dealAmount = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[11]/text()").get();
            String stockAmount = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[12]/text()").get();
            String stockHolding = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[13]/text()").get();
            String peRatio = page.getHtml().xpath("/html/body/table/tbody/tr[" + i + "]/td[13]/text()").get();
            Map<String, String> map = Maps.newHashMap();
            map.put("code", code);
            map.put("name", name);
            map.put("price", price);
            map.put("upDownRatio", upDownRatio);
            map.put("transferRatio", transferRatio);
            map.put("dealAmount", dealAmount);
            map.put("stockAmount", stockAmount);
            map.put("stockHolding", stockHolding);
            map.put("peRatio", peRatio);
            stockList.add(map);
        }
        page.putField("stockList", stockList);
        logger.info("{}", page.getResultItems());
    }

    @Override
    public Site getSite() {
        site.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        site.addHeader("Accept-Encoding", "gzip, deflate, br");
        site.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        site.addHeader("Cache-Control", "max-age=0");
        site.addHeader("Connection", "keep-alive");
        site.addHeader("Host", "q.10jqka.com.cn");
        site.addHeader("Cookie", "log=; Hm_lvt_78c58f01938e4d85eaf619eae71b4ed1=1566543009; __utma=156575163.1667051327.1566543013.1566543013.1566543013.1; __utmc=156575163; __utmz=156575163.1566543013.1.1.utmcsr=10jqka.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/; __utmb=156575163.1.10.1566543013; Hm_lpvt_78c58f01938e4d85eaf619eae71b4ed1=1566543115; historystock=600872; spversion=20130314; v=Apqjvv-hk8pMWR9mRdlZspaE60uoyx3ykECSc6QTRBr5WjT3jFtutWDf4ix3");
        site.addHeader("User-Agent:","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        return site;
    }

    public static void main(String[] args) {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            logger.info("Scheduled spider task is start");
            startSpider();
        }, 0, 1, TimeUnit.MINUTES);
        DBconsumer.start();
    }

    static void startSpider() {
        List<String> urlList = Lists.newArrayList();
        for (int i = 1; i <= 181; i++) {
            urlList.add("http://q.10jqka.com.cn/index/index/board/all/field/zdf/order/desc/page/" + i + "/ajax/1/");
        }
        Spider.create(new JqkaStockSpider())
                .addUrl(urlList.toArray(new String[urlList.size()]))
                /*.addPipeline(new FilePipeline("c:\\data\\stock"))*/.thread(1).run();
    }
}
