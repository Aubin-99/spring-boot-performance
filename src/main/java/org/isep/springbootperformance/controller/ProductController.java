package org.isep.springbootperformance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.isep.springbootperformance.model.Product;
import org.isep.springbootperformance.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Contrôleur REST pour les opérations sur les produits avec des optimisations de performance web.
 * Démontre la mise en cache HTTP, la compression et le traitement asynchrone.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Récupère tous les produits avec mise en cache HTTP.
     * Utilise les en-têtes ETag et Cache-Control pour une mise en cache efficace.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Product>> getProductById(@PathVariable Long id) {
        log.info("REST request to get Product by id: {}", id);
        Optional<Product> product = productService.getProductById(id);

        // Set cache control headers for browser caching (30 minutes)
        CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES)
                .noTransform()
                .mustRevalidate();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(product);
    }

    /**
     * Crée un nouveau produit.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("REST request to save Product: {}", product);
        Product result = productService.saveProduct(product);
        return ResponseEntity.ok(result);
    }

    /**
     * Met à jour un produit existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("REST request to update Product: {}", product);
        product.setId(id);
        Product result = productService.updateProduct(product);
        return ResponseEntity.ok(result);
    }

    /**
     * Supprime un produit.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete Product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les produits par catégorie.
     * Utilise la mise en cache HTTP pour les données fréquemment consultées et relativement statiques.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("REST request to get Products by category: {}", category);
        List<Product> products = productService.getProductsByCategory(category);

        // Set cache control headers (10 minutes)
        CacheControl cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES)
                .noTransform();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(products);
    }

    /**
     * Récupère les produits en dessous d'un certain prix avec pagination.
     * La pagination améliore les performances en limitant le transfert de données.
     */
    @GetMapping("/price")
    public ResponseEntity<Page<Product>> getProductsBelowPrice(
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get Products below price: {}", maxPrice);
        Page<Product> products = productService.getProductsBelowPrice(maxPrice, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupère les résumés de produits par catégorie.
     * Utilise la projection pour ne renvoyer que les données nécessaires.
     */
    @GetMapping("/summary/{category}")
    public ResponseEntity<List<Object[]>> getProductSummaryByCategory(@PathVariable String category) {
        log.info("REST request to get Product summaries by category: {}", category);
        List<Object[]> summaries = productService.getProductSummaryByCategory(category);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Recherche des produits de manière asynchrone.
     * Démontre le traitement asynchrone pour une meilleure évolutivité.
     */
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<Product>>> searchProducts(@RequestParam String keyword) {
        log.info("REST request to search Products with keyword: {}", keyword);
        // Effectue la recherche de manière asynchrone
        return CompletableFuture.supplyAsync(() -> {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(products);
        });
    }

    /**
     * Récupère tous les produits avec pagination.
     * La pagination améliore les performances en limitant le transfert de données.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get all Products (page: {}, size: {})", page, size);
        Page<Product> productPage = productService.getAllProducts(page, size);

        // Définit les en-têtes de contrôle de cache (5 minutes)
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
                .noTransform();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(productPage.getContent());
    }

    /**
     * Vide le cache des produits (opération d'administration).
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Void> clearProductCache() {
        log.info("REST request to clear product cache");
        productService.clearProductCache();
        return ResponseEntity.ok().build();
    }
}
