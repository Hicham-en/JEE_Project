# Guide de lancement — Plateforme d'annotation NLP
# Tout tourne dans Docker, aucune commande MariaDB locale requise.

## Prérequis

Vérifie que ces outils sont installés :

    java -version        → Java 21+
    mvn -version         → Maven 3.8+
    docker --version     → Docker 29+
    docker compose version → Docker Compose v2+

## Étape 1 — Préparer le fichier .env

Copie le fichier exemple et renseigne tes valeurs :

    cp .env.example .env

Contenu final de .env :

    MARIADB_DATABASE=annotation_db
    MARIADB_ROOT_PASSWORD=password
    DB_USER=annotation
    DB_PASSWORD=password
    NLP_PYTHON_EXECUTABLE=python
    NLP_SCRIPTS_DIR=.

Explication de chaque variable :
- MARIADB_DATABASE      : nom de la base créée automatiquement par le conteneur MariaDB
- MARIADB_ROOT_PASSWORD : mot de passe root MariaDB (usage interne Docker uniquement)
- DB_USER               : utilisateur applicatif créé automatiquement par MariaDB
- DB_PASSWORD           : mot de passe de cet utilisateur applicatif
- NLP_PYTHON_EXECUTABLE : commande Python pour les scripts NLP (python ou python3)
- NLP_SCRIPTS_DIR       : dossier des scripts NLP relatif au conteneur app

## Étape 2 — Nettoyer le build précédent

Si tu as déjà lancé un build Docker, nettoie tout pour repartir de zéro :

    docker compose down -v
    docker system prune -f
    docker volume prune -f

La commande `down -v` supprime aussi le volume `mariadb_data` pour que
Flyway reparte d'une base vide propre avec les nouvelles migrations.

## Étape 3 — Builder le jar Maven

Le Dockerfile copie le jar depuis target/, il faut le construire avant :

    mvn clean package -DskipTests

