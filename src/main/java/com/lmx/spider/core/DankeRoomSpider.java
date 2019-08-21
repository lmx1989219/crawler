package com.lmx.spider.core;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.lmx.spider.core.persist.DBconsumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 蛋壳公寓全国房源爬取
 *
 * @author: lucas
 * @create: 2019-08-20 13:13
 **/
public class DankeRoomSpider implements PageProcessor {
    private static Logger logger = LoggerFactory.getLogger(DankeRoomSpider.class);
    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
            .setRetryTimes(0).setSleepTime(100);

    @Override
    public void process(Page page) {
        if (page.getUrl().regex("https://www.danke.com/room/(\\w+\\?page=\\d+)").match()) {
            List<String> roomUrlList = page.getHtml().regex("https://www.danke.com/room/(\\w+\\.html)").all();
            if (CollectionUtils.isNotEmpty(roomUrlList)) {
                roomUrlList.forEach(roomUrl -> {
                    String url = page.getUrl().toString();
                    Request request = new Request();
                    request.setUrl("https://www.danke.com/room/" + roomUrl);
                    Map ext = Maps.newHashMap();
                    ext.put("city", url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")));
                    request.setExtras(ext);
                    page.addTargetRequest(request);
                });
            }
        } else {
            ResultItems resultItems = page.getResultItems();
            List<String> imageList = page.getHtml().xpath("//*[@id=\"myCarousel\"]/div/div").css("img", "src").all();
            String title = page.getHtml().xpath("/html/body/div[3]/div[1]/div[2]/div[2]/div[1]/h1/text()").get();
            String price = page.getHtml().xpath("/html/body/div[3]/div[1]/div[2]/div[2]/div[3]/div[2]/div/span/div/text()").get();
            String area = page.getHtml().xpath("/html/body/div[3]/div[1]/div[2]/div[2]/div[4]/div[2]/div[3]/label/div/a[1]/text()").get();
            String type = page.getHtml().xpath("/html/body/div[3]/div[1]/div[2]/div[2]/div[4]/div[1]/div[3]/label/text()").get();
            String floor = page.getHtml().xpath("/html/body/div[3]/div[1]/div[2]/div[2]/div[4]/div[2]/div[2]/label/text()").get();
            resultItems.put("title", title);
            resultItems.put("price", price);
            resultItems.put("area", area);
            resultItems.put("type", type.replaceAll("户型：", ""));
            resultItems.put("floor", floor.replaceAll("楼层：", ""));
            resultItems.put("imageList", imageList);
            resultItems.put("city", page.getRequest().getExtra("city"));
            resultItems.put("source", "蛋壳公寓");
            logger.info("房间标题={}, 价格={}, 户型={}, 楼层={}, 区域={}, 图片={}",
                    title, price, type.replaceAll("户型：", ""), floor.replaceAll("楼层：", ""), area, imageList);
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
        try (InputStream inputStream = HttpClients.createDefault()
                .execute(new HttpGet("https://www.danke.com/web-api/base-configure/city-list"))
                .getEntity().getContent()) {
            String jsonStr = CharStreams.toString(new InputStreamReader(inputStream));
            List<City> cityList = JSONObject.parseArray(jsonStr).toJavaList(City.class);
            List<String> cityCodeList = cityList.stream().map(City::getCode).collect(Collectors.toList());
            cityCodeList = Lists.reverse(cityCodeList);
            //初始化待爬取城市的房源数据
            cityCodeList.forEach(cityCode -> {
                for (int i = 1; i <= 1000; i++) {
                    urlList.add("https://www.danke.com/room/" + cityCode + "?page=" + i);
                }
            });
        } catch (IOException e) {
            logger.error("", e);
        }
        Spider.create(new DankeRoomSpider())
                .addUrl(urlList.toArray(new String[urlList.size()]))
                .addPipeline((resultItems, task) -> {
                    if (resultItems.get("title") != null)
                        DBconsumer.queue.offer(resultItems);
                }).thread(32).run();
    }

    static class City {
        private String code, name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
