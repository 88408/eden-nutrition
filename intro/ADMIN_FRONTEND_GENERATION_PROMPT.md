# Eden Nutrition Admin Frontend Generation Prompt

**Role:** You are a Senior Frontend Engineer and UI/UX Designer.
**Objective:** Build a modern, responsive, and robust **Admin Management System** for the "Eden Nutrition" e-commerce platform.
**Target Audience:** System Administrators and Operation Staff.

---

## 1. Technical Stack Requirements
*   **Framework:** React 18+ (Functional Components, Hooks)
*   **Build Tool:** Vite
*   **Language:** TypeScript (Strict mode)
*   **Styling:** Tailwind CSS (v3/v4)
*   **State Management:** Zustand or Redux Toolkit
*   **Routing:** React Router DOM v6
*   **HTTP Client:** Axios (with interceptors for JWT token handling)
*   **UI Component Library:** Shadcn UI (preferred) or Ant Design (if complex tables are prioritized) - *Please verify availability or suggest standard Tailwind components.*
*   **Icons:** Lucide React or Heroicons

## 2. Global Configuration
*   **Base URL:** `/api` (configured via Vite proxy to `http://localhost:8080`)
*   **Authentication:** JWT Bearer Token.
    *   Token storage: `localStorage`
    *   Header: `Authorization: Bearer <token>`
*   **Theme:** Clean, Professional, "Nutrition/Health" vibe (Primary colors: Emerald Green/Sage, White, Dark Gray).

## 3. Core Modules & Features (Based on API)

### A. Authentication (Priority High)
*   **Page:** `/login`
*   **API:** `POST /user/login`
*   **Logic:**
    *   Form with Username/Password.
    *   Handle Loading/Error states.
    *   Store Token & User Info upon success.
    *   Redirect to Dashboard.

### B. Dashboard (Priority Medium)
*   **Page:** `/dashboard`
*   **Content:**
    *   Quick Stats cards (Orders today, Total Sales, New Users - Mock data if API missing).
    *   Recent Orders table.

### C. Product Management (Priority High)
*   **Page:** `/product/list`
*   **API:**
    *   List: `GET /product/list` (Params: `pageNum`, `pageSize`, `keyword`, `categoryId`)
    *   Detail: `GET /product/{id}`
*   **Features:**
    *   Data Table with image preview, price, stock, status.
    *   Pagination & Search.
    *   **Actions:** Edit, Delete (Mock/Future), Toggle Status (Shelve/Unshelve).

### D. Category Management (Priority High)
*   **Page:** `/category/list`
*   **API:**
    *   Tree: `GET /category/tree`
    *   Add: `POST /category/add`
*   **Features:**
    *   Tree Table or nested list view.
    *   "Add Category" Modal (Fields: Name, Parent Category, Icon, Sort Order).
    *   Ensure numeric fields (sortOrder) are valid.

### E. Order Management (Priority High)
*   **Page:** `/order/list`
*   **API:**
    *   List: `GET /order/admin/list` (Params: `orderNo`, `status`)
    *   Detail: `GET /order/{orderNo}`
    *   Ship: `POST /order/ship/{orderNo}` (Mock/Future if not ready)
*   **Features:**
    *   Filter by Order Status (Pending, Paid, Shipped, Completed, Cancelled).
    *   Order Details Modal (Items, Address, Price breakdown).

### F. Marketing / Seckill (Priority Medium)
*   **Page:** `/marketing/seckill`
*   **API:**
    *   List: `GET /seckill/list`
    *   Create: `POST /seckill/create`
    *   Publish: `POST /seckill/publish/{id}`
*   **Features:**
    *   List of Flash Sale sessions.
    *   Form to add a product to Flash Sale (Product ID, Price, Stock, Time range).

## 4. Directory Structure Recommendation
```
src/
  api/          # Axios instances and API service functions (user.ts, product.ts...)
  assets/       # Images, styles
  components/   # Reusable components (Button, Modal, Table...)
  hooks/        # Custom hooks (useAuth, useFetch...)
  layouts/      # AdminLayout (Sidebar + Header + Content)
  pages/        # Page components (Login, Dashboard, ProductList...)
  router/       # Route definitions
  store/        # Global state
  types/        # TypeScript interfaces (Product, User, Order...)
  utils/        # Helpers (formatCurrency, formatDate...)
  App.tsx
  main.tsx
```

## 5. Implementation Steps for Agent
1.  **Initialize Project**: Setup Vite + React + TS + Tailwind.
2.  **Setup Core**: Configure Axios client (interceptors), Auth Context/Store, and Router.
3.  **Build Layout**: Create a responsive Admin Layout with a Sidebar Navigation.
4.  **Develop Modules**: Implement features A -> E sequentially.
5.  **Refine**: Add Error Handling (Toast notifications) and Loading Spinners.

---
**Note to Agent:** The backend strictly enforces `context-path: /api`. Ensure all API requests are prefixed correctly or proxied. Handle `403 Forbidden` by redirecting to Login.
