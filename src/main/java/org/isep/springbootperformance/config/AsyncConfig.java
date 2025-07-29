package org.isep.springbootperformance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration asynchrone pour l'optimisation des performances.
 * Active le traitement asynchrone pour améliorer la réactivité de l'application.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure un pool de threads personnalisé pour les opérations asynchrones.
     * Cela améliore les performances en permettant le traitement concurrent des requêtes.
     */
    @Bean(name = "taskExecutor")
    @Description("Configure un pool de threads pour les opérations asynchrones")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Définit la taille du pool de base - le nombre minimum de workers à maintenir en vie
        executor.setCorePoolSize(5);
        // Définit la taille maximale du pool - le nombre maximum de workers autorisés
        executor.setMaxPoolSize(10);
        // Définit la capacité de la file d'attente - combien de tâches peuvent attendre si tous les threads sont occupés
        executor.setQueueCapacity(25);
        // Définit le préfixe de nom de thread pour une meilleure surveillance
        executor.setThreadNamePrefix("async-");
        // Attend que les tâches se terminent lors de l'arrêt
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // Initialise l'exécuteur
        executor.initialize();
        return executor;
    }
}
