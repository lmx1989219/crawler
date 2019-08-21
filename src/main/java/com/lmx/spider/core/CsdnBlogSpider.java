package com.lmx.spider.core;

import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * csdn博客进行自动点赞+点评
 *
 * @author: lucas
 * @create: 2019-08-19 10:08
 **/
public class CsdnBlogSpider implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(CsdnBlogSpider.class);
    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
            .setRetryTimes(0).setSleepTime(500);
    private static ChromeDriver driver;
    private int endPos = 1;//待爬取的页面数量，可灵活调整
    private List<String> replyList = Lists.newArrayList("我只看看不说话，搬个小板凳先占个座...",
            "顶一下老铁", "文章比较新颖，有深度，能看出作者是个狠角色", "沙发");

    public void process(Page page) {
        if (!page.getUrl().regex("https://blog.csdn.net/\\w+/article/details/\\w+").match()) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            driver.get(page.getUrl().toString());
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            //模拟下拉加载更多数据
            for (int startPos = 1; startPos <= endPos; startPos++) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
                driver.executeScript("window.scrollTo(0," + startPos * 500 + ")");
            }
            List<String> urlList = Lists.newArrayList();
            //获取当前页博客列表
            for (WebElement webElement : driver.findElementsByCssSelector("#feedlist_id > li > div > div.title > h2 > a")) {
                urlList.add(webElement.getAttribute("href"));
            }
            page.addTargetRequests(urlList);
            logger.info("待抓取的url个数为{}", urlList.size());
        } else {
            String url = page.getUrl().toString();
            driver.get(url);
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            try {
                //已经点赞忽略
                By.cssSelector(".liked").findElement(driver);
                logger.info("已经点赞过,blog={}", url);
            } catch (NoSuchElementException e) {
                try {
                    //点赞
                    By.cssSelector(".btn-like").findElement(driver).click();
                    //展开评论输入框
                    By.xpath("//*[@id=\"comment_content\"]").findElement(driver).click();
                    //填充评论输入框
                    By.xpath("//*[@id=\"comment_content\"]").findElement(driver)
                            .sendKeys(replyList.get(Math.abs((int) System.currentTimeMillis() % replyList.size())));
                    //提交评论
                    By.cssSelector("#commentform > div > div.right-box > input.btn.btn-sm.btn-red.btn-comment").findElement(driver).click();
                } catch (Exception ex) {
                    logger.error("", ex);
                }
            }
        }

    }

    public Site getSite() {
        return site;
    }

    /**
     * program parameters
     * <p>
     * e.g:userName pwd driverPath
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3)
            throw new RuntimeException("用户名[1]、密码[2]、webdriver[3]路径必须输入");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("chrome driver is exiting");
                driver.quit();
            }
        });
        System.setProperty("webdriver.chrome.driver", args[2]);
        CsdnBlogSpider spiderMain = new CsdnBlogSpider();
        spiderMain.mockLogin(args[0], args[1]);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            logger.info("Scheduled spider task is start");
            Spider.create(spiderMain).addUrl("https://blog.csdn.net/").thread(1).run();
        }, 0, 10, TimeUnit.MINUTES);
    }

    void mockLogin(String username, String pwd) {
        ChromeOptions options = new ChromeOptions();
        //开启开发者模式
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        driver = new ChromeDriver(options);
        driver.get("https://passport.csdn.net/login");
        By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/ul/li[2]/a").findElement(driver).click();
        By.xpath("//*[@id=\"all\"]").findElement(driver).sendKeys(username);
        By.xpath("//*[@id=\"password-number\"]").findElement(driver).sendKeys(pwd);
        By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/div/div[6]/div/button").findElement(driver).click();
    }
}
