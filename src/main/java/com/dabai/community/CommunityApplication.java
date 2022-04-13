package com.dabai.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init() {
		// 解决netty的启动冲突问题
		// 源码：Netty4Utils.setAvailableProcessors()方法
		System.getProperty("es.set.netty.runtime.available.processors", "true");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
