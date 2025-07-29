# Spring Boot Performance Optimization Strategies

This project demonstrates various performance optimization techniques for Spring Boot applications. It provides a comprehensive set of strategies to improve application performance, from startup time to database access, caching, web performance, and more.

## Table of Contents

- [Startup and Configuration Optimizations](#startup-and-configuration-optimizations)
- [Database Optimizations](#database-optimizations)
- [Cache Optimizations](#cache-optimizations)
- [Web Performance Optimizations](#web-performance-optimizations)
- [JVM and Monitoring Optimizations](#jvm-and-monitoring-optimizations)
- [Security and Network Optimizations](#security-and-network-optimizations)
- [Architectural Optimizations](#architectural-optimizations)
- [Running the Application](#running-the-application)

## Startup and Configuration Optimizations

### Property Configuration

Efficient property configuration can significantly improve application startup time and runtime performance:

```properties
# Enable lazy initialization to improve startup time
spring.main.lazy-initialization=true

# Exclude unnecessary auto-configurations
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

- **Lazy Initialization**: Components are initialized only when needed, reducing startup time.
- **Auto-configuration Exclusion**: Unnecessary auto-configurations are excluded to reduce startup time and memory usage.

### Dependency Management

- **Minimal Dependencies**: Include only necessary dependencies to reduce the application's footprint.
- **Dependency Exclusion**: Exclude transitive dependencies that are not needed.

## Database Optimizations

### Connection Pooling

Efficient connection pooling is crucial for database performance:

```properties
# Connection Pool Configuration (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.connection-timeout=20000
```

- **Pool Size**: Properly sized connection pool based on application needs.
- **Timeout Configuration**: Optimized timeout settings to prevent resource leaks.

### Query and Mapping Optimizations

```java
@Query("SELECT p FROM Product p WHERE p.price <= :maxPrice")
Page<Product> findProductsBelowPrice(@Param("maxPrice") Double maxPrice, Pageable pageable);

@Query("SELECT p.id, p.name, p.price FROM Product p WHERE p.category = :category")
List<Object[]> findProductSummaryByCategory(@Param("category") String category);
```

- **Custom Queries**: Optimized JPQL queries for specific use cases.
- **Pagination**: Implemented to limit the amount of data transferred.
- **Projection**: Used to fetch only necessary fields, reducing data transfer.

### Indexing

```java
@Table(indexes = {
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category")
})
public class Product {
    // ...
}
```

- **Database Indexes**: Added on frequently queried fields to improve query performance.
- **Composite Indexes**: Used for queries with multiple conditions.

### Batch Processing

```properties
# Batch processing for better performance
spring.jpa.properties.hibernate.jdbc.batch_size=30
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

- **Batch Size**: Configured for optimal batch processing.
- **Ordered Operations**: Enabled to improve batch efficiency.

## Cache Optimizations

### Application Cache

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("productCache");
    }
}

@Service
public class ProductService {
    @Cacheable(value = "productCache", key = "#id")
    public Optional<Product> getProductById(Long id) {
        // ...
    }
}
```

- **Cache Configuration**: Centralized cache configuration.
- **@Cacheable**: Used to cache method results.
- **@CachePut**: Used to update cache entries.
- **@CacheEvict**: Used to remove cache entries.

### HTTP Cache

```java
@GetMapping("/{id}")
public ResponseEntity<Optional<Product>> getProductById(@PathVariable Long id) {
    Optional<Product> product = productService.getProductById(id);
    
    CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES)
            .noTransform()
            .mustRevalidate();
    
    return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .body(product);
}
```

- **Cache-Control Headers**: Set for browser and proxy caching.
- **ETag Support**: Implemented for conditional requests.
- **Resource Versioning**: Used for static resources.

## Web Performance Optimizations

### Compression

```properties
# Enable response compression
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
server.compression.min-response-size=1024
```

- **Response Compression**: Enabled to reduce data transfer size.
- **MIME Type Configuration**: Configured for specific content types.
- **Minimum Size**: Set to avoid compressing small responses.

### Static Resources

```properties
# Static resources caching
spring.web.resources.cache.cachecontrol.max-age=365d
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
```

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                    .cachePublic()
                    .immutable());
}
```

- **Resource Caching**: Configured for long-term caching.
- **Resource Chain**: Enabled for content-based versioning.
- **Cache-Control Headers**: Set for aggressive caching.

### Async Processing

```java
@GetMapping("/search")
public CompletableFuture<ResponseEntity<List<Product>>> searchProducts(@RequestParam String keyword) {
    return CompletableFuture.supplyAsync(() -> {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    });
}
```

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

- **Async Controllers**: Implemented for non-blocking request processing.
- **Thread Pool Configuration**: Optimized for the application's needs.
- **CompletableFuture**: Used for asynchronous operations.

## JVM and Monitoring Optimizations

### Garbage Collection

```
# JVM arguments for optimal garbage collection
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+ParallelRefProcEnabled
-XX:+DisableExplicitGC
-XX:+AlwaysPreTouch
-XX:+UseStringDeduplication
```

- **G1 Garbage Collector**: Configured for low pause times.
- **Memory Settings**: Optimized for the application's needs.
- **GC Logging**: Enabled for monitoring and tuning.

### Profiling

- **JVM Profiling**: Tools and techniques for identifying performance bottlenecks.
- **Memory Analysis**: Strategies for detecting memory leaks and optimizing memory usage.

### Metrics

```properties
# Enable all actuator endpoints for monitoring
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Enable metrics
management.prometheus.metrics.export.enabled=true
```

- **Spring Boot Actuator**: Configured for comprehensive monitoring.
- **Prometheus Integration**: Enabled for metrics collection.
- **Custom Metrics**: Added for application-specific monitoring.

## Security and Network Optimizations

### Security

- **Minimal Security Configuration**: Configured only necessary security features.
- **Efficient Authentication**: Implemented token-based authentication for better performance.

### API and Serialization

```java
@Bean
public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // Disable features that can impact performance
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    return mapper;
}
```

- **JSON Serialization**: Optimized for performance.
- **Content Negotiation**: Configured to prefer JSON.
- **CORS Configuration**: Optimized to reduce preflight requests.

## Architectural Optimizations

### Event-Driven Architecture

```java
@Service
public class ProductService {
    private final ApplicationEventPublisher eventPublisher;
    
    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductEvent(this, ProductEvent.EventType.CREATED, savedProduct));
        return savedProduct;
    }
}

@Component
public class ProductEventListener {
    @Async
    @EventListener(condition = "#event.eventType == T(org.isep.springbootperformance.event.ProductEvent.EventType).CREATED")
    public void handleProductCreatedEvent(ProductEvent event) {
        // Asynchronous processing
    }
}
```

- **Event Publishing**: Used to decouple components and enable asynchronous processing.
- **Event Listeners**: Implemented for handling events asynchronously.
- **Async Processing**: Applied to offload time-consuming operations.

### Microservices Patterns

- **API Gateway**: Implemented for efficient routing and load balancing.
- **Circuit Breaker**: Used to prevent cascading failures.
- **Service Discovery**: Implemented for dynamic service resolution.

## Running the Application

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

### Build and Run

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/spring-boot-performance-0.0.1-SNAPSHOT.jar
```

### Configuration

The application can be configured through `application.properties` or environment variables. See the [Startup and Configuration Optimizations](#startup-and-configuration-optimizations) section for details.

## Conclusion

This project demonstrates a comprehensive set of performance optimization strategies for Spring Boot applications. By applying these techniques, you can significantly improve your application's performance, from startup time to runtime efficiency, database access, web performance, and more.