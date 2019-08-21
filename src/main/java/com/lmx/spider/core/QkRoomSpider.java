package com.lmx.spider.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lmx.spider.core.persist.DBconsumer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 青客公寓全国房源
 *
 * @author: lucas
 * @create: 2019-08-21 10:53
 **/
public class QkRoomSpider implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(QkRoomSpider.class);

    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
            .setRetryTimes(0).setSleepTime(200);

    @Override
    public void process(Page page) {
        if (page.getUrl().regex("https://\\w+.qk365.com/list/\\w+").match()) {
            List<String> roomUrlList = page.getHtml().regex("https://\\w+.qk365.com/room/\\d+").all();
            if (CollectionUtils.isNotEmpty(roomUrlList)) {
                roomUrlList.forEach(roomUrl -> {
                    String url = page.getUrl().toString();
                    Request request = new Request();
                    request.setUrl(roomUrl);
                    Map ext = Maps.newHashMap();
                    ext.put("city", url.substring(url.lastIndexOf("//") + 2, url.indexOf(".")));
                    request.setExtras(ext);
                    page.addTargetRequest(request);
                });
            }
        } else {
            ResultItems resultItems = page.getResultItems();
            List<String> imageList = page.getHtml().xpath("//*[@id=\"box1\"]/ul/li/a").css("img", "src").all();
            String title = page.getHtml().xpath("/html/body/div[4]/div[1]/h1/text()").get();
            String price = page.getHtml().xpath("/html/body/div[4]/div[1]/dl/div[1]/dd[1]/text()").get();
            String area = page.getHtml().xpath("/html/body/div[4]/div[1]/dl/div[2]/dd[4]/a[1]/text()").get();
            String type = page.getHtml().xpath("/html/body/div[4]/div[1]/dl/div[1]/dd[2]/text()").get();
            String floor = page.getHtml().xpath("/html/body/div[4]/div[1]/dl/div[1]/dd[4]/text()").get();
            resultItems.put("title", title);
            resultItems.put("price", price.replaceAll("元/月", ""));
            resultItems.put("area", area);
            resultItems.put("type", type);
            resultItems.put("floor", floor);
            resultItems.put("imageList", imageList);
            resultItems.put("city", page.getRequest().getExtra("city"));
            resultItems.put("source", "青客公寓");
            logger.info("房间标题={}, 价格={}, 户型={}, 楼层={}, 区域={}, 图片={}",
                    title, price, type, floor, area, imageList);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        new Thread(() -> startSpider()).start();
        Executors.newScheduledThreadPool(1).schedule(() -> {
            logger.info("Scheduled spider task is start");
            startSpider();
        }, 1, TimeUnit.DAYS);
        DBconsumer.start();
    }

    static void startSpider() {
        List<String> urlList = Lists.newArrayList();
        for (int i = 1; i <= 1000; i++) {
            urlList.add("https://sh.qk365.com/list/p" + i);
            urlList.add("https://sz.qk365.com/list/p" + i);
            urlList.add("https://nj.qk365.com/list/p" + i);
            urlList.add("https://wh.qk365.com/list/p" + i);
            urlList.add("https://bj.qk365.com/list/p" + i);
            urlList.add("https://jx.qk365.com/list/p" + i);
            urlList.add("https://hz.qk365.com/list/p" + i);
        }
        Spider.create(new QkRoomSpider())
                .addUrl(urlList.toArray(new String[urlList.size()]))
                .addPipeline((resultItems, task) -> {
                    if (resultItems.get("title") != null)
                        DBconsumer.queue.offer(resultItems);
                }).thread(64).run();
    }
}
