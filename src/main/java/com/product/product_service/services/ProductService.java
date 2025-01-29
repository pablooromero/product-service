package com.product.product_service.services;

import com.product.product_service.dtos.ExistentProductsRecord;
import com.product.product_service.dtos.ProductDTO;
import com.product.product_service.dtos.ProductQuantityRecord;
import com.product.product_service.dtos.ProductRecord;
import com.product.product_service.exceptions.IllegalAttributeException;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.exceptions.ProductNotFoundException;
import com.product.product_service.models.Product;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

public interface ProductService {
    ResponseEntity<List<Product>> getAllProducts();

    Product saveProduct(Product product);

    ResponseEntity<ProductDTO> createProduct(ProductDTO productDTO) throws IllegalAttributeException;

    ResponseEntity<ProductDTO> updateProduct(Long id, ProductDTO productDTO) throws IllegalAttributeException;

    ResponseEntity<String> deleteProduct(Long id);

    void validateProduct(ProductDTO productDTO) throws IllegalAttributeException;

    boolean existsProductById(Long id);

    Product getProductById(Long id) throws ProductNotFoundException;

    HashMap<Long, Integer> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList);

    ExistentProductsRecord getOneAvailableProduct(ProductQuantityRecord quantityRecord);

    void updateProductsQuantity(List<ProductQuantityRecord> quantityRecord);

    void updateProductQuantity(Long idProduct, Integer quantity) throws ProductException;

    ProductRecord getDataProductById(Long id) throws ProductException;
}
