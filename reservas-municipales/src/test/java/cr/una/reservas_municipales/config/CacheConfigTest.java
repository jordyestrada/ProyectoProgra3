package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para CacheConfig
 */
class CacheConfigTest {

    @Test
    void testCacheManagerCreation() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof CaffeineCacheManager);
    }

    @Test
    void testCacheManagerHasDashboardMetricsCache() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertNotNull(cacheManager.getCache("dashboardMetrics"));
    }

    @Test
    void testCacheManagerHasWeatherCache() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertNotNull(cacheManager.getCache("weatherCache"));
    }

    @Test
    void testCacheManagerConfiguration() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;

        // Assert
        assertNotNull(caffeineCacheManager);
        assertTrue(caffeineCacheManager instanceof CaffeineCacheManager);
    }

    @Test
    void testCacheNamesAreAvailable() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();
        
        // Assert
        var cacheNames = cacheManager.getCacheNames();
        assertTrue(cacheNames.contains("dashboardMetrics"));
        assertTrue(cacheNames.contains("weatherCache"));
    }

    @Test
    void testCachePutAndGet() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        var cache = cacheManager.getCache("dashboardMetrics");
        
        String key = "testKey";
        String value = "testValue";

        // Act
        assertNotNull(cache);
        cache.put(key, value);
        var cachedValue = cache.get(key, String.class);

        // Assert
        assertEquals(value, cachedValue);
    }

    @Test
    void testCacheClear() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        var cache = cacheManager.getCache("weatherCache");
        
        String key = "testKey";
        String value = "testValue";

        // Act
        assertNotNull(cache);
        cache.put(key, value);
        assertNotNull(cache.get(key));
        
        cache.clear();
        
        // Assert
        assertNull(cache.get(key));
    }

    @Test
    void testCacheEvict() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        var cache = cacheManager.getCache("dashboardMetrics");
        
        String key1 = "key1";
        String key2 = "key2";
        String value1 = "value1";
        String value2 = "value2";

        // Act
        assertNotNull(cache);
        cache.put(key1, value1);
        cache.put(key2, value2);
        
        cache.evict(key1);
        
        // Assert
        assertNull(cache.get(key1));
        assertNotNull(cache.get(key2));
    }

    @Test
    void testMultipleCachesIndependent() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        var dashboardCache = cacheManager.getCache("dashboardMetrics");
        var weatherCache = cacheManager.getCache("weatherCache");
        
        String key = "sameKey";
        String dashboardValue = "dashboardValue";
        String weatherValue = "weatherValue";

        // Act
        assertNotNull(dashboardCache);
        assertNotNull(weatherCache);
        
        dashboardCache.put(key, dashboardValue);
        weatherCache.put(key, weatherValue);
        
        // Assert
        assertEquals(dashboardValue, dashboardCache.get(key, String.class));
        assertEquals(weatherValue, weatherCache.get(key, String.class));
    }

    @Test
    void testCacheManagerType() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertEquals(CaffeineCacheManager.class, cacheManager.getClass());
    }
}
