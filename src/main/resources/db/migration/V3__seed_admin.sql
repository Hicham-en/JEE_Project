-- Seed initial des rôles
INSERT INTO roles (nom_role) VALUES ('ADMIN_ROLE');
INSERT INTO roles (nom_role) VALUES ('ANNOTATOR_ROLE');

-- Seed de l'administrateur par défaut
-- INFO BCRYPT: Mot de passe original en clair : "Admin1234"
-- Hash généré via Python bcrypt avec rounds=12, prefix=b'2a'
INSERT INTO users (nom, prenom, login, password_hash, active, created_at)
VALUES ('Admin', 'System', 'admin', '$2a$12$1HIo7ExfFvk.TRXk8Z4YQ.ZXc7ZhdBYz.k8.RDkv3QHoywE7kN5jm', 1, CURRENT_TIMESTAMP);

-- Mémorise et peuple la jointure user_role
SET @admin_id = (SELECT id FROM users WHERE login = 'admin');
SET @role_id = (SELECT id FROM roles WHERE nom_role = 'ADMIN_ROLE');

INSERT INTO user_roles (user_id, role_id) VALUES (@admin_id, @role_id);
INSERT INTO administrators (id) VALUES (@admin_id);
