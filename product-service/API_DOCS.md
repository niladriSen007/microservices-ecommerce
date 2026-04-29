# Product Service — API Documentation

**Base URL:** `http://localhost:8082/api/v1/products`  
**Content-Type:** `application/json`

---

## Response Structure

All endpoints return a unified `ApiResponse` envelope:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "...",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

| Field        | Type           | Description                                      |
|--------------|----------------|--------------------------------------------------|
| `success`    | boolean        | `true` on success, `false` on failure            |
| `statusCode` | int            | HTTP status code                                 |
| `message`    | string         | Human-readable result message                    |
| `data`       | object / array | Response payload (null on delete or error)       |
| `errors`     | map / string   | Validation errors map or error message (on fail) |
| `timestamp`  | ISO-8601       | Time the response was generated                  |

---

## Endpoints

### 1. Create Product

| Property | Value              |
|----------|--------------------|
| Method   | `POST`             |
| URL      | `/api/v1/products` |
| Status   | `201 Created`      |

> **Note:** A new Category document is created using `categoryName`. The request fails if a category with that name already exists.

**Request Body:**

```json
{
  "name": "Wireless Noise-Cancelling Headphones",
  "description": "Over-ear headphones with active noise cancellation and 30-hour battery life.",
  "originalPrice": 2999.00,
  "currentPrice": 2499.99,
  "sellerId": "seller_abc123",
  "stockQuantity": 150,
  "categoryName": "Electronics",
  "attributes": {
    "brand": "SoundMax",
    "color": "Midnight Black",
    "connectivity": "Bluetooth 5.3"
  },
  "imageUrl": [
    "https://cdn.example.com/products/headphones-front.jpg",
    "https://cdn.example.com/products/headphones-side.jpg"
  ]
}
```

**Request Fields:**

| Field           | Type              | Required | Constraints               |
|-----------------|-------------------|----------|---------------------------|
| `name`          | string            | Yes      | 1–255 characters          |
| `description`   | string            | No       | Max 2000 characters       |
| `originalPrice` | number            | Yes      | 0.00–999,999.99           |
| `currentPrice`  | number            | Yes      | 0.00–999,999.99           |
| `sellerId`      | string            | Yes      | Must not be blank         |
| `stockQuantity` | integer           | No       | ≥ 0 (defaults to 0)       |
| `categoryName`  | string            | Yes      | 1–100 characters          |
| `attributes`    | object            | No       | Arbitrary key-value pairs |
| `imageUrl`      | array of strings  | No       | List of image URLs        |

**Example Response (`201`):**

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Product created successfully",
  "data": {
    "id": "6630f1a2b4e3c20012ef9abc",
    "productId": "prod_4a7f9c2e1b3d",
    "name": "Wireless Noise-Cancelling Headphones",
    "description": "Over-ear headphones with active noise cancellation and 30-hour battery life.",
    "originalPrice": 2999.00,
    "currentPrice": 2499.99,
    "sellerId": "seller_abc123",
    "stockQuantity": 150,
    "categoryName": "Electronics",
    "attributes": {
      "brand": "SoundMax",
      "color": "Midnight Black",
      "connectivity": "Bluetooth 5.3"
    },
    "imageUrl": [
      "https://cdn.example.com/products/headphones-front.jpg",
      "https://cdn.example.com/products/headphones-side.jpg"
    ],
    "stockStatus": null,
    "createdAt": "2026-04-29T10:00:00",
    "updatedAt": "2026-04-29T10:00:00"
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### 2. Get All Products

| Property | Value              |
|----------|--------------------|
| Method   | `GET`              |
| URL      | `/api/v1/products` |
| Status   | `200 OK`           |

No request body or parameters required.

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": "6630f1a2b4e3c20012ef9abc",
      "productId": "prod_4a7f9c2e1b3d",
      "name": "Wireless Noise-Cancelling Headphones",
      "originalPrice": 2999.00,
      "currentPrice": 2499.99,
      "sellerId": "seller_abc123",
      "stockQuantity": 150,
      "categoryName": "Electronics",
      "attributes": { "brand": "SoundMax" },
      "imageUrl": ["https://cdn.example.com/products/headphones-front.jpg"],
      "stockStatus": null,
      "createdAt": "2026-04-29T10:00:00",
      "updatedAt": "2026-04-29T10:00:00"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### 3. Get Product by ID

| Property | Value                          |
|----------|--------------------------------|
| Method   | `GET`                          |
| URL      | `/api/v1/products/{productId}` |
| Status   | `200 OK`                       |

> Uses the business `productId` (e.g. `prod_4a7f9c2e1b3d`), **not** the internal MongoDB `id`.

**Path Parameters:**

| Parameter   | Type   | Required | Constraints         |
|-------------|--------|----------|---------------------|
| `productId` | string | Yes      | Must not be blank   |

**Example:**
```
GET /api/v1/products/prod_4a7f9c2e1b3d
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product retrieved successfully",
  "data": {
    "id": "6630f1a2b4e3c20012ef9abc",
    "productId": "prod_4a7f9c2e1b3d",
    "name": "Wireless Noise-Cancelling Headphones",
    "originalPrice": 2999.00,
    "currentPrice": 2499.99,
    "sellerId": "seller_abc123",
    "stockQuantity": 150,
    "categoryName": "Electronics",
    "attributes": { "brand": "SoundMax" },
    "imageUrl": ["https://cdn.example.com/products/headphones-front.jpg"],
    "stockStatus": null,
    "createdAt": "2026-04-29T10:00:00",
    "updatedAt": "2026-04-29T10:00:00"
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

**Error Response (`404`):**

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Product not found with id: prod_4a7f9c2e1b3d",
  "errors": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### 4. Get Products by Category

| Property | Value                                  |
|----------|----------------------------------------|
| Method   | `GET`                                  |
| URL      | `/api/v1/products/category/{category}` |
| Status   | `200 OK`                               |

**Path Parameters:**

| Parameter  | Type   | Required | Constraints       |
|------------|--------|----------|-------------------|
| `category` | string | Yes      | Must not be blank |

**Example:**
```
GET /api/v1/products/category/Electronics
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": "6630f1a2b4e3c20012ef9abc",
      "productId": "prod_4a7f9c2e1b3d",
      "name": "Wireless Noise-Cancelling Headphones",
      "originalPrice": 2999.00,
      "currentPrice": 2499.99,
      "sellerId": "seller_abc123",
      "stockQuantity": 150,
      "categoryName": "Electronics",
      "stockStatus": null,
      "createdAt": "2026-04-29T10:00:00",
      "updatedAt": "2026-04-29T10:00:00"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### 5. Search Products by Name

| Property | Value                     |
|----------|---------------------------|
| Method   | `GET`                     |
| URL      | `/api/v1/products/search` |
| Status   | `200 OK`                  |

**Query Parameters:**

| Parameter | Type   | Required | Constraints       |
|-----------|--------|----------|-------------------|
| `name`    | string | Yes      | Must not be blank |

**Example:**
```
GET /api/v1/products/search?name=Headphones
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Search results retrieved successfully",
  "data": [
    {
      "id": "6630f1a2b4e3c20012ef9abc",
      "productId": "prod_4a7f9c2e1b3d",
      "name": "Wireless Noise-Cancelling Headphones",
      "originalPrice": 2999.00,
      "currentPrice": 2499.99,
      "sellerId": "seller_abc123",
      "stockQuantity": 150,
      "categoryName": "Electronics",
      "stockStatus": null,
      "createdAt": "2026-04-29T10:00:00",
      "updatedAt": "2026-04-29T10:00:00"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### 6. Update Product

| Property | Value                          |
|----------|--------------------------------|
| Method   | `PUT`                          |
| URL      | `/api/v1/products/{productId}` |
| Status   | `200 OK`                       |

> Uses the business `productId`. Request body follows the same structure as Create.

**Path Parameters:**

| Parameter   | Type   | Required | Constraints       |
|-------------|--------|----------|-------------------|
| `productId` | string | Yes      | Must not be blank |

**Request Body:**

```json
{
  "name": "Wireless Noise-Cancelling Headphones Pro",
  "description": "Updated model with improved ANC and 40-hour battery life.",
  "originalPrice": 3499.00,
  "currentPrice": 2999.99,
  "sellerId": "seller_abc123",
  "stockQuantity": 100,
  "categoryName": "Electronics",
  "attributes": {
    "brand": "SoundMax",
    "color": "Platinum Silver"
  },
  "imageUrl": [
    "https://cdn.example.com/products/headphones-pro.jpg"
  ]
}
```

**Example:**
```
PUT /api/v1/products/prod_4a7f9c2e1b3d
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product updated successfully",
  "data": {
    "id": "6630f1a2b4e3c20012ef9abc",
    "productId": "prod_4a7f9c2e1b3d",
    "name": "Wireless Noise-Cancelling Headphones Pro",
    "originalPrice": 3499.00,
    "currentPrice": 2999.99,
    "sellerId": "seller_abc123",
    "stockQuantity": 100,
    "categoryName": "Electronics",
    "attributes": { "brand": "SoundMax", "color": "Platinum Silver" },
    "imageUrl": ["https://cdn.example.com/products/headphones-pro.jpg"],
    "stockStatus": null,
    "createdAt": "2026-04-29T10:00:00",
    "updatedAt": "2026-04-29T10:30:00"
  },
  "timestamp": "2026-04-29T10:30:00"
}
```

---

### 7. Delete Product

| Property | Value                          |
|----------|--------------------------------|
| Method   | `DELETE`                       |
| URL      | `/api/v1/products/{productId}` |
| Status   | `200 OK`                       |

> Uses the business `productId`.

**Path Parameters:**

| Parameter   | Type   | Required | Constraints       |
|-------------|--------|----------|-------------------|
| `productId` | string | Yes      | Must not be blank |

**Example:**
```
DELETE /api/v1/products/prod_4a7f9c2e1b3d
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product deleted successfully",
  "data": null,
  "errors": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

