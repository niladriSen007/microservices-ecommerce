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
  "timestamp": "2026-04-28T10:00:00Z"
}
```

| Field       | Type              | Description                                      |
|-------------|-------------------|--------------------------------------------------|
| `success`   | boolean           | `true` on success, `false` on failure            |
| `statusCode`| int               | HTTP status code                                 |
| `message`   | string            | Human-readable result message                    |
| `data`      | object / array    | Response payload (null on delete or error)       |
| `errors`    | map / string      | Validation errors map or error message (on fail) |
| `timestamp` | ISO-8601 string   | Time the response was generated                  |

---

## Endpoints

### 1. Create Product

| Property  | Value                    |
|-----------|--------------------------|
| Method    | `POST`                   |
| URL       | `/api/v1/products`       |
| Status    | `201 Created`            |

**Request Body:**

```json
{
  "name": "Wireless Noise-Cancelling Headphones",
  "description": "Over-ear headphones with active noise cancellation and 30-hour battery life.",
  "price": 2499.99,
  "stockQuantity": 150,
  "category": "Electronics",
  "imageUrl": "https://cdn.example.com/products/headphones.jpg"
}
```

**Request Fields:**

| Field           | Type       | Required | Constraints                              |
|-----------------|------------|----------|------------------------------------------|
| `name`          | string     | Yes      | 1–255 characters                         |
| `description`   | string     | No       | Max 2000 characters                      |
| `price`         | number     | Yes      | 0.00–999999.99                           |
| `stockQuantity` | integer    | No       | ≥ 0                                      |
| `category`      | string     | No       | Max 100 characters                       |
| `imageUrl`      | string     | No       | Valid URL (http/https)                   |

**Example Response (`201`):**

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Wireless Noise-Cancelling Headphones",
    "description": "Over-ear headphones with active noise cancellation and 30-hour battery life.",
    "price": 2499.99,
    "stockQuantity": 150,
    "category": "Electronics",
    "imageUrl": "https://cdn.example.com/products/headphones.jpg",
    "createdAt": "2026-04-28T10:00:00Z",
    "updatedAt": "2026-04-28T10:00:00Z"
  },
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

### 2. Get All Products

| Property  | Value                    |
|-----------|--------------------------|
| Method    | `GET`                    |
| URL       | `/api/v1/products`       |
| Status    | `200 OK`                 |

No request body or parameters required.

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Wireless Noise-Cancelling Headphones",
      "price": 2499.99,
      "stockQuantity": 150,
      "category": "Electronics"
    }
  ],
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

### 3. Get Product by ID

| Property  | Value                         |
|-----------|-------------------------------|
| Method    | `GET`                         |
| URL       | `/api/v1/products/{productId}`|
| Status    | `200 OK`                      |

**Path Parameters:**

| Parameter   | Type   | Required | Constraints           |
|-------------|--------|----------|-----------------------|
| `productId` | long   | Yes      | Must be a positive number |

**Example:**
```
GET /api/v1/products/1
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Wireless Noise-Cancelling Headphones",
    "price": 2499.99,
    "stockQuantity": 150,
    "category": "Electronics",
    "imageUrl": "https://cdn.example.com/products/headphones.jpg",
    "createdAt": "2026-04-28T10:00:00Z",
    "updatedAt": "2026-04-28T10:00:00Z"
  },
  "timestamp": "2026-04-28T10:00:00Z"
}
```

**Error Response (`404`):**

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Product not found with id: 99",
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

### 4. Get Products by Category

| Property  | Value                                    |
|-----------|------------------------------------------|
| Method    | `GET`                                    |
| URL       | `/api/v1/products/category/{category}`   |
| Status    | `200 OK`                                 |

**Path Parameters:**

| Parameter  | Type   | Required | Constraints           |
|------------|--------|----------|-----------------------|
| `category` | string | Yes      | Must not be blank     |

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
      "id": 1,
      "name": "Wireless Noise-Cancelling Headphones",
      "price": 2499.99,
      "category": "Electronics"
    }
  ],
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

### 5. Search Products by Name

| Property  | Value                         |
|-----------|-------------------------------|
| Method    | `GET`                         |
| URL       | `/api/v1/products/search`     |
| Status    | `200 OK`                      |

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
      "id": 1,
      "name": "Wireless Noise-Cancelling Headphones",
      "price": 2499.99,
      "category": "Electronics"
    }
  ],
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

### 6. Update Product

| Property  | Value                          |
|-----------|--------------------------------|
| Method    | `PUT`                          |
| URL       | `/api/v1/products/{productId}` |
| Status    | `200 OK`                       |

**Path Parameters:**

| Parameter   | Type | Required | Constraints               |
|-------------|------|----------|---------------------------|
| `productId` | long | Yes      | Must be a positive number |

**Request Body:** *(same structure as Create)*

```json
{
  "name": "Wireless Noise-Cancelling Headphones Pro",
  "description": "Updated model with improved ANC and 40-hour battery life.",
  "price": 2999.99,
  "stockQuantity": 100,
  "category": "Electronics",
  "imageUrl": "https://cdn.example.com/products/headphones-pro.jpg"
}
```

**Example:**
```
PUT /api/v1/products/1
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product updated successfully",
  "data": {
    "id": 1,
    "name": "Wireless Noise-Cancelling Headphones Pro",
    "price": 2999.99,
    "stockQuantity": 100,
    "category": "Electronics",
    "imageUrl": "https://cdn.example.com/products/headphones-pro.jpg",
    "createdAt": "2026-04-28T10:00:00Z",
    "updatedAt": "2026-04-28T10:30:00Z"
  },
  "timestamp": "2026-04-28T10:30:00Z"
}
```

---

### 7. Delete Product

| Property  | Value                          |
|-----------|--------------------------------|
| Method    | `DELETE`                       |
| URL       | `/api/v1/products/{productId}` |
| Status    | `200 OK`                       |

**Path Parameters:**

| Parameter   | Type | Required | Constraints               |
|-------------|------|----------|---------------------------|
| `productId` | long | Yes      | Must be a positive number |

**Example:**
```
DELETE /api/v1/products/1
```

**Example Response (`200`):**

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Product deleted successfully",
  "data": null,
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

## Validation Error Response

When request body fails validation (`400 Bad Request`):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation failed",
  "errors": {
    "name": "Product name is required",
    "price": "Price is required"
  },
  "timestamp": "2026-04-28T10:00:00Z"
}
```

---

## Error Reference

| Status | Scenario                                      |
|--------|-----------------------------------------------|
| 400    | Validation failure (missing/invalid fields)   |
| 404    | Product not found                             |
| 405    | HTTP method not supported on the endpoint     |
| 500    | Internal server error                         |
