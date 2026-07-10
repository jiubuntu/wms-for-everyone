import { Route, Routes } from "react-router-dom"
import { Landing } from "@/pages/Landing"
import { Login } from "@/pages/auth/Login"
import { Signup } from "@/pages/auth/Signup"

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
    </Routes>
  )
}
