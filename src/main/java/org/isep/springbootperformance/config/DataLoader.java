package org.isep.springbootperformance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.isep.springbootperformance.model.Product;
import org.isep.springbootperformance.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Random;

/**
 * Chargeur de données pour initialiser la base de données avec des données d'exemple.
 * Ceci est utile pour tester et démontrer les optimisations de performance.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataLoader {

    private final ProductRepository productRepository;
    private final Random random = new Random();

    /**
     * Initialise la base de données avec des données d'exemple.
     * Ce bean n'est actif que dans le profil "default" (pas dans "test" ou "prod").
     */
    @Bean
    @Profile("!test & !prod")
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Initialisation de la base de données avec des données d'exemple...");

            if (productRepository.count() > 0) {
                log.info("La base de données contient déjà des données, initialisation ignorée");
                return;
            }

            // Crée des catégories d'exemple
            String[] categories = {"Electronics", "Books", "Clothing", "Home", "Sports"};

            // Crée des produits d'exemple
            List<Product> products = List.of(
                createProduct("Smartphone", categories[0], 799.99, "High-end smartphone with advanced features"),
                createProduct("Laptop", categories[0], 1299.99, "Powerful laptop for professional use"),
                createProduct("Headphones", categories[0], 199.99, "Noise-cancelling wireless headphones"),
                createProduct("Smart Watch", categories[0], 299.99, "Fitness tracker and smartwatch"),
                createProduct("Tablet", categories[0], 499.99, "Lightweight tablet for entertainment"),

                createProduct("Java Programming", categories[1], 49.99, "Comprehensive guide to Java programming"),
                createProduct("Spring Boot in Action", categories[1], 39.99, "Learn Spring Boot development"),
                createProduct("Clean Code", categories[1], 44.99, "Guide to writing clean and maintainable code"),
                createProduct("Design Patterns", categories[1], 54.99, "Essential design patterns for software development"),
                createProduct("Algorithms", categories[1], 59.99, "Introduction to algorithms and data structures"),

                createProduct("T-Shirt", categories[2], 19.99, "Comfortable cotton t-shirt"),
                createProduct("Jeans", categories[2], 49.99, "Classic blue jeans"),
                createProduct("Sweater", categories[2], 39.99, "Warm winter sweater"),
                createProduct("Jacket", categories[2], 89.99, "Waterproof outdoor jacket"),
                createProduct("Socks", categories[2], 9.99, "Pack of 5 pairs of socks"),

                createProduct("Sofa", categories[3], 699.99, "Comfortable 3-seater sofa"),
                createProduct("Dining Table", categories[3], 399.99, "Wooden dining table for 6 people"),
                createProduct("Bed Frame", categories[3], 299.99, "Queen size bed frame"),
                createProduct("Coffee Table", categories[3], 149.99, "Modern coffee table"),
                createProduct("Bookshelf", categories[3], 199.99, "5-tier bookshelf"),

                createProduct("Tennis Racket", categories[4], 129.99, "Professional tennis racket"),
                createProduct("Basketball", categories[4], 29.99, "Official size basketball"),
                createProduct("Yoga Mat", categories[4], 24.99, "Non-slip yoga mat"),
                createProduct("Dumbbells", categories[4], 79.99, "Set of adjustable dumbbells"),
                createProduct("Running Shoes", categories[4], 99.99, "Lightweight running shoes")
            );

            // Enregistre tous les produits
            productRepository.saveAll(products);

            log.info("Base de données initialisée avec {} produits", products.size());
        };
    }

    /**
     * Méthode auxiliaire pour créer un produit.
     */
    private Product createProduct(String name, String category, double price, String description) {
        return Product.builder()
                .name(name)
                .category(category)
                .price(price)
                .description(description)
                .build();
    }
}
