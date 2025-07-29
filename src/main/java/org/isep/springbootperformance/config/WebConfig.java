package org.isep.springbootperformance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration web pour l'optimisation des performances.
 * Configure divers paramètres liés au web pour améliorer les performances.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure la gestion des ressources statiques pour de meilleures performances.
     * Définit les en-têtes de contrôle de cache pour les ressources statiques.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .immutable());
    }

    /**
     * Configure CORS pour de meilleures performances réseau.
     * Une configuration CORS appropriée évite les requêtes préliminaires lorsque c'est possible.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .maxAge(3600); // Met en cache les requêtes préliminaires pendant 1 heure
    }

    /**
     * Configure la négociation de contenu pour préférer JSON.
     * Cela améliore les performances en évitant les frais généraux de négociation de type de contenu.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    /**
     * Configure la sérialisation JSON pour de meilleures performances.
     * Désactive les fonctionnalités qui peuvent impacter les performances.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }

    /**
     * Configure Jackson pour une sérialisation JSON optimisée.
     */
    @Bean
    @Description("Configure Jackson pour une sérialisation JSON optimisée")
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    /**
     * Configure ObjectMapper avec des optimisations de performance.
     */
    @Bean
    @Description("Configure ObjectMapper avec des optimisations de performance")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Désactive les fonctionnalités qui peuvent impacter les performances
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }
}
