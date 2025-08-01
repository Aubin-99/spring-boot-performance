package org.isep.springbootperformance.event;

import lombok.extern.slf4j.Slf4j;
import org.isep.springbootperformance.model.Product;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Écouteur pour les événements de produit.
 * Démontre le traitement asynchrone des événements pour de meilleures performances et une meilleure évolutivité.
 * 
 * Avantages de l'architecture orientée événements pour les performances:
 * 1. Découplage - Les composants sont faiblement couplés, améliorant la maintenabilité
 * 2. Traitement asynchrone - Les opérations peuvent être traitées en arrière-plan
 * 3. Évolutivité - Les gestionnaires d'événements peuvent être mis à l'échelle indépendamment
 * 4. Résilience - Le traitement d'événements échoué n'affecte pas le flux de requêtes principal
 */
@Component
@Slf4j
public class ProductEventListener {

    /**
     * Gère les événements de création de produit de manière asynchrone.
     * L'annotation @Async fait exécuter cette méthode dans un thread séparé.
     */
    @Async
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).CREATED")
    public void handleProductCreatedEvent(ProductEvent event) {
        Product product = event.getProduct();
        log.info("Traitement asynchrone de l'événement de création de produit: {}", product.getName());

        // Simuler un traitement en arrière-plan
        try {
            Thread.sleep(500);
            log.info("Traitement terminé de l'événement de création de produit pour: {}", product.getName());
            // Dans une application réelle, cela pourrait être:
            // - Envoi de notifications
            // - Mise à jour des index de recherche
            // - Génération de rapports
            // - Synchronisation avec des systèmes externes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors du traitement de l'événement de création de produit", e);
        }
    }

    /**
     * Gère les événements de mise à jour de produit de manière asynchrone.
     */
    @Async
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).UPDATED")
    public void handleProductUpdatedEvent(ProductEvent event) {
        Product product = event.getProduct();
        log.info("Traitement asynchrone de l'événement de mise à jour de produit: {}", product.getName());

        // Simuler un traitement en arrière-plan
        try {
            Thread.sleep(300);
            log.info("Traitement terminé de l'événement de mise à jour de produit pour: {}", product.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors du traitement de l'événement de mise à jour de produit", e);
        }
    }

    /**
     * Gère les événements de suppression de produit de manière asynchrone.
     */
    @Async
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).DELETED")
    public void handleProductDeletedEvent(ProductEvent event) {
        Product product = event.getProduct();
        log.info("Traitement asynchrone de l'événement de suppression de produit: {}", product.getId());

        // Simuler un traitement en arrière-plan
        try {
            Thread.sleep(200);
            log.info("Traitement terminé de l'événement de suppression de produit pour l'ID: {}", product.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors du traitement de l'événement de suppression de produit", e);
        }
    }
}
