CREATE TABLE IF NOT EXISTS events
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL ,
    name VARCHAR(255) NOT NULL UNIQUE ,
    description VARCHAR(128) NOT NULL ,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    user_id BIGINT NOT NULL ,
    FOREIGN KEY(user_id) REFERENCES users(id)
) engine = InnoDB;