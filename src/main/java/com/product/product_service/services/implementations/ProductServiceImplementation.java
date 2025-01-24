package com.product.product_service.services.implementations;

import com.product.product_service.dtos.ExistentProductsRecord;
import com.product.product_service.dtos.ProductDTO;
import com.product.product_service.dtos.ProductQuantityRecord;
import com.product.product_service.exceptions.IllegalAttributeException;
import com.product.product_service.exceptions.ProductNotFoundException;
import com.product.product_service.models.Product;
import com.product.product_service.repositories.ProductRepository;
import com.product.product_service.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImplementation implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public ResponseEntity<ProductDTO> createProduct(ProductDTO productDTO) throws IllegalAttributeException {
        validateProduct(productDTO);

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());

        Product savedProduct = saveProduct(product);
        return new ResponseEntity<>(new ProductDTO(savedProduct), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProductDTO> updateProduct(Long id, ProductDTO productDTO) throws IllegalAttributeException {
        validateProduct(productDTO);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());

        Product updatedProduct = saveProduct(existingProduct);
        return new ResponseEntity<>(new ProductDTO(updatedProduct), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        productRepository.deleteById(id);
        return new ResponseEntity<>("Product deleted!", HttpStatus.OK);
    }

    @Override
    public void validateProduct(ProductDTO productDTO) throws IllegalAttributeException {

        if(productDTO.getName() == null) {
            throw new IllegalAttributeException("Name cannot be null or empty");
        }

        if(productDTO.getDescription() == null) {
            throw new IllegalAttributeException("Description cannot be null or empty");
        }

        if(productDTO.getPrice() == null) {
            throw new IllegalAttributeException("Price cannot be null or empty");
        }
    }

    @Override
    public boolean existsProductById(Long id) {
        return productRepository.existsById(id);
    }

    @Override
    public Product getProductById(Long id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(()->new ProductNotFoundException("Product not found with ID: " + id));

    }

    @Override
    public ResponseEntity<List<ExistentProductsRecord>> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList){
        List<ExistentProductsRecord> listOfProducts = new ArrayList<>();

        productQuantityRecordList.forEach( product -> {
            if (existsProductById(product.id())){
                try {
                    Product realProduct = getProductById(product.id());

                    if (realProduct.getStock()>=product.quantity()){
                        listOfProducts.add(new ExistentProductsRecord(product.id(), realProduct.getPrice(), product.quantity()));
                        realProduct.setStock(realProduct.getStock() - product.quantity());

                        productRepository.save(realProduct);
                    } else {
                        listOfProducts.add(new ExistentProductsRecord(product.id(), null, realProduct.getStock()));
                    }
                } catch (ProductNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return new ResponseEntity<>(listOfProducts, HttpStatus.OK);
    }
}
