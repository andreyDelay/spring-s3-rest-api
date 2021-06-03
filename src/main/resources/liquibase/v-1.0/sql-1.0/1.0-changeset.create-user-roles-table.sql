CREATE TABLE IF NOT EXISTS user_roles(
    user_id BIGINT NOT NULL ,
    role_id BIGINT NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
)engine = InnoDB;