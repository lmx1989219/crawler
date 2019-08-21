# spider
爬虫程序，基于chromedriver模拟网页事件
## 准备工具
chromedriver、chrome浏览器，一定要版本对应。
        
        v2.46	v71-73
        v2.45	v70-72
        v2.44	v69-71
        v2.43	v69-71
        v2.42	v68-70
        v2.41	v67-69
        v2.40	v66-68
        v2.39	v66-68
        v2.38	v65-67
        v2.37	v64-66
        v2.36	v63-65
        v2.35	v62-64
        v2.34	v61-63
        v2.33	v60-62
        v2.32	v59-61
        v2.31	v58-60
        v2.30	v58-60
        v2.29	v56-58
        v2.28	v55-57
        v2.27	v54-56
        v2.26	v53-55
        v2.25	v53-55
        v2.24	v52-54
        v2.23	v51-53
        v2.22	v49-52
        v2.21	v46-50
        v2.20	v43-48
        v2.19	v43-47


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
````
    SELECT
        AVG(price),
        source,
        max(price),
        MIN(price),
        COUNT(*)
    FROM
        `room`
    GROUP BY
        source;
````
````
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
````