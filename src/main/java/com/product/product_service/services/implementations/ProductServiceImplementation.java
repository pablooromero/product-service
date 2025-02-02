package com.product.product_service.services.implementations;

import com.product.product_service.dtos.*;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.models.Product;
import com.product.product_service.repositories.ProductRepository;
import com.product.product_service.services.ProductService;
import com.product.product_service.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplementation implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplementation.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        logger.debug("Saving product: {}", product);
        Product savedProduct = productRepository.save(product);
        logger.info("Product saved with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public ResponseEntity<Set<ExistentProductsRecord>> getAllProducts() {
        logger.info("Retrieving all products");
        Set<ExistentProductsRecord> products = productRepository.findAll()
                .stream()
                .map(product -> new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), product.getStock()))
                .collect(Collectors.toSet());
        logger.debug("Number of products found: {}", products.size());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @Override
    public Product getProductById(Long id) throws ProductException {
        logger.info("Getting product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductException(Constants.PRODUCT_NOT_FOUND);
                });
        logger.debug("Product found: {}", product);
        return product;
    }

    @Override
    public ResponseEntity<ProductRecord> getDataProductById(Long id) throws ProductException {
        logger.info("Getting detailed product data for ID: {}", id);
        Product product = getProductById(id);
        ProductRecord productRecord = new ProductRecord(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock());
        logger.debug("Product record: {}", productRecord);
        return new ResponseEntity<>(productRecord, HttpStatus.OK);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<Product> createProduct(NewProductRecord newProduct) throws ProductException {
        logger.info("Creating product with name: {}", newProduct.name());
        validateName(newProduct.name());
        validatePrice(newProduct.price());
        validateStock(newProduct.stock());

        Product product = new Product(newProduct.name(), newProduct.description(), newProduct.price(), newProduct.stock());
        saveProduct(product);

        logger.info("Product created successfully with ID: {}", product.getId());
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<ExistentProductsRecord> updateProduct(Long id, NewProductRecord newProduct) throws ProductException {
        logger.info("Updating product with ID: {}", id);
        validatePrice(newProduct.price());
        validateStock(newProduct.stock());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        if (newProduct.description() != null && !newProduct.description().isBlank()) {
            product.setDescription(newProduct.description());
        }

        if (newProduct.price() != null) {
            product.setPrice(newProduct.price());
        }

        if (newProduct.stock() != null) {
            product.setStock(newProduct.stock());
        }

        if (newProduct.name() != null && !newProduct.name().equals(product.getName())) {
            validateName(newProduct.name());
            product.setName(newProduct.name());
        }

        product = saveProduct(product);
        logger.info("Product updated successfully with ID: {}", product.getId());
        ExistentProductsRecord record = new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), product.getStock());
        return new ResponseEntity<>(record, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteProductById(Long id) throws ProductException {
        logger.info("Deleting product with ID: {}", id);
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            logger.info("Product deleted with ID: {}", id);
        } else {
            logger.error("Product not found with ID: {}", id);
            throw new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(Constants.PRODUCT_DELETED, HttpStatus.OK);
    }

    @Override
    public boolean existsProductById(Long id) {
        boolean exists = productRepository.existsById(id);
        logger.debug("Product exists with ID {}: {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsProductByName(String name) {
        boolean exists = productRepository.existsByName(name);
        logger.debug("Product exists with name {}: {}", name, exists);
        return exists;
    }

    @Override
    public Long getIdByName(String name) throws ProductException {
        logger.info("Getting product ID by name: {}", name);
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> {
                    logger.error("Product not found with name: {}", name);
                    return new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
        logger.debug("Product ID found: {}", product.getId());
        return product.getId();
    }

    @Override
    public HashMap<Long, Integer> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList) {
        logger.info("Checking available products for a list of products");
        HashMap<Long, Integer> availableProductMap = new HashMap<>();

        productQuantityRecordList.forEach(product -> {
            try {
                Product aux = getProductById(product.id());
                availableProductMap.put(aux.getId(), aux.getStock());
            } catch (ProductException e) {
                logger.warn("Product not found for ID: {} during availability check", product.id());
            }
        });
        logger.debug("Available products map: {}", availableProductMap);
        return availableProductMap;
    }

    @Override
    public ExistentProductsRecord getOneAvailableProduct(ProductQuantityRecord quantityRecord) {
        logger.info("Getting one available product for ID: {}", quantityRecord.id());
        try {
            Product product = getProductById(quantityRecord.id());
            if (product.getStock() >= quantityRecord.quantity()) {
                product.setStock(product.getStock() - quantityRecord.quantity());
                productRepository.save(product);
                logger.info("Sufficient stock for product ID: {}", product.getId());
                return new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), quantityRecord.quantity());
            } else {
                logger.warn("Insufficient stock for product ID: {}", product.getId());
                return new ExistentProductsRecord(product.getId(), product.getName(), null, quantityRecord.quantity());
            }
        } catch (ProductException e) {
            logger.error("Error retrieving product with ID: {}", quantityRecord.id(), e);
            return null;
        }
    }

    @Override
    public void updateProductsQuantity(List<ProductQuantityRecord> quantityRecord) {
        logger.info("Updating products quantity for a list of products");
        quantityRecord.forEach(product -> {
            try {
                updateProductQuantity(product.id(), product.quantity());
            } catch (ProductException e) {
                logger.error("Error updating product quantity for ID: {}", product.id(), e);
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProductQuantity(Long idProduct, Integer quantity) throws ProductException {
        logger.info("Updating product quantity for product ID: {} with change: {}", idProduct, quantity);
        Product product = getProductById(idProduct);
        if (product.getStock() + quantity < 0) {
            logger.error("Negative stock error for product ID: {}. Current stock: {}, change: {}",
                    idProduct, product.getStock(), quantity);
            throw new ProductException(Constants.NEGATIVE_STOCK, HttpStatus.NOT_ACCEPTABLE);
        }

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        logger.info("Product quantity updated for product ID: {}. New stock: {}", idProduct, product.getStock());
    }

    private void validateName(String name) throws ProductException {
        logger.debug("Validating product name: {}", name);
        if (existsProductByName(name)) {
            logger.error("Product name already exists: {}", name);
            throw new ProductException(Constants.PRODUCT_EXISTS, HttpStatus.CONFLICT);
        } else {
            if (name != null && name.isBlank()) {
                logger.error("Invalid product name: {}", name);
                throw new ProductException(Constants.INVALID_NAME);
            }
        }
    }

    private void validatePrice(Double price) throws ProductException {
        logger.debug("Validating product price: {}", price);
        if (price != null && price < 0) {
            logger.error("Invalid product price: {}", price);
            throw new ProductException(Constants.INVALID_PRICE);
        }
    }

    private void validateStock(Integer stock) throws ProductException {
        logger.debug("Validating product stock: {}", stock);
        if (stock != null && stock < 0) {
            logger.error("Invalid product stock: {}", stock);
            throw new ProductException(Constants.INVALID_STOCK);
        }
    }
}
