package com.lmx.spider.core.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * 名称
 * <p>
 * 第一字节
 * <p>
 * 第二字节
 * <p>
 * GB2312
 * <p>
 * 0xB0-0xF7(176-247)
 * <p>
 * 0xA0-0xFE（160-254）
 * <p>
 * GBK
 * <p>
 * 0x81-0xFE（129-254）
 * <p>
 * 0x40-0xFE（64-254）
 * <p>
 * Big5
 * <p>
 * 0x81-0xFE（129-255）
 * <p>
 * 0x40-0x7E（64-126）
 * <p>
 * 0xA1－0xFE（161-254）
 *
 * @author: lucas
 * @create: 2019-08-22 13:41
 **/
public class CnWordsGenerator {
    public static char getRandomChar() {
        String str = "";
        Random random = new Random();
        int hightPos = (176 + Math.abs(random.nextInt(39))),
                lowPos = (161 + Math.abs(random.nextInt(93)));
        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(hightPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();
        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }
}
