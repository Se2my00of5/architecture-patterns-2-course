const AUTH_KEY = 'employee_hits_bank_auth';

export const storage = {
  setAuthData: (data) => {
    localStorage.setItem(AUTH_KEY, JSON.stringify(data));
  },
  
  getAuthData: () => {
    const data = localStorage.getItem(AUTH_KEY);
    return data ? JSON.parse(data) : null;
  },
  
  clearAuthData: () => {
    localStorage.removeItem(AUTH_KEY);
  }
};