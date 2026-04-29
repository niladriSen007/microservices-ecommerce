package com.niladri.productservice.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@CompoundIndex(name = "idx_products_name_category", def = "{'name': 1, 'category': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends Auditable {

    @Id
    private String id;

    @Field("product_id")
    private String productId;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    @Indexed
    @Field("name")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Field("description")
    private String description;

    @NotNull(message = "Original Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Original Price must be zero or greater")
    @DecimalMax(value = "999999.99", message = "Original Price must not exceed 999,999.99")
    @Field("original_price")
    private BigDecimal originalPrice;

    @NotNull(message = "Current Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Current Price must be zero or greater")
    @DecimalMax(value = "999999.99", message = "Current Price must not exceed 999,999.99")
    @Field("current_price")
    private BigDecimal currentPrice;

    @NotNull(message = "Seller ID is required")
    @Field("seller_id")
    private String sellerId;

    @Min(value = 0, message = "Stock quantity must be zero or greater")
    @Builder.Default
    @Field("stock_quantity")
    private Integer stockQuantity = 0;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Indexed
    @Field("category")
    @DBRef(lazy = true)
    private Category category;

    @Field("attributes")
    private Map<String, Object> attributes;

    @Field("image_url")
    private List<String> imageUrl;

    @Field("stock_status")
    private StockStatus stockStatus;

    @Field("reviews")
    private ReviewSummary reviews;

    @Field("computed")
    private ComputedFields computed;

}
