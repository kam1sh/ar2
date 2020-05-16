CREATE TABLE groups (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL UNIQUE,
    owner_id bigint REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_groups_name ON groups(name);

CREATE TABLE group_roles (
    id serial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id bigint NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    role varchar(16) NOT NULL
);
CREATE INDEX idx_grouproles_userid ON group_roles(user_id);
CREATE INDEX idx_grouproles_groupid ON group_roles(group_id);

CREATE TABLE projects (
    id serial PRIMARY KEY,
    name varchar(32) NOT NULL,
    group_id bigint REFERENCES groups(id) ON DELETE SET NULL,
    owner_id bigint REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE projects_roles (
    id serial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id bigint NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    role varchar(16) NOT NULL
);

ALTER TABLE users ADD COLUMN disabled BOOLEAN NOT NULL DEFAULT false;
