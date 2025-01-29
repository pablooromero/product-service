package com.product.product_service.controllers;

import com.product.product_service.dtos.ExistentProductsRecord;
import com.product.product_service.dtos.ProductDTO;
import com.product.product_service.dtos.ProductQuantityRecord;
import com.product.product_service.dtos.ProductRecord;
import com.product.product_service.exceptions.IllegalAttributeException;
import com.product.product_service.exceptions.ProductException;
import com.product.product_service.models.Product;
import com.product.product_service.services.ProductService;
import com.product.product_service.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieve a list of all products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of products",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "[{\"id\": 1, \"name\": \"Product 1\", \"description\": \"Description of product 1\", \"price\": \"5\", \"stock\": 10}]")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return productService.getAllProducts();
    }

    @Operation(summary = "Create a new product", description = "Create a new product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product name cannot be null or empty")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) throws IllegalAttributeException {
        return productService.createProduct(productDTO);
    }


    @Operation(summary = "Update a product", description = "Update an existing product by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product name cannot be null or empty")
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) throws IllegalAttributeException {
        return productService.updateProduct(id, productDTO);
    }

    @Operation(summary = "Delete a product", description = "Delete an existing product by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product not found with ID: 1")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @PutMapping()
    public ResponseEntity<HashMap<Long, Integer>> existsProducts(@RequestBody List<ProductQuantityRecord> recordList){
        HashMap<Long, Integer> products = productService.getAllAvailableProducts(recordList);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/to-order")
    public ResponseEntity<String> existProduct(@RequestBody List<ProductQuantityRecord> quantityRecord) throws ProductException {
        productService.updateProductsQuantity(quantityRecord);
        return new ResponseEntity<String>(Constants.UPDATED_PDT, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRecord> getProductById(@PathVariable Long id) throws ProductException {
        ProductRecord product = productService.getDataProductById(id);
        return ResponseEntity.ok(product);
    }
}
