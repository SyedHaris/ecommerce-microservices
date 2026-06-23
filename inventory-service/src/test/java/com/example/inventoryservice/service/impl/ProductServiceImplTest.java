package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.CreateProductRequestDTO;
import com.example.inventoryservice.dto.ProductDetailsResponseDTO;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.exception.ProductAlreadyExistsException;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createPersistsProductAndInventory() {
        CreateProductRequestDTO request = new CreateProductRequestDTO("Keyboard", "Mechanical", BigDecimal.valueOf(50), "KB-1", 5);
        when(productRepository.findBySku("KB-1")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            return product;
        });

        ProductDetailsResponseDTO response = productService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.quantity()).isEqualTo(5);
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertThat(inventoryCaptor.getValue().getProduct().getSku()).isEqualTo("KB-1");
    }

    @Test
    void createRejectsDuplicateSku() {
        when(productRepository.findBySku("KB-1")).thenReturn(Optional.of(new Product()));

        assertThatThrownBy(() -> productService.create(new CreateProductRequestDTO("Keyboard", "Mechanical", BigDecimal.TEN, "KB-1", 5)))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessage("Product already exists");
    }

    @Test
    void getProductDetailsByIdsMapsProducts() {
        when(productRepository.findByIdIn(Set.of(1L))).thenReturn(List.of(product(1L, 3)));

        assertThat(productService.getProductDetailsByIds(Set.of(1L)))
                .extracting(ProductDetailsResponseDTO::quantity)
                .containsExactly(3);
    }

    @Test
    void getProductsByIdsReturnsEntities() {
        Product product = product(1L, 3);
        when(productRepository.findByIdIn(Set.of(1L))).thenReturn(List.of(product));

        assertThat(productService.getProductsByIds(Set.of(1L))).containsExactly(product);
    }

    @Test
    void getProductsUsesPaging() {
        when(productRepository.findBy(any())).thenReturn(List.of(product(2L, 8)));

        assertThat(productService.getProducts(0, 10)).extracting(ProductDetailsResponseDTO::id).containsExactly(2L);
    }

    private static Product product(Long id, int quantity) {
        Product product = new Product();
        product.setId(id);
        product.setName("Keyboard");
        product.setDescription("Mechanical");
        product.setPrice(BigDecimal.TEN);
        product.setSku("KB-" + id);
        Inventory inventory = new Inventory();
        inventory.setAvailableQuantity(quantity);
        inventory.setProduct(product);
        product.setInventory(inventory);
        return product;
    }
}
