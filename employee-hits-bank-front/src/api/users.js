import apiClient from './client';

const API_BASE_URL = 'http://localhost:1115/api/users';
const ACCOUNTS_API_URL = 'http://localhost:1111/api';

export const usersApi = {
  getClients: async () => {
    try {
      const response = await apiClient.get(`${API_BASE_URL}/by-role?role=CLIENT`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении списка клиентов',
        status: 500
      };
    }
  },

  createClient: async (login, fullName) => {
    try {
      const response = await apiClient.post(`${API_BASE_URL}`, {
        login,
        fullName,
        role: 'CLIENT'
      });
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при создании клиента',
        status: 500
      };
    }
  },

  block: async (userId) => {
    try {
      const response = await apiClient.patch(`${API_BASE_URL}/${userId}/block`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при блокировке',
        status: 500
      };
    }
  },

  unblock: async (userId) => {
    try {
      const response = await apiClient.patch(`${API_BASE_URL}/${userId}/unblock`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при разблокировке',
        status: 500
      };
    }
  },

  getUserAccounts: async (userId) => {
    try {
      const response = await apiClient.get(`${ACCOUNTS_API_URL}/accounts/user/${userId}`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении счетов',
        status: 500
      };
    }
  },

  getAccountOperations: async (accountId) => {
    try {
      const response = await apiClient.get(`${ACCOUNTS_API_URL}/accounts/${accountId}/operations`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении истории операций',
        status: 500
      };
    }
  },

  getEmployees: async () => {
    try {
      const response = await apiClient.get(`${API_BASE_URL}/by-role?role=EMPLOYEE`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении списка сотрудников',
        status: 500
      };
    }
  },

  createEmployee: async (login, fullName) => {
    try {
      const response = await apiClient.post(`${API_BASE_URL}`, {
        login,
        fullName,
        role: 'EMPLOYEE'
      });
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message,
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при создании сотрудника',
        status: 500
      };
    }
  }
};