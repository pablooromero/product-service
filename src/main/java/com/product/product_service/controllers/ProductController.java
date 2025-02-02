package com.product.product_service.controllers;

import com.product.product_service.dtos.*;
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
import java.util.Set;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieve a list of all products available to the public")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of products",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExistentProductsRecord.class),
                            examples = @ExampleObject(value = "[{\"id\": 1, \"name\": \"Product 1\", \"description\": \"Description of product 1\", \"price\": \"5\", \"stock\": 10}]")
                    )
            )
    })
    @GetMapping("/public")
    public ResponseEntity<Set<ExistentProductsRecord>> getAllProducts() {
        return productService.getAllProducts();
    }


    @Operation(summary = "Get product by ID (admin)", description = "Retrieve detailed product information by its ID (admin access)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductRecord.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product not found with ID: 1")))
    })
    @GetMapping("admin/{id}")
    public ResponseEntity<ProductRecord> getProductById(@PathVariable Long id) throws ProductException {
        return productService.getDataProductById(id);
    }


    @Operation(summary = "Create a new product", description = "Create a new product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NewProductRecord.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product name cannot be null or empty")
                    )
            )
    })
    @PostMapping("/admin")
    public ResponseEntity<Product> createProduct(@RequestBody NewProductRecord newProductRecord) throws ProductException {
        return productService.createProduct(newProductRecord);
    }


    @Operation(summary = "Update a product", description = "Update an existing product by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExistentProductsRecord.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Product name cannot be null or empty")
                    )
            )
    })
    @PutMapping("/admin/{id}")
    public ResponseEntity<ExistentProductsRecord> updateProduct(@PathVariable Long id, @RequestBody NewProductRecord newProductRecord) throws ProductException {
        return productService.updateProduct(id, newProductRecord);
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
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteProductById(@PathVariable Long id) throws ProductException {
        return productService.deleteProductById(id);
    }


    @Operation(summary = "Check available products", description = "Check the availability and stock for a list of products with requested quantities")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of available products and their quantities",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HashMap.class),
                            examples = @ExampleObject(value = "{\"1\": 5, \"2\": 3}")))
    })
    @PutMapping("/private")
    public ResponseEntity<HashMap<Long, Integer>> existsProducts(@RequestBody List<ProductQuantityRecord> recordList){
        HashMap<Long, Integer> products = productService.getAllAvailableProducts(recordList);
        return ResponseEntity.ok(products);
    }


    @Operation(summary = "Update product quantities for an order", description = "Update the quantities of products based on an order (private access)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products updated successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = Constants.UPDATED_PDT))),
            @ApiResponse(responseCode = "400", description = "Validation errors or insufficient stock",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Insufficient stock for product with ID: 1")))
    })
    @PutMapping("private/to-order")
    public ResponseEntity<String> existProduct(@RequestBody List<ProductQuantityRecord> quantityRecord) throws ProductException {
        productService.updateProductsQuantity(quantityRecord);
        return new ResponseEntity<String>(Constants.UPDATED_PDT, HttpStatus.OK);
    }
}
