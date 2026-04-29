package com.niladri.productservice.repository;

import com.niladri.productservice.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category,String> {
    boolean existsByName(String categoryName);
}
