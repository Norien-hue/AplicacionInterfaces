-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 18-11-2025 a las 12:08:28
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
-- Base de datos: `reciclaje_db`
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
('EAN13', 8410031961234, 'Botella agua 50cl Bezoya', 1.2, 'PET', NULL),
('EAN13', 8410031961241, 'Botella agua 1L Bezoya', 2.1, 'PET', NULL),
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
(1, 'EAN13', 8410031961234, '2024-01-15', '09:30:00'),
(4, 'EAN13', 8410031961241, '2024-01-23', '19:45:00'),
(3, 'EAN13', 8410100222224, '2024-01-17', '16:45:00'),
(2, 'EAN13', 8410123151234, '2024-01-20', '14:30:00'),
(4, 'EAN13', 8410314021012, '2024-01-18', '20:20:00'),
(1, 'EAN13', 8410376101246, '2024-01-15', '09:32:00'),
(1, 'EAN13', 8410596004108, '2024-01-19', '10:00:00'),
(5, 'EAN13', 8410654012345, '2024-01-21', '18:15:00'),
(3, 'EAN13', 8437001234567, '2024-01-22', '12:00:00'),
(2, 'EAN13', 8480000178324, '2024-01-16', '11:15:00');

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
(1, 15.5, '$2y$10$abc123def456ghi789jkl', 'cliente', 'maria_garcia', 12345),
(2, 28.3, '$2y$10$xyz789uvw012abc345def', 'cliente', 'carlos_ruiz', 67890),
(3, 0, '$2y$10$admin123hash456secure', 'administrador', 'admin_recicla', 54321),
(4, 7.8, '$2y$10$userpass123hash456789', 'cliente', 'ana_martinez', 98765),
(5, 42.1, '$2y$10$anotherhash7890123456', 'cliente', 'javier_lopez', 13579);

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
  ADD KEY `Tipo` (`Tipo`,`Numero_barras`);

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
  MODIFY `Id_Usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `Recicla`
--
ALTER TABLE `Recicla`
  ADD CONSTRAINT `Recicla_ibfk_1` FOREIGN KEY (`Id_Usuario`) REFERENCES `Usuarios` (`Id_Usuario`),
  ADD CONSTRAINT `Recicla_ibfk_2` FOREIGN KEY (`Tipo`,`Numero_barras`) REFERENCES `Productos` (`Tipo`, `Numero_barras`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
