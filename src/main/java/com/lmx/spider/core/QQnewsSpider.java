package com.lmx.spider.core;

import com.google.common.collect.Lists;
import com.lmx.spider.core.driver.ChromeDriverMgr;
import com.lmx.spider.core.util.CnWordsGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 腾讯新闻自动点评
 *
 * @author: lucas
 * @create: 2019-08-19 10:08
 **/
public class QQnewsSpider implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(QQnewsSpider.class);
    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
            .setRetryTimes(0).setSleepTime(500);
    private int endPos = 10;//待爬取的页面数量，可灵活调整

    public void process(Page page) {
        if (page.getUrl().regex("https://new.qq.com/\\w+/video/\\w+").match()) {
            return;
        }
        if (page.getUrl().regex("https://new.qq.com/\\w+/template/(\\?id=\\w+)").match()) {
            return;
        }
        if (!page.getUrl().regex("https://new.qq.com/\\w+/\\w+/(\\w+.html)").match()) {
            ChromeDriverMgr.sleep(2000L);
            ChromeDriverMgr.get(page.getUrl().toString());
            //模拟下拉加载更多数据
            for (int startPos = 1; startPos <= endPos; startPos++) {
                ChromeDriverMgr.sleep(100L);
                ChromeDriverMgr.executeScript("window.scrollTo(0," + startPos * 800 + ")");
            }
            List<String> urlList = Lists.newArrayList();
            for (WebElement webElement : ChromeDriverMgr.driver.findElementsByCssSelector("div > h3 > a")) {
                urlList.add(webElement.getAttribute("href"));
            }
            page.addTargetRequests(urlList);
            logger.info("待抓取的url个数为{}", urlList.size());
        } else {
            String url = page.getUrl().toString();
            ChromeDriverMgr.get(url);
            ChromeDriverMgr.sleep(5000L);
            try {
                //触发评论区加载
                for (int startPos = 1; startPos <= 10; startPos++) {
                    ChromeDriverMgr.sleep(100L);
                    ChromeDriverMgr.executeScript("window.scrollTo(0," + startPos * 800 + ")");
                }
                ChromeDriverMgr.sleep(5000L);
                //切换到第一个iframe即评论区
                WebDriver webDriver = ChromeDriverMgr.driver.switchTo().frame(0);
                String pubMsg = String.valueOf(CnWordsGenerator.getRandomChar());
                //填充评论输入框
                By.cssSelector("#J_Textarea").findElement(webDriver)
                        .sendKeys(pubMsg + pubMsg + "...");
                //提交评论
                By.cssSelector("#J_PostBtn").findElement(webDriver).click();
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }

    }

    public Site getSite() {
        return site;
    }

    /**
     * program parameters
     * <p>
     * e.g:driverPath qq
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2)
            throw new RuntimeException("webdriver[3]路径必须输入");
        System.setProperty("webdriver.chrome.driver", args[0]);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                ChromeDriverMgr.initDriver();
                QQnewsSpider spiderMain = new QQnewsSpider();
                spiderMain.mockLogin(args[1]);
                logger.info("Scheduled spider task is start");
                //娱乐热点新闻
                Spider.create(spiderMain).addUrl("https://new.qq.com/ch/ent/").thread(1).run();
                logger.info("start close chrome");
                //关闭浏览器
                ChromeDriverMgr.quit();
            } catch (Exception e) {
                logger.error("", e);
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    void mockLogin(String qq) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("chrome driver is exiting");
            ChromeDriverMgr.quit();
        }));
        ChromeDriverMgr.get("https://graph.qq.com/oauth2.0/show?which=Login&display=pc&response_type=code&client_id=101487368&redirect_uri=https%3A%2F%2Fpacaio.match.qq.com%2Fqq%2FloginBack%3Fsurl%3Dhttps%253A%252F%252Fnews.qq.com%252F&state=5b481c68e379d");
        //对于iframe嵌套的需要切换窗口
        WebDriver webDriver = ChromeDriverMgr.driver.switchTo().frame(0);
        WebElement addTrainer = (new WebDriverWait(webDriver, 5))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"img_out_" + qq + "\"]")));
        addTrainer.click();
    }

}
