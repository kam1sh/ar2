CREATE TABLE IF NOT EXISTS users_sessions (
    session_key varchar(40) NOT NULL PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires timestamp NOT NULL
)
