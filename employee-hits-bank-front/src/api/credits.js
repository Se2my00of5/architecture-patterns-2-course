import axios from 'axios';

const CREDITS_API_URL = 'http://localhost:5005/api/Credit';

export const creditsApi = {

  getAllTariffs: async () => {
    try {
      const response = await axios.get(`${CREDITS_API_URL}/tariffs?onlyActive=false`, {
        headers: {
          'accept': 'text/plain'
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

  createTariff: async (name, interestRate) => {
    try {
      const response = await axios.post(
        `${CREDITS_API_URL}/tariffs`,
        {
          name,
          interestRate
        },
        {
          headers: {
            'accept': 'text/plain',
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

    getClientCredits: async (clientId) => {
    try {
      const response = await axios.get(`${CREDITS_API_URL}/client/${clientId}`, {
        headers: {
          'accept': 'text/plain'
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
};