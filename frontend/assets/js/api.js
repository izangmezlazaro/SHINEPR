// ============================================
// Shine - API Client
// ============================================

const ID_USUARIO = Number(localStorage.getItem('shineUserId')) || null;
window.ID_USUARIO = ID_USUARIO;

(function () {
  const API_BASE_URL = 'http://localhost:8080/api/v1';
  const DEFAULT_HEADERS = {
    Accept: 'application/json'
  };

  function getToken() {
    return localStorage.getItem('shineToken') || null;
  }

  function buildUrl(endpoint, queryParams = {}) {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const url = new URL(`${API_BASE_URL}${cleanEndpoint}`);

    Object.entries(queryParams).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.append(key, value);
      }
    });

    return url.toString();
  }

  async function parseResponse(response) {
    const contentType = response.headers.get('content-type') || '';

    if (response.status === 204) {
      return null;
    }

    if (contentType.includes('application/json')) {
      return await response.json();
    }

    const text = await response.text();
    return text || null;
  }

  function createApiError(response, data) {
    const message =
      data?.message ||
      data?.error ||
      data?.detalle ||
      data?.mensaje ||
      `Error HTTP ${response.status}`;

    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    return error;
  }

  async function request(endpoint, options = {}) {
    const {
      method = 'GET',
      body,
      headers = {},
      queryParams,
      signal
    } = options;

    const isFormData = body instanceof FormData;
    const token = getToken();

    const requestHeaders = {
      ...DEFAULT_HEADERS,
      ...headers
    };

    if (token) {
      requestHeaders['Authorization'] = `Bearer ${token}`;
    }

    const requestOptions = {
      method,
      credentials: 'include',
      headers: requestHeaders,
      signal
    };

    if (body !== undefined && body !== null) {
      requestOptions.body = isFormData ? body : JSON.stringify(body);

      if (!isFormData) {
        requestOptions.headers['Content-Type'] = 'application/json';
      }
    }

    try {
      const response = await fetch(buildUrl(endpoint, queryParams), requestOptions);
      const data = await parseResponse(response);

      if (!response.ok) {
        // Token expired or invalid → redirect to login (except on auth endpoints)
        if (response.status === 401 && !endpoint.includes('/auth/')) {
          localStorage.removeItem('shineToken');
          localStorage.removeItem('shineUser');
          localStorage.removeItem('shineUserId');
          window.ID_USUARIO = null;
          window.location.href = 'login.html';
          return;
        }
        throw createApiError(response, data);
      }

      return data;
    } catch (error) {
      if (error.name === 'AbortError') {
        throw error;
      }

      console.error('[ShineAPI]', method, endpoint, error);
      throw error;
    }
  }

  window.ShineAPI = Object.freeze({
    baseUrl: API_BASE_URL,
    request,
    get(endpoint, queryParams, options = {}) {
      return request(endpoint, {
        ...options,
        method: 'GET',
        queryParams
      });
    },
    post(endpoint, body, options = {}) {
      return request(endpoint, {
        ...options,
        method: 'POST',
        body
      });
    },
    put(endpoint, body, options = {}) {
      return request(endpoint, {
        ...options,
        method: 'PUT',
        body
      });
    },
    delete(endpoint, options = {}) {
      return request(endpoint, {
        ...options,
        method: 'DELETE'
      });
    }
  });
})();
