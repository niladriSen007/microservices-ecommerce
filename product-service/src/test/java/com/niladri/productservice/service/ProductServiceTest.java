package com.niladri.productservice.service;

import com.niladri.productservice.dto.ProductRequest;
import com.niladri.productservice.dto.ProductResponse;
import com.niladri.productservice.exception.ProductNotFoundException;
import com.niladri.productservice.model.Product;
import com.niladri.productservice.repository.ProductRepository;
import com.niladri.productservice.service.impl.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductService}.
 *
 * No Spring context is loaded — the repository is mocked via Mockito so tests
 * are fast and isolated from the database and Redis.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Wireless Headphones")
                .description("Noise-cancelling headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .category("Electronics")
                .imageUrl("http://example.com/headphones.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = ProductRequest.builder()
                .name("Wireless Headphones")
                .description("Noise-cancelling headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .category("Electronics")
                .imageUrl("http://example.com/headphones.jpg")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should persist product and return correctly mapped response")
        void createProduct_validRequest_returnsMappedResponse() {
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            ProductResponse response = productService.createProduct(sampleRequest);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Wireless Headphones");
            assertThat(response.getDescription()).isEqualTo("Noise-cancelling headphones");
            assertThat(response.getPrice()).isEqualByComparingTo("149.99");
            assertThat(response.getStockQuantity()).isEqualTo(50);
            assertThat(response.getCategory()).isEqualTo("Electronics");
            assertThat(response.getImageUrl()).isEqualTo("http://example.com/headphones.jpg");

            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should default stockQuantity to 0 when null in request")
        void createProduct_nullStockQuantity_defaultsToZero() {
            sampleRequest.setStockQuantity(null);

            Product savedProduct = Product.builder()
                    .id(2L)
                    .name("Wireless Headphones")
                    .price(new BigDecimal("149.99"))
                    .stockQuantity(0)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            ProductResponse response = productService.createProduct(sampleRequest);

            assertThat(response.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("should map null imageUrl without error")
        void createProduct_nullImageUrl_mapsSuccessfully() {
            sampleRequest.setImageUrl(null);

            Product savedProduct = Product.builder()
                    .id(3L)
                    .name("Wireless Headphones")
                    .price(new BigDecimal("149.99"))
                    .stockQuantity(50)
                    .imageUrl(null)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            ProductResponse response = productService.createProduct(sampleRequest);

            assertThat(response.getImageUrl()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — single
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("should return mapped response when product exists")
        void getProductById_existingId_returnsProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            ProductResponse response = productService.getProductById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Wireless Headphones");
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when product does not exist")
        void getProductById_nonExistingId_throwsProductNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — list
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("should return all products mapped as responses")
        void getAllProducts_multipleProducts_returnsAll() {
            Product second = Product.builder()
                    .id(2L)
                    .name("Smart Watch")
                    .price(new BigDecimal("299.99"))
                    .stockQuantity(20)
                    .category("Electronics")
                    .build();

            when(productRepository.findAll()).thenReturn(List.of(sampleProduct, second));

            List<ProductResponse> result = productService.getAllProducts();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProductResponse::getId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void getAllProducts_noProducts_returnsEmptyList() {
            when(productRepository.findAll()).thenReturn(List.of());

            List<ProductResponse> result = productService.getAllProducts();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — by category
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductsByCategory")
    class GetProductsByCategory {

        @Test
        @DisplayName("should return products matching the given category")
        void getProductsByCategory_matchingCategory_returnsList() {
            when(productRepository.findByCategory("Electronics")).thenReturn(List.of(sampleProduct));

            List<ProductResponse> result = productService.getProductsByCategory("Electronics");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("should return empty list for a category with no products")
        void getProductsByCategory_noMatch_returnsEmptyList() {
            when(productRepository.findByCategory("Sports")).thenReturn(List.of());

            List<ProductResponse> result = productService.getProductsByCategory("Sports");

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchProductsByName")
    class SearchProductsByName {

        @Test
        @DisplayName("should return products whose name contains the search term")
        void searchProductsByName_matchFound_returnsList() {
            when(productRepository.findByNameContainingIgnoreCase("head"))
                    .thenReturn(List.of(sampleProduct));

            List<ProductResponse> result = productService.searchProductsByName("head");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).containsIgnoringCase("head");
        }

        @Test
        @DisplayName("should return empty list when no product name matches")
        void searchProductsByName_noMatch_returnsEmptyList() {
            when(productRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

            List<ProductResponse> result = productService.searchProductsByName("xyz");

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("should update all fields and return updated response")
        void updateProduct_existingId_returnsUpdatedProduct() {
            ProductRequest updateRequest = ProductRequest.builder()
                    .name("Gaming Headset")
                    .description("7.1 surround sound")
                    .price(new BigDecimal("199.99"))
                    .stockQuantity(30)
                    .category("Gaming")
                    .imageUrl("http://example.com/gaming.jpg")
                    .build();

            Product updatedProduct = Product.builder()
                    .id(1L)
                    .name("Gaming Headset")
                    .description("7.1 surround sound")
                    .price(new BigDecimal("199.99"))
                    .stockQuantity(30)
                    .category("Gaming")
                    .imageUrl("http://example.com/gaming.jpg")
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            ProductResponse response = productService.updateProduct(1L, updateRequest);

            assertThat(response.getName()).isEqualTo("Gaming Headset");
            assertThat(response.getPrice()).isEqualByComparingTo("199.99");
            assertThat(response.getCategory()).isEqualTo("Gaming");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should preserve existing stockQuantity when request stockQuantity is null")
        void updateProduct_nullStockQuantityInRequest_keepsExistingStock() {
            sampleRequest.setStockQuantity(null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            ProductResponse response = productService.updateProduct(1L, sampleRequest);

            // existing product had stockQuantity = 50 and it should be unchanged
            assertThat(response.getStockQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when product does not exist")
        void updateProduct_nonExistingId_throwsProductNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(99L, sampleRequest))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("99");

            verify(productRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("should call deleteById when product exists")
        void deleteProduct_existingId_deletesSuccessfully() {
            when(productRepository.existsById(1L)).thenReturn(true);

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException and never call deleteById when not found")
        void deleteProduct_nonExistingId_throwsProductNotFoundException() {
            when(productRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(99L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("99");

            verify(productRepository, never()).deleteById(any());
        }
    }
}
