CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_role VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE administrators (
    id BIGINT PRIMARY KEY,
    CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES users(id)
);

CREATE TABLE annotators (
    id BIGINT PRIMARY KEY,
    CONSTRAINT fk_annotator_user FOREIGN KEY (id) REFERENCES users(id)
);

CREATE TABLE dataset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    langue VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE possible_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    dataset_id BIGINT NOT NULL,
    CONSTRAINT fk_class_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE CASCADE
);

CREATE TABLE text_pair (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text1 TEXT NOT NULL,
    text2 TEXT,
    metadata JSON,
    dataset_id BIGINT NOT NULL,
    CONSTRAINT fk_textpair_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE CASCADE
);

CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_limit TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    dataset_id BIGINT NOT NULL,
    annotator_id BIGINT NOT NULL,
    CONSTRAINT fk_task_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(id),
    CONSTRAINT fk_task_annotator FOREIGN KEY (annotator_id) REFERENCES annotators(id)
);

CREATE TABLE annotation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chosen_class VARCHAR(100) NOT NULL,
    annotation_time TIMESTAMP NOT NULL,
    source VARCHAR(50) NOT NULL,
    task_id BIGINT NOT NULL,
    text_pair_id BIGINT NOT NULL,
    annotator_id BIGINT NOT NULL,
    CONSTRAINT fk_annotation_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_annotation_textpair FOREIGN KEY (text_pair_id) REFERENCES text_pair(id),
    CONSTRAINT fk_annotation_annotator FOREIGN KEY (annotator_id) REFERENCES annotators(id)
);

CREATE TABLE nlp_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_seconds BIGINT,
    script_path VARCHAR(255),
    params JSON,
    logs TEXT,
    accuracy DOUBLE,
    f1_score DOUBLE,
    confusion_matrix JSON,
    dataset_id BIGINT NOT NULL,
    CONSTRAINT fk_nlprun_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(id)
);
