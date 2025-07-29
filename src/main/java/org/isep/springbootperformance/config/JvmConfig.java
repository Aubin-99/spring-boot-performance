package org.isep.springbootperformance.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration JVM pour l'optimisation des performances.
 * 
 * Note: La plupart des optimisations JVM sont effectuées via des arguments en ligne de commande lors du démarrage de l'application.
 * Cette classe fournit de la documentation et des journalisations des paramètres JVM recommandés.
 * 
 * Arguments JVM recommandés pour la production:
 * -XX:+UseG1GC                      # Utiliser le Garbage Collector G1
 * -XX:MaxGCPauseMillis=200          # Temps de pause GC maximum cible
 * -XX:+ParallelRefProcEnabled       # Activer le traitement parallèle des références
 * -XX:+DisableExplicitGC            # Désactiver les appels GC explicites
 * -XX:+AlwaysPreTouch               # Pré-toucher les pages mémoire
 * -XX:+UseStringDeduplication       # Dédupliquer les chaînes pour économiser de la mémoire
 * -XX:+UseCompressedOops            # Utiliser des pointeurs d'objets compressés
 * -XX:+UseCompressedClassPointers   # Utiliser des pointeurs de classe compressés
 * -Xms2g -Xmx2g                     # Définir la taille initiale et maximale du tas à la même valeur
 * -XX:+HeapDumpOnOutOfMemoryError   # Créer un dump du tas en cas d'erreur OOM
 * -XX:HeapDumpPath=/path/to/dumps   # Chemin pour les dumps du tas
 * -XX:+ExitOnOutOfMemoryError       # Quitter en cas d'erreur OOM pour permettre le redémarrage du conteneur
 */
@Configuration
@Slf4j
public class JvmConfig {

    /**
     * Journalise les informations JVM et les recommandations au démarrage.
     * Cela aide à surveiller et à assurer des paramètres JVM optimaux.
     */
    @PostConstruct
    public void logJvmInfo() {
        Runtime runtime = Runtime.getRuntime();

        log.info("Informations de performance JVM:");
        log.info("Processeurs disponibles: {}", runtime.availableProcessors());
        log.info("Mémoire libre: {} MB", runtime.freeMemory() / (1024 * 1024));
        log.info("Mémoire maximale: {} MB", runtime.maxMemory() / (1024 * 1024));
        log.info("Mémoire totale: {} MB", runtime.totalMemory() / (1024 * 1024));

        log.info("Nom JVM: {}", System.getProperty("java.vm.name"));
        log.info("Version JVM: {}", System.getProperty("java.version"));

        // Journalise les informations GC si disponibles
        String gcInfo = System.getProperty("java.vm.gc.strategy");
        if (gcInfo != null) {
            log.info("Stratégie GC: {}", gcInfo);
        } else {
            log.info("Stratégie GC: Non définie explicitement. Vérifiez les arguments JVM.");
        }

        log.info("Pour des performances optimales, envisagez de définir les arguments JVM appropriés comme documenté dans la classe JvmConfig.");
    }
}
