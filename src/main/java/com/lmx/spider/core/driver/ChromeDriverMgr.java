package com.lmx.spider.core.driver;

import com.lmx.spider.core.QQnewsSpider;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 一句话描述一下
 *
 * @author: lucas
 * @create: 2019-08-22 10:34
 **/
public class ChromeDriverMgr {
    private static Logger logger = LoggerFactory.getLogger(ChromeDriverMgr.class);
    public static ChromeDriver driver;

    public static void initDriver() {
        ChromeOptions options = new ChromeOptions();
        //全屏显示
        options.addArguments("--start-maximized");
        //开启开发者模式
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        driver = new ChromeDriver(options);
    }

    public static void executeScript(String script) {
        driver.executeScript(script);
    }

    public static void get(String url) {
        driver.get(url);
    }

    public static void quit() {
        driver.quit();
    }

    public static void sleep(Long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }
}
