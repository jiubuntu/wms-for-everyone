import { Route, Routes } from "react-router-dom"
import { Landing } from "@/pages/Landing"
import { Login } from "@/pages/auth/Login"
import { Signup } from "@/pages/auth/Signup"
import { ResetPassword } from "@/pages/auth/ResetPassword"
import { Forbidden } from "@/pages/Forbidden"
import { ProtectedRoute } from "@/routes/ProtectedRoute"
import { GuestRoute } from "@/routes/GuestRoute"
import { BackofficeLayout } from "@/components/common/BackofficeLayout"
import { ADMIN_NAV, APP_NAV } from "@/routes/nav"
import { DashboardPage as AdminDashboardPage } from "@/pages/admin/DashboardPage"
import { CompanyApprovalListPage } from "@/pages/admin/CompanyApprovalListPage"
import { CompanyApprovalDetailPage } from "@/pages/admin/CompanyApprovalDetailPage"
import { CommonCodePage } from "@/pages/admin/CommonCodePage"
import { DashboardPage as AppDashboardPage } from "@/pages/app/DashboardPage"
import { ProductCategoryPage } from "@/pages/app/ProductCategoryPage"
import { ProductUnitPage } from "@/pages/app/ProductUnitPage"

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<GuestRoute><Landing /></GuestRoute>} />
      <Route path="/login" element={<GuestRoute><Login /></GuestRoute>} />
      <Route path="/signup" element={<GuestRoute><Signup /></GuestRoute>} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/forbidden" element={<Forbidden />} />

      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={["SYSTEM_ADMIN"]}>
            <BackofficeLayout brandLabel="모두의 WMS - 관리자" homeTo="/admin" nav={ADMIN_NAV} />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminDashboardPage />} />
        <Route path="companies" element={<CompanyApprovalListPage />} />
        <Route path="companies/:id" element={<CompanyApprovalDetailPage />} />
        <Route path="common-codes" element={<CommonCodePage />} />
      </Route>

      <Route
        path="/app"
        element={
          <ProtectedRoute allowedRoles={["COMPANY_ADMIN", "WAREHOUSE_MANAGER", "WORKER"]}>
            <BackofficeLayout brandLabel="모두의 WMS" homeTo="/app" nav={APP_NAV} />
          </ProtectedRoute>
        }
      >
        <Route index element={<AppDashboardPage />} />
        <Route
          path="product-categories"
          element={
            <ProtectedRoute allowedRoles={["COMPANY_ADMIN"]}>
              <ProductCategoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="product-units"
          element={
            <ProtectedRoute allowedRoles={["COMPANY_ADMIN", "WAREHOUSE_MANAGER"]}>
              <ProductUnitPage />
            </ProtectedRoute>
          }
        />
      </Route>
    </Routes>
  )
}
