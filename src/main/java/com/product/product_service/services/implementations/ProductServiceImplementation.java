package com.product.product_service.services.implementations;

import com.product.product_service.dtos.ExistentProductsRecord;
import com.product.product_service.dtos.ProductDTO;
import com.product.product_service.dtos.ProductQuantityRecord;
import com.product.product_service.dtos.ProductRecord;
import com.product.product_service.exceptions.IllegalAttributeException;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.exceptions.ProductNotFoundException;
import com.product.product_service.models.Product;
import com.product.product_service.repositories.ProductRepository;
import com.product.product_service.services.ProductService;
import com.product.product_service.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    public HashMap<Long, Integer> getAllAvailableProducts(List<ProductQuantityRecord> productQuantityRecordList){
        HashMap<Long, Integer> availableProductMap = new HashMap<>();

        productQuantityRecordList.forEach( product -> {
            try{
                Product aux = getProductById(product.id());
                availableProductMap.put(aux.getId(), aux.getStock());
            }catch (ProductNotFoundException e){

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
                return new ExistentProductsRecord(product.getId(), product.getPrice(), quantityRecord.quantity());
            }else{
                return new ExistentProductsRecord(product.getId(), null, quantityRecord.quantity());
            }
        } catch (ProductNotFoundException e) {
            return null;
        }
    }

    @Override
    public void updateProductsQuantity(List<ProductQuantityRecord> quantityRecord){
        quantityRecord.forEach(product ->{
            try {
                updateProductQuantity(product.id(), product.quantity());
            } catch (ProductException e) {
            }
        });
    }

    @Override
    public void updateProductQuantity(Long idProduct, Integer quantity) throws ProductException {
        Product product = getProductById(idProduct);
        if (product.getStock()+quantity<0){
            throw new ProductException(Constants.NEGATIVE_STOCK, HttpStatus.NOT_ACCEPTABLE);
        }
        product.setStock(product.getStock()+quantity);
        productRepository.save(product);
    }

    @Override
    public ProductRecord getDataProductById(Long id) throws ProductException {
        Product product = getProductById(id);
        return new ProductRecord(product.getId(),product.getName(), product.getDescription(), product.getPrice(), product.getStock());
    }
}
