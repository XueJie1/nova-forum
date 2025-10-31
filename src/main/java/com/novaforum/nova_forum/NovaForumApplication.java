package com.novaforum.nova_forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.novaforum.nova_forum.mapper")
@SpringBootApplication
public class NovaForumApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovaForumApplication.class, args);
	}

}
