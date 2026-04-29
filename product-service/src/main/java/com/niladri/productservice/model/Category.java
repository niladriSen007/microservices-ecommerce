package com.niladri.productservice.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Document(collection = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends Auditable {
    @Id
    private String id;

    @NotBlank(message = "Category ID is required")
    @Size(min = 1, max = 50, message = "Category ID must be between 1 and 50 characters")
    @Indexed(unique = true)
    @Field("category_id")
    private String categoryId;

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 255, message = "Category name must be between 1 and 255 characters")
    @Field("name")
    private String name;

    @Field("is_active")
    private Boolean isActive;
}
