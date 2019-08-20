package com.lmx.spider.core;

import com.google.common.collect.Lists;
import org.openqa.selenium.By;
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

/**
 * 一句话描述一下
 *
 * @author: lucas
 * @create: 2019-08-19 10:08
 **/
public class CsdnBlogSpider implements PageProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
            .setRetryTimes(3).setSleepTime(1000);
    private static ChromeDriver driver;
    private int endPos = 1000;//待爬取的页面数量
    private List<String> replyList = Lists.newArrayList("我只看看不说话，搬个小板凳先占个座...",
            "老衲前指一算，哟！咱们思路基本一致", "文章比较新颖，有深度，能看出作者是个狠角色", "沙发");

    public void process(Page page) {
        if (!page.getUrl().regex("https://blog.csdn.net/\\w+/article/details/\\w+").match()) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.get(page.getUrl().toString());
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //模拟下拉加载更多数据
            for (int startPos = 1; startPos <= endPos; startPos++) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            driver.get(page.getUrl().toString());
            //提取点赞按钮
            WebElement submit = By.cssSelector(".btn-like").findElement(driver);
            if (submit != null)
                submit.click();
            try {
                Thread.sleep(5000L);
                //展开评论输入框
                WebElement element = By.xpath("//*[@id=\"comment_content\"]").findElement(driver);
                if (element != null)
                    element.click();
                Thread.sleep(500L);
                //填充评论输入框
                element = By.xpath("//*[@id=\"comment_content\"]").findElement(driver);
                if (element != null)
                    element.sendKeys(replyList.get(Math.abs((int) System.currentTimeMillis() % replyList.size())));
                //提交评论
                element = By.cssSelector("#commentform > div > div.right-box > input.btn.btn-sm.btn-red.btn-comment").findElement(driver);
                if (element != null)
                    element.click();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Site getSite() {
        return site;
    }

    /**
     * program parameters
     * <p>
     * e.g:userName pwd
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2)
            throw new RuntimeException("用户名和密码必须输入");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("chrome driver is exiting");
                driver.quit();
            }
        });
        System.setProperty("webdriver.chrome.driver", "D:\\git-rep\\chromedriver.exe");
        CsdnBlogSpider spiderMain = new CsdnBlogSpider();
        spiderMain.mockLogin(args[0], args[1]);
        Spider.create(spiderMain).addUrl("https://blog.csdn.net/").thread(1).run();
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
