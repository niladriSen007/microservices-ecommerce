package com.niladri.productservice.dto;

import com.niladri.productservice.model.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String productId;
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal currentPrice;
    private String sellerId;
    private Integer stockQuantity;
    private String categoryName;
    private Map<String, Object> attributes;
    private List<String> imageUrl;
    private StockStatus stockStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
