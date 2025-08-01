package org.isep.springbootperformance.event;

import lombok.Getter;
import org.isep.springbootperformance.model.Product;
import org.springframework.context.ApplicationEvent;

/**
 * Classe d'événement pour les événements liés aux produits.
 * Fait partie du modèle d'architecture orientée événements pour de meilleures performances et une meilleure évolutivité.
 */
@Getter
public class ProductEvent extends ApplicationEvent {

    private final EventType eventType;
    private final Product product;

    /**
     * Crée un nouvel événement de produit.
     *
     * @param source    la source de l'événement
     * @param eventType le type d'événement
     * @param product   le produit associé à l'événement
     */
    public ProductEvent(Object source, EventType eventType, Product product) {
        super(source);
        this.eventType = eventType;
        this.product = product;
    }

    /**
     * Enum représentant différents types d'événements de produit.
     */
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}
