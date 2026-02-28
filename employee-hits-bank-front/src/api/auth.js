import axios from 'axios';

const API_BASE_URL = 'http://localhost:1115/api/users';

export const authApi = {
  login: async (login) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/login/${login}`, {
        headers: {
          'accept': '*/*'
        }
      });
      return { success: true, data: response.data };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при входе',
          status: error.response.status
        };
      } else if (error.request) {
        return { success: false, error: 'Сервер не отвечает' };
      } else {
        return { success: false, error: 'Ошибка при выполнении запроса' };
      }
    }
  }
};