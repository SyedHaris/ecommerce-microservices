package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.CreateProductRequestDTO;
import com.example.inventoryservice.dto.ProductDetailsResponseDTO;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.exception.ProductAlreadyExistsException;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.repository.ProductRepository;
import com.example.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public ProductDetailsResponseDTO create(CreateProductRequestDTO createProductRequestDTO) {
        if (productRepository.findBySku(createProductRequestDTO.sku()).isPresent()) {
            throw new ProductAlreadyExistsException("Product already exists");
        }
        Product product = createProduct(createProductRequestDTO);
        Inventory inventory = createInventory(createProductRequestDTO, product);
        return new ProductDetailsResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                inventory.getAvailableQuantity()
        );
    }

    @Override
    public List<ProductDetailsResponseDTO> getProductDetailsByIds(Set<Long> productIdList) {
        List<Product> products = productRepository.findByIdIn(productIdList);
        return products.stream()
                .map(ProductDetailsResponseDTO::from)
                .toList();
    }

    @Override
    public List<Product> getProductsByIds(Set<Long> productIdList) {
        return productRepository.findByIdIn(productIdList);
    }

    @Override
    public List<ProductDetailsResponseDTO> getProducts(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        return productRepository.findBy(pageRequest)
                .stream().map(ProductDetailsResponseDTO::from)
                .toList();
    }

    private Inventory createInventory(CreateProductRequestDTO createProductRequestDTO, Product product) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(createProductRequestDTO.quantity());
        inventoryRepository.save(inventory);
        return inventory;
    }

    private Product createProduct(CreateProductRequestDTO createProductRequestDTO) {
        Product product = new Product();
        product.setSku(createProductRequestDTO.sku());
        product.setName(createProductRequestDTO.name());
        product.setDescription(createProductRequestDTO.description());
        product.setPrice(createProductRequestDTO.price());
        productRepository.save(product);
        return product;
    }

}
