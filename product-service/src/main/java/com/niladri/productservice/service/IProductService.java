package com.niladri.productservice.service;

/**
 * Main product service contract.
 * Extends both {@link IProductReadService} and {@link IProductWriteService}
 * so that any component depending on this interface gets full read + write
 * access.
 */
public interface IProductService extends IProductReadService, IProductWriteService {
    // no additional methods — acts as a single entry-point marker interface
}
