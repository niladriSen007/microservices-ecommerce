package com.niladri.productservice.controller;

import com.niladri.productservice.dto.ApiResponse;
import com.niladri.productservice.dto.ProductRequest;
import com.niladri.productservice.dto.ProductResponse;
import com.niladri.productservice.service.IProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

        private final IProductService productService;

        // ── CREATE ────────────────────────────────────────────────────────────────

        @PostMapping
        public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
                        @Valid @RequestBody ProductRequest productRequest) {

                ProductResponse created = productService.createProduct(productRequest);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success(created, "Product created successfully",
                                                HttpStatus.CREATED.value()));
        }

        // ── READ ─────────────────────────────────────────────────────────────────

        @GetMapping("/{productId}")
        public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
                        @PathVariable("productId") @NotBlank(message = "Product ID must not be blank") String productId) {

                ProductResponse product = productService.getProductById(productId);
                return ResponseEntity.ok(
                                ApiResponse.success(product, "Product retrieved successfully", HttpStatus.OK.value()));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
                List<ProductResponse> products = productService.getAllProducts();
                return ResponseEntity.ok(
                                ApiResponse.success(products, "Products retrieved successfully",
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/category/{category}")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
                        @PathVariable("category") @NotBlank(message = "Category must not be blank") String category) {

                List<ProductResponse> products = productService.getProductsByCategory(category);
                return ResponseEntity.ok(
                                ApiResponse.success(products, "Products retrieved successfully",
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
                        @RequestParam("name") @NotBlank(message = "Search term must not be blank") String name) {

                List<ProductResponse> products = productService.searchProductsByName(name);
                return ResponseEntity.ok(
                                ApiResponse.success(products, "Search results retrieved successfully",
                                                HttpStatus.OK.value()));
        }

        // ── UPDATE ────────────────────────────────────────────────────────────────

        @PutMapping("/{productId}")
        public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
                        @PathVariable("productId") @NotBlank(message = "Product ID must not be blank") String productId,
                        @Valid @RequestBody ProductRequest productRequest) {

                ProductResponse updated = productService.updateProduct(productId, productRequest);
                return ResponseEntity.ok(
                                ApiResponse.success(updated, "Product updated successfully", HttpStatus.OK.value()));
        }

        // ── DELETE ────────────────────────────────────────────────────────────────

        @DeleteMapping("/{productId}")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(
                        @PathVariable("productId") @NotBlank(message = "Product ID must not be blank") String productId) {

                productService.deleteProduct(productId);
                return ResponseEntity.ok(
                                ApiResponse.success(null, "Product deleted successfully", HttpStatus.OK.value()));
        }
}
