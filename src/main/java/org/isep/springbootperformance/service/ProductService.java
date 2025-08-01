package org.isep.springbootperformance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.isep.springbootperformance.event.ProductEvent;
import org.isep.springbootperformance.model.Product;
import org.isep.springbootperformance.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour les opérations sur les produits avec des optimisations de performance.
 * Démontre la mise en cache, la gestion des transactions et d'autres techniques de performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Récupère un produit par ID avec mise en cache.
     * L'annotation @Cacheable stocke le résultat en cache pour éviter les requêtes de base de données pour les appels ultérieurs.
     */
    @Cacheable(value = "productCache", key = "#id")
    public Optional<Product> getProductById(Long id) {
        log.info("Récupération du produit avec l'id: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Enregistre un nouveau produit.
     * @CachePut met à jour le cache lorsqu'un nouveau produit est enregistré.
     * Publie également un événement pour un traitement asynchrone.
     */
    @CachePut(value = "productCache", key = "#result.id")
    @Transactional
    public Product saveProduct(Product product) {
        log.info("Enregistrement d'un nouveau produit: {}", product.getName());
        Product savedProduct = productRepository.save(product);

        // Publie un événement pour un traitement asynchrone
        eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.CREATED, savedProduct));
        log.debug("Événement de création de produit publié");

        return savedProduct;
    }

    /**
     * Met à jour un produit existant.
     * @CachePut met à jour le cache lorsqu'un produit est mis à jour.
     * Publie également un événement pour un traitement asynchrone.
     */
    @CachePut(value = "productCache", key = "#product.id")
    @Transactional
    public Product updateProduct(Product product) {
        log.info("Mise à jour du produit avec l'id: {}", product.getId());
        Product updatedProduct = productRepository.save(product);

        // Publie un événement pour un traitement asynchrone
        eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.UPDATED, updatedProduct));
        log.debug("Événement de mise à jour de produit publié");

        return updatedProduct;
    }

    /**
     * Supprime un produit par ID.
     * @CacheEvict supprime le produit du cache lors de la suppression.
     * Publie également un événement pour un traitement asynchrone.
     */
    @CacheEvict(value = "productCache", key = "#id")
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Suppression du produit avec l'id: {}", id);

        // Récupère le produit avant de le supprimer pour pouvoir l'inclure dans l'événement
        Optional<Product> productOptional = productRepository.findById(id);

        productRepository.deleteById(id);

        // Publie un événement pour un traitement asynchrone si le produit existe
        productOptional.ifPresent(product -> {
            eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.DELETED, product));
            log.debug("Événement de suppression de produit publié");
        });
    }

    /**
     * Récupère les produits par catégorie avec tri.
     * Cette méthode utilise la méthode optimisée du repository.
     */
    public List<Product> getProductsByCategory(String category) {
        log.info("Récupération des produits par catégorie: {}", category);
        return productRepository.findByCategoryOrderByNameAsc(category);
    }

    /**
     * Récupère les produits en dessous d'un certain prix avec pagination.
     * La pagination améliore les performances en limitant la quantité de données transférées.
     */
    public Page<Product> getProductsBelowPrice(Double maxPrice, int page, int size) {
        log.info("Récupération des produits en dessous du prix: {} (page: {}, taille: {})", maxPrice, page, size);
        return productRepository.findProductsBelowPrice(maxPrice, PageRequest.of(page, size));
    }

    /**
     * Récupère les résumés de produits par catégorie.
     * Cette méthode utilise la projection pour ne récupérer que les champs nécessaires.
     */
    public List<Object[]> getProductSummaryByCategory(String category) {
        log.info("Récupération des résumés de produits pour la catégorie: {}", category);
        return productRepository.findProductSummaryByCategory(category);
    }

    /**
     * Recherche des produits par mot-clé.
     */
    public List<Product> searchProducts(String keyword) {
        log.info("Recherche de produits avec le mot-clé: {}", keyword);
        return productRepository.searchProductsByKeyword(keyword);
    }

    /**
     * Récupère tous les produits avec pagination.
     * La pagination améliore les performances en limitant la quantité de données transférées.
     */
    public Page<Product> getAllProducts(int page, int size) {
        log.info("Récupération de tous les produits (page: {}, taille: {})", page, size);
        return productRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Vide tous les caches de produits.
     * Utile pour les opérations d'administration ou lors de mises à jour en masse.
     */
    @CacheEvict(value = "productCache", allEntries = true)
    public void clearProductCache() {
        log.info("Vidage de tous les caches de produits");
    }
}
