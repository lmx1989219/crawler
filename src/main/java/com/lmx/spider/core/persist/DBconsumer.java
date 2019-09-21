package com.lmx.spider.core.persist;

import com.google.common.base.Joiner;
import com.lmx.spider.core.QkRoomSpider;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 一句话描述一下
 *
 * @author: lucas
 * @create: 2019-08-21 10:53
 **/
public class DBconsumer {
    public static ArrayBlockingQueue<ResultItems> queue = new ArrayBlockingQueue<>(1024);
    private static Logger logger = LoggerFactory.getLogger(QkRoomSpider.class);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }

    static Thread dbTask = new Thread(() -> {
        logger.info("dbTask is start");
        Connection connection = null;
        try {
            connection = getConn();
        } catch (Exception e) {
            logger.error("", e);
        }

        while (!Thread.interrupted()) {
            try {
                ResultItems resultItems = queue.poll(5, TimeUnit.MINUTES);
                if (resultItems == null) {
                    connection.createStatement().executeQuery("select 1");
                    continue;
                }
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
                            "insert into room(title,price,area,type,floor,image_list,create_time,village,subway,source,city) values(?,?,?,?,?,?,?,?,?,?,?)");
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, resultItems.get("price"));
                    preparedStatement.setString(3, resultItems.get("area"));
                    preparedStatement.setString(4, resultItems.get("type"));
                    preparedStatement.setString(5, resultItems.get("floor"));
                    preparedStatement.setString(6, Joiner.on(",").join((List) resultItems.get("imageList")));
                    preparedStatement.setString(7, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    preparedStatement.setString(8, title.split(" ")[1]);
                    preparedStatement.setString(9, title.split(" ")[0]);
                    preparedStatement.setString(10, resultItems.get("source"));
                    preparedStatement.setString(11, resultItems.get("city"));
                    preparedStatement.execute();
                    preparedStatement.close();
                }
            } catch (Exception e) {
                logger.error("", e);
                connection = getConn();
            }
        }
    });

    static Connection getConn() {
        try {
            return DriverManager
                    .getConnection("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", "root", "root");
        } catch (SQLException e1) {
            logger.error("", e1);
        }
        return null;
    }

    public static void start() {
        dbTask.start();
    }
}
