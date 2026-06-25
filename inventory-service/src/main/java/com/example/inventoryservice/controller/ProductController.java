package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.CreateProductRequestDTO;
import com.example.inventoryservice.dto.ProductDetailsResponseDTO;
import com.example.inventoryservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDetailsResponseDTO create(@Valid @RequestBody CreateProductRequestDTO createProductRequestDTO) {
        return productService.create(createProductRequestDTO);
    }

    @PostMapping("/bulk-fetch")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<ProductDetailsResponseDTO> getProductsByIds(@Valid @RequestBody Set<Long> productIdList) {
        return productService.getProductDetailsByIds(productIdList);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<ProductDetailsResponseDTO> getProducts(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return productService.getProducts(page, size);
    }

}