Tu dois voir : BUILD SUCCESS et un fichier target/*.jar créé.

## Étape 4 — Lancer l'application

    docker compose up --build

Ce qui se passe dans l'ordre :
1. Docker construit l'image du conteneur `app` depuis le Dockerfile
2. Le conteneur `mariadb` démarre et initialise automatiquement :
   - la base `annotation_db`
   - l'utilisateur `annotation` avec son mot de passe
   - aucune commande SQL manuelle n'est nécessaire
3. Le healthcheck vérifie que MariaDB est prêt (jusqu'à 10 tentatives)
4. Le conteneur `app` démarre seulement quand MariaDB est healthy
5. Flyway applique automatiquement les 5 migrations (V1 à V3 + V4/V5)
6. L'application est accessible sur http://localhost:8080

Pour lancer en arrière-plan :

    docker compose up --build -d

Pour voir les logs en temps réel après un lancement en arrière-plan :

    docker compose logs -f app
    docker compose logs -f mariadb

## Étape 5 — Vérifier que tout est up

    docker compose ps

Tu dois voir deux conteneurs avec le statut `running` :

    NAME                        STATUS
    boudaa_projectt-mariadb-1   running (healthy)
    boudaa_projectt-app-1       running

Si `app` est en `restarting`, consulte les logs :

    docker compose logs app

## Étape 6 — Premier login

Ouvre : http://localhost:8080/login

    Login    : admin
    Password : Admin@1234

Tu seras redirigé automatiquement vers /admin/datasets.
Change ce mot de passe avant toute démonstration publique.

## Étape 7 — Scénario de test complet

### 7.1 Créer 3 annotateurs
- Menu Annotateurs → + Ajouter Annotateur
- Crée 3 comptes (ex: ali.karim, sara.idrissi, nour.benali)
- Note chaque mot de passe affiché (affiché une seule fois)

### 7.2 Créer un dataset
- Menu Datasets → + Créer Dataset
- Nom         : demo-similarite
- Langue      : fr
- Classes     : SIMILAIRE;DIFFERENT
- Fichier     : importe demo_dataset.csv (contenu ci-dessous)

### 7.3 Affecter les annotateurs
- Dans le détail du dataset → Ajouter Annotateurs
- Coche les 3 annotateurs créés → Valider
- La distribution automatique crée une tâche par annotateur

### 7.4 Annoter en tant qu'annotateur
- Déconnecte-toi (bouton logout)
- Connecte-toi avec ali.karim et son mot de passe
- Menu Mes Tâches → Travailler sur la tâche demo-similarite
- Annote au moins 3 paires de textes
- Répète pour sara.idrissi et nour.benali

### 7.5 Vérifier les métriques (en admin)
- Reconnecte-toi en admin
- Menu Métriques → sélectionne demo-similarite
- Vérifie la valeur Fleiss Kappa et le tableau Cohen par paire
- Clique sur Spammeurs pour voir les scores de suspicion

### 7.6 Exporter les résultats
- Menu Datasets → Détails de demo-similarite
- Clique sur Télécharger CSV ou Télécharger JSON
- Vérifie que le fichier téléchargé contient bien les annotations

### 7.7 Tester le NLP (optionnel)
- Menu NLP → sélectionne demo-similarite → Lancer Training
- Si Python et train.py ne sont pas disponibles, le run passera en FAILED
- Vérifie que le polling de logs fonctionne et que le statut s'affiche

## Fichier CSV d'exemple — demo_dataset.csv

Crée ce fichier localement avant l'import :

    text1,text2
    "Le service client répond rapidement.","Le support répond vite aux demandes."
    "Ce produit est facile à installer.","L'installation de ce produit est simple."
    "La livraison est arrivée en retard.","Le colis a été livré avant la date prévue."
    "Le modèle donne de bons résultats.","Les performances du modèle sont satisfaisantes."
    "L'interface est confuse pour les débutants.","La page d'accueil est très intuitive."
    "Le texte contient une opinion positive.","La phrase exprime un avis favorable."
    "La batterie dure toute la journée.","L'autonomie couvre une journée complète."
    "Le prix est trop élevé.","Le tarif est abordable."
    "La documentation manque d'exemples.","Le guide contient peu de cas pratiques."
    "Le système détecte les erreurs.","L'application signale les anomalies."

## Arrêter l'application

Arrêt simple (conserve les données) :

    docker compose down

Arrêt avec suppression complète des données (repart de zéro) :

    docker compose down -v

## Résolution des problèmes courants

### Port 8080 déjà utilisé
    lsof -i :8080
Arrête le processus concerné, ou change le port dans docker-compose.yml :
    ports:
      - "8081:8080"
Puis ouvre http://localhost:8081

### Le conteneur app redémarre en boucle
    docker compose logs app
Cause probable : DB_URL mal formée ou MariaDB pas encore healthy.
Vérifie que .env est bien renseigné et relance :
    docker compose down -v && docker compose up --build

### Flyway checksum error
Une migration déjà appliquée a été modifiée. Solution en dev :
    docker compose down -v
    docker compose up --build
Cela repart d'une base vide et réapplique toutes les migrations proprement.

### Login admin refusé
La migration V3__seed_admin.sql n'a pas été appliquée.
Vérifie dans les logs Flyway :
    docker compose logs app | grep -i flyway
Si V3 est manquant, vérifie que le fichier existe dans src/main/resources/db/migration/

### MariaDB healthcheck timeout
Le conteneur app démarre trop vite. Docker Compose gère ça automatiquement
via le `depends_on: condition: service_healthy` — attends simplement que
le healthcheck passe (jusqu'à 100 secondes au total, 10 tentatives x 10s).

### Scripts NLP introuvables
Vérifie NLP_PYTHON_EXECUTABLE et NLP_SCRIPTS_DIR dans .env.
Le run passera en statut FAILED avec le message d'erreur dans les logs de la vue NLP.