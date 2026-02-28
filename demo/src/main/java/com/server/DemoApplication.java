package com.server;

import com.server.domain.Item;
import com.server.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class DemoApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ItemRepository itemRepository) {
		return args -> {
			// 데이터가 없을 경우에만 테스트 상품 생성
			if (itemRepository.count() == 0) {
				itemRepository.save(new Item("테스트 상품(100원)", 100, 100)); // ID: 1
				itemRepository.save(new Item("맛있는 피자", 15000, 50));        // ID: 2
			}
		};
	}

	// 루트 경로("/") 접속 시 index.html로 포워딩
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.html");
	}

	// 외부 client 폴더를 정적 리소스 경로로 매핑
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**") // 모든 URL 요청에 대해
				.addResourceLocations("file:///c:/kimminsu/demo/client/"); // client 폴더에서 파일을 찾음
	}
}
