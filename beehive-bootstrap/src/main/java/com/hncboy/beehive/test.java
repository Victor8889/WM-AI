package com.hncboy.beehive;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @author ll
 * @date 2023-7-18
 */
public class test {
    public static void main(String[] args) {
        String directoryPath = "D:\\16chatgpt\\new-xx.txt";//"C:\\Users\\liulin\\Desktop\\"; // 指定目录路径
        String key = "key"; // 指定关键字



        List<File> filesToInsert = new ArrayList<>();

        // Traverse the directory and find files starting with "sk"
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        //if (files != null) {
        //    for (File file : files) {
        //        if (file.isFile() &&  file.getName().contains("91个(2)(1).txt")) {
        //            filesToInsert.add(file);
        //        }
        //    }
        //}

        // Insert the data into the database
        try {
            Connection connection = getConnection(); // Replace with your database connection method
            insertWord(directoryPath,connection);
            //测试
            //printKey(connection);

            if(filesToInsert == null)
                return;
            for (File file : filesToInsert) {
                String apiKey = readApiKeyFromFile(file,connection);

                //insertIntoDatabase(connection, apiKey);
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertWord(String filePath,Connection connection) throws Exception {
        String insertQuery = "INSERT INTO bh_sensitive_word (id, word, status, is_deleted) VALUES (NULL, ?, 1, 0)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        int count =0;
        // 读取文本文件内容
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            // 按顿号分隔词汇
            String[] words = line.split("、");

            // 插入每个词汇到数据库
            for (String word : words) {
                System.out.println(count+"--------"+word);
                count++;
                // 设置插入参数
                try {
                    preparedStatement.setString(1, word);

                    // 执行插入操作
                    preparedStatement.executeUpdate();
                }catch(Exception e){

                }
            }
        }
    }


    private static Connection getConnection() throws SQLException {
        // Replace with your database connection details
        String url = "jdbc:mysql://1.1.1.1:2203/ai_beehive";
        String user = "xxxx";
        String password = "xxxxxx";
        return DriverManager.getConnection(url, user, password);
    }

    private static String readApiKeyFromFile(File file,Connection connection) throws IOException {
        StringBuilder apiKeyBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (testOpenAIKey(line.substring(line.indexOf(":")+1))) {
                    insertIntoDatabase(connection, line);
                }
                //apiKeyBuilder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiKeyBuilder.toString();
    }

    private static void insertIntoDatabase(Connection connection, String apiKey) throws Exception {
        String baseUrl = "https://xxx.top/";
        String useScenes = "[\"GPT3.5\",\"CHATGPT\"]";
        //double[] result = checkOpenAICredit(apiKey);
        //printKey(connection);
        //BigDecimal totalBalance = new BigDecimal(String.valueOf(result[0]));
        //BigDecimal usageBalance = new BigDecimal(String.valueOf(result[1]));
        //BigDecimal remainBalance = new BigDecimal(String.valueOf(result[2]));
        BigDecimal totalBalance = new BigDecimal(5);
        BigDecimal usageBalance = new BigDecimal(0);
        BigDecimal remainBalance = new BigDecimal(0);
        BigDecimal balanceWaterLine = new BigDecimal("0");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(count > 500) {
            totalBalance = new BigDecimal("5");
            remainBalance = new BigDecimal("5");
        }
        String sql = "INSERT INTO bh_openai_api_key (api_key, base_url, use_scenes, total_balance, usage_balance, remain_balance, balance_water_line, refresh_status_time, refresh_balance_time, is_refresh_balance, is_refresh_status, weight, status, remark, update_reason, error_info, version, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, apiKey);
            statement.setString(2, baseUrl);
            statement.setString(3, useScenes);
            statement.setBigDecimal(4, totalBalance);
            statement.setBigDecimal(5, usageBalance);
            statement.setBigDecimal(6, remainBalance);
            statement.setBigDecimal(7, balanceWaterLine);
            statement.setString(8, now.format(formatter));
            statement.setString(9, now.format(formatter));
            statement.setInt(10, 1);
            statement.setInt(11, 1);
            statement.setInt(12, 0);
            statement.setString(13, "valid");
            statement.setString(14, "1");
            statement.setString(15, "2");
            statement.setString(16, "3");
            statement.setInt(17, 3);
            statement.setString(18, now.format(formatter));
            statement.setString(19, now.format(formatter));
            statement.executeUpdate();
        }
    }
    public static void printKey(Connection connection){
        // MySQL数据库连接信息

        // 查询语句
        String query = "SELECT api_key FROM bh_openai_api_key where status='enable'";

        try (
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                // 获取并打印api_key字段的值
                String apiKey = resultSet.getString("api_key");
                System.out.println(apiKey);
                //testOpenAIKey(apiKey);
                getBlance(apiKey);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static int count = 0;
    public static boolean testOpenAIKey(String apiKey) {
        String url = "https://xxxxx.top";//"https://api.openai.com/v1/engines/davinci";//
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            count++;
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 检查 API 响应是否包含引擎信息
                String responseBody = response.toString();
                System.out.println("测试 OpenAI 密钥有效。-----------------------------key：" + apiKey);
                if (responseBody.contains("\"object\": \"engine\"")) {
                    return true;
                }
            } else {
                System.out.println(new Date()+" 测试 OpenAI 密钥时出现错误。 错误码：" + responseCode +"  " +apiKey +"  " +count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void getBlance(String apiKey) {
        try {
            double[] result = checkOpenAICredit(apiKey);
            if (result != null) {
                System.out.println("Total Amount: " + result[0]);
                System.out.println("Used: " + result[1]);
                System.out.println("Remaining: " + result[2]);
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    public static double[] checkOpenAICredit(String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Calculate start date and end date
        long now = System.currentTimeMillis();
        long ninetyDaysAgo = now - 90 * 24 * 60 * 60 * 1000;
        long oneDayLater = now + 24 * 60 * 60 * 1000;

        // Set API request URLs
        HttpUrl subscriptionUrl = HttpUrl.parse("https://xxx.top/v1/dashboard/billing/subscription");
        HttpUrl usageUrl = HttpUrl.parse("https://xxx.top/v1/dashboard/billing/usage")
                .newBuilder()
                .addQueryParameter("start_date", formatDate(ninetyDaysAgo))
                .addQueryParameter("end_date", formatDate(oneDayLater))
                .build();

        // Create and execute the subscription request
        Request subscriptionRequest = new Request.Builder()
                .url(subscriptionUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        try (Response subscriptionResponse = client.newCall(subscriptionRequest).execute()) {
            if (!subscriptionResponse.isSuccessful()) {
                System.out.println("Your account has been suspended. Please log in to OpenAI for details.");
                return null;
            }

            // Process the subscription response here to get totalAmount, isSubscribed, etc.
            JSONObject subscriptionData = new JSONObject(subscriptionResponse.body().string());
            double totalAmount = subscriptionData.getDouble("hard_limit_usd");
            boolean isSubscribed = subscriptionData.getBoolean("has_payment_method");

            // Check if user is subscribed
            if (isSubscribed) {
                // Create and execute the usage request
                Request usageRequest = new Request.Builder()
                        .url(usageUrl)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .get()
                        .build();

                try (Response usageResponse = client.newCall(usageRequest).execute()) {
                    if (!usageResponse.isSuccessful()) {
                        System.out.println("Your account has been suspended. Please log in to OpenAI for details.");
                        return null;
                    }

                    // Process the usage response here to get totalUsage
                    JSONObject usageData = new JSONObject(usageResponse.body().string());
                    double totalUsage = usageData.getDouble("total_usage") / 100.0;

                    // Calculate remaining credits
                    double remaining = totalAmount - totalUsage;
                    return new double[]{totalAmount, totalUsage, remaining};
                }
            } else {
                // If not subscribed, just return the total amount
                return new double[]{totalAmount, 0.0, totalAmount};
            }
        }
    }

    public static String formatDate(long timestamp) {
        // Implement your date formatting logic here, if needed
        return Long.toString(timestamp / 1000);
    }
}
