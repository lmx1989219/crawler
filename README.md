# spider
爬虫程序，基于chromedriver模拟网页事件
## 准备工具
chromedriver、chrome浏览器，一定要版本对应。

编辑chromedriver.exe 搜索关键字cdc
      
      var key = '$cdc_asdjflasutopfhvcZLmcaa_';

更新为上面这段内容，否则会被反爬虫拦截提示你人工验证

## csdn
webmagic+selenium实现的模拟网页事件

    1.模拟登录；
    2.获取列表；
    3.对每一个博客进行点赞+评论

    
 ## 蛋壳公寓房源
sql脚本
 
    CREATE TABLE `room` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `city` varchar(255) DEFAULT NULL,
      `title` varchar(255) DEFAULT NULL,
      `subway` varchar(255) DEFAULT NULL,
      `village` varchar(255) DEFAULT NULL,
      `price` decimal(10,2) DEFAULT NULL,
      `area` varchar(255) DEFAULT NULL,
      `type` varchar(255) DEFAULT NULL,
      `floor` varchar(255) DEFAULT NULL,
      `image_list` text,
      `source` varchar(255) DEFAULT NULL,
      `create_time` datetime DEFAULT NULL,
      `update_time` datetime DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=6539 DEFAULT CHARSET=utf8mb4;
    
    

