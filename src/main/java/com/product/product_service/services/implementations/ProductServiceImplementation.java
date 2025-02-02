package com.product.product_service.services.implementations;

import com.product.product_service.dtos.*;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.models.Product;
import com.product.product_service.repositories.ProductRepository;
import com.product.product_service.services.ProductService;
import com.product.product_service.utils.Constants;
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

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public ResponseEntity<Set<ExistentProductsRecord>> getAllProducts() {
        Set<ExistentProductsRecord> products = productRepository.findAll()
                .stream()
                .map(product -> new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), product.getStock()))
                .collect(Collectors.toSet());

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @Override
    public Product getProductById(Long id) throws ProductException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException(Constants.PRODUCT_NOT_FOUND));

        return product;
    }

    @Override
    public ResponseEntity<ProductRecord> getDataProductById(Long id) throws ProductException {
        Product product = getProductById(id);

        return new ResponseEntity<>(new ProductRecord(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock()), HttpStatus.OK);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<Product> createProduct(NewProductRecord newProduct) throws ProductException {
        validateName(newProduct.name());
        validatePrice(newProduct.price());
        validateStock(newProduct.stock());

        Product product = new Product(newProduct.name(), newProduct.description(), newProduct.price(), newProduct.stock());
        saveProduct(product);

        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<ExistentProductsRecord> updateProduct(Long id, NewProductRecord newProduct) throws ProductException {
        validatePrice(newProduct.price());
        validateStock(newProduct.stock());

        Product product = productRepository.findById(id).orElseThrow(()->new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (newProduct.description()!= null && !newProduct.description().isBlank()) {
            product.setDescription(newProduct.description());
        }

        if (newProduct.price()!= null) {
            product.setPrice(newProduct.price());
        }

        if (newProduct.stock()!= null) {
            product.setStock(newProduct.stock());
        }

        if (newProduct.name()!= null && !newProduct.name().equals(product.getName())){
            validateName(newProduct.name());
            product.setName(newProduct.name());
        }

        product = saveProduct(product);

        return new ResponseEntity<>(new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), product.getStock()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteProductById(Long id) throws ProductException {
        if (productRepository.existsById(id)){
            productRepository.deleteById(id);
        } else {
            throw new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(Constants.PRODUCT_DELETED, HttpStatus.OK);
    }


    @Override
    public boolean existsProductById(Long id) {
        return productRepository.existsById(id);
    }

    @Override
    public boolean existsProductByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public Long getIdByName(String name) throws ProductException {
        Product product = productRepository.findByName(name)
                .orElseThrow(()->new ProductException(Constants.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
        return product.getId();
    }


    @Override
    public HashMap<Long, Integer> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList){
        HashMap<Long, Integer> availableProductMap = new HashMap<>();

        productQuantityRecordList.forEach( product -> {
            try {
                Product aux = getProductById(product.id());
                availableProductMap.put(aux.getId(), aux.getStock());
            } catch (ProductException e) {

            }
        });
        return availableProductMap;
    }

    @Override
    public ExistentProductsRecord getOneAvailableProduct(ProductQuantityRecord quantityRecord){
        try {
            Product product = getProductById(quantityRecord.id());
            if (product.getStock()>= quantityRecord.quantity()){
                product.setStock(product.getStock()-quantityRecord.quantity());
                productRepository.save(product);
                return new ExistentProductsRecord(product.getId(), product.getName(), product.getPrice(), quantityRecord.quantity());
            }else{
                return new ExistentProductsRecord(product.getId(), product.getName(),null, quantityRecord.quantity());
            }
        } catch (ProductException e) {
            return null;
        }
    }

    @Override
    public void updateProductsQuantity(List<ProductQuantityRecord> quantityRecord) {
        quantityRecord.forEach(product ->{
            try {
                updateProductQuantity(product.id(), product.quantity());
            } catch (ProductException e) {
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProductQuantity(Long idProduct, Integer quantity) throws ProductException {
        Product product = getProductById(idProduct);
        if (product.getStock() + quantity < 0){
            throw new ProductException(Constants.NEGATIVE_STOCK, HttpStatus.NOT_ACCEPTABLE);
        }

        product.setStock(product.getStock()+quantity);
        productRepository.save(product);

    }

    private void validateName(String name) throws ProductException {
        if (existsProductByName(name)){
            throw new ProductException(Constants.PRODUCT_EXISTS,HttpStatus.CONFLICT);
        } else {
            if (name!=null && name.isBlank()) {
                throw new ProductException(Constants.INVALID_NAME);
            }
        }
    }

    private void validatePrice(Double price) throws ProductException {
        if (price != null && price < 0){
            throw new ProductException(Constants.INVALID_PRICE);
        }
    }

    private void validateStock(Integer stock) throws ProductException {
        if (stock != null && stock < 0){
            throw new ProductException(Constants.INVALID_STOCK);
        }
    }
}
