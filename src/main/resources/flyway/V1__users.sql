CREATE TABLE IF NOT EXISTS users (
    id serial PRIMARY KEY,
    username VARCHAR (64) UNIQUE NOT NULL,
    password_hash VARCHAR (128) NOT NULL,
    email VARCHAR (256) NOT NULL,
    name VARCHAR(256) NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    created_on TIMESTAMP NOT NULL,
    last_login TIMESTAMP
);