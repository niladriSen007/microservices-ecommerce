package com.niladri.productservice.cache;

/**
 * Central registry of all Redis cache names used by the product service.
 * Using constants avoids magic strings in @Cacheable / @CacheEvict annotations.
 */
public final class CacheNames {

    /** Single product cached by its ID. TTL: 10 minutes. */
    public static final String PRODUCT = "product";

    /** Full list of all products. TTL: 5 minutes. */
    public static final String PRODUCT_LIST = "productList";

    /** Products grouped by category. TTL: 5 minutes. */
    public static final String PRODUCTS_BY_CATEGORY = "productsByCategory";

    private CacheNames() {
        // utility class — not instantiable
    }
}
