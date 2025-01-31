package com.product.product_service.services;

import com.product.product_service.dtos.ExistentProductsRecord;
import com.product.product_service.dtos.NewProductRecord;
import com.product.product_service.dtos.ProductQuantityRecord;
import com.product.product_service.dtos.ProductRecord;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface ProductService {

    Product saveProduct(Product product);

    ResponseEntity<Set<ExistentProductsRecord>> getAllProducts();

    Product getProductById(Long id) throws ProductException;

    ResponseEntity<ProductRecord> getDataProductById(Long id) throws ProductException;

    @Transactional(rollbackFor = Exception.class)
    ResponseEntity<Product> createProduct(NewProductRecord newProduct) throws ProductException;

    @Transactional(rollbackFor = Exception.class)
    ResponseEntity<ExistentProductsRecord> updateProduct(Long id, NewProductRecord newProduct) throws ProductException;

    ResponseEntity<String> deleteProductById(Long id) throws ProductException;

    boolean existsProductById(Long id);

    boolean existsProductByName(String name);

    Long getIdByName(String name) throws ProductException;

    HashMap<Long, Integer> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList);

    ExistentProductsRecord getOneAvailableProduct(ProductQuantityRecord quantityRecord);

    void updateProductsQuantity(List<ProductQuantityRecord> quantityRecord);

    @Transactional(rollbackFor = Exception.class)
    void updateProductQuantity(Long idProduct, Integer quantity) throws ProductException;
}
