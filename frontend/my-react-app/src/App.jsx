import { useEffect } from "react";
import './App.css';
import AppRouter from "./router/AppRouter";
import { AuthProvider } from "./contexts/AuthContext";
import { checkTokenOnStartup } from "./utils/api";

function App() {
  useEffect(() => {
    // Check and refresh token when app loads
    checkTokenOnStartup();
  }, []);

  return (
    <AuthProvider>
      <AppRouter />
    </AuthProvider>
  );
}

export default App;
