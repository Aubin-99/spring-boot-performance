package org.isep.springbootperformance.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

/**
 * Configuration du cache pour l'optimisation des performances.
 * Active la mise en cache au niveau de l'application pour réduire la charge de la base de données.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure un gestionnaire de cache en mémoire simple.
     * En production, envisagez d'utiliser des solutions plus robustes comme Caffeine, Redis ou Hazelcast.
     */
    @Bean
    @Description("Configure un gestionnaire de cache en mémoire simple à des fins de démonstration")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("productCache");
    }
}
