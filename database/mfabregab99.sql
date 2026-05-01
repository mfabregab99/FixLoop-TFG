-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 01-12-2025 a las 22:15:07
-- Versión del servidor: 8.0.44-0ubuntu0.24.04.1
-- Versión de PHP: 8.4.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `mfabregab99`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categories`
--

CREATE TABLE `categories` (
  `id` int NOT NULL,
  `nom` varchar(50) NOT NULL,
  `icona_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `favorits`
--

CREATE TABLE `favorits` (
  `usuari_id` int NOT NULL,
  `reparador_id` int NOT NULL,
  `data_afegit` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `missatges`
--

CREATE TABLE `missatges` (
  `id` int NOT NULL,
  `xat_id` int NOT NULL,
  `emissor_id` int NOT NULL,
  `contingut` text NOT NULL,
  `tipus` enum('text','pressupost','imatge') DEFAULT 'text',
  `llegit` tinyint(1) DEFAULT '0',
  `data_enviament` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reparador_categories`
--

CREATE TABLE `reparador_categories` (
  `usuari_id` int NOT NULL,
  `categoria_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sollicituds`
--

CREATE TABLE `sollicituds` (
  `id` int NOT NULL,
  `usuari_id` int NOT NULL,
  `categoria_id` int NOT NULL,
  `titol` varchar(100) NOT NULL,
  `descripcio` text NOT NULL,
  `foto_url` varchar(255) DEFAULT NULL,
  `estat` enum('oberta','assignada','finalitzada') DEFAULT 'oberta',
  `data_creacio` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuaris`
--

CREATE TABLE `usuaris` (
  `id` int NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nom_complet` varchar(100) NOT NULL,
  `tipus` enum('client','reparador','admin') NOT NULL DEFAULT 'client',
  `es_pro` tinyint(1) DEFAULT '0',
  `foto_perfil` varchar(255) DEFAULT NULL,
  `descripcio` text,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `co2_estalviat` int DEFAULT '0',
  `data_registre` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `valoracions`
--

CREATE TABLE `valoracions` (
  `id` int NOT NULL,
  `sollicitud_id` int NOT NULL,
  `autor_id` int NOT NULL,
  `reparador_id` int NOT NULL,
  `puntuacio` int NOT NULL,
  `comentari` text,
  `data_valoracio` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `xats`
--

CREATE TABLE `xats` (
  `id` int NOT NULL,
  `sollicitud_id` int DEFAULT NULL,
  `client_id` int NOT NULL,
  `reparador_id` int NOT NULL,
  `data_ultima_activitat` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `favorits`
--
ALTER TABLE `favorits`
  ADD PRIMARY KEY (`usuari_id`,`reparador_id`),
  ADD KEY `reparador_id` (`reparador_id`);

--
-- Indices de la tabla `missatges`
--
ALTER TABLE `missatges`
  ADD PRIMARY KEY (`id`),
  ADD KEY `xat_id` (`xat_id`),
  ADD KEY `emissor_id` (`emissor_id`);

--
-- Indices de la tabla `reparador_categories`
--
ALTER TABLE `reparador_categories`
  ADD PRIMARY KEY (`usuari_id`,`categoria_id`),
  ADD KEY `categoria_id` (`categoria_id`);

--
-- Indices de la tabla `sollicituds`
--
ALTER TABLE `sollicituds`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuari_id` (`usuari_id`),
  ADD KEY `categoria_id` (`categoria_id`);

--
-- Indices de la tabla `usuaris`
--
ALTER TABLE `usuaris`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indices de la tabla `valoracions`
--
ALTER TABLE `valoracions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sollicitud_id` (`sollicitud_id`),
  ADD KEY `autor_id` (`autor_id`),
  ADD KEY `reparador_id` (`reparador_id`);

--
-- Indices de la tabla `xats`
--
ALTER TABLE `xats`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sollicitud_id` (`sollicitud_id`),
  ADD KEY `client_id` (`client_id`),
  ADD KEY `reparador_id` (`reparador_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `missatges`
--
ALTER TABLE `missatges`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `sollicituds`
--
ALTER TABLE `sollicituds`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuaris`
--
ALTER TABLE `usuaris`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `valoracions`
--
ALTER TABLE `valoracions`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `xats`
--
ALTER TABLE `xats`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `favorits`
--
ALTER TABLE `favorits`
  ADD CONSTRAINT `favorits_ibfk_1` FOREIGN KEY (`usuari_id`) REFERENCES `usuaris` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `favorits_ibfk_2` FOREIGN KEY (`reparador_id`) REFERENCES `usuaris` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `missatges`
--
ALTER TABLE `missatges`
  ADD CONSTRAINT `missatges_ibfk_1` FOREIGN KEY (`xat_id`) REFERENCES `xats` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `missatges_ibfk_2` FOREIGN KEY (`emissor_id`) REFERENCES `usuaris` (`id`);

--
-- Filtros para la tabla `reparador_categories`
--
ALTER TABLE `reparador_categories`
  ADD CONSTRAINT `reparador_categories_ibfk_1` FOREIGN KEY (`usuari_id`) REFERENCES `usuaris` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reparador_categories_ibfk_2` FOREIGN KEY (`categoria_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `sollicituds`
--
ALTER TABLE `sollicituds`
  ADD CONSTRAINT `sollicituds_ibfk_1` FOREIGN KEY (`usuari_id`) REFERENCES `usuaris` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `sollicituds_ibfk_2` FOREIGN KEY (`categoria_id`) REFERENCES `categories` (`id`);

--
-- Filtros para la tabla `valoracions`
--
ALTER TABLE `valoracions`
  ADD CONSTRAINT `valoracions_ibfk_1` FOREIGN KEY (`sollicitud_id`) REFERENCES `sollicituds` (`id`),
  ADD CONSTRAINT `valoracions_ibfk_2` FOREIGN KEY (`autor_id`) REFERENCES `usuaris` (`id`),
  ADD CONSTRAINT `valoracions_ibfk_3` FOREIGN KEY (`reparador_id`) REFERENCES `usuaris` (`id`);

--
-- Filtros para la tabla `xats`
--
ALTER TABLE `xats`
  ADD CONSTRAINT `xats_ibfk_1` FOREIGN KEY (`sollicitud_id`) REFERENCES `sollicituds` (`id`),
  ADD CONSTRAINT `xats_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `usuaris` (`id`),
  ADD CONSTRAINT `xats_ibfk_3` FOREIGN KEY (`reparador_id`) REFERENCES `usuaris` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
