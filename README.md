# AnnotateNLP

Plateforme Spring Boot 3 pour l'annotation collaborative de datasets NLP, avec interface administrateur, interface annotateur, métriques qualité, export et lancement de scripts Python.

## Prérequis

- Java 21
- Maven 3.9+
- MariaDB 10.11 ou Docker
- Python disponible dans le `PATH` pour les scripts NLP

## Installation locale

```bash
cp .env.example .env
mvn spring-boot:run
```

Par défaut, le profil `dev` lit MariaDB sur `jdbc:mariadb://localhost:3306/annotation_db`.

## Docker

```bash
cp .env.example .env
mvn clean package
docker compose up --build
```

L'application écoute sur `http://localhost:8080`.

## Connexion initiale

Compte administrateur seedé par Flyway :

- Login : `admin`
- Mot de passe : `Admin1234`

Changez ce mot de passe immédiatement après la première connexion.

## Format datasets

CSV attendu :

```csv
id,text1,text2
1,"Premier texte","Second texte optionnel"
2,"Texte simple",
```

JSON attendu :

```json
[
  {"id": 1, "text1": "Premier texte", "text2": "Second texte optionnel"},
  {"id": 2, "text1": "Texte simple"}
]
```

## Scripts NLP

Depuis l'écran admin `NLP Training`, indiquez le chemin du script Python et les hyperparamètres. Le backend exécute :

```bash
python train.py --dataset <path> --lr 0.001 --epochs 10 --batch-size 32
```

Les logs sont capturés en temps réel et le polling renvoie `{logs,status,metrics}`. Les métriques reconnues dans stdout sont `accuracy: 0.91`, `f1=0.87` et `confusion_matrix: ...`.

## Structure

- `domain`: entités JPA et repositories
- `application`: DTOs, ports et services applicatifs
- `infrastructure`: sécurité, parsing, NLP et configuration technique
- `interfaces`: contrôleurs web Thymeleaf

## Tests

```bash
mvn test
```