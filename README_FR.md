# Stratégies d'Optimisation des Performances pour Spring Boot

Ce projet démontre diverses techniques d'optimisation des performances pour les applications Spring Boot. Il fournit un ensemble complet de stratégies pour améliorer les performances de l'application, du temps de démarrage à l'accès à la base de données, la mise en cache, les performances web, et plus encore.

## Table des Matières

- [Structure du Projet](#structure-du-projet)
- [Explication Détaillée des Fichiers](#explication-détaillée-des-fichiers)
- [Stratégies d'Optimisation des Performances](#stratégies-doptimisation-des-performances)
- [Guide de Test des Stratégies de Performance](#guide-de-test-des-stratégies-de-performance)
- [Prérequis et Exécution](#prérequis-et-exécution)

## Structure du Projet

Le projet est organisé selon la structure standard d'une application Spring Boot :

```
src/main/java/org/isep/springbootperformance/
├── SpringBootPerformanceApplication.java    # Point d'entrée de l'application
├── config/                                  # Configuration de l'application
│   ├── AsyncConfig.java                     # Configuration du traitement asynchrone
│   ├── CacheConfig.java                     # Configuration de la mise en cache
│   ├── DataLoader.java                      # Chargeur de données initiales
│   ├── JvmConfig.java                       # Configuration et informations JVM
│   └── WebConfig.java                       # Configuration web et HTTP
├── controller/                              # Contrôleurs REST
│   └── ProductController.java               # API REST pour les produits
├── event/                                   # Architecture orientée événements
│   ├── ProductEvent.java                    # Classe d'événement pour les produits
│   └── ProductEventListener.java            # Écouteur d'événements asynchrone
├── model/                                   # Entités de domaine
│   └── Product.java                         # Entité Produit avec indexation optimisée
├── repository/                              # Couche d'accès aux données
│   └── ProductRepository.java               # Repository avec requêtes optimisées
└── service/                                 # Couche de service
    └── ProductService.java                  # Service avec mise en cache et transactions
```

## Explication Détaillée des Fichiers

### SpringBootPerformanceApplication.java

Point d'entrée principal de l'application Spring Boot. Ce fichier contient la méthode `main` qui démarre l'application.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance;

// Importation des classes nécessaires de Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Annotation qui combine @Configuration, @EnableAutoConfiguration et @ComponentScan
// Cette annotation indique à Spring Boot que c'est la classe principale de l'application
@SpringBootApplication
public class SpringBootPerformanceApplication {

    // Méthode principale qui est le point d'entrée de l'application Java
    public static void main(String[] args) {
        // Appel à la méthode statique run de SpringApplication qui démarre l'application Spring Boot
        // Le premier argument est la classe principale, le second est les arguments de ligne de commande
        SpringApplication.run(SpringBootPerformanceApplication.class, args);
    }
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance;` - Définit le package dans lequel se trouve cette classe.
2. Lignes d'importation - Importent les classes nécessaires du framework Spring Boot.
3. `@SpringBootApplication` - Annotation clé qui active la configuration automatique de Spring Boot, le scan des composants et d'autres fonctionnalités.
4. `public class SpringBootPerformanceApplication {` - Déclare la classe principale de l'application.
5. `public static void main(String[] args) {` - Méthode principale qui est exécutée au démarrage de l'application.
6. `SpringApplication.run(SpringBootPerformanceApplication.class, args);` - Démarre l'application Spring Boot en initialisant le contexte d'application Spring.

### Configuration

#### AsyncConfig.java

Configure le traitement asynchrone pour améliorer la réactivité de l'application.

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);         // Nombre minimum de threads
        executor.setMaxPoolSize(10);         // Nombre maximum de threads
        executor.setQueueCapacity(25);       // Capacité de la file d'attente
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
```

#### CacheConfig.java

Active et configure la mise en cache au niveau de l'application pour réduire la charge de la base de données.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("productCache");
    }
}
```

#### DataLoader.java

Initialise la base de données avec des données d'exemple pour tester les performances.

```java
@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataLoader {
    private final ProductRepository productRepository;

    @Bean
    @Profile("!test & !prod")
    public CommandLineRunner initDatabase() {
        return args -> {
            // Vérifie si la base de données contient déjà des données
            if (productRepository.count() > 0) {
                return;
            }

            // Crée et sauvegarde 25 produits d'exemple dans 5 catégories
            // (Electronics, Books, Clothing, Home, Sports)
            // Création de produits d'exemple
            List<Product> products = new ArrayList<>();
            products.add(createProduct("Smartphone", "Electronics", 799.99, "Description"));
            // ... autres produits
            productRepository.saveAll(products);
        };
    }
}
```

#### JvmConfig.java

Fournit des informations et des recommandations sur les paramètres JVM pour optimiser les performances.

```java
@Configuration
@Slf4j
public class JvmConfig {
    @PostConstruct
    public void logJvmInfo() {
        // Journalise les informations JVM au démarrage
        Runtime runtime = Runtime.getRuntime();
        log.info("Processeurs disponibles: {}", runtime.availableProcessors());
        log.info("Mémoire libre: {} MB", runtime.freeMemory() / (1024 * 1024));
        // ... autres informations
    }
}
```

#### WebConfig.java

Configure divers paramètres web pour améliorer les performances.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Configuration des ressources statiques avec mise en cache agressive
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .immutable());
    }

    // Configuration CORS optimisée
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .maxAge(3600); // Cache des requêtes préliminaires pendant 1 heure
    }

    // Configuration de la négociation de contenu
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    // Configuration de Jackson pour une sérialisation JSON optimisée
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }
}
```

### Modèle

#### Product.java

Entité JPA représentant un produit avec indexation optimisée de la base de données.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance.model;

// Importations des annotations JPA pour la persistance
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
// Importations des annotations de validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
// Importations des annotations Lombok pour réduire le code boilerplate
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité Produit avec indexation optimisée de la base de données pour les performances.
 * L'utilisation d'index sur les champs fréquemment interrogés améliore les performances des requêtes de base de données.
 */
// Annotation indiquant que cette classe est une entité JPA (sera mappée à une table dans la base de données)
@Entity
// Annotation Lombok qui génère automatiquement getters, setters, equals, hashCode et toString
@Data
// Annotation Lombok qui implémente le pattern Builder pour cette classe
@Builder
// Annotation Lombok qui génère un constructeur sans arguments
@NoArgsConstructor
// Annotation Lombok qui génère un constructeur avec tous les arguments
@AllArgsConstructor
// Annotation qui définit les index de la table pour optimiser les requêtes
@Table(indexes = {
    // Index sur la colonne "name" pour accélérer les recherches par nom
    @Index(name = "idx_product_name", columnList = "name"),
    // Index sur la colonne "category" pour accélérer les recherches par catégorie
    @Index(name = "idx_product_category", columnList = "category")
})
public class Product {

    // Annotation indiquant que ce champ est la clé primaire
    @Id
    // Annotation indiquant que la valeur de l'ID est générée automatiquement (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Annotation de validation qui vérifie que le champ n'est pas vide
    @NotBlank(message = "Le nom du produit est requis")
    private String name;

    // Annotation de validation qui vérifie que le champ n'est pas vide
    @NotBlank(message = "La catégorie est requise")
    private String category;

    // Annotation de validation qui vérifie que le prix est positif
    @Positive(message = "Le prix doit être positif")
    private Double price;

    // Champ description sans contrainte de validation
    private String description;
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.model;` - Définit le package dans lequel se trouve cette classe.
2-14. Lignes d'importation - Importent les annotations JPA, les validations et les annotations Lombok nécessaires.
15-19. Commentaire Javadoc expliquant le but de cette classe et l'avantage de l'indexation.
20. `@Entity` - Indique que cette classe est une entité JPA qui sera persistée dans la base de données.
21. `@Data` - Annotation Lombok qui génère automatiquement les méthodes getter, setter, equals, hashCode et toString.
22. `@Builder` - Annotation Lombok qui implémente le pattern Builder pour créer des instances de manière fluide.
23. `@NoArgsConstructor` - Génère un constructeur sans arguments, requis par JPA.
24. `@AllArgsConstructor` - Génère un constructeur avec tous les champs comme arguments.
25-30. `@Table(indexes = {...})` - Définit des index sur les colonnes "name" et "category" pour optimiser les performances des requêtes.
31. `public class Product {` - Déclare la classe Product.
33. `@Id` - Marque le champ comme clé primaire de l'entité.
34. `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Configure la génération automatique des valeurs d'ID.
35. `private Long id;` - Déclare le champ id de type Long.
37-38. `@NotBlank(message = "Le nom du produit est requis")` et `private String name;` - Déclare le champ name avec validation.
40-41. `@NotBlank(message = "La catégorie est requise")` et `private String category;` - Déclare le champ category avec validation.
43-44. `@Positive(message = "Le prix doit être positif")` et `private Double price;` - Déclare le champ price avec validation.
46. `private String description;` - Déclare le champ description sans validation spécifique.

### Repository

#### ProductRepository.java

Interface de repository avec des méthodes de requête optimisées.

```java
// Déclaration du package pour cette interface
package org.isep.springbootperformance.repository;

// Importation de l'entité Product
import org.isep.springbootperformance.model.Product;
// Importations pour la pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Importation du repository JPA
import org.springframework.data.jpa.repository.JpaRepository;
// Importations pour les requêtes personnalisées
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
// Importation de l'annotation Repository
import org.springframework.stereotype.Repository;

// Importation pour les collections
import java.util.List;

/**
 * Repository pour les entités Produit avec des méthodes de requête optimisées.
 * L'utilisation de méthodes de requête spécifiques et de requêtes JPQL personnalisées améliore les performances de la base de données.
 */
// Annotation indiquant que cette interface est un repository Spring
@Repository
// Interface qui étend JpaRepository avec Product comme entité et Long comme type d'ID
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Requête optimisée utilisant la convention de nommage des méthodes.
     * Spring Data JPA génère automatiquement une requête optimisée basée sur le nom de la méthode.
     */
    // Méthode qui trouve tous les produits d'une catégorie donnée et les trie par nom en ordre croissant
    List<Product> findByCategoryOrderByNameAsc(String category);

    /**
     * Requête JPQL personnalisée avec pagination pour de meilleures performances.
     * L'utilisation de la pagination évite de charger trop d'enregistrements à la fois.
     */
    // Annotation définissant une requête JPQL personnalisée
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice")
    // Méthode qui trouve tous les produits dont le prix est inférieur ou égal à maxPrice, avec pagination
    Page<Product> findProductsBelowPrice(@Param("maxPrice") Double maxPrice, Pageable pageable);

    /**
     * Requête optimisée qui sélectionne uniquement les champs nécessaires.
     * La projection des champs requis uniquement réduit le transfert de données et améliore les performances.
     */
    // Requête JPQL qui sélectionne seulement certains champs (projection)
    @Query("SELECT p.id, p.name, p.price FROM Product p WHERE p.category = :category")
    // Méthode qui retourne un résumé des produits (id, nom, prix) pour une catégorie donnée
    List<Object[]> findProductSummaryByCategory(@Param("category") String category);

    /**
     * Requête avec join fetch pour éviter le problème N+1 select.
     * Ceci est utile lorsque vous avez des relations et que vous souhaitez charger efficacement les entités associées.
     */
    // Requête JPQL qui recherche des produits par mot-clé dans le nom
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
    // Méthode qui trouve tous les produits dont le nom contient le mot-clé spécifié
    List<Product> searchProductsByKeyword(@Param("keyword") String keyword);
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.repository;` - Définit le package dans lequel se trouve cette interface.
2-12. Lignes d'importation - Importent les classes nécessaires pour le repository, la pagination, et les requêtes personnalisées.
13-16. Commentaire Javadoc expliquant le but de cette interface et les avantages des requêtes optimisées.
17. `@Repository` - Annotation Spring qui marque cette interface comme un composant repository.
18. `public interface ProductRepository extends JpaRepository<Product, Long> {` - Déclare l'interface qui étend JpaRepository avec Product comme type d'entité et Long comme type d'ID.
19-23. Commentaire Javadoc expliquant la première méthode de requête.
24. `List<Product> findByCategoryOrderByNameAsc(String category);` - Méthode qui utilise la convention de nommage de Spring Data pour générer une requête qui trouve les produits par catégorie et les trie par nom.
25-29. Commentaire Javadoc expliquant la deuxième méthode de requête avec pagination.
30. `@Query("SELECT p FROM Product p WHERE p.price <= :maxPrice")` - Annotation définissant une requête JPQL personnalisée.
31. `Page<Product> findProductsBelowPrice(@Param("maxPrice") Double maxPrice, Pageable pageable);` - Méthode qui trouve les produits dont le prix est inférieur à une valeur donnée, avec pagination.
32-36. Commentaire Javadoc expliquant la troisième méthode de requête avec projection.
37. `@Query("SELECT p.id, p.name, p.price FROM Product p WHERE p.category = :category")` - Requête JPQL qui sélectionne seulement certains champs.
38. `List<Object[]> findProductSummaryByCategory(@Param("category") String category);` - Méthode qui retourne un résumé des produits pour une catégorie donnée.
39-43. Commentaire Javadoc expliquant la quatrième méthode de requête.
44. `@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")` - Requête JPQL qui recherche des produits par mot-clé.
45. `List<Product> searchProductsByKeyword(@Param("keyword") String keyword);` - Méthode qui trouve les produits dont le nom contient un mot-clé spécifié.

### Service

#### ProductService.java

Service pour les opérations sur les produits avec mise en cache, gestion des transactions et architecture orientée événements.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance.service;

// Importations des annotations Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Importations des classes du projet
import org.isep.springbootperformance.event.ProductEvent;
import org.isep.springbootperformance.model.Product;
import org.isep.springbootperformance.repository.ProductRepository;
// Importations des annotations de cache
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
// Importations pour la publication d'événements
import org.springframework.context.ApplicationEventPublisher;
// Importations pour la pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
// Importation de l'annotation Service
import org.springframework.stereotype.Service;
// Importation pour la gestion des transactions
import org.springframework.transaction.annotation.Transactional;

// Importations pour les collections et les types optionnels
import java.util.List;
import java.util.Optional;

/**
 * Service pour les opérations sur les produits avec des optimisations de performance.
 * Démontre la mise en cache, la gestion des transactions et d'autres techniques de performance.
 */
// Annotation indiquant que cette classe est un service Spring
@Service
// Annotation Lombok qui génère un constructeur avec tous les champs finals comme paramètres
@RequiredArgsConstructor
// Annotation Lombok qui ajoute un logger SLF4J à la classe
@Slf4j
public class ProductService {

    // Injection du repository via le constructeur (grâce à @RequiredArgsConstructor)
    private final ProductRepository productRepository;
    // Injection du publisher d'événements via le constructeur
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Récupère un produit par ID avec mise en cache.
     * L'annotation @Cacheable stocke le résultat en cache pour éviter les requêtes de base de données pour les appels ultérieurs.
     */
    // Annotation qui met en cache le résultat de cette méthode
    @Cacheable(value = "productCache", key = "#id")
    public Optional<Product> getProductById(Long id) {
        // Journalisation de l'opération
        log.info("Récupération du produit avec l'id: {}", id);
        // Appel au repository pour trouver le produit par ID
        return productRepository.findById(id);
    }

    /**
     * Enregistre un nouveau produit.
     * @CachePut met à jour le cache lorsqu'un nouveau produit est enregistré.
     * Publie également un événement pour un traitement asynchrone.
     */
    // Annotation qui met à jour le cache avec le résultat de cette méthode
    @CachePut(value = "productCache", key = "#result.id")
    // Annotation qui entoure cette méthode dans une transaction
    @Transactional
    public Product saveProduct(Product product) {
        // Journalisation de l'opération
        log.info("Enregistrement d'un nouveau produit: {}", product.getName());
        // Sauvegarde du produit dans la base de données
        Product savedProduct = productRepository.save(product);

        // Publie un événement pour un traitement asynchrone
        eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.CREATED, savedProduct));
        // Journalisation de la publication de l'événement
        log.debug("Événement de création de produit publié");

        // Retourne le produit sauvegardé
        return savedProduct;
    }

    /**
     * Met à jour un produit existant.
     * @CachePut met à jour le cache lorsqu'un produit est mis à jour.
     * Publie également un événement pour un traitement asynchrone.
     */
    // Annotation qui met à jour le cache avec le résultat de cette méthode
    @CachePut(value = "productCache", key = "#product.id")
    // Annotation qui entoure cette méthode dans une transaction
    @Transactional
    public Product updateProduct(Product product) {
        // Journalisation de l'opération
        log.info("Mise à jour du produit avec l'id: {}", product.getId());
        // Sauvegarde du produit mis à jour dans la base de données
        Product updatedProduct = productRepository.save(product);

        // Publie un événement pour un traitement asynchrone
        eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.UPDATED, updatedProduct));
        // Journalisation de la publication de l'événement
        log.debug("Événement de mise à jour de produit publié");

        // Retourne le produit mis à jour
        return updatedProduct;
    }

    /**
     * Supprime un produit par ID.
     * @CacheEvict supprime le produit du cache lors de la suppression.
     * Publie également un événement pour un traitement asynchrone.
     */
    // Annotation qui supprime l'entrée du cache correspondant à l'ID
    @CacheEvict(value = "productCache", key = "#id")
    // Annotation qui entoure cette méthode dans une transaction
    @Transactional
    public void deleteProduct(Long id) {
        // Journalisation de l'opération
        log.info("Suppression du produit avec l'id: {}", id);

        // Récupère le produit avant de le supprimer pour pouvoir l'inclure dans l'événement
        Optional<Product> productOptional = productRepository.findById(id);

        // Supprime le produit de la base de données
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
        // Journalisation de l'opération
        log.info("Récupération des produits par catégorie: {}", category);
        // Appel à la méthode optimisée du repository
        return productRepository.findByCategoryOrderByNameAsc(category);
    }

    /**
     * Récupère les produits en dessous d'un certain prix avec pagination.
     * La pagination améliore les performances en limitant la quantité de données transférées.
     */
    public Page<Product> getProductsBelowPrice(Double maxPrice, int page, int size) {
        // Journalisation de l'opération
        log.info("Récupération des produits en dessous du prix: {} (page: {}, taille: {})", maxPrice, page, size);
        // Appel à la méthode du repository avec pagination
        return productRepository.findProductsBelowPrice(maxPrice, PageRequest.of(page, size));
    }

    /**
     * Récupère les résumés de produits par catégorie.
     * Cette méthode utilise la projection pour ne récupérer que les champs nécessaires.
     */
    public List<Object[]> getProductSummaryByCategory(String category) {
        // Journalisation de l'opération
        log.info("Récupération des résumés de produits pour la catégorie: {}", category);
        // Appel à la méthode du repository qui utilise la projection
        return productRepository.findProductSummaryByCategory(category);
    }

    /**
     * Recherche des produits par mot-clé.
     */
    public List<Product> searchProducts(String keyword) {
        // Journalisation de l'opération
        log.info("Recherche de produits avec le mot-clé: {}", keyword);
        // Appel à la méthode de recherche du repository
        return productRepository.searchProductsByKeyword(keyword);
    }

    /**
     * Récupère tous les produits avec pagination.
     * La pagination améliore les performances en limitant la quantité de données transférées.
     */
    public Page<Product> getAllProducts(int page, int size) {
        // Journalisation de l'opération
        log.info("Récupération de tous les produits (page: {}, taille: {})", page, size);
        // Appel à la méthode findAll du repository avec pagination
        return productRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Vide tous les caches de produits.
     * Utile pour les opérations d'administration ou lors de mises à jour en masse.
     */
    // Annotation qui vide toutes les entrées du cache productCache
    @CacheEvict(value = "productCache", allEntries = true)
    public void clearProductCache() {
        // Journalisation de l'opération
        log.info("Vidage de tous les caches de produits");
    }
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.service;` - Définit le package dans lequel se trouve cette classe.
2-19. Lignes d'importation - Importent les classes et annotations nécessaires pour le service, la mise en cache, les transactions, etc.
20-23. Commentaire Javadoc expliquant le but de cette classe et ses optimisations de performance.
24. `@Service` - Annotation Spring qui marque cette classe comme un composant service.
25. `@RequiredArgsConstructor` - Annotation Lombok qui génère un constructeur avec tous les champs finals comme paramètres, facilitant l'injection de dépendances.
26. `@Slf4j` - Annotation Lombok qui ajoute un logger SLF4J à la classe pour la journalisation.
27. `public class ProductService {` - Déclare la classe ProductService.
29-30. Déclaration des dépendances injectées via le constructeur généré par Lombok.
32-36. Commentaire Javadoc expliquant la méthode getProductById et son utilisation du cache.
37. `@Cacheable(value = "productCache", key = "#id")` - Annotation qui met en cache le résultat de la méthode, évitant des appels répétés à la base de données.
38-41. Méthode getProductById qui récupère un produit par son ID avec journalisation.
42-47. Commentaire Javadoc expliquant la méthode saveProduct et ses optimisations.
48. `@CachePut(value = "productCache", key = "#result.id")` - Annotation qui met à jour le cache avec le résultat de la méthode.
49. `@Transactional` - Annotation qui entoure la méthode dans une transaction pour garantir l'intégrité des données.
50-58. Méthode saveProduct qui sauvegarde un produit, publie un événement et journalise les opérations.
59-64. Commentaire Javadoc expliquant la méthode updateProduct et ses optimisations.
65-76. Méthode updateProduct similaire à saveProduct mais pour la mise à jour d'un produit existant.
77-83. Commentaire Javadoc expliquant la méthode deleteProduct et ses optimisations.
84. `@CacheEvict(value = "productCache", key = "#id")` - Annotation qui supprime l'entrée du cache correspondant à l'ID lors de la suppression.
85-98. Méthode deleteProduct qui supprime un produit, publie un événement et journalise les opérations.
99-107. Méthode getProductsByCategory qui récupère les produits par catégorie avec tri.
108-116. Méthode getProductsBelowPrice qui récupère les produits en dessous d'un certain prix avec pagination.
117-125. Méthode getProductSummaryByCategory qui utilise la projection pour optimiser les performances.
126-133. Méthode searchProducts qui recherche des produits par mot-clé.
134-142. Méthode getAllProducts qui récupère tous les produits avec pagination.
143-148. Commentaire Javadoc expliquant la méthode clearProductCache.
149. `@CacheEvict(value = "productCache", allEntries = true)` - Annotation qui vide toutes les entrées du cache productCache.
150-152. Méthode clearProductCache qui vide le cache des produits avec journalisation.

### Contrôleur

#### ProductController.java

Contrôleur REST pour les opérations sur les produits avec mise en cache HTTP, compression et traitement asynchrone.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance.controller;

// Importations des annotations Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Importations des classes du projet
import org.isep.springbootperformance.model.Product;
import org.isep.springbootperformance.service.ProductService;
// Importations pour la pagination
import org.springframework.data.domain.Page;
// Importations pour la mise en cache HTTP
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
// Importations des annotations REST
import org.springframework.web.bind.annotation.*;

// Importations pour les collections, les types optionnels et le traitement asynchrone
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Contrôleur REST pour les opérations sur les produits avec des optimisations de performance web.
 * Démontre la mise en cache HTTP, la compression et le traitement asynchrone.
 */
// Annotation indiquant que cette classe est un contrôleur REST
@RestController
// Annotation définissant le chemin de base pour toutes les méthodes de ce contrôleur
@RequestMapping("/api/products")
// Annotation Lombok qui génère un constructeur avec tous les champs finals comme paramètres
@RequiredArgsConstructor
// Annotation Lombok qui ajoute un logger SLF4J à la classe
@Slf4j
public class ProductController {

    // Injection du service via le constructeur (grâce à @RequiredArgsConstructor)
    private final ProductService productService;

    /**
     * Récupère tous les produits avec mise en cache HTTP.
     * Utilise les en-têtes ETag et Cache-Control pour une mise en cache efficace.
     */
    // Annotation mappant les requêtes GET sur "/{id}" à cette méthode
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Product>> getProductById(@PathVariable Long id) {
        // Journalisation de la requête
        log.info("REST request to get Product by id: {}", id);
        // Appel au service pour récupérer le produit
        Optional<Product> product = productService.getProductById(id);

        // Configuration des en-têtes de contrôle de cache pour le navigateur (30 minutes)
        CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES)
                .noTransform()
                .mustRevalidate();

        // Retourne une réponse avec les en-têtes de cache et le produit
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(product);
    }

    /**
     * Crée un nouveau produit.
     */
    // Annotation mappant les requêtes POST sur le chemin de base à cette méthode
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Journalisation de la requête
        log.info("REST request to save Product: {}", product);
        // Appel au service pour sauvegarder le produit
        Product result = productService.saveProduct(product);
        // Retourne une réponse avec le produit créé
        return ResponseEntity.ok(result);
    }

    /**
     * Met à jour un produit existant.
     */
    // Annotation mappant les requêtes PUT sur "/{id}" à cette méthode
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // Journalisation de la requête
        log.info("REST request to update Product: {}", product);
        // Définit l'ID du produit à partir du chemin
        product.setId(id);
        // Appel au service pour mettre à jour le produit
        Product result = productService.updateProduct(product);
        // Retourne une réponse avec le produit mis à jour
        return ResponseEntity.ok(result);
    }

    /**
     * Supprime un produit.
     */
    // Annotation mappant les requêtes DELETE sur "/{id}" à cette méthode
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Journalisation de la requête
        log.info("REST request to delete Product: {}", id);
        // Appel au service pour supprimer le produit
        productService.deleteProduct(id);
        // Retourne une réponse 204 No Content
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les produits par catégorie.
     * Utilise la mise en cache HTTP pour les données fréquemment consultées et relativement statiques.
     */
    // Annotation mappant les requêtes GET sur "/category/{category}" à cette méthode
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        // Journalisation de la requête
        log.info("REST request to get Products by category: {}", category);
        // Appel au service pour récupérer les produits par catégorie
        List<Product> products = productService.getProductsByCategory(category);

        // Configuration des en-têtes de contrôle de cache (10 minutes)
        CacheControl cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES)
                .noTransform();

        // Retourne une réponse avec les en-têtes de cache et les produits
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(products);
    }

    /**
     * Récupère les produits en dessous d'un certain prix avec pagination.
     * La pagination améliore les performances en limitant le transfert de données.
     */
    // Annotation mappant les requêtes GET sur "/price" à cette méthode
    @GetMapping("/price")
    public ResponseEntity<Page<Product>> getProductsBelowPrice(
            // Paramètre de requête pour le prix maximum
            @RequestParam Double maxPrice,
            // Paramètre de requête pour la page avec valeur par défaut
            @RequestParam(defaultValue = "0") int page,
            // Paramètre de requête pour la taille de page avec valeur par défaut
            @RequestParam(defaultValue = "10") int size) {
        // Journalisation de la requête
        log.info("REST request to get Products below price: {}", maxPrice);
        // Appel au service pour récupérer les produits en dessous du prix avec pagination
        Page<Product> products = productService.getProductsBelowPrice(maxPrice, page, size);
        // Retourne une réponse avec les produits
        return ResponseEntity.ok(products);
    }

    /**
     * Récupère les résumés de produits par catégorie.
     * Utilise la projection pour ne renvoyer que les données nécessaires.
     */
    // Annotation mappant les requêtes GET sur "/summary/{category}" à cette méthode
    @GetMapping("/summary/{category}")
    public ResponseEntity<List<Object[]>> getProductSummaryByCategory(@PathVariable String category) {
        // Journalisation de la requête
        log.info("REST request to get Product summaries by category: {}", category);
        // Appel au service pour récupérer les résumés de produits
        List<Object[]> summaries = productService.getProductSummaryByCategory(category);
        // Retourne une réponse avec les résumés
        return ResponseEntity.ok(summaries);
    }

    /**
     * Recherche des produits de manière asynchrone.
     * Démontre le traitement asynchrone pour une meilleure évolutivité.
     */
    // Annotation mappant les requêtes GET sur "/search" à cette méthode
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<Product>>> searchProducts(@RequestParam String keyword) {
        // Journalisation de la requête
        log.info("REST request to search Products with keyword: {}", keyword);
        // Effectue la recherche de manière asynchrone
        return CompletableFuture.supplyAsync(() -> {
            // Appel au service pour rechercher les produits (exécuté dans un thread séparé)
            List<Product> products = productService.searchProducts(keyword);
            // Retourne une réponse avec les produits trouvés
            return ResponseEntity.ok(products);
        });
    }

    /**
     * Récupère tous les produits avec pagination.
     * La pagination améliore les performances en limitant le transfert de données.
     */
    // Annotation mappant les requêtes GET sur le chemin de base à cette méthode
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            // Paramètre de requête pour la page avec valeur par défaut
            @RequestParam(defaultValue = "0") int page,
            // Paramètre de requête pour la taille de page avec valeur par défaut
            @RequestParam(defaultValue = "20") int size) {
        // Journalisation de la requête
        log.info("REST request to get all Products (page: {}, size: {})", page, size);
        // Appel au service pour récupérer tous les produits avec pagination
        Page<Product> productPage = productService.getAllProducts(page, size);

        // Configuration des en-têtes de contrôle de cache (5 minutes)
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
                .noTransform();

        // Retourne une réponse avec les en-têtes de cache et le contenu de la page
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(productPage.getContent());
    }

    /**
     * Vide le cache des produits (opération d'administration).
     */
    // Annotation mappant les requêtes POST sur "/cache/clear" à cette méthode
    @PostMapping("/cache/clear")
    public ResponseEntity<Void> clearProductCache() {
        // Journalisation de la requête
        log.info("REST request to clear product cache");
        // Appel au service pour vider le cache
        productService.clearProductCache();
        // Retourne une réponse 200 OK sans contenu
        return ResponseEntity.ok().build();
    }
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.controller;` - Définit le package dans lequel se trouve cette classe.
2-16. Lignes d'importation - Importent les classes et annotations nécessaires pour le contrôleur REST, la mise en cache HTTP, etc.
17-20. Commentaire Javadoc expliquant le but de cette classe et ses optimisations de performance.
21. `@RestController` - Annotation Spring qui combine @Controller et @ResponseBody, indiquant que cette classe gère les requêtes HTTP et que les valeurs de retour sont automatiquement converties en JSON.
22. `@RequestMapping("/api/products")` - Définit le chemin de base pour toutes les méthodes de ce contrôleur.
23. `@RequiredArgsConstructor` - Annotation Lombok qui génère un constructeur avec tous les champs finals comme paramètres.
24. `@Slf4j` - Annotation Lombok qui ajoute un logger SLF4J à la classe.
25. `public class ProductController {` - Déclare la classe ProductController.
27. `private final ProductService productService;` - Déclare la dépendance au service qui sera injectée via le constructeur.
29-33. Commentaire Javadoc expliquant la méthode getProductById et ses optimisations.
34. `@GetMapping("/{id}")` - Mappe les requêtes GET sur "/{id}" à cette méthode.
35-46. Méthode getProductById qui récupère un produit par son ID et configure la mise en cache HTTP.
47-56. Méthode createProduct qui crée un nouveau produit.
57-67. Méthode updateProduct qui met à jour un produit existant.
68-77. Méthode deleteProduct qui supprime un produit.
78-95. Méthode getProductsByCategory qui récupère les produits par catégorie avec mise en cache HTTP.
96-109. Méthode getProductsBelowPrice qui récupère les produits en dessous d'un certain prix avec pagination.
110-120. Méthode getProductSummaryByCategory qui utilise la projection pour optimiser les performances.
121-134. Méthode searchProducts qui démontre le traitement asynchrone pour une meilleure évolutivité.
135-154. Méthode getAllProducts qui récupère tous les produits avec pagination et mise en cache HTTP.
155-164. Méthode clearProductCache qui vide le cache des produits.

### Événements

#### ProductEvent.java

Classe d'événement pour les événements liés aux produits.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance.event;

// Importation de l'annotation Lombok pour générer les getters
import lombok.Getter;
// Importation de l'entité Product
import org.isep.springbootperformance.model.Product;
// Importation de la classe de base pour les événements Spring
import org.springframework.context.ApplicationEvent;

/**
 * Classe d'événement pour les événements liés aux produits.
 * Fait partie du modèle d'architecture orientée événements pour de meilleures performances et une meilleure évolutivité.
 */
// Annotation Lombok qui génère automatiquement les méthodes getter pour tous les champs
@Getter
// Classe qui étend ApplicationEvent pour intégration avec le système d'événements de Spring
public class ProductEvent extends ApplicationEvent {

    // Champ pour stocker le type d'événement (CREATED, UPDATED, DELETED)
    private final EventType eventType;
    // Champ pour stocker le produit associé à l'événement
    private final Product product;

    /**
     * Crée un nouvel événement de produit.
     *
     * @param source    la source de l'événement
     * @param eventType le type d'événement
     * @param product   le produit associé à l'événement
     */
    public ProductEvent(Object source, EventType eventType, Product product) {
        // Appel au constructeur de la classe parente avec la source de l'événement
        super(source);
        // Initialisation du type d'événement
        this.eventType = eventType;
        // Initialisation du produit
        this.product = product;
    }

    /**
     * Enum représentant différents types d'événements de produit.
     */
    public enum EventType {
        // Événement de création de produit
        CREATED,
        // Événement de mise à jour de produit
        UPDATED,
        // Événement de suppression de produit
        DELETED
    }
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.event;` - Définit le package dans lequel se trouve cette classe.
2-7. Lignes d'importation - Importent les classes et annotations nécessaires pour l'événement.
8-11. Commentaire Javadoc expliquant le but de cette classe dans l'architecture orientée événements.
12. `@Getter` - Annotation Lombok qui génère automatiquement les méthodes getter pour tous les champs de la classe.
13. `public class ProductEvent extends ApplicationEvent {` - Déclare la classe ProductEvent qui étend ApplicationEvent de Spring.
15. `private final EventType eventType;` - Déclare un champ final pour stocker le type d'événement.
16. `private final Product product;` - Déclare un champ final pour stocker le produit associé à l'événement.
17-24. Commentaire Javadoc expliquant le constructeur et ses paramètres.
25. `public ProductEvent(Object source, EventType eventType, Product product) {` - Déclare le constructeur avec ses paramètres.
26. `super(source);` - Appelle le constructeur de la classe parente (ApplicationEvent) avec la source de l'événement.
27-28. Initialisation des champs de la classe avec les valeurs passées au constructeur.
30-37. Définition de l'énumération EventType qui représente les différents types d'événements possibles pour un produit.

#### ProductEventListener.java

Écouteur pour les événements de produit avec traitement asynchrone.

```java
// Déclaration du package pour cette classe
package org.isep.springbootperformance.event;

// Importation de l'annotation Lombok pour la journalisation
import lombok.extern.slf4j.Slf4j;
// Importation de l'entité Product
import org.isep.springbootperformance.model.Product;
// Importation de l'annotation pour l'écoute d'événements
import org.springframework.context.event.EventListener;
// Importation de l'annotation pour le traitement asynchrone
import org.springframework.scheduling.annotation.Async;
// Importation de l'annotation Component
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
// Annotation indiquant que cette classe est un composant Spring
@Component
// Annotation Lombok qui ajoute un logger SLF4J à la classe
@Slf4j
public class ProductEventListener {

    /**
     * Gère les événements de création de produit de manière asynchrone.
     * L'annotation @Async fait exécuter cette méthode dans un thread séparé.
     */
    // Annotation qui fait exécuter cette méthode dans un thread séparé
    @Async
    // Annotation qui enregistre cette méthode comme écouteur d'événements avec une condition de filtrage
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).CREATED")
    public void handleProductCreatedEvent(ProductEvent event) {
        // Récupération du produit à partir de l'événement
        Product product = event.getProduct();
        // Journalisation du début du traitement
        log.info("Traitement asynchrone de l'événement de création de produit: {}", product.getName());

        // Simuler un traitement en arrière-plan
        try {
            // Pause de 500ms pour simuler un traitement long
            Thread.sleep(500);
            // Journalisation de la fin du traitement
            log.info("Traitement terminé de l'événement de création de produit pour: {}", product.getName());
            // Dans une application réelle, cela pourrait être:
            // - Envoi de notifications
            // - Mise à jour des index de recherche
            // - Génération de rapports
            // - Synchronisation avec des systèmes externes
        } catch (InterruptedException e) {
            // Rétablissement du flag d'interruption
            Thread.currentThread().interrupt();
            // Journalisation de l'erreur
            log.error("Erreur lors du traitement de l'événement de création de produit", e);
        }
    }

    /**
     * Gère les événements de mise à jour de produit de manière asynchrone.
     */
    // Annotation qui fait exécuter cette méthode dans un thread séparé
    @Async
    // Annotation qui enregistre cette méthode comme écouteur d'événements avec une condition de filtrage
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).UPDATED")
    public void handleProductUpdatedEvent(ProductEvent event) {
        // Récupération du produit à partir de l'événement
        Product product = event.getProduct();
        // Journalisation du début du traitement
        log.info("Traitement asynchrone de l'événement de mise à jour de produit: {}", product.getName());

        // Simuler un traitement en arrière-plan
        try {
            // Pause de 300ms pour simuler un traitement long
            Thread.sleep(300);
            // Journalisation de la fin du traitement
            log.info("Traitement terminé de l'événement de mise à jour de produit pour: {}", product.getName());
        } catch (InterruptedException e) {
            // Rétablissement du flag d'interruption
            Thread.currentThread().interrupt();
            // Journalisation de l'erreur
            log.error("Erreur lors du traitement de l'événement de mise à jour de produit", e);
        }
    }

    /**
     * Gère les événements de suppression de produit de manière asynchrone.
     */
    // Annotation qui fait exécuter cette méthode dans un thread séparé
    @Async
    // Annotation qui enregistre cette méthode comme écouteur d'événements avec une condition de filtrage
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).DELETED")
    public void handleProductDeletedEvent(ProductEvent event) {
        // Récupération du produit à partir de l'événement
        Product product = event.getProduct();
        // Journalisation du début du traitement
        log.info("Traitement asynchrone de l'événement de suppression de produit: {}", product.getId());

        // Simuler un traitement en arrière-plan
        try {
            // Pause de 200ms pour simuler un traitement long
            Thread.sleep(200);
            // Journalisation de la fin du traitement
            log.info("Traitement terminé de l'événement de suppression de produit pour l'ID: {}", product.getId());
        } catch (InterruptedException e) {
            // Rétablissement du flag d'interruption
            Thread.currentThread().interrupt();
            // Journalisation de l'erreur
            log.error("Erreur lors du traitement de l'événement de suppression de produit", e);
        }
    }
}
```

**Explication ligne par ligne:**

1. `package org.isep.springbootperformance.event;` - Définit le package dans lequel se trouve cette classe.
2-11. Lignes d'importation - Importent les classes et annotations nécessaires pour l'écouteur d'événements.
12-20. Commentaire Javadoc expliquant le but de cette classe et les avantages de l'architecture orientée événements.
21. `@Component` - Annotation Spring qui marque cette classe comme un composant géré par Spring.
22. `@Slf4j` - Annotation Lombok qui ajoute un logger SLF4J à la classe.
23. `public class ProductEventListener {` - Déclare la classe ProductEventListener.
24-27. Commentaire Javadoc expliquant la méthode handleProductCreatedEvent et son fonctionnement asynchrone.
28. `@Async` - Annotation qui fait exécuter cette méthode dans un thread séparé.
29. `@EventListener(condition = "...")` - Annotation qui enregistre cette méthode comme écouteur d'événements avec une condition pour filtrer uniquement les événements de création.
30. `public void handleProductCreatedEvent(ProductEvent event) {` - Déclare la méthode qui gère les événements de création de produit.
31-46. Corps de la méthode qui traite l'événement de création, simule un traitement long et gère les erreurs.
47-65. Méthode handleProductUpdatedEvent qui gère les événements de mise à jour de produit de manière similaire.
66-84. Méthode handleProductDeletedEvent qui gère les événements de suppression de produit de manière similaire.

## Stratégies d'Optimisation des Performances

### 1. Optimisations de Démarrage et Configuration

- **Initialisation Paresseuse** : Active l'initialisation paresseuse pour améliorer le temps de démarrage.
- **Exclusion d'Auto-configurations** : Exclut les auto-configurations inutiles.
- **Gestion des Dépendances** : Inclut uniquement les dépendances nécessaires.

### 2. Optimisations de Base de Données

- **Pool de Connexions** : Configuration optimisée du pool de connexions HikariCP.
- **Requêtes Optimisées** : Utilisation de requêtes JPQL personnalisées et de méthodes de requête spécifiques.
- **Pagination** : Implémentation de la pagination pour limiter la quantité de données transférées.
- **Projection** : Sélection uniquement des champs nécessaires pour réduire le transfert de données.
- **Indexation** : Ajout d'index sur les champs fréquemment interrogés.
- **Traitement par Lots** : Configuration du traitement par lots pour de meilleures performances.

### 3. Optimisations de Cache

- **Cache d'Application** : Mise en cache des résultats de méthode avec Spring Cache.
- **Cache HTTP** : Configuration des en-têtes Cache-Control pour le cache navigateur et proxy.
- **Cache de Second Niveau** : Configuration du cache de second niveau Hibernate.

### 4. Optimisations Web

- **Compression** : Activation de la compression des réponses.
- **Mise en Cache des Ressources Statiques** : Configuration de la mise en cache à long terme des ressources statiques.
- **Traitement Asynchrone** : Implémentation de contrôleurs asynchrones pour un traitement non bloquant des requêtes.
- **Négociation de Contenu** : Configuration pour préférer JSON comme type de contenu par défaut.
- **Sérialisation JSON** : Optimisation de la sérialisation JSON.
- **Configuration CORS** : Optimisation pour réduire les requêtes préliminaires.

### 5. Optimisations JVM et Surveillance

- **Collecteur de Déchets** : Recommandations pour la configuration du GC G1.
- **Paramètres de Mémoire** : Optimisation des paramètres de mémoire JVM.
- **Métriques** : Activation des points de terminaison Actuator pour la surveillance.

### 6. Optimisations Architecturales

- **Architecture Orientée Événements** : Utilisation pour découpler les composants et permettre un traitement asynchrone.
- **Écouteurs d'Événements** : Implémentation pour traiter les événements de manière asynchrone.
- **Traitement Asynchrone** : Application pour décharger les opérations coûteuses en temps.

## Guide de Test des Stratégies de Performance

Ce guide détaillé vous explique comment tester chacune des stratégies d'optimisation des performances implémentées dans ce projet. Vous apprendrez à mesurer l'impact de chaque optimisation, à interpréter les résultats et à identifier les améliorations de performance.

### Prérequis

- Java 21 ou supérieur
- Maven 3.6 ou supérieur
- Outils de test d'API (Postman, cURL, ou similaire)
- Navigateur web moderne
- Outils de surveillance (VisualVM, JConsole, ou similaire) - optionnel mais recommandé

### Préparation de l'Environnement de Test

#### Étape 1 : Cloner et Construire le Projet

```bash
# Cloner le projet
git clone [URL_DU_REPO]
cd spring-boot-performance

# Construire le projet
mvn clean package
```

#### Étape 2 : Configurer les Outils de Surveillance (Optionnel mais Recommandé)

1. Téléchargez et installez VisualVM depuis https://visualvm.github.io/
2. Configurez VisualVM pour se connecter à votre application Java:
   ```bash
   # Ajoutez ces options JVM lors du démarrage de l'application
   -Dcom.sun.management.jmxremote
   -Dcom.sun.management.jmxremote.port=9010
   -Dcom.sun.management.jmxremote.local.only=false
   -Dcom.sun.management.jmxremote.authenticate=false
   -Dcom.sun.management.jmxremote.ssl=false
   ```

### 1. Test des Optimisations de Démarrage et Configuration

#### Préparation

1. Créez deux fichiers de propriétés pour comparer les performances:
   - `application-lazy.properties` avec l'initialisation paresseuse activée
   - `application-eager.properties` avec l'initialisation paresseuse désactivée

   **application-lazy.properties**:
   ```properties
   spring.main.lazy-initialization=true
   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
   ```

   **application-eager.properties**:
   ```properties
   spring.main.lazy-initialization=false
   ```

#### Exécution des Tests

1. Mesurez le temps de démarrage avec l'initialisation paresseuse:
   ```bash
   time java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar --spring.config.location=file:./application-lazy.properties
   ```

2. Mesurez le temps de démarrage sans l'initialisation paresseuse:
   ```bash
   time java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar --spring.config.location=file:./application-eager.properties
   ```

3. Comparez l'utilisation de la mémoire au démarrage:
   - Avec VisualVM, connectez-vous à l'application et observez l'onglet "Monitor"
   - Notez l'utilisation de la mémoire heap au démarrage dans les deux configurations

#### Analyse des Résultats

- **Temps de démarrage**: Comparez les temps de démarrage entre les deux configurations. L'initialisation paresseuse devrait réduire le temps de démarrage de 20-30%.
- **Utilisation de la mémoire**: L'initialisation paresseuse devrait réduire l'utilisation initiale de la mémoire.
- **Premier accès aux endpoints**: Notez que le premier accès à un endpoint peut être plus lent avec l'initialisation paresseuse, car les composants sont initialisés à la demande.

#### Conseils de Dépannage

- Si vous ne constatez pas d'amélioration significative, vérifiez que l'application a suffisamment de composants pour que l'initialisation paresseuse fasse une différence.
- Utilisez les logs de démarrage pour identifier les composants qui prennent le plus de temps à s'initialiser.

### 2. Test des Optimisations de Base de Données

#### Préparation

1. Démarrez l'application:
   ```bash
   java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
   ```

2. Préparez des requêtes SQL pour comparer les performances:
   - Requêtes avec index
   - Requêtes sans index
   - Requêtes avec projection
   - Requêtes sans projection
   - Requêtes avec pagination
   - Requêtes sans pagination

#### Exécution des Tests

1. Accédez à la console H2 pour exécuter des requêtes SQL directes:
   - URL: http://localhost:8087/h2-console
   - JDBC URL: jdbc:h2:mem:performancedb
   - Utilisateur: sa
   - Mot de passe: (laissez vide)

2. Vérifiez les index existants:
   ```sql
   SHOW INDEXES FROM PRODUCT;
   ```

3. Testez les requêtes avec et sans index:
   ```sql
   -- Avec index (rapide)
   SELECT * FROM PRODUCT WHERE NAME = 'Smartphone';

   -- Sans index (plus lent)
   SELECT * FROM PRODUCT WHERE DESCRIPTION LIKE '%smartphone%';
   ```

4. Testez les requêtes avec et sans projection via l'API:
   ```bash
   # Avec projection (retourne seulement id, name, price)
   curl http://localhost:8087/api/products/summary/Electronics

   # Sans projection (retourne tous les champs)
   curl http://localhost:8087/api/products/category/Electronics
   ```

5. Testez les requêtes avec et sans pagination:
   ```bash
   # Avec pagination (limite les résultats)
   curl http://localhost:8087/api/products/price?maxPrice=100&page=0&size=5

   # Sans pagination (tous les résultats)
   curl http://localhost:8087/api/products
   ```

6. Testez le pool de connexions sous charge:
   - Utilisez un outil comme Apache JMeter ou wrk pour simuler plusieurs utilisateurs simultanés
   - Configurez un test avec 50-100 utilisateurs simultanés pendant 1 minute
   - Surveillez les logs pour les messages liés au pool de connexions

#### Analyse des Résultats

- **Requêtes indexées vs non indexées**: Les requêtes utilisant des champs indexés devraient être 10-100 fois plus rapides, selon la taille des données.
- **Projection**: Comparez la taille des réponses JSON. La projection devrait réduire considérablement la taille des données transférées.
- **Pagination**: Observez le temps de réponse et l'utilisation de la mémoire. La pagination devrait maintenir des temps de réponse constants quelle que soit la taille totale des données.
- **Pool de connexions**: Sous charge, l'application devrait maintenir de bonnes performances jusqu'à la limite du pool (10 connexions dans notre configuration).

#### Conseils de Dépannage

- Si les requêtes indexées ne sont pas plus rapides, vérifiez que les index sont correctement créés et utilisés.
- Pour les problèmes de pool de connexions, vérifiez les logs pour des messages comme "HikariPool-1 - Connection is not available, request timed out".
- Utilisez `EXPLAIN` avant vos requêtes SQL pour voir si les index sont utilisés: `EXPLAIN SELECT * FROM PRODUCT WHERE NAME = 'Smartphone';`

### 3. Test des Optimisations de Cache

#### Préparation

1. Assurez-vous que l'application est démarrée avec la mise en cache activée (par défaut).
2. Préparez un outil pour mesurer les temps de réponse (comme curl avec l'option -w "%{time_total}").

#### Exécution des Tests

1. Testez le cache d'application (Spring Cache):
   ```bash
   # Premier appel (non mis en cache) - notez le temps
   time curl http://localhost:8087/api/products/1

   # Deuxième appel (depuis le cache) - notez le temps
   time curl http://localhost:8087/api/products/1
   ```

2. Vérifiez les logs pour confirmer l'utilisation du cache:
   - Le premier appel devrait afficher: "Récupération du produit avec l'id: 1"
   - Le deuxième appel ne devrait pas afficher ce message (car servi depuis le cache)

3. Testez l'éviction du cache:
   ```bash
   # Videz le cache
   curl -X POST http://localhost:8087/api/products/cache/clear

   # Vérifiez que le prochain appel accède à la base de données
   time curl http://localhost:8087/api/products/1
   ```

4. Testez le cache HTTP:
   ```bash
   # Premier appel - notez les en-têtes de cache
   curl -v http://localhost:8087/api/products/category/Electronics

   # Deuxième appel avec validation conditionnelle
   curl -v -H "If-None-Match: [ETag de la réponse précédente]" http://localhost:8087/api/products/category/Electronics
   ```

5. Testez le cache des ressources statiques:
   ```bash
   curl -v http://localhost:8087/static/index.html
   ```

#### Analyse des Résultats

- **Cache d'application**: Le deuxième appel devrait être 10-100 fois plus rapide que le premier.
- **Cache HTTP**: Le deuxième appel conditionnel devrait retourner un statut 304 (Not Modified) sans corps de réponse.
- **Cache des ressources statiques**: Vérifiez les en-têtes Cache-Control et Expires qui devraient indiquer une mise en cache à long terme.

#### Mesures de Performance

- **Temps de réponse**: Comparez les temps de réponse avec et sans cache.
- **Charge de la base de données**: Avec le cache, la base de données devrait recevoir beaucoup moins de requêtes.
- **Utilisation de la mémoire**: Surveillez l'utilisation de la mémoire pour vous assurer que le cache ne consomme pas trop de ressources.

#### Conseils de Dépannage

- Si le cache ne semble pas fonctionner, vérifiez que `@EnableCaching` est bien présent dans la configuration.
- Pour les problèmes de cache HTTP, vérifiez les en-têtes de réponse pour vous assurer que Cache-Control est correctement défini.
- Si l'utilisation de la mémoire augmente trop, ajustez la taille du cache ou utilisez un cache externe comme Redis.

### 4. Test des Optimisations Web

#### Préparation

1. Installez des outils pour tester les performances web:
   - Chrome DevTools ou Firefox Developer Tools
   - curl pour les tests en ligne de commande

#### Exécution des Tests

1. Testez la compression des réponses:
   ```bash
   # Avec compression
   curl -H "Accept-Encoding: gzip" -v http://localhost:8087/api/products | wc -c

   # Sans compression
   curl -H "Accept-Encoding: identity" -v http://localhost:8087/api/products | wc -c
   ```

2. Testez le traitement asynchrone:
   ```bash
   # Endpoint asynchrone
   time curl http://localhost:8087/api/products/search?keyword=phone

   # Créez plusieurs requêtes simultanées pour tester la capacité de traitement
   for i in {1..10}; do curl http://localhost:8087/api/products/search?keyword=phone & done
   ```

3. Testez la mise en cache des ressources statiques:
   ```bash
   # Premier appel
   curl -v http://localhost:8087/static/index.html

   # Deuxième appel (devrait utiliser le cache du navigateur)
   curl -v -H "If-Modified-Since: [date de la première réponse]" http://localhost:8087/static/index.html
   ```

4. Testez la configuration CORS:
   ```bash
   # Requête préliminaire OPTIONS
   curl -X OPTIONS -H "Origin: http://example.com" -H "Access-Control-Request-Method: GET" -v http://localhost:8087/api/products
   ```

#### Analyse des Résultats

- **Compression**: Comparez la taille des réponses avec et sans compression. La compression devrait réduire la taille de 60-80% pour les réponses JSON.
- **Traitement asynchrone**: Sous charge, l'application devrait maintenir de bonnes performances grâce au traitement parallèle.
- **Cache des ressources statiques**: Le deuxième appel devrait retourner un statut 304 (Not Modified).
- **CORS**: La réponse OPTIONS devrait inclure les en-têtes CORS appropriés et un max-age de 3600 secondes.

#### Mesures de Performance

- **Taille des réponses**: Mesurez la réduction de taille grâce à la compression.
- **Temps de réponse sous charge**: Comparez les temps de réponse avec et sans traitement asynchrone.
- **Requêtes réseau**: Utilisez les outils de développement du navigateur pour vérifier que les ressources statiques sont correctement mises en cache.

#### Conseils de Dépannage

- Si la compression ne fonctionne pas, vérifiez que le client envoie bien l'en-tête `Accept-Encoding: gzip`.
- Pour les problèmes de traitement asynchrone, vérifiez les logs pour des erreurs liées au pool de threads.
- Si les ressources statiques ne sont pas mises en cache, vérifiez la configuration dans WebConfig.java.

### 5. Test de l'Architecture Orientée Événements

#### Préparation

1. Assurez-vous que l'application est démarrée avec le traitement asynchrone activé.
2. Préparez des requêtes pour créer, mettre à jour et supprimer des produits.

#### Exécution des Tests

1. Testez la création de produit et le traitement d'événement:
   ```bash
   # Créez un nouveau produit
   curl -X POST -H "Content-Type: application/json" -d '{
     "name": "Test Product",
     "category": "Test",
     "price": 99.99,
     "description": "Test product for event architecture"
   }' http://localhost:8087/api/products
   ```

2. Observez les logs pour vérifier:
   - La création du produit
   - La publication de l'événement
   - Le traitement asynchrone de l'événement dans un thread séparé

3. Testez la mise à jour de produit:
   ```bash
   # Récupérez l'ID du produit créé précédemment
   curl http://localhost:8087/api/products

   # Mettez à jour le produit
   curl -X PUT -H "Content-Type: application/json" -d '{
     "name": "Updated Test Product",
     "category": "Test",
     "price": 129.99,
     "description": "Updated test product"
   }' http://localhost:8087/api/products/1
   ```

4. Testez la suppression de produit:
   ```bash
   # Supprimez le produit
   curl -X DELETE http://localhost:8087/api/products/1
   ```

5. Testez le comportement sous charge:
   - Créez un script pour envoyer plusieurs requêtes de création en parallèle
   - Observez comment les événements sont traités de manière asynchrone

#### Analyse des Résultats

- **Découplage**: Vérifiez que la réponse HTTP est retournée avant que le traitement asynchrone ne soit terminé.
- **Traitement parallèle**: Observez comment plusieurs événements sont traités en parallèle par différents threads.
- **Résilience**: Même si le traitement d'un événement échoue, cela ne devrait pas affecter la réponse HTTP.

#### Mesures de Performance

- **Temps de réponse**: Les opérations devraient retourner rapidement, car le traitement lourd est effectué de manière asynchrone.
- **Débit**: L'architecture orientée événements devrait permettre un débit plus élevé sous charge.
- **Utilisation des threads**: Observez l'utilisation des threads dans VisualVM pour voir comment le pool de threads gère les événements.

#### Conseils de Dépannage

- Si les événements ne sont pas traités de manière asynchrone, vérifiez que `@EnableAsync` est présent dans la configuration.
- Pour les problèmes de pool de threads, vérifiez la configuration dans AsyncConfig.java.
- Si les événements ne sont pas publiés, vérifiez l'injection de `ApplicationEventPublisher` dans le service.

### 6. Test des Métriques et de la Surveillance

#### Préparation

1. Assurez-vous que l'application est démarrée avec les endpoints Actuator exposés.
2. Installez Prometheus et Grafana (optionnel) pour une surveillance avancée.

#### Exécution des Tests

1. Explorez les endpoints Actuator de base:
   ```bash
   # Vérifiez l'état de santé de l'application
   curl http://localhost:8087/actuator/health

   # Listez tous les endpoints disponibles
   curl http://localhost:8087/actuator
   ```

2. Examinez les métriques JVM:
   ```bash
   # Listez toutes les métriques disponibles
   curl http://localhost:8087/actuator/metrics

   # Vérifiez l'utilisation de la mémoire
   curl http://localhost:8087/actuator/metrics/jvm.memory.used

   # Vérifiez les statistiques du garbage collector
   curl http://localhost:8087/actuator/metrics/jvm.gc.pause
   ```

3. Examinez les métriques HTTP:
   ```bash
   # Vérifiez les statistiques des requêtes HTTP
   curl http://localhost:8087/actuator/metrics/http.server.requests
   ```

4. Examinez les métriques de cache:
   ```bash
   # Vérifiez les statistiques de cache
   curl http://localhost:8087/actuator/metrics/cache.gets
   curl http://localhost:8087/actuator/metrics/cache.puts
   ```

5. Configurez Prometheus pour collecter ces métriques (optionnel):
   - Créez un fichier `prometheus.yml` avec la configuration appropriée
   - Démarrez Prometheus: `prometheus --config.file=prometheus.yml`
   - Accédez à l'interface Prometheus: http://localhost:9090

6. Configurez Grafana pour visualiser les métriques (optionnel):
   - Démarrez Grafana: `grafana-server`
   - Accédez à l'interface Grafana: http://localhost:3000
   - Ajoutez Prometheus comme source de données
   - Créez des tableaux de bord pour visualiser les métriques importantes

#### Analyse des Résultats

- **Santé de l'application**: Vérifiez que tous les composants sont UP.
- **Utilisation des ressources**: Surveillez l'utilisation de la mémoire, du CPU et des threads.
- **Performance HTTP**: Analysez les temps de réponse et le débit des requêtes HTTP.
- **Performance du cache**: Vérifiez les taux de succès et d'échec du cache.

#### Utilisation pour l'Optimisation

- Utilisez ces métriques pour identifier les goulots d'étranglement:
  - Si l'utilisation de la mémoire est élevée, optimisez la gestion de la mémoire ou augmentez la taille du tas.
  - Si les temps de réponse HTTP sont longs, identifiez les endpoints lents et optimisez-les.
  - Si le taux de succès du cache est faible, ajustez la stratégie de mise en cache.

#### Conseils de Dépannage

- Si les endpoints Actuator ne sont pas accessibles, vérifiez la configuration dans application.properties.
- Pour les problèmes de métriques manquantes, assurez-vous que les dépendances appropriées sont incluses dans le pom.xml.
- Si Prometheus ne collecte pas les métriques, vérifiez que l'endpoint /actuator/prometheus est accessible.

### 7. Test des Optimisations JVM

#### Préparation

1. Préparez différentes configurations JVM à tester:
   ```bash
   # Configuration de base
   java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar

   # Configuration avec GC G1 optimisé
   java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar

   # Configuration avec taille de tas fixe
   java -Xms512m -Xmx512m -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
   ```

2. Installez des outils de profilage JVM:
   - VisualVM avec plugins GC Visualizer et MBeans
   - Java Mission Control (JMC)

#### Exécution des Tests

1. Testez différentes configurations de garbage collector:
   - Démarrez l'application avec chaque configuration
   - Générez une charge avec JMeter ou un script similaire
   - Observez les pauses GC et l'utilisation de la mémoire

2. Analysez les logs GC:
   ```bash
   # Activez la journalisation GC détaillée
   java -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar

   # Analysez les logs avec GCViewer ou un outil similaire
   ```

3. Testez l'impact des options de compilation JIT:
   ```bash
   # Avec compilation agressive
   java -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
   ```

4. Testez l'impact de la déduplication des chaînes:
   ```bash
   # Avec déduplication des chaînes
   java -XX:+UseG1GC -XX:+UseStringDeduplication -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
   ```

#### Analyse des Résultats

- **Pauses GC**: Comparez la durée et la fréquence des pauses GC entre les différentes configurations.
- **Utilisation de la mémoire**: Observez comment la mémoire est utilisée et libérée au fil du temps.
- **Temps de démarrage**: Mesurez l'impact des différentes options sur le temps de démarrage.
- **Performance sous charge**: Comparez le débit et les temps de réponse sous charge.

#### Mesures de Performance

- **Durée des pauses GC**: Les pauses devraient être inférieures à 200ms avec G1GC bien configuré.
- **Fréquence des GC**: Moins de collections complètes est généralement mieux.
- **Utilisation de la mémoire**: Une utilisation stable sans croissance continue indique l'absence de fuites de mémoire.

#### Conseils de Dépannage

- Si les pauses GC sont trop longues, ajustez MaxGCPauseMillis ou augmentez la taille du tas.
- Pour les problèmes de mémoire, utilisez VisualVM pour faire un heap dump et analyser l'utilisation de la mémoire.
- Si l'application est lente au démarrage, essayez d'utiliser TieredCompilation pour accélérer la compilation JIT.

### Conseils Généraux pour les Tests de Performance

1. **Établissez une référence**: Avant d'optimiser, mesurez les performances actuelles pour avoir une base de comparaison.

2. **Isolez les variables**: Testez une optimisation à la fois pour comprendre son impact spécifique.

3. **Testez sous charge réaliste**: Utilisez des outils comme JMeter, Gatling ou wrk pour simuler une charge réaliste.

4. **Surveillez toutes les métriques**: Ne vous concentrez pas uniquement sur le temps de réponse; surveillez également l'utilisation des ressources, le débit, etc.

5. **Documentez vos résultats**: Gardez une trace des tests effectués et des améliorations observées.

6. **Itérez**: L'optimisation des performances est un processus itératif. Mesurez, optimisez, puis mesurez à nouveau.

7. **Attention aux compromis**: Certaines optimisations peuvent améliorer un aspect des performances au détriment d'un autre (par exemple, la mise en cache peut améliorer les temps de réponse mais augmenter l'utilisation de la mémoire).

### Outils Recommandés pour les Tests de Performance

1. **JMeter**: Pour les tests de charge et de performance
2. **VisualVM**: Pour le profilage JVM et l'analyse de la mémoire
3. **Prometheus + Grafana**: Pour la surveillance et la visualisation des métriques
4. **wrk ou ab**: Pour les tests de charge HTTP simples
5. **Spring Boot Actuator**: Pour les métriques d'application
6. **GCViewer**: Pour l'analyse des logs de garbage collection
7. **Chrome DevTools**: Pour l'analyse des performances frontend

En suivant ce guide détaillé, vous serez en mesure de tester efficacement toutes les stratégies d'optimisation des performances implémentées dans ce projet, de mesurer leur impact et d'identifier les domaines où des améliorations supplémentaires pourraient être apportées.

## Prérequis et Exécution

### Prérequis

- Java 21 ou supérieur
- Maven 3.6 ou supérieur

### Construction et Exécution

```bash
# Construire l'application
mvn clean package

# Exécuter l'application
java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
```

### Configuration

L'application peut être configurée via `application.properties` ou des variables d'environnement. Voir la section [Optimisations de Démarrage et Configuration](#1-optimisations-de-démarrage-et-configuration) pour plus de détails.

## Conclusion

Ce projet démontre un ensemble complet de stratégies d'optimisation des performances pour les applications Spring Boot. En appliquant ces techniques, vous pouvez améliorer significativement les performances de votre application, du temps de démarrage à l'efficacité d'exécution, l'accès à la base de données, les performances web, et plus encore.
