package com.hncboy.beehive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ll
 * @date 2023-3-22
 * BeehiveApplication
 */
@EnableScheduling
@MapperScan(value = {"com.hncboy.beehive.**.mapper"})
//@ComponentScan("com.hncboy.beehive.cell.midjourney.handler.scheduler")
@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class BeehiveApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(BeehiveApplication.class, args);

            System.out.println("this is wmyc");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
