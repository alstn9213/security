package com.server;

import com.server.domain.Item;
import com.server.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ItemRepository itemRepository) {
		return args -> {
			// 데이터가 없을 경우에만 테스트 상품 생성
			if (itemRepository.count() == 0) {
				itemRepository.save(new Item("햄버거", 6000, 100));
				itemRepository.save(new Item("피자", 15000, 50));
			}
		};
	}
}
