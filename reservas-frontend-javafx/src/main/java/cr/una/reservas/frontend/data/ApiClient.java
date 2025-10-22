package cr.una.reservas.frontend.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

/**
 * Cliente HTTP para comunicación con la API REST
 * Módulo de red completamente aislado
 */
public class ApiClient {
    
    private static ApiClient instance;
    private static String baseUrl;
    private static String authToken;
    
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    
    private ApiClient() {
        this.httpClient = HttpClients.createDefault();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    public static void initialize(String apiBaseUrl) {
        baseUrl = apiBaseUrl;
        instance = new ApiClient();
    }
    
    public static ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient no ha sido inicializado. Llama a initialize() primero.");
        }
        return instance;
    }
    
    public static void setAuthToken(String token) {
        authToken = token;
    }
    
    public static void clearAuthToken() {
        authToken = null;
    }
    
    /**
     * GET request
     */
    public <T> ApiResponse<T> get(String endpoint, Class<T> responseType) throws IOException {
        HttpGet request = new HttpGet(baseUrl + endpoint);
        addAuthHeader(request);
        return executeRequest(request, responseType);
    }
    
    /**
     * POST request
     */
    public <T> ApiResponse<T> post(String endpoint, Object body, Class<T> responseType) throws IOException {
        HttpPost request = new HttpPost(baseUrl + endpoint);
        addAuthHeader(request);
        
        if (body != null) {
            String jsonBody = gson.toJson(body);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        }
        
        return executeRequest(request, responseType);
    }
    
    /**
     * PUT request
     */
    public <T> ApiResponse<T> put(String endpoint, Object body, Class<T> responseType) throws IOException {
        HttpPut request = new HttpPut(baseUrl + endpoint);
        addAuthHeader(request);
        
        if (body != null) {
            String jsonBody = gson.toJson(body);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        }
        
        return executeRequest(request, responseType);
    }
    
    /**
     * DELETE request
     */
    public <T> ApiResponse<T> delete(String endpoint, Class<T> responseType) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + endpoint);
        addAuthHeader(request);
        return executeRequest(request, responseType);
    }
    
    private void addAuthHeader(HttpUriRequestBase request) {
        if (authToken != null && !authToken.isEmpty()) {
            request.addHeader("Authorization", "Bearer " + authToken);
        }
        request.addHeader("Content-Type", "application/json");
    }
    
    private <T> ApiResponse<T> executeRequest(HttpUriRequestBase request, Class<T> responseType) throws IOException {
        try (ClassicHttpResponse response = httpClient.executeOpen(null, request, null)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (statusCode >= 200 && statusCode < 300) {
                T data = gson.fromJson(responseBody, responseType);
                return new ApiResponse<>(true, statusCode, data, null);
            } else {
                return new ApiResponse<>(false, statusCode, null, responseBody);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing HTTP response", e);
        }
    }
    
    public static void shutdown() {
        if (instance != null && instance.httpClient != null) {
            try {
                instance.httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Clase para encapsular respuestas de la API
     */
    public static class ApiResponse<T> {
        private final boolean success;
        private final int statusCode;
        private final T data;
        private final String errorMessage;
        
        public ApiResponse(boolean success, int statusCode, T data, String errorMessage) {
            this.success = success;
            this.statusCode = statusCode;
            this.data = data;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public int getStatusCode() { return statusCode; }
        public T getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
    }
}
