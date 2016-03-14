


CREATE TABLE IF NOT EXISTS `annotation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_name` char(52) NOT NULL,
  `type` char(52) NOT NULL,
  `annotation` text NOT NULL,
  UNIQUE KEY `id` (`id`),
  KEY `owner_name` (`owner_name`),
  KEY `type_of_anotation` (`type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=984 ;
CREATE TABLE IF NOT EXISTS `base` (
`name` char(52)
,`type` varchar(52)
,`Кр. заглавие` text
,`номер` text
);
CREATE TABLE IF NOT EXISTS `pragm` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_name` char(52) NOT NULL,
  `type` char(52) NOT NULL,
  `pragm` text NOT NULL,
  `last_update` bigint(20) NOT NULL DEFAULT '1',
  UNIQUE KEY `id` (`id`),
  KEY `owner_name` (`owner_name`),
  KEY `type` (`type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=182 ;

CREATE TABLE IF NOT EXISTS `primary` (
  `name` char(52) NOT NULL,
  `type` varchar(52) NOT NULL DEFAULT 'первичный',
  `charac` text NOT NULL,
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `sinonim` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_name` char(52) NOT NULL,
  `sinonim` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_name` (`owner_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=21 ;

CREATE TABLE IF NOT EXISTS `type_of_annotation` (
  `type` char(52) NOT NULL,
  UNIQUE KEY `type_2` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `type_of_pragm` (
  `type` char(52) NOT NULL,
  UNIQUE KEY `type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `base`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `base` AS select distinct `primary`.`name` AS `name`,`primary`.`type` AS `type`,`an1`.`annotation` AS `Кр. заглавие`,`an2`.`annotation` AS `номер` from (`primary` join (`annotation` `an1` join `annotation` `an2` on((`an1`.`owner_name` = `an2`.`owner_name`)))) where ((`an1`.`owner_name` = `primary`.`name`) and (`an2`.`owner_name` = `primary`.`name`) and (`an1`.`type` = 'Краткое заглавие') and (`an2`.`type` = 'Номер'));


ALTER TABLE `annotation`
  ADD CONSTRAINT `annotation_ibfk_2` FOREIGN KEY (`type`) REFERENCES `type_of_annotation` (`type`),
  ADD CONSTRAINT `annotation_ibfk_4` FOREIGN KEY (`owner_name`) REFERENCES `primary` (`name`) ON DELETE CASCADE;

ALTER TABLE `pragm`
  ADD CONSTRAINT `pragm_ibfk_2` FOREIGN KEY (`type`) REFERENCES `type_of_pragm` (`type`),
  ADD CONSTRAINT `pragm_ibfk_4` FOREIGN KEY (`owner_name`) REFERENCES `primary` (`name`) ON DELETE CASCADE;

ALTER TABLE `sinonim`
  ADD CONSTRAINT `sinonim_ibfk_2` FOREIGN KEY (`owner_name`) REFERENCES `primary` (`name`) ON DELETE CASCADE ON UPDATE CASCADE;