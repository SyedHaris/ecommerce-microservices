package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.CreateProductRequestDTO;
import com.example.inventoryservice.dto.ProductDetailsResponseDTO;
import com.example.inventoryservice.entity.Product;

import java.util.List;
import java.util.Set;

public interface ProductService {

    ProductDetailsResponseDTO create(CreateProductRequestDTO createProductRequestDTO);

    List<ProductDetailsResponseDTO> getProductDetailsByIds(Set<Long> productIdList);

    List<Product> getProductsByIds(Set<Long> productIdList);

    List<ProductDetailsResponseDTO> getProducts(Integer page, Integer size);

}
