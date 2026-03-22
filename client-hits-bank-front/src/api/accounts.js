import apiClient from './client';

/**
 * @typedef {import('../types/api').Account} Account
 * @typedef {import('../types/api').CreateAccountRequest} CreateAccountRequest
 * @typedef {import('../types/api').DepositRequest} DepositRequest
 * @typedef {import('../types/api').WithdrawRequest} WithdrawRequest
 * @typedef {import('../types/api').TransferRequest} TransferRequest
 * @typedef {import('../types/api').Operation} Operation
 * @typedef {import('../types/api').ApiResponse} ApiResponse
 */

const ACCOUNTS_API_URL = 'http://localhost:1111/api/accounts';

export const accountsApi = {
  /**
   * @param {string} sourceAccountId
   * @param {string} targetAccountId
   * @param {number} amount
   * @param {string} [description]
   * @returns {Promise<ApiResponse<Account>>}
   */
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

  /**
   * @param {string} userId
   * @returns {Promise<ApiResponse<Account[]>>}
   */
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

  /**
   * @param {string} userId
   * @param {string} [currency]
   * @returns {Promise<ApiResponse<Account>>}
   */
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

  /**
   * @param {string} accountId
   * @param {number} amount
   * @param {string} [description]
   * @returns {Promise<ApiResponse<Account>>}
   */
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

  /**
   * @param {string} accountId
   * @param {number} amount
   * @param {string} [description]
   * @returns {Promise<ApiResponse<Account>>}
   */
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

  /**
   * @param {string} accountId
   * @returns {Promise<ApiResponse<Account>>}
   */
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

  /**
   * @param {string} accountId
   * @returns {Promise<ApiResponse<Operation[]>>}
   */
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