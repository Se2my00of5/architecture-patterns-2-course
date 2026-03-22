import apiClient from './client';

/**
 * @typedef {import('../types/api').Tariff} Tariff
 * @typedef {import('../types/api').CreateTariffRequest} CreateTariffRequest
 * @typedef {import('../types/api').Credit} Credit
 * @typedef {import('../types/api').CreditRating} CreditRating
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
   * @returns {Promise<{ success: boolean; data: Tariff[]; error?: string; status?: number }>}
   */
  getAllTariffs: async () => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/tariffs?onlyActive=false`);
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
   * @param {string} name
   * @param {number} interestRate
   * @returns {Promise<{ success: boolean; data?: Tariff; error?: string; status?: number }>}
   */
  createTariff: async (name, interestRate) => {
    try {
      const response = await apiClient.post(`${CREDITS_API_URL}/tariffs`, {
        name,
        interestRate
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
          error: error.response.data.message || 'Ошибка при создании тарифа',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при создании тарифа',
        status: 500
      };
    }
  },

  /**
   * @param {string} clientId
   * @returns {Promise<{ success: boolean; data: Credit[]; error?: string; status?: number }>}
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
   * @param {string} clientId
   * @returns {Promise<{ success: boolean; data?: CreditRating; error?: string; status?: number }>}
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
          error: error.response.data.message || 'Ошибка при получении рейтинга',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении рейтинга',
        status: 500
      };
    }
  }
};