package com.lmx.spider.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 一句话描述一下
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
            page.addTargetRequests(page.getHtml().regex("https://www.danke.com/room/(\\w+\\.html)").all());
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
            logger.info("房间标题={}, 价格={}, 户型={}, 楼层={}, 区域={}, 图片={}",
                    title, price, type.replaceAll("户型：", ""), floor.replaceAll("楼层：", ""), area, imageList);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    static ArrayBlockingQueue<ResultItems> queue = new ArrayBlockingQueue<>(1024);

    static Thread dbTask = new Thread(() -> {
        logger.info("dbTask is start");
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager
                    .getConnection("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            try {
                ResultItems resultItems = queue.take();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id from room where title=?");
                String title = resultItems.get("title");
                preparedStatement.setString(1, title);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    preparedStatement = connection.prepareStatement(
                            "update room set price=?,image_list=?,update_time=? where id=" + rs.getLong(1));
                    preparedStatement.setString(1, resultItems.get("price"));
                    preparedStatement.setString(2, Joiner.on(",").join((List) resultItems.get("imageList")));
                    preparedStatement.setString(3, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    rs.close();
                } else {
                    preparedStatement = connection.prepareStatement(
                            "insert into room(title,price,area,type,floor,image_list,create_time,village,subway,source) values(?,?,?,?,?,?,?,?,?,?)");
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, resultItems.get("price"));
                    preparedStatement.setString(3, resultItems.get("area"));
                    preparedStatement.setString(4, resultItems.get("type"));
                    preparedStatement.setString(5, resultItems.get("floor"));
                    preparedStatement.setString(6, Joiner.on(",").join((List) resultItems.get("imageList")));
                    preparedStatement.setString(7, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    preparedStatement.setString(8, title.split(" ")[1]);
                    preparedStatement.setString(9, title.split(" ")[0]);
                    preparedStatement.setString(10, "蛋壳公寓");
                    preparedStatement.execute();
                    preparedStatement.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public static void main(String[] args) {
        new Thread(() -> startSpider()).start();
        Executors.newScheduledThreadPool(1).schedule(() -> {
            logger.info("Scheduled spider task is start");
            startSpider();
        }, 1, TimeUnit.DAYS);
        dbTask.start();
    }

    static void startSpider() {
        List<String> urlList = Lists.newArrayList();
        for (int i = 1; i <= 1000; i++) {
            urlList.add("https://www.danke.com/room/sh?page=" + i);
        }
        Spider.create(new DankeRoomSpider())
                .addUrl(urlList.toArray(new String[urlList.size()]))
                .addPipeline((resultItems, task) -> {
                    if (resultItems.get("title") != null)
                        queue.offer(resultItems);
                }).thread(16).run();
    }
}
