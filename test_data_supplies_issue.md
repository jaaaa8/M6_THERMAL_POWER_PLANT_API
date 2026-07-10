# Test Data for SuppliesIssueController

## Based on seed data from V3__seed_sample_data.sql

### Available Test Data:
- **Work Orders**: 1, 2, 3
- **Spare Parts**: 1 (Bearing), 2 (Mechanical Seal), 3 (Motor Contactor), 4 (Valve Gasket)
- **Consumables**: 1 (Hydraulic Oil), 2 (Grease), 3 (Cleaning Rag), 4 (Rust Remover)
- **Account**: Use `admin` / `123456` or `wforeman_1` / `123456` for authentication

---

## 1. Create Supplies Issue - Both Spare Parts and Consumables

**Endpoint:** `POST /api/v1/work-orders/2/supplies-issues`

**Request Body:**
```json
{
  "spareParts": [
    {
      "sparePartId": 1,
      "quantity": 2
    },
    {
      "sparePartId": 3,
      "quantity": 1
    }
  ],
  "consumables": [
    {
      "consumableId": 1,
      "quantity": 10
    },
    {
      "consumableId": 3,
      "quantity": 5
    }
  ]
}
```

---

## 2. Create Supplies Issue - Only Spare Parts

**Endpoint:** `POST /api/v1/work-orders/2/supplies-issues`

**Request Body:**
```json
{
  "spareParts": [
    {
      "sparePartId": 2,
      "quantity": 1
    },
    {
      "sparePartId": 4,
      "quantity": 3
    }
  ]
}
```

---

## 3. Create Supplies Issue - Only Consumables

**Endpoint:** `POST /api/v1/work-orders/2/supplies-issues`

**Request Body:**
```json
{
  "consumables": [
    {
      "consumableId": 2,
      "quantity": 3
    },
    {
      "consumableId": 4,
      "quantity": 2
    }
  ]
}
```

---

## 4. Get Supplies Issue History

**Endpoint:** `GET /api/v1/work-orders/1/supplies-issues`

**No Request Body** - This is a GET request

Returns all spare parts and consumables issues for work order ID 1.

---

## 5. Get Supplies Issue History for Work Order 2

**Endpoint:** `GET /api/v1/work-orders/2/supplies-issues`

**No Request Body**

---

## Quick Reference - IDs from Seed Data

### Spare Parts:
- **1**: Bearing 6312 C3 (SKF) - 1,250,000 VND
- **2**: Mechanical Seal 50mm (EagleBurgmann) - 3,750,000 VND
- **3**: Motor Contactor 220V (Schneider) - 980,000 VND
- **4**: Valve Gasket DN100 (Local Supplier) - 150,000 VND

### Consumables:
- **1**: Hydraulic Oil ISO VG 68 (Shell) - 85,000 VND
- **2**: Grease EP2 (Mobil) - 120,000 VND
- **3**: Cleaning Rag (Local Supplier) - 15,000 VND
- **4**: Rust Remover Spray (RP7) - 65,000 VND

### Work Orders:
- **1**: WO-2026-0001 - COMPLETED (Pump vibration repair)
- **2**: WO-2026-0002 - IN_PROGRESS (MCC panel temperature issue)
- **3**: WO-2026-0003 - COMPLETED (Condensate pump seal leakage)

---

## Authentication

Before testing, login first:

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "123456"
}
```

Copy the `accessToken` from the response and use it as Bearer token in Authorization header for subsequent requests.

---

## Expected Errors

### Error: Empty supplies issue
```json
{
  "spareParts": [],
  "consumables": []
}
```
**Response:** 400 Bad Request - "Phiếu cấp vật tư phải có ít nhất 1 dòng vật tư thay thế hoặc vật tư tiêu hao"

### Error: Invalid quantity
```json
{
  "consumables": [
    {
      "consumableId": 1,
      "quantity": -5
    }
  ]
}
```
**Response:** 400 Bad Request - "quantity phải lớn hơn 0"

### Error: Missing required field
```json
{
  "spareParts": [
    {
      "sparePartId": 1
    }
  ]
}
```
**Response:** 400 Bad Request - "quantity là bắt buộc"
