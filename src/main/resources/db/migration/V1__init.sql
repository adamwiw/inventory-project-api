CREATE TABLE IF NOT EXISTS `inventory` (
    `id` int NOT NULL PRIMARY KEY,
    `name` varchar(20)
)ENGINE=InnoDB DEFAULT CHARSET=UTF8;

INSERT INTO inventory (id, name) VALUES (0, 'No items found');