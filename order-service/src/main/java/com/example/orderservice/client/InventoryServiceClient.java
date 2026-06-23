package com.example.orderservice.client;

import com.example.orderservice.dto.ProductDetailsResponseDTO;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(name = "inventoryServiceClient", url = "${inventory-service.url}")
@Retry(name = "inventory-service")
public interface InventoryServiceClient {

    @PostMapping("/products/bulk-fetch")
    List<ProductDetailsResponseDTO> getProductsByIds(@RequestBody Set<Long> productIdList);

}
