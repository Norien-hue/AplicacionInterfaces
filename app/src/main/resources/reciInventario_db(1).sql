-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 10-12-2025 a las 09:43:08
-- Versión del servidor: 5.7.35-0ubuntu0.18.04.2
-- Versión de PHP: 8.0.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `reciInventario_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Productos`
--

CREATE TABLE `Productos` (
  `Tipo` varchar(10) NOT NULL,
  `Numero_barras` bigint(20) NOT NULL,
  `Nombre` varchar(50) DEFAULT NULL,
  `Emisiones_Reducibles` float DEFAULT NULL,
  `Material` varchar(15) DEFAULT NULL,
  `Imagen` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Volcado de datos para la tabla `Productos`
--

INSERT INTO `Productos` (`Tipo`, `Numero_barras`, `Nombre`, `Emisiones_Reducibles`, `Material`, `Imagen`) VALUES
('EAN13', 123456789, 'Botellin 2.0', 0.1, 'PET', NULL),
('EAN13', 8410031961234, 'Botella agua 50cl Bezoya', 1.2, 'PET', NULL),
('EAN13', 8410031961241, 'Botella agua 1L Bezoya', 200, 'PET', NULL),
('EAN13', 8410100222224, 'Brick leche 1L Pascual', 1.7, 'Brick', NULL),
('EAN13', 8410123151234, 'Yogur natural Danone', 0.5, 'Plástico', NULL),
('EAN13', 8410314021012, 'Botella cerveza 33cl Mahou', 1.5, 'Vidrio', NULL),
('EAN13', 8410376101246, 'Lata Coca-Cola 33cl', 0.8, 'Aluminio', NULL),
('EAN13', 8410596004108, 'Botella detergente 1L Skip', 4.2, 'Plástico', NULL),
('EAN13', 8410654012345, 'Bote champú 400ml Pantene', 2.8, 'Plástico', NULL),
('EAN13', 8437001234567, 'Bote tomate frito 500g Origen', 1.9, 'Vidrio', NULL),
('EAN13', 8480000178324, 'Botella aceite 1L Carbonell', 3.5, 'Vidrio', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Recicla`
--

CREATE TABLE `Recicla` (
  `Id_Usuario` int(11) NOT NULL,
  `Tipo` varchar(10) NOT NULL,
  `Numero_barras` bigint(20) NOT NULL,
  `Fecha` date NOT NULL,
  `Hora` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Volcado de datos para la tabla `Recicla`
--

INSERT INTO `Recicla` (`Id_Usuario`, `Tipo`, `Numero_barras`, `Fecha`, `Hora`) VALUES
(13, 'EAN13', 8410031961241, '2025-12-09', '19:19:59'),
(12, 'EAN13', 8410123151234, '2025-12-09', '14:38:29'),
(13, 'EAN13', 8410314021012, '2025-12-09', '12:28:46'),
(15, 'EAN13', 8410314021012, '2025-12-09', '12:25:42'),
(13, 'EAN13', 8410376101246, '2025-12-09', '19:19:38'),
(12, 'EAN13', 8410596004108, '2025-12-09', '18:59:25');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Usuarios`
--

CREATE TABLE `Usuarios` (
  `Id_Usuario` int(11) NOT NULL,
  `Emisiones_Reducidas` float DEFAULT '0',
  `Hash_Contraseña` varchar(100) NOT NULL,
  `Permisos` varchar(15) DEFAULT NULL,
  `Nombre` varchar(50) NOT NULL,
  `TAP` mediumint(9) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Volcado de datos para la tabla `Usuarios`
--

INSERT INTO `Usuarios` (`Id_Usuario`, `Emisiones_Reducidas`, `Hash_Contraseña`, `Permisos`, `Nombre`, `TAP`) VALUES
(12, 4.7, '$2y$10$20761617', 'administrador', 'Admin', 224969),
(13, 4.4, '$2y$10$-267577715', 'cliente', 'User', 824330),
(15, 1.5, '$2y$10$-995380578', 'cliente', 'Antonio Rueda', NULL),
(16, 0, '$2y$10$983918467', 'cliente', 'PepitoPalotes', NULL),
(18, 0, '$2y$10$-995380578', 'administrador', 'Profesor', NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `Productos`
--
ALTER TABLE `Productos`
  ADD PRIMARY KEY (`Tipo`,`Numero_barras`);

--
-- Indices de la tabla `Recicla`
--
ALTER TABLE `Recicla`
  ADD PRIMARY KEY (`Id_Usuario`,`Tipo`,`Numero_barras`,`Fecha`,`Hora`),
  ADD KEY `Recicla_ibfk_2` (`Tipo`,`Numero_barras`);

--
-- Indices de la tabla `Usuarios`
--
ALTER TABLE `Usuarios`
  ADD PRIMARY KEY (`Id_Usuario`),
  ADD UNIQUE KEY `Nombre` (`Nombre`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `Usuarios`
--
ALTER TABLE `Usuarios`
  MODIFY `Id_Usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `Recicla`
--
ALTER TABLE `Recicla`
  ADD CONSTRAINT `Recicla_ibfk_1` FOREIGN KEY (`Id_Usuario`) REFERENCES `Usuarios` (`Id_Usuario`) ON DELETE CASCADE,
  ADD CONSTRAINT `Recicla_ibfk_2` FOREIGN KEY (`Tipo`,`Numero_barras`) REFERENCES `Productos` (`Tipo`, `Numero_barras`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
