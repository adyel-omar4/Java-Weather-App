# Java-Weather-App
A console-based weather application built with Java and Open-Meteo API.
# Java Weather App

**A console-based weather application developed in Java using the Open-Meteo API.**

## English

### 1. Project Overview

Java Weather App is a simple console-based application that allows users to retrieve real-time weather information and hourly forecasts for different cities around the world.

The application uses the Open-Meteo API to retrieve weather data and displays it directly in the terminal.

### 2. Features

- **City Search:** Search for weather information by city name.
- **Current Weather:** Display current temperature, feels-like temperature, humidity, wind speed, and weather conditions.
- **Hourly Forecast:** Display an hourly weather forecast for the next 12 hours.
- **Weather Details:** View precipitation, rain, showers, snowfall, and UV index.
- **Day/Night Detection:** Identify whether it is currently daytime or nighttime.
- **Temperature Chart:** Display a simple ASCII temperature chart in the console.
- **Continuous Search:** Search for multiple cities without restarting the application.
- **Error Handling:** Handle invalid city names and unsuccessful requests.

### 3. Technologies Used

- **Programming Language:** Java
- **JDK:** Java 17
- **API:** Open-Meteo
- **HTTP Requests:** Java HTTP Client
- **Data Processing:** JSON parsing
- **Development Environment:** IntelliJ IDEA / Visual Studio Code

### 4. API Used

This project uses two Open-Meteo endpoints:

- **Geocoding API:** Converts city names into geographical coordinates.
- **Weather Forecast API:** Retrieves current weather conditions and hourly forecasts.

Official documentation: https://open-meteo.com/

### 5. Installation and Execution

**Prerequisites:**
- Java Development Kit (JDK) 17 or later.
- An internet connection.

**Clone the repository:**

```bash
git clone https://github.com/adyel-omar4/Java-Weather-App.git
```

**Navigate to the project directory:**

```bash
cd Java-Weather-App
```

**Compile the application:**

```bash
javac Main.java
```

**Run the application:**

```bash
java Main
```

### 6. Usage

1. Launch the application.
2. Enter the name of a city.
3. View the current weather information.
4. Explore the hourly forecast and temperature chart.
5. Search for another city or type `q`, `quit`, or `esc` to exit.

### 7. Project Structure

```text
Java-Weather-App/
│
├── Main.java
├── README.md
└── .gitignore
```

### 8. Future Improvements

Potential improvements include:

- Implementing a graphical user interface (GUI).
- Improving JSON parsing using a dedicated library.
- Adding weather forecasts for multiple days.
- Supporting additional weather information.
- Improving the overall user experience.

---

## Version française

### 1. Présentation du projet

Java Weather App est une application météorologique fonctionnant en ligne de commande, développée en Java. Elle permet de consulter les conditions météorologiques actuelles ainsi que les prévisions horaires de différentes villes dans le monde.

L'application utilise l'API Open-Meteo pour récupérer les données météorologiques et les afficher directement dans le terminal.

### 2. Fonctionnalités

- **Recherche de villes :** Rechercher les informations météorologiques à partir du nom d'une ville.
- **Météo actuelle :** Afficher la température, la température ressentie, l'humidité, la vitesse du vent et les conditions météorologiques.
- **Prévisions horaires :** Consulter les prévisions météorologiques des 12 prochaines heures.
- **Données météorologiques détaillées :** Afficher les précipitations, la pluie, les averses, les chutes de neige et l'indice UV.
- **Détection jour/nuit :** Identifier si les conditions correspondent au jour ou à la nuit.
- **Graphique des températures :** Afficher un graphique ASCII représentant l'évolution des températures dans la console.
- **Recherche continue :** Effectuer plusieurs recherches de villes sans redémarrer l'application.
- **Gestion des erreurs :** Gérer les noms de villes incorrects et les requêtes qui échouent.

### 3. Technologies utilisées

- **Langage de programmation :** Java
- **JDK :** Java 17
- **API :** Open-Meteo
- **Requêtes HTTP :** Java HTTP Client
- **Traitement des données :** Analyse JSON
- **Environnement de développement :** IntelliJ IDEA / Visual Studio Code

### 4. API utilisée

Le projet repose sur deux services Open-Meteo :

- **Geocoding API :** Convertit les noms des villes en coordonnées géographiques.
- **Weather Forecast API :** Récupère les conditions météorologiques actuelles et les prévisions horaires.

Documentation officielle : https://open-meteo.com/

### 5. Installation et exécution

**Prérequis :**
- Java Development Kit (JDK) 17 ou version ultérieure.
- Une connexion Internet.

**Cloner le dépôt :**

```bash
git clone https://github.com/adyel-omar4/Java-Weather-App.git
```

**Accéder au répertoire du projet :**

```bash
cd Java-Weather-App
```

**Compiler l'application :**

```bash
javac Main.java
```

**Exécuter l'application :**

```bash
java Main
```

### 6. Utilisation

1. Lancer l'application.
2. Saisir le nom d'une ville.
3. Consulter les informations météorologiques actuelles.
4. Examiner les prévisions horaires et le graphique des températures.
5. Rechercher une autre ville ou saisir `q`, `quit` ou `esc` pour quitter.

### 7. Structure du projet

```text
Java-Weather-App/
│
├── Main.java
├── README.md
└── .gitignore
```

### 8. Améliorations futures

Quelques évolutions possibles :

- Développer une interface graphique (GUI).
- Améliorer le traitement JSON à l'aide d'une bibliothèque spécialisée.
- Ajouter des prévisions météorologiques sur plusieurs jours.
- Intégrer davantage de données météorologiques.
- Améliorer l'expérience utilisateur.

---

**Author:** [adyel-omar4](https://github.com/adyel-omar4)

**Project:** Java Weather App

**License:** Not specified
