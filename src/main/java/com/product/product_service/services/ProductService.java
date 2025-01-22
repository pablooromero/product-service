package com.product.product_service.services;

import com.product.product_service.dtos.ProductDTO;
import com.product.product_service.exceptions.IllegalAttributeException;
import com.product.product_service.models.Product;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    ResponseEntity<List<Product>> getAllProducts();

    Product saveProduct(Product product);

    ResponseEntity<ProductDTO> createProduct(ProductDTO productDTO) throws IllegalAttributeException;

    ResponseEntity<ProductDTO> updateProduct(Long id, ProductDTO productDTO) throws IllegalAttributeException;

    ResponseEntity<String> deleteProduct(Long id);

    void validateProduct(ProductDTO productDTO) throws IllegalAttributeException;
}
