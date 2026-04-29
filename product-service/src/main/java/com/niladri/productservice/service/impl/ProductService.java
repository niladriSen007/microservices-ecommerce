package com.niladri.productservice.service.impl;

import com.niladri.productservice.cache.CacheNames;
import com.niladri.productservice.dto.ProductRequest;
import com.niladri.productservice.dto.ProductResponse;
import com.niladri.productservice.exception.CategoryAlreadyExists;
import com.niladri.productservice.exception.ProductNotFoundException;
import com.niladri.productservice.model.Category;
import com.niladri.productservice.model.Product;
import com.niladri.productservice.repository.CategoryRepository;
import com.niladri.productservice.repository.ProductRepository;
import com.niladri.productservice.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * On create, invalidate the full-list and category caches so
     * subsequent reads reflect the new product.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.PRODUCTS_BY_CATEGORY, allEntries = true)
    })
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());

        String categoryName = productRequest.getCategoryName();
        boolean isCategoryExists = categoryRepository.existsByName(categoryName);
        if (isCategoryExists) {
            throw new CategoryAlreadyExists("Category with name - " + categoryName + "already exists");
        }

        String categoryId = generateCategoryId();
        Category category = Category.builder().categoryId(categoryId).name(categoryName).build();
        Category savedCategory = categoryRepository.save(category);

        String productId = generateProductId();
        Product product = Product.builder()
                .name(productRequest.getName())
                .productId(productId)
                .description(productRequest.getDescription())
                .originalPrice(productRequest.getOriginalPrice())
                .currentPrice(productRequest.getCurrentPrice())
                .sellerId(productRequest.getSellerId())
                .stockQuantity(productRequest.getStockQuantity() != null ? productRequest.getStockQuantity() : 0)
                .category(savedCategory)
                .attributes(productRequest.getAttributes())
                .imageUrl(productRequest.getImageUrl())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());
        return mapToProductResponse(savedProduct);
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Cache individual product by ID for 10 minutes.
     * Cache miss → DB hit → result stored under key "product::<id>".
     */
    @Override
    @Cacheable(value = CacheNames.PRODUCT, key = "#id")
    public ProductResponse getProductById(String id) {
        log.info("Cache miss — fetching product from DB with id: {}", id);
        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    /**
     * Cache the entire product list for 5 minutes.
     */
    @Override
    @Cacheable(value = CacheNames.PRODUCT_LIST, key = "'all'")
    public List<ProductResponse> getAllProducts() {
        log.info("Cache miss — fetching all products from DB");
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    /**
     * Cache products per category for 5 minutes.
     */
    @Override
    @Cacheable(value = CacheNames.PRODUCTS_BY_CATEGORY, key = "#category")
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Cache miss — fetching products from DB for category: {}", category);
        return productRepository.findByCategory(category)
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    /**
     * Search results are not cached — they are highly dynamic and vary per keyword.
     */
    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        log.info("Searching products with name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * @CachePut updates the single-product cache with the fresh value.
     * @CacheEvict invalidates list caches so they are re-fetched next read.
     */
    @Override
    @Caching(put = {@CachePut(value = CacheNames.PRODUCT, key = "#id")}, evict = {
            @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.PRODUCTS_BY_CATEGORY, allEntries = true)
    })
    public ProductResponse updateProduct(String id, ProductRequest productRequest) {
        log.info("Updating product with id: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setOriginalPrice(productRequest.getOriginalPrice());
        existingProduct.setCurrentPrice(productRequest.getCurrentPrice());
        existingProduct.setSellerId(productRequest.getSellerId());
        if (productRequest.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productRequest.getStockQuantity());
        }
//        existingProduct.setCategory(productRequest.getCategoryName());
        existingProduct.setAttributes(productRequest.getAttributes());
        existingProduct.setImageUrl(productRequest.getImageUrl());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated with id: {}", updatedProduct.getId());
        return mapToProductResponse(updatedProduct);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Evict single-product entry plus all list caches.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PRODUCT, key = "#id"),
            @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.PRODUCTS_BY_CATEGORY, allEntries = true)
    })
    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .originalPrice(product.getOriginalPrice())
                .currentPrice(product.getCurrentPrice())
                .sellerId(product.getSellerId())
                .stockQuantity(product.getStockQuantity())
                .categoryName(product.getCategory().getName())
                .attributes(product.getAttributes())
                .imageUrl(product.getImageUrl())
                .stockStatus(product.getStockStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String generateCategoryId(){
        return "cat_" + shortUuid();
    }

    private String generateProductId(){
        return "prod_" + shortUuid();
    }

    private static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
