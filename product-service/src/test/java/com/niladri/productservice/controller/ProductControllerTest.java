package com.niladri.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.niladri.productservice.dto.ProductRequest;
import com.niladri.productservice.dto.ProductResponse;
import com.niladri.productservice.exception.GlobalExceptionHandler;
import com.niladri.productservice.exception.ProductNotFoundException;
import com.niladri.productservice.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link ProductController}.
 *
 * Uses pure Mockito + standalone MockMvc — no Spring Boot autoconfigure or
 * application context is loaded, making the tests fast and self-contained.
 * GlobalExceptionHandler is registered manually so exception-mapping is still
 * tested.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final String BASE_URL = "/api/v1/products";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponse sampleResponse;
    private ProductRequest validRequest;

    @BeforeEach
    void setUp() {
        // ObjectMapper that handles LocalDateTime correctly
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Validator that processes @Valid / bean-validation annotations on @RequestBody
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        sampleResponse = ProductResponse.builder()
                .id(1L)
                .name("Wireless Headphones")
                .description("Noise-cancelling headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .category("Electronics")
                .imageUrl("http://example.com/headphones.jpg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();

        validRequest = ProductRequest.builder()
                .name("Wireless Headphones")
                .description("Noise-cancelling headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .category("Electronics")
                .imageUrl("http://example.com/headphones.jpg")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/products
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProduct {

        @Test
        @DisplayName("should return 201 and created product for valid request")
        void createProduct_validRequest_returns201() throws Exception {
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Wireless Headphones"))
                    .andExpect(jsonPath("$.data.price").value(149.99))
                    .andExpect(jsonPath("$.data.category").value("Electronics"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void createProduct_blankName_returns400() throws Exception {
            validRequest.setName("");

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("should return 400 when price is null")
        void createProduct_nullPrice_returns400() throws Exception {
            validRequest.setPrice(null);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.price").exists());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("should return 400 when price is negative")
        void createProduct_negativePrice_returns400() throws Exception {
            validRequest.setPrice(new BigDecimal("-1.00"));

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.price").exists());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("should return 400 when stockQuantity is negative")
        void createProduct_negativeStock_returns400() throws Exception {
            validRequest.setStockQuantity(-1);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.stockQuantity").exists());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("should return 400 when request body is malformed JSON")
        void createProduct_malformedJson_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/products/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("should return 200 and product when found")
        void getProductById_existingId_returns200() throws Exception {
            when(productService.getProductById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Wireless Headphones"));
        }

        @Test
        @DisplayName("should return 404 when product is not found")
        void getProductById_nonExistingId_returns404() throws Exception {
            when(productService.getProductById(99L))
                    .thenThrow(new ProductNotFoundException("Product not found with id: 99"));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/products
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAllProducts {

        @Test
        @DisplayName("should return 200 and full product list")
        void getAllProducts_productsExist_returns200WithList() throws Exception {
            ProductResponse second = ProductResponse.builder()
                    .id(2L).name("Smart Watch").price(new BigDecimal("299.99"))
                    .stockQuantity(20).category("Electronics").build();

            when(productService.getAllProducts()).thenReturn(List.of(sampleResponse, second));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[1].id").value(2));
        }

        @Test
        @DisplayName("should return 200 and empty list when no products exist")
        void getAllProducts_noProducts_returns200WithEmptyList() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/products/category/{category}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/products/category/{category}")
    class GetProductsByCategory {

        @Test
        @DisplayName("should return 200 and products for the given category")
        void getProductsByCategory_matchingCategory_returns200() throws Exception {
            when(productService.getProductsByCategory("Electronics"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/category/Electronics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].category").value("Electronics"));
        }

        @Test
        @DisplayName("should return 200 and empty list when category has no products")
        void getProductsByCategory_noMatch_returns200WithEmptyList() throws Exception {
            when(productService.getProductsByCategory("Sports")).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/category/Sports"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/products/search?name=...
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/products/search")
    class SearchProducts {

        @Test
        @DisplayName("should return 200 and matching products")
        void searchProducts_matchFound_returns200() throws Exception {
            when(productService.searchProductsByName("Wireless"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/search").param("name", "Wireless"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("Wireless Headphones"));
        }

        @Test
        @DisplayName("should return 400 when search term is missing")
        void searchProducts_missingParam_returns400() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/products/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("should return 200 and updated product")
        void updateProduct_existingId_returns200() throws Exception {
            ProductResponse updated = ProductResponse.builder()
                    .id(1L).name("Gaming Headset").price(new BigDecimal("199.99"))
                    .stockQuantity(30).category("Gaming").build();

            when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(updated);

            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Gaming Headset"))
                    .andExpect(jsonPath("$.data.price").value(199.99));
        }

        @Test
        @DisplayName("should return 404 when product to update is not found")
        void updateProduct_nonExistingId_returns404() throws Exception {
            when(productService.updateProduct(eq(99L), any(ProductRequest.class)))
                    .thenThrow(new ProductNotFoundException("Product not found with id: 99"));

            mockMvc.perform(put(BASE_URL + "/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.statusCode").value(404));
        }

        @Test
        @DisplayName("should return 400 when update request body fails validation")
        void updateProduct_invalidBody_returns400() throws Exception {
            validRequest.setName(null);

            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.name").exists());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/products/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("should return 200 when product is deleted successfully")
        void deleteProduct_existingId_returns200() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Product deleted successfully"));
        }

        @Test
        @DisplayName("should return 404 when product to delete is not found")
        void deleteProduct_nonExistingId_returns404() throws Exception {
            doThrow(new ProductNotFoundException("Product not found with id: 99"))
                    .when(productService).deleteProduct(99L);

            mockMvc.perform(delete(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
        }
    }
}
