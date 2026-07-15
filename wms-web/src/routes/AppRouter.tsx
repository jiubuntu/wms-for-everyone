import { Route, Routes } from "react-router-dom"
import { Landing } from "@/pages/Landing"
import { Login } from "@/pages/auth/Login"
import { Signup } from "@/pages/auth/Signup"
import { ResetPassword } from "@/pages/auth/ResetPassword"
import { Forbidden } from "@/pages/Forbidden"
import { ProtectedRoute } from "@/routes/ProtectedRoute"
import { GuestRoute } from "@/routes/GuestRoute"
import { AdminLayout } from "@/components/common/AdminLayout"
import { DashboardPage } from "@/pages/admin/DashboardPage"
import { CompanyApprovalListPage } from "@/pages/admin/CompanyApprovalListPage"
import { CompanyApprovalDetailPage } from "@/pages/admin/CompanyApprovalDetailPage"

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
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="companies" element={<CompanyApprovalListPage />} />
        <Route path="companies/:id" element={<CompanyApprovalDetailPage />} />
      </Route>
    </Routes>
  )
}
