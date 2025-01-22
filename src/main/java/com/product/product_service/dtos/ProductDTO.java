package com.product.product_service.dtos;

import com.product.product_service.models.Product;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;

    public ProductDTO() {}

    public ProductDTO(Product product) {
        id = product.getId();
        name = product.getName();
        description = product.getDescription();
        price = product.getPrice();
        stock = product.getStock();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
