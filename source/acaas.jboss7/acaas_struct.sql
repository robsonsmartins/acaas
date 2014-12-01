-- phpMyAdmin SQL Dump
-- version 4.0.10.6
-- http://www.phpmyadmin.net
--
-- Máquina: 127.5.234.2:3306
-- Data de Criação: 01-Dez-2014 às 15:12
-- Versão do servidor: 5.1.73
-- versão do PHP: 5.3.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de Dados: `acaas`
--
CREATE DATABASE IF NOT EXISTS `acaas` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
USE `acaas`;

-- --------------------------------------------------------

--
-- Estrutura da tabela `imagens`
--

DROP TABLE IF EXISTS `imagens`;
CREATE TABLE IF NOT EXISTS `imagens` (
  `id` varchar(4) COLLATE utf8_bin NOT NULL,
  `jpg` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Estrutura da tabela `informal`
--

DROP TABLE IF EXISTS `informal`;
CREATE TABLE IF NOT EXISTS `informal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `palavra` varchar(50) COLLATE utf8_bin NOT NULL,
  `sinonimo` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sinonimo` (`sinonimo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=231967 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `libras`
--

DROP TABLE IF EXISTS `libras`;
CREATE TABLE IF NOT EXISTS `libras` (
  `palavra` varchar(50) COLLATE utf8_bin NOT NULL,
  `descricao` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `portugues` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `libras` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `classe` varchar(20) COLLATE utf8_bin NOT NULL,
  `origem` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `midia` varchar(4) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`palavra`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Estrutura da tabela `sinonimos`
--

DROP TABLE IF EXISTS `sinonimos`;
CREATE TABLE IF NOT EXISTS `sinonimos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `palavra` varchar(50) COLLATE utf8_bin NOT NULL,
  `sinonimo` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sinonimo` (`sinonimo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=17181 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `verbos`
--

DROP TABLE IF EXISTS `verbos`;
CREATE TABLE IF NOT EXISTS `verbos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `verbo` varchar(50) COLLATE utf8_bin NOT NULL,
  `conjugacao` varchar(50) COLLATE utf8_bin NOT NULL,
  `pessoa` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `tempo` varchar(15) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `conjugacao` (`conjugacao`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=69027 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `verbos_informal`
--

DROP TABLE IF EXISTS `verbos_informal`;
CREATE TABLE IF NOT EXISTS `verbos_informal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `verbo` varchar(50) COLLATE utf8_bin NOT NULL,
  `conjugacao` varchar(50) COLLATE utf8_bin NOT NULL,
  `pessoa` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `tempo` varchar(15) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `conjugacao` (`conjugacao`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=279970 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `verbos_sinonimos`
--

DROP TABLE IF EXISTS `verbos_sinonimos`;
CREATE TABLE IF NOT EXISTS `verbos_sinonimos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `verbo` varchar(50) COLLATE utf8_bin NOT NULL,
  `conjugacao` varchar(50) COLLATE utf8_bin NOT NULL,
  `pessoa` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `tempo` varchar(15) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `conjugacao` (`conjugacao`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=118747 ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `videos`
--

DROP TABLE IF EXISTS `videos`;
CREATE TABLE IF NOT EXISTS `videos` (
  `id` varchar(4) COLLATE utf8_bin NOT NULL,
  `ogg` longblob,
  `mp4` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
