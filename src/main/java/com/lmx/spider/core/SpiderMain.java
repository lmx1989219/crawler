package com.lmx.spider.core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Arrays;

/**
 * 一句话描述一下
 *
 * @author: lucas
 * @create: 2019-08-19 10:08
 **/
public class SpiderMain implements PageProcessor {
    Site site = Site.me().
            setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36").setRetryTimes(3).setSleepTime(1000);
    static ChromeDriver driver;

//    String cookie = "uuid_tt_dd=10_19182977900-1566199326147-671626; dc_session_id=10_1566199326147.414663; UserName=lmx1989219; UserInfo=308caa7baf7b48049350c07c400df8f0; UserToken=308caa7baf7b48049350c07c400df8f0; UserNick=moon.byebye; AU=B5A; UN=lmx1989219; BT=1566199343325; p_uid=U000000; Hm_ct_6bcd52f51e9b3dce32bec4a3997715ac=6525*1*10_19182977900-1566199326147-671626!5744*1*lmx1989219; dc_tos=pwh35z; SESSION=ad319f5f-3537-46e6-b638-de4620af1f95; Hm_lvt_6bcd52f51e9b3dce32bec4a3997715ac=1566199352,1566199908; Hm_lpvt_6bcd52f51e9b3dce32bec4a3997715ac=1566199908; MSG-SESSION=504e6037-fabc-4da5-a16d-051e0b98016e";

    public void process(Page page) {
        if (!page.getUrl().regex("https://blog.csdn.net/\\w+/article/details/\\w+").match()) {
//            driver.get(page.getUrl().toString());
//            driver.manage().getCookies();
//            driver.manage().deleteAllCookies();
//            ((JavascriptExecutor) driver).executeScript("document.cookie=\"" + cookie + "\"");
//            driver.get(page.getUrl().toString());
            //获取blog链接
            page.addTargetRequests(page.getHtml().xpath("//ul[@class='feedlist_mod']/li/div/div/h2/a").links().all());
        } else {
            driver.get(page.getUrl().toString());
            //提取点赞按钮
            WebElement submit = By.cssSelector(".btn-like").findElement(driver);
            submit.click();
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public Site getSite() {
//        site.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        site.addHeader("Accept-Encoding", "gzip, deflate, br");
//        site.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
//        site.addHeader("Cache-Control", "max-age=0");
//        site.addHeader("Connection", "keep-alive");
//        site.addHeader("Host", "passport.csdn.net");
        return site;
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("chrome driver is exiting");
                driver.quit();
            }
        });
        System.setProperty("webdriver.chrome.driver", "E:\\chromedriver\\chromedriver.exe");
        SpiderMain spiderMain = new SpiderMain();
        try {
            spiderMain.mockLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Spider.create(spiderMain).addUrl("https://blog.csdn.net/").thread(1).run();
    }

    void mockLogin() throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        //开启开发者模式
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        driver = new ChromeDriver(options);
        driver.get("https://passport.csdn.net/login");
        Thread.sleep(1000L);
        By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/ul/li[2]/a").findElement(driver).click();
        Thread.sleep(1000L);
        By.xpath("//*[@id=\"all\"]").findElement(driver).sendKeys("lmx1989219");
        Thread.sleep(1000L);
        By.xpath("//*[@id=\"password-number\"]").findElement(driver).sendKeys("limx5201314");
        Thread.sleep(1000L);
        By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/div/div[6]/div/button").findElement(driver).click();
    }
}
