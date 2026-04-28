package com.niladri.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be zero or greater")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999,999.99")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity must be zero or greater")
    private Integer stockQuantity;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

//    @Pattern(regexp = "^(https?://.*)?$", message = "Image URL must start with http:// or https://")
//    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}
