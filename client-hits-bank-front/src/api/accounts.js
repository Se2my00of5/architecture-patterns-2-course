import apiClient from './client';

const ACCOUNTS_API_URL = 'http://localhost:1111/api/accounts';

export const accountsApi = {

  transfer: async (sourceAccountId, targetAccountId, amount, description = '') => {
    try {
      const response = await apiClient.post(`${ACCOUNTS_API_URL}/${sourceAccountId}/transfer`, {
        targetAccountId,
        amount,
        description
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
        error: 'Ошибка при выполнении перевода',
        status: 500
      };
    }
  },

  getUserAccounts: async (userId) => {
    try {
      const response = await apiClient.get(`${ACCOUNTS_API_URL}/user/${userId}`);
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

  createAccount: async (userId, currency = 'RUB') => {
    try {
      const response = await apiClient.post(ACCOUNTS_API_URL, { 
        userId,
        currency 
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
        error: 'Ошибка при создании счета',
        status: 500
      };
    }
  },

  deposit: async (accountId, amount, description = '') => {
    try {
      const response = await apiClient.post(`${ACCOUNTS_API_URL}/${accountId}/deposit`, {
        amount,
        description
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
        error: 'Ошибка при пополнении счета',
        status: 500
      };
    }
  },

  withdraw: async (accountId, amount, description = '') => {
    try {
      const response = await apiClient.post(`${ACCOUNTS_API_URL}/${accountId}/withdraw`, {
        amount,
        description
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
        error: 'Ошибка при снятии со счета',
        status: 500
      };
    }
  },

  closeAccount: async (accountId) => {
    try {
      const response = await apiClient.post(`${ACCOUNTS_API_URL}/${accountId}/close`, {});
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
        error: 'Ошибка при закрытии счета',
        status: 500
      };
    }
  },

  getAccountOperations: async (accountId) => {
    try {
      const response = await apiClient.get(`${ACCOUNTS_API_URL}/${accountId}/operations`);
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
  }
};