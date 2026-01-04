import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import Home from "../pages/Home";
import ProtectedRoute from "../components/ProtectedRoute";
import Register from "../pages/Register";
import ForgotPassword from "../pages/ForgotPassword";
import ResetPassword from "../pages/ResetPassword";
import UserProfile from "../pages/UserProfile";
import LawyerProfile from "../pages/LawyerProfile";
import AdminDashboard from "../pages/AdminDashboard";
import UserManagement from "../pages/UserManagement";
import LawyerManagement from "../pages/LawyerManagement";
import DocumentManagement from "../pages/DocumentManagement";
import LegalDocuments from "../pages/LegalDocuments";
import FindLawyer from "../pages/FindLawyer";
import LawyerDetail from "../pages/LawyerDetail";
import Contact from "../pages/Contact";
import Cases from "../pages/Cases";
import CaseDetail from "../pages/CaseDetail";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />
        <Route path="/home" element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/profile" element={
          <ProtectedRoute>
            <UserProfile />
          </ProtectedRoute>
        } />
        <Route path="/lawyer-profile" element={
          <ProtectedRoute>
            <LawyerProfile />
          </ProtectedRoute>
        } />
        <Route path="/admin/dashboard" element={
          <ProtectedRoute>
            <AdminDashboard />
          </ProtectedRoute>
        } />
        <Route path="/admin/users" element={
          <ProtectedRoute>
            <UserManagement />
          </ProtectedRoute>
        } />
        <Route path="/admin/lawyers" element={
          <ProtectedRoute>
            <LawyerManagement />
          </ProtectedRoute>
        } />
        <Route path="/admin/documents" element={
          <ProtectedRoute>
            <DocumentManagement />
          </ProtectedRoute>
        } />
        <Route path="/legal-documents" element={<LegalDocuments />} />
        <Route path="/find-lawyer" element={<FindLawyer />} />
        <Route path="/lawyers/:id" element={<LawyerDetail />} />
        <Route path="/lawyer/:id" element={<LawyerDetail />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/cases" element={
          <ProtectedRoute>
            <Cases />
          </ProtectedRoute>
        } />
        <Route path="/cases/:id" element={
          <ProtectedRoute>
            <CaseDetail />
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}
