package com.hncboy.beehive.base.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ll
 * @date 2023-9-6
 */
public class RandUtil {
    /**
     * 毫秒级时间戳+微妙
     * @return
     */
    public static String getLong() {
        // 步骤1：创建一个Calendar对象
        Calendar calendar = Calendar.getInstance();

        // 步骤2：获取当前时间的秒数
        int seconds = calendar.get(Calendar.SECOND);

        // 步骤3：获取当前时间的微秒数
        long currentTimeMillis = System.currentTimeMillis();
        int microseconds = (int) (currentTimeMillis % 1000) * 1000 + calendar.get(Calendar.MILLISECOND);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        return sdf.format(new Date()) + microseconds;
    }
    /**
     * 微妙
     * @return
     */
    public static String getShort() {
        // 步骤1：创建一个Calendar对象
        Calendar calendar = Calendar.getInstance();

        // 步骤2：获取当前时间的秒数
        int seconds = calendar.get(Calendar.SECOND);

        // 步骤3：获取当前时间的微秒数
        long currentTimeMillis = System.currentTimeMillis();
        int microseconds = (int) (currentTimeMillis % 1000) * 1000 + calendar.get(Calendar.MILLISECOND);

        return ""+ microseconds;
    }
    public static void main(String[] svg){

        LocalDateTime dateTime = LocalDateTime.of(2023, 2, 4, 13, 11, 11);
        long milliseconds = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();

        System.out.println(milliseconds);
        for (int i = 0; i < 1; i++) {
            long currentTimeMillis = System.currentTimeMillis();
            System.out.println(currentTimeMillis);
        }
    }

}
