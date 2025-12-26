import React from "react";
import Header from "./Header";
import Footer from "./Footer";

export default function Layout({ children, showFooter = false, fullWidth = false }) {
  return (
    <div className={`bg-background-light dark:bg-background-dark font-display ${fullWidth ? 'h-screen' : 'min-h-screen'} flex flex-col ${fullWidth ? 'overflow-hidden' : ''}`}>
      <Header />
      <main className={`flex-1 ${fullWidth ? 'overflow-hidden' : ''}`}>
        {children}
      </main>
      {showFooter && <Footer />}
    </div>
  );
}
