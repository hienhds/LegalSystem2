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
import LegalDocumentDetail from "../pages/LegalDocumentDetail";
import FindLawyer from "../pages/FindLawyer";
import LawyerDetail from "../pages/LawyerDetail";
import Contact from "../pages/Contact";
import CreateCase from "../pages/CreateCase";
import CaseDetail from "../pages/CaseDetail";
import CaseList from "../pages/CaseList";
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
        <Route path="/legal-documents/:id" element={<LegalDocumentDetail />} />
        <Route path="/document/:id" element={<LegalDocumentDetail />} />
        <Route path="/find-lawyer" element={<FindLawyer />} />
        <Route path="/lawyers/:id" element={<LawyerDetail />} />
        <Route path="/lawyer/:id" element={<LawyerDetail />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/create-case" element={
          <ProtectedRoute>
            <CreateCase />
          </ProtectedRoute>
        } />

        {/* Route xem chi tiết vụ án */}
        <Route path="/cases/:id" element={
          <ProtectedRoute>
            <CaseDetail />
          </ProtectedRoute>
        } />
        <Route path="/cases" element={<ProtectedRoute><CaseList /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
