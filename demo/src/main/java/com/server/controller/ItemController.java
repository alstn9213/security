package com.server.controller;

import com.server.dto.ItemResponse;
import com.server.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    @GetMapping("/api/items")
    public List<ItemResponse> getItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::new)
                .collect(Collectors.toList());
    }
}