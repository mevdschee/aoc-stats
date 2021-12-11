CREATE DATABASE `aoc-stats` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'aoc-stats'@'localhost' IDENTIFIED BY 'aoc-stats';
GRANT ALL PRIVILEGES ON `aoc-stats`.* TO 'aoc-stats'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
