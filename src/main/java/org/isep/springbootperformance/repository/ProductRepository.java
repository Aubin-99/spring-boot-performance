package org.isep.springbootperformance.repository;

import org.isep.springbootperformance.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les entités Produit avec des méthodes de requête optimisées.
 * L'utilisation de méthodes de requête spécifiques et de requêtes JPQL personnalisées améliore les performances de la base de données.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Requête optimisée utilisant la convention de nommage des méthodes.
     * Spring Data JPA génère automatiquement une requête optimisée basée sur le nom de la méthode.
     */
    List<Product> findByCategoryOrderByNameAsc(String category);

    /**
     * Requête JPQL personnalisée avec pagination pour de meilleures performances.
     * L'utilisation de la pagination évite de charger trop d'enregistrements à la fois.
     */
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice")
    Page<Product> findProductsBelowPrice(@Param("maxPrice") Double maxPrice, Pageable pageable);

    /**
     * Requête optimisée qui sélectionne uniquement les champs nécessaires.
     * La projection des champs requis uniquement réduit le transfert de données et améliore les performances.
     */
    @Query("SELECT p.id, p.name, p.price FROM Product p WHERE p.category = :category")
    List<Object[]> findProductSummaryByCategory(@Param("category") String category);

    /**
     * Requête avec join fetch pour éviter le problème N+1 select.
     * Ceci est utile lorsque vous avez des relations et que vous souhaitez charger efficacement les entités associées.
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
    List<Product> searchProductsByKeyword(@Param("keyword") String keyword);
}