---

## Response Fields Reference

| Field           | Type             | Description                                          |
|-----------------|------------------|------------------------------------------------------|
| `id`            | string           | Internal MongoDB ObjectId                            |
| `productId`     | string           | Auto-generated business ID (`prod_<12-char hex>`)    |
| `name`          | string           | Product name                                         |
| `description`   | string           | Product description                                  |
| `originalPrice` | number           | Original / listed price                              |
| `currentPrice`  | number           | Current / selling price (may reflect discounts)      |
| `sellerId`      | string           | ID of the seller who owns this product               |
| `stockQuantity` | integer          | Available units in stock                             |
| `categoryName`  | string           | Name of the associated category                      |
| `attributes`    | object           | Arbitrary product attributes (brand, color, etc.)    |
| `imageUrl`      | array of strings | Ordered list of image URLs                           |
| `stockStatus`   | string (enum)    | `IN_STOCK`, `OUT_OF_STOCK`, or `PREORDER` (nullable) |
| `createdAt`     | ISO-8601         | Timestamp when the product was created               |
| `updatedAt`     | ISO-8601         | Timestamp of the last update                         |

---

## Validation Error Response

When the request body fails validation (`400 Bad Request`):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation failed",
  "errors": {
    "name": "Product name is required",
    "originalPrice": "Original Price is required",
    "currentPrice": "Current Price is required",
    "sellerId": "Seller ID is required",
    "categoryName": "Category name is required"
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

---

## Error Reference

| Status | Scenario                                             |
|--------|------------------------------------------------------|
| 400    | Validation failure (missing/invalid fields)          |
| 400    | Malformed or unreadable JSON body                    |
| 400    | Required query/path parameter missing or blank       |
| 404    | Product not found for the given `productId`          |
| 405    | HTTP method not supported on the endpoint            |
| 500    | Category name already exists (duplicate `categoryName`) |
| 500    | Unexpected internal server error                     |
