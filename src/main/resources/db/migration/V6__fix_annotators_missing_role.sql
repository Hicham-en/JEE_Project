-- Répare les annotateurs créés sans rôle suite au bug ERR-A-01
-- (createAnnotator cherchait 'ANNOTATOR' au lieu de 'ANNOTATOR_ROLE' dans roles)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE r.nom_role = 'ANNOTATOR_ROLE'
AND u.id NOT IN (SELECT user_id FROM user_roles)
AND u.id IN (SELECT id FROM annotators);
