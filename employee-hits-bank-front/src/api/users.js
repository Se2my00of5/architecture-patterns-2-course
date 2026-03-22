import apiClient from './client';

/**
 * @typedef {import('../types/api').User} User
 * @typedef {import('../types/api').CreateUserRequest} CreateUserRequest
 * @typedef {import('../types/api').CreateUserResponse} CreateUserResponse
 */

const API_BASE_URL = 'http://localhost:1115/api/users';
const ACCOUNTS_API_URL = 'http://localhost:1111/api';

export const usersApi = {
  /**
   * @returns {Promise<{ success: boolean; data: User[]; error?: string; status?: number }>}
   */
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

  /**
   * @param {string} login
   * @param {string} password
   * @param {string} fullName
   * @param {('CLIENT' | 'EMPLOYEE')[]} roles
   * @returns {Promise<{ success: boolean; data?: CreateUserResponse; error?: string; status?: number }>}
   */
  createUser: async (login, password, fullName, roles) => {
    try {
      const response = await apiClient.post(`${API_BASE_URL}`, {
        login,
        password,
        fullName,
        roles
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
        error: 'Ошибка при создании пользователя',
        status: 500
      };
    }
  },

  /**
   * @param {string} userId
   * @returns {Promise<{ success: boolean; data?: User; error?: string; status?: number }>}
   */
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

  /**
   * @param {string} userId
   * @returns {Promise<{ success: boolean; data?: User; error?: string; status?: number }>}
   */
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

  /**
   * @param {string} userId
   * @returns {Promise<{ success: boolean; data?: import('../types/api').Account[]; error?: string; status?: number }>}
   */
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

  /**
   * @param {string} accountId
   * @returns {Promise<{ success: boolean; data?: import('../types/api').Operation[]; error?: string; status?: number }>}
   */
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

  /**
   * @returns {Promise<{ success: boolean; data?: User[]; error?: string; status?: number }>}
   */
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
  }
};