package com.niladri.productservice.dto;

import com.niladri.productservice.model.Category;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @NotNull(message = "Original Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Original Price must be zero or greater")
    @DecimalMax(value = "999999.99", message = "Original Price must not exceed 999,999.99")
    private BigDecimal originalPrice;

    @NotNull(message = "Current Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Current Price must be zero or greater")
    @DecimalMax(value = "999999.99", message = "Current Price must not exceed 999,999.99")
    private BigDecimal currentPrice;

    @NotBlank(message = "Seller ID is required")
    private String sellerId;

    @Min(value = 0, message = "Stock quantity must be zero or greater")
    private Integer stockQuantity;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName;

    private Map<String, Object> attributes;

    private List<String> imageUrl;
}
