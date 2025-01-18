package com.hncboy.beehive.base.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ll
 * @date 2023-8-31
 */
public class QrUtil {
    /**
     * CODE_WIDTH：二维码宽度，单位像素
     * CODE_HEIGHT：二维码高度，单位像素
     * FRONT_COLOR：二维码前景色，0x000000 表示黑色
     * BACKGROUND_COLOR：二维码背景色，0xFFFFFF 表示白色
     * 演示用 16 进制表示，和前端页面 CSS 的取色是一样的，注意前后景颜色应该对比明显，如常见的黑白
     */
    private static final int CODE_WIDTH = 300;
    private static final int CODE_HEIGHT = 300;
    private static final int FRONT_COLOR = 0x000000;
    private static final int BACKGROUND_COLOR = 0xFFFFFF;
    private static String img = "png";


    /**
     *        二维码图片文件名称，带格式,如 123.png
     */
    public static Boolean createCodeToFile(String qrCodeText, String filePath) {
        try {
            Map<EncodeHintType, Object> hintMap = new HashMap<EncodeHintType, Object>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            try {
                BitMatrix matrix = new MultiFormatWriter().encode(
                        new String(qrCodeText.getBytes("UTF-8"), "ISO-8859-1"),
                        BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT, hintMap);
                MatrixToImageWriter.writeToFile(matrix, img, new File(filePath));
                System.out.println("QR code image created successfully!");
            } catch (WriterException | IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static void main( String[] srg){
        // 步骤1：创建一个Calendar对象
        Calendar calendar = Calendar.getInstance();

        // 步骤2：获取当前时间的秒数
        int seconds = calendar.get(Calendar.SECOND);

        // 步骤3：获取当前时间的微秒数
        long currentTimeMillis = System.currentTimeMillis();
        int microseconds = (int) (currentTimeMillis % 1000) * 1000 + calendar.get(Calendar.MILLISECOND);

        System.out.println("当前时间的微秒数：" + microseconds);
        System.out.println(System.nanoTime());
        System.out.println(System.currentTimeMillis());
        //String st = "https://excashier.alipay.com/standard/auth.htm?payOrderId=e4e1db3a82114d089f567ca4a31ba99d.25";
        //String qrFilePathName = "D:\\chatgpt\\hello\\111.png";
        //createCodeToFile(st,qrFilePathName);
    }

}
