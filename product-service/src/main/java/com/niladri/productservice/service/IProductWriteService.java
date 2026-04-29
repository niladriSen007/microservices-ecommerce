package com.niladri.productservice.service;

import com.niladri.productservice.dto.ProductRequest;
import com.niladri.productservice.dto.ProductResponse;

/**
 * Contract for all write (mutating) product operations.
 */
public interface IProductWriteService {

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(String id, ProductRequest productRequest);

    void deleteProduct(String id);
}
