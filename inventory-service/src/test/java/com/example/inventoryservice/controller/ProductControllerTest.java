package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.CreateProductRequestDTO;
import com.example.inventoryservice.dto.ProductDetailsResponseDTO;
import com.example.inventoryservice.service.ProductService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    private final ProductService productService = mock(ProductService.class);
    private final ProductController controller = new ProductController(productService);

    @Test
    void createDelegatesToService() {
        CreateProductRequestDTO request = new CreateProductRequestDTO("Keyboard", "Mechanical", BigDecimal.TEN, "KB", 2);
        ProductDetailsResponseDTO expected = product(1L);
        when(productService.create(request)).thenReturn(expected);

        assertThat(controller.create(request)).isEqualTo(expected);
    }

    @Test
    void bulkFetchDelegatesToService() {
        Set<Long> ids = Set.of(1L);
        when(productService.getProductDetailsByIds(ids)).thenReturn(List.of(product(1L)));

        assertThat(controller.getProductsByIds(ids)).extracting(ProductDetailsResponseDTO::id).containsExactly(1L);
    }

    @Test
    void listDelegatesToService() {
        when(productService.getProducts(0, 10)).thenReturn(List.of(product(2L)));

        assertThat(controller.getProducts(0, 10)).extracting(ProductDetailsResponseDTO::id).containsExactly(2L);
    }

    private static ProductDetailsResponseDTO product(Long id) {
        return new ProductDetailsResponseDTO(id, "Keyboard", "Mechanical", BigDecimal.TEN, "KB", 2);
    }
}
