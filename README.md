# spider
## csdn
webmagic+selenium实现的模拟网页事件

    1.模拟登录；
    2.获取列表；
    3.对每一个博客进行点赞+评论
    
    
 ## 蛋壳公寓房源
sql脚本
 
    CREATE TABLE `room` (
      `id` bigint(20) NOT NULL,
      `title` varchar(255) DEFAULT NULL,
      `price` decimal(10,2) DEFAULT NULL,
      `area` varchar(255) DEFAULT NULL,
      `type` varchar(255) DEFAULT NULL,
      `floor` varchar(255) DEFAULT NULL,
      `image_list` text,
      `source` varchar(255) DEFAULT NULL,
      `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
      `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    

