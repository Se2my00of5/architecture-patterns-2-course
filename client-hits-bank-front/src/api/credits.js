import apiClient from './client';

/**
 * @typedef {import('../types/api').Tariff} Tariff
 * @typedef {import('../types/api').ApplyCreditRequest} ApplyCreditRequest
 * @typedef {import('../types/api').Credit} Credit
 * @typedef {import('../types/api').CreditRating} CreditRating
 * @typedef {import('../types/api').MakePaymentRequest} MakePaymentRequest
 * @typedef {import('../types/api').ApiResponse} ApiResponse
 */

const CREDITS_API_URL = 'http://localhost:5005/api/Credit';

export const creditsApi = {
  /**
   * @param {string} clientId
   * @returns {Promise<ApiResponse<Array<{
   *   id: string;
   *   creditId: string;
   *   paymentDate: string;
   *   dueDate: string;
   *   status: string;
   *   transactionId: string | null;
   * }>>>}
   */
  getClientOverduePayments: async (clientId) => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/client/${clientId}/payments/overdue`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении просроченных платежей',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении просроченных платежей',
        status: 500
      };
    }
  },

  /**
   * @param {string} clientId
   * @returns {Promise<ApiResponse<CreditRating>>}
   */
  getClientRating: async (clientId) => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/client/${clientId}/rating`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении кредитного рейтинга',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении кредитного рейтинга',
        status: 500
      };
    }
  },

  /**
   * @returns {Promise<ApiResponse<Tariff[]>>}
   */
  getActiveTariffs: async () => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/tariffs?onlyActive=true`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении тарифов',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении тарифов',
        status: 500
      };
    }
  },

  /**
   * @param {string} clientId
   * @param {string} accountId
   * @param {string} tariffId
   * @param {number} amount
   * @param {number} termInMonths
   * @returns {Promise<ApiResponse<Credit>>}
   */
  applyForCredit: async (clientId, accountId, tariffId, amount, termInMonths) => {
    try {
      const response = await apiClient.post(`${CREDITS_API_URL}/apply`, {
        clientId,
        accountId,
        tariffId,
        amount,
        termInMonths
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
          error: error.response.data.message || 'Ошибка при оформлении кредита',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при оформлении кредита',
        status: 500
      };
    }
  },

  /**
   * @param {string} clientId
   * @returns {Promise<ApiResponse<Credit[]>>}
   */
  getClientCredits: async (clientId) => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/client/${clientId}`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении кредитов',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении кредитов',
        status: 500
      };
    }
  },

  /**
   * @param {string} creditId
   * @param {string} accountId
   * @param {number} amount
   * @returns {Promise<ApiResponse<object>>}
   */
  makePayment: async (creditId, accountId, amount) => {
    try {
      const response = await apiClient.post(`${CREDITS_API_URL}/payments`, {
        creditId,
        accountId,
        amount
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
          error: error.response.data.message || 'Ошибка при платеже',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при платеже',
        status: 500
      };
    }
  }
};