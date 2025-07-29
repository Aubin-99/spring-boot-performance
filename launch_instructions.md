# Instructions pour lancer l'application Spring Boot Performance

Voici les différentes méthodes pour lancer l'application Spring Boot Performance. Choisissez celle qui convient le mieux à votre environnement.

## Méthode la plus simple: Utiliser le script de lancement automatique

Un script batch Windows a été créé pour faciliter le lancement de l'application:

1. Double-cliquez sur le fichier `run_app.bat` dans le répertoire du projet
2. Le script vérifiera automatiquement la version de Java et lancera l'application avec la méthode la plus appropriée

Si vous préférez lancer l'application manuellement, suivez les instructions ci-dessous.

## Prérequis

- Java 21 ou supérieur (OBLIGATOIRE - l'application ne fonctionnera pas avec une version inférieure)
- Maven 3.6 ou supérieur (pour certaines méthodes)

### Vérification de la version Java

Avant de lancer l'application, vérifiez que vous utilisez Java 21 ou supérieur:

```bash
java -version
```

Si la version affichée est inférieure à 21, vous devez installer Java 21 et vous assurer qu'il est utilisé par défaut.

## Méthode 1: Exécuter l'application avec Java (si les classes sont déjà compilées)

Si les classes sont déjà compilées dans le répertoire target/classes:

```bash
# Sous Windows
java -cp target\classes org.isep.springbootperformance.SpringBootPerformanceApplication
```

## Méthode 2: Utiliser Maven directement

Si Maven est déjà installé sur votre système:

```bash
mvn spring-boot:run
```

## Méthode 3: Construire le JAR et l'exécuter

```bash
# Construire l'application avec le wrapper Maven
.\mvnw.cmd clean package

# Exécuter le JAR généré
java -jar target\spring-boot-performance-0.0.1-SNAPSHOT.jar
```

## Accéder à l'application

Une fois l'application démarrée, vous pouvez y accéder via:

- Interface utilisateur: http://localhost:8087
- Console H2 (base de données): http://localhost:8087/h2-console
  - JDBC URL: jdbc:h2:mem:performancedb
  - Utilisateur: sa
  - Mot de passe: (laissez vide)
- Endpoints Actuator: http://localhost:8087/actuator

## Tester les fonctionnalités

Vous pouvez tester les différentes optimisations de performance en utilisant les endpoints REST:

```bash
# Récupérer tous les produits
curl http://localhost:8087/api/products

# Récupérer un produit par ID
curl http://localhost:8087/api/products/1

# Récupérer des produits par catégorie (avec pagination)
curl http://localhost:8087/api/products/category/Electronics?page=0&size=10

# Récupérer un résumé des produits par catégorie (avec projection)
curl http://localhost:8087/api/products/summary/Electronics
```

## Arrêter l'application

Pour arrêter l'application, appuyez sur `Ctrl+C` dans le terminal où l'application est en cours d'exécution.
