-- Administrateur (login: admin, mot de passe: admin)
INSERT INTO users (id, login, password_hash, role, first_name, last_name)
VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'ADMIN', 'Admin', 'System');

-- Enseignants (mot de passe = prénom en minuscules)
INSERT INTO users (id, login, password_hash, role, first_name, last_name) VALUES
(2, 'turing', '02558a70324e7c4f269c69825450cec8', 'TEACHER', 'Alan', 'Turing'),
(3, 'hopper', '15e5c87b18c1289d45bb4a72961b58e8', 'TEACHER', 'Grace', 'Hopper'),
(4, 'knuth', '0d343c0f0ca763f983c8042350059f56', 'TEACHER', 'Donald', 'Knuth'),
(5, 'dijkstra', '35ad6d4a985ed75a723bae987f6f8209', 'TEACHER', 'Edsger', 'Dijkstra'),
(6, 'lovelace', '8c8d357b5e872bbacd45197626bd5759', 'TEACHER', 'Ada', 'Lovelace');

-- Étudiants de profils variés (mot de passe = prénom en minuscules)
INSERT INTO users (id, login, password_hash, role, first_name, last_name) VALUES
(7, 'jdupont', 'b71985397688d6f1820685dde534981b', 'STUDENT', 'Jean', 'Dupont'),
(8, 'cgomez', 'dc599a9972fde3045dab59dbd1ae170b', 'STUDENT', 'Carlos', 'Gomez'),
(9, 'khaddad', '2167a6ac80340b69f3b05b800417d6c7', 'STUDENT', 'Karim', 'Haddad'),
(10, 'ybenali', '09f96867a8dc816a021fd861f200abef', 'STUDENT', 'Youssef', 'Benali'),
(11, 'amansour', '30d2310007b75bf0180f5ed831f20fdb', 'STUDENT', 'Amine', 'Mansour'),
(12, 'fzahra', 'b5d5f67b30809413156655abdda382a3', 'STUDENT', 'Fatima', 'Zahra'),
(13, 'jsmith', '527bd5b5d689e2c32ae974c6229ff785', 'STUDENT', 'John', 'Smith'),
(14, 'lwei', 'd70c1e5d44de8a9150eb91ecff563578', 'STUDENT', 'Li', 'Wei'),
(15, 'smartin', '6988ec3aba1eaddf2435141bf10487ca', 'STUDENT', 'Sophie', 'Martin'),
(16, 'msilva', '263bce650e68ab4e23f28263760b9fa5', 'STUDENT', 'Maria', 'Silva'),
(17, 'okhoury', 'd4466cce49457cfea18222f5a7cd3573', 'STUDENT', 'Omar', 'Khoury'),
(18, 'ltaleb', '754f9968bf5f5f68d7dea029889b7415', 'STUDENT', 'Leila', 'Taleb'),
(19, 'mbelkacem', '7f7d49795dcf0a82605fb1103ed20d28', 'STUDENT', 'Mehdi', 'Belkacem'),
(20, 'nchahed', 'ccbc1770bb10486495d127a7d65c252b', 'STUDENT', 'Nour', 'Chahed'),
(21, 'dmiller', '172522ec1028ab781d9dfd17eaca4427', 'STUDENT', 'David', 'Miller'),
(22, 'zming', 'd0cd2693b3506677e4c55e91d6365bff', 'STUDENT', 'Zhang', 'Ming');

-- Sujets préchargés (8 sujets associés aux enseignants)
INSERT INTO subjects (id, title, description, teacher_id) VALUES
(1, 'Détection de spam par apprentissage automatique', 'Conception et entraînement d''un modèle de classification pour filtrer les courriers indésirables.', 2),
(2, 'Analyse de stratégies au Tic-Tac-Toe', 'Étude et implémentation d''algorithmes minimax et apprentissage par renforcement sur le jeu du morpion.', 3),
(3, 'Gestion de bibliothèque universitaire', 'Développement d''un système complet de gestion des emprunts, réservations et catalogue de livres.', 4),
(4, 'Application de suivi des stages', 'Plateforme web permettant aux étudiants de déposer leurs conventions et aux tuteurs de suivre les évaluations.', 5),
(5, 'Comparaison d''algorithmes de tri', 'Étude comparative des performances empiriques et théoriques de différents algorithmes de tri sur de grands volumes.', 4),
(6, 'Plus courts chemins dans un réseau urbain', 'Modélisation d''un réseau de transport sous forme de graphe et implémentation de variantes de l''algorithme de Dijkstra.', 5),
(7, 'Plateforme de gestion de projets étudiants', 'Conception d''une application agile pour le choix et l''affectation optimale des projets d''études.', 6),
(8, 'Application de réservation de salles', 'Système de réservation avec gestion des conflits, des capacités et des équipements disponibles.', 2);
