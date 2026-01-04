import { useEffect, useState } from "react";
import { apiFetch } from "../utils/api";

export default function useUserProfile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await apiFetch("http://localhost:8080/api/users/profile");
        if (res.ok) {
          const data = await res.json();
          setUser(data.data);
          
          // Lưu userId và userRole vào localStorage
          if (data.data) {
            // Lưu userId (lawyerId hoặc clientId)
            const userId = data.data.lawyerId || data.data.clientId;
            if (userId) {
              localStorage.setItem("userId", userId.toString());
            }
            
            // Lưu userRole từ roles array (lấy LAWYER hoặc CLIENT, bỏ qua USER)
            if (data.data.roles && data.data.roles.length > 0) {
              const mainRole = data.data.roles.find(r => r === "LAWYER" || r === "CLIENT") || data.data.roles[0];
              localStorage.setItem("userRole", mainRole);
            }
          }
        } else {
          setUser(null);
        }
      } catch {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);
  return { user, loading };
}
