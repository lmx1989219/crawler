# spider
爬虫程序，基于chromedriver模拟网页事件
## 准备工具
chromedriver、chrome浏览器，一定要版本对应。

编辑chromedriver.exe 搜索关键字cdc_
      
      var key = '$cdc_asdjflasutopfhvcZLmcaa_';

更新为上面这段内容，否则会被反爬虫拦截提示你人工验证

## csdn
webmagic+selenium实现的模拟网页事件

    1.模拟登录；
    2.获取列表；
    3.对每一个博客进行点赞+评论

    
 ## 蛋壳、青客待租房源爬取
1. 需要执行doc下的sql脚本
2. 数据差异比较sql


    SELECT
    	sum(
    		CASE source
    		WHEN '蛋壳公寓' THEN
    			1
    		ELSE
    			0
    		END
    	) AS '蛋壳公寓待租房间数',
    	sum(
    		CASE source
    		WHEN '青客公寓' THEN
    			1
    		ELSE
    			0
    		END
    	) AS '青客公寓待租房间数',
    	ci. NAME AS '城市'
    FROM
    	room rom
    INNER JOIN city ci ON rom.city = ci.`code`
    GROUP BY
    	city;
