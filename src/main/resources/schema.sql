CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT
);

CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    teacher_id INTEGER NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS student_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id INTEGER NOT NULL,
    student_id INTEGER UNIQUE NOT NULL,
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS preferences (
    group_id INTEGER NOT NULL,
    rank INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    PRIMARY KEY (group_id, rank),
    UNIQUE (group_id, subject_id),
    FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);
