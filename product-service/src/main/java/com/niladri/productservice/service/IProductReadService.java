package com.niladri.productservice.service;

import com.niladri.productservice.dto.ProductResponse;

import java.util.List;

/**
 * Contract for all read-only product operations.
 */
public interface IProductReadService {

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> searchProductsByName(String name);
}
