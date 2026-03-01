import axios from 'axios';

const ACCOUNTS_API_URL = 'http://localhost:1111/api/accounts';

export const accountsApi = {
  getUserAccounts: async (userId) => {
    try {
      const response = await axios.get(`${ACCOUNTS_API_URL}/user/${userId}`, {
        headers: {
          'accept': '*/*'
        }
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
        error: 'Ошибка при получении счетов',
        status: 500
      };
    }
  },

  createAccount: async (userId) => {
    try {
      const response = await axios.post(
        ACCOUNTS_API_URL,
        { userId },
        {
          headers: {
            'accept': '*/*',
            'Content-Type': 'application/json'
          }
        }
      );
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
      const response = await axios.post(
        `${ACCOUNTS_API_URL}/${accountId}/deposit`,
        { amount, description },
        {
          headers: {
            'accept': '*/*',
            'Content-Type': 'application/json'
          }
        }
      );
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
      const response = await axios.post(
        `${ACCOUNTS_API_URL}/${accountId}/withdraw`,
        { amount, description },
        {
          headers: {
            'accept': '*/*',
            'Content-Type': 'application/json'
          }
        }
      );
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
      const response = await axios.post(
        `${ACCOUNTS_API_URL}/${accountId}/close`,
        {},
        {
          headers: {
            'accept': '*/*'
          }
        }
      );
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
      const response = await axios.get(`${ACCOUNTS_API_URL}/${accountId}/operations`, {
        headers: {
          'accept': '*/*'
        }
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
        error: 'Ошибка при получении истории операций',
        status: 500
      };
    }
  }
};