-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: fitness_db
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `add_on`
--

DROP TABLE IF EXISTS `add_on`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `add_on` (
  `capacity` int DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `tax_percent` decimal(5,2) DEFAULT NULL,
  `addon_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `addon_name` varchar(100) NOT NULL,
  `addon_type` enum('SERVICE','FACILITY','OTHER') NOT NULL,
  PRIMARY KEY (`addon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `add_on`
--

LOCK TABLES `add_on` WRITE;
/*!40000 ALTER TABLE `add_on` DISABLE KEYS */;
/*!40000 ALTER TABLE `add_on` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `alert_flag` bit(1) NOT NULL,
  `branch_id` bigint NOT NULL,
  `check_in_time` datetime(6) NOT NULL,
  `check_out_time` datetime(6) DEFAULT NULL,
  `class_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `override_by` bigint DEFAULT NULL,
  `override_reason` text,
  `scan_method` enum('QR','CARD','MANUAL') NOT NULL,
  `sync_status` enum('SYNCED','PENDING') NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `FKtkkv9kxj3yjs8p51qbh0y90le` (`branch_id`),
  KEY `FKrx58locko31i5sa3goghxssli` (`class_id`),
  KEY `FKslaf4mu3eu0gi72u4t9xcsxjd` (`member_id`),
  KEY `FKfy45h23gs2i2y378jgngjyiyx` (`override_by`),
  CONSTRAINT `FKfy45h23gs2i2y378jgngjyiyx` FOREIGN KEY (`override_by`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `FKrx58locko31i5sa3goghxssli` FOREIGN KEY (`class_id`) REFERENCES `classes` (`class_id`),
  CONSTRAINT `FKslaf4mu3eu0gi72u4t9xcsxjd` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKtkkv9kxj3yjs8p51qbh0y90le` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `audit_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `entity_id` bigint NOT NULL,
  `performed_by` bigint NOT NULL,
  `entity_name` varchar(60) NOT NULL,
  `new_value` json DEFAULT NULL,
  `old_value` json DEFAULT NULL,
  `action` enum('CREATE','UPDATE','DELETE','LOGIN','OVERRIDE') NOT NULL,
  PRIMARY KEY (`audit_id`),
  KEY `FKtgeaxw2psllq9m08751q09eut` (`performed_by`),
  CONSTRAINT `FKtgeaxw2psllq9m08751q09eut` FOREIGN KEY (`performed_by`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
INSERT INTO `audit_log` VALUES (1,'2026-05-27 08:46:16.922616',11,11,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(2,'2026-05-27 08:48:24.487529',26,26,'SystemUser','\"Password reset initiated\"',NULL,'UPDATE'),(3,'2026-05-27 08:48:50.823059',26,26,'SystemUser','\"Password reset successful\"',NULL,'UPDATE'),(4,'2026-05-27 08:49:06.499178',26,26,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(5,'2026-05-27 21:06:55.888872',11,11,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(6,'2026-05-27 21:18:13.017689',28,28,'SystemUser','\"Password reset initiated\"',NULL,'UPDATE'),(7,'2026-05-27 21:19:37.296501',28,28,'SystemUser','\"Password reset successful\"',NULL,'UPDATE'),(8,'2026-05-27 21:20:01.128772',28,28,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(9,'2026-05-27 21:23:10.081901',16,28,'Member','{\"status\": \"PROSPECT\", \"mem_name\": \"Hritik\"}',NULL,'CREATE'),(10,'2026-05-27 21:23:21.644323',1,28,'Invoice','\"Invoice created for member: Hritik | Amount: ₹17999.00\"',NULL,'CREATE'),(11,'2026-05-27 21:23:21.676796',1,28,'Membership','\"Plan selected (PENDING payment): Diamond Annual for member Hritik\"',NULL,'CREATE'),(12,'2026-05-27 21:25:40.906960',24,24,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(13,'2026-05-27 21:26:25.292001',26,26,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(14,'2026-05-27 21:27:02.724378',28,28,'SystemUser','\"Password reset initiated\"',NULL,'UPDATE'),(15,'2026-05-27 21:27:32.982063',28,28,'SystemUser','\"Password reset successful\"',NULL,'UPDATE'),(16,'2026-05-27 21:28:47.498943',29,29,'SystemUser','\"Password reset initiated\"',NULL,'UPDATE'),(17,'2026-05-27 21:29:16.879327',29,29,'SystemUser','\"Password reset successful\"',NULL,'UPDATE'),(18,'2026-05-27 21:29:39.189613',29,29,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(19,'2026-05-27 21:34:26.373854',2,29,'Invoice','\"Invoice created for member: Hritik | Amount: ₹713.11\"',NULL,'CREATE'),(20,'2026-05-27 21:34:34.287357',1,29,'Receipt','\"Receipt generated for payment 1\"',NULL,'CREATE'),(21,'2026-05-27 21:34:34.334545',1,29,'Payment','\"Payment processed: 500.11 via CARD\"',NULL,'CREATE'),(22,'2026-05-27 21:55:05.443521',1,1,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(23,'2026-05-27 21:59:40.826444',11,11,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(24,'2026-05-27 22:01:10.250149',30,30,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(25,'2026-05-27 22:03:43.991835',5,5,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(26,'2026-05-27 22:05:38.717569',1,5,'Class','\"Class created: YOGA | Trainer: 1 | Room: Aqua Pool\"',NULL,'CREATE'),(27,'2026-05-27 22:05:50.368064',29,29,'SystemUser','\"login_successful\"',NULL,'LOGIN'),(28,'2026-05-27 22:07:10.586882',2,29,'Receipt','\"Receipt generated for payment 2\"',NULL,'CREATE'),(29,'2026-05-27 22:07:10.702826',2,29,'Payment','\"Payment processed: 713.11 via CARD\"',NULL,'CREATE'),(30,'2026-05-27 22:07:39.232862',3,29,'Invoice','\"Invoice created for member: Hritik | Amount: ₹2948.82\"',NULL,'CREATE'),(31,'2026-05-27 22:07:40.682468',3,29,'Receipt','\"Receipt generated for payment 3\"',NULL,'CREATE'),(32,'2026-05-27 22:07:40.702757',3,29,'Payment','\"Payment processed: 2948.82 via CARD\"',NULL,'CREATE'),(33,'2026-05-27 22:08:22.803488',4,29,'Receipt','\"Receipt generated for payment 4\"',NULL,'CREATE'),(34,'2026-05-27 22:08:22.825995',4,29,'Payment','\"Payment processed: 17999 via CARD\"',NULL,'CREATE'),(35,'2026-05-27 22:08:22.874009',1,29,'Membership','\"Status changed to ACTIVE - dunning resolved\"',NULL,'UPDATE'),(36,'2026-05-27 22:10:27.598494',1,29,'HealthConsent','\"memberId=16, formVersion=v1.0\"',NULL,'CREATE');
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `branch`
--

DROP TABLE IF EXISTS `branch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch` (
  `branch_id` bigint NOT NULL AUTO_INCREMENT,
  `branch_name` varchar(120) NOT NULL,
  `address` text NOT NULL,
  `contact` varchar(20) NOT NULL,
  `op_hours` varchar(100) NOT NULL,
  `timezone` varchar(60) NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branch`
--

LOCK TABLES `branch` WRITE;
/*!40000 ALTER TABLE `branch` DISABLE KEYS */;
INSERT INTO `branch` VALUES (1,'East End Fitness','22 Marathahalli Bridge Road, Bengaluru, Karnataka','+91-9876543213','06:00 - 22:00','Asia/Kolkata',1,'2026-01-01 08:00:00'),(2,'West Wing Wellness','10 Rajajinagar Main Road, Bengaluru, Karnataka','+91-9876543214','05:30 - 23:00','Asia/Kolkata',1,'2026-01-01 08:00:00'),(3,'Central Power Zone','3 Cubbon Park Road, Bengaluru, Karnataka','+91-9876543215','06:00 - 21:30','Asia/Kolkata',1,'2026-02-01 08:00:00'),(4,'Tech Park Gym','77 Electronic City Phase 1, Bengaluru, Karnataka','+91-9876543216','05:00 - 23:00','Asia/Kolkata',1,'2026-03-01 08:00:00'),(5,'Lakeside Health Club','5 Ulsoor Lake Road, Bengaluru, Karnataka','+91-9876543217','06:30 - 21:00','Asia/Kolkata',1,'2026-03-15 08:00:00');
/*!40000 ALTER TABLE `branch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_booking`
--

DROP TABLE IF EXISTS `class_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_booking` (
  `waitlist_position` int DEFAULT NULL,
  `booking_id` bigint NOT NULL AUTO_INCREMENT,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `class_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `member_id` bigint NOT NULL,
  `override_by` bigint DEFAULT NULL,
  `waitlist_expiration` datetime(6) DEFAULT NULL,
  `override_reason` text,
  `booking_status` enum('CONFIRMED','WAITLISTED','CANCELLED','NO_SHOW','PENDING_CONFIRMATION') NOT NULL,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `UKaj6cayall18d35o0ad5g0xcr1` (`class_id`,`member_id`),
  KEY `FKmayrqhh9ucalu6w7sp256pl5h` (`member_id`),
  KEY `FKhckcjjt7kjeav3mjtu40ciuuo` (`override_by`),
  CONSTRAINT `FKhckcjjt7kjeav3mjtu40ciuuo` FOREIGN KEY (`override_by`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `FKmayrqhh9ucalu6w7sp256pl5h` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKtbqmyu54sd1dmk4nrrun0nedq` FOREIGN KEY (`class_id`) REFERENCES `classes` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_booking`
--

LOCK TABLES `class_booking` WRITE;
/*!40000 ALTER TABLE `class_booking` DISABLE KEYS */;
/*!40000 ALTER TABLE `class_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classes` (
  `capacity` int NOT NULL,
  `class_time` time(6) NOT NULL,
  `duration_mins` int NOT NULL,
  `end_date` date NOT NULL,
  `start_date` date NOT NULL,
  `branch_id` bigint NOT NULL,
  `class_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `room_id` bigint NOT NULL,
  `trainer_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `weekdays` varchar(100) NOT NULL,
  `class_name` varchar(120) NOT NULL,
  `cancel_reason` text,
  `plan_eligibility` varchar(255) DEFAULT NULL,
  `prerequisites` text,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`class_id`),
  KEY `FKh8mddj6i5oqlsnf82o9d9un2n` (`branch_id`),
  KEY `FKok4aw9cf1yjqtn8320m33yd16` (`room_id`),
  KEY `FKlgxg1smqe1vuewo31mvp7rahw` (`trainer_id`),
  CONSTRAINT `FKh8mddj6i5oqlsnf82o9d9un2n` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `FKlgxg1smqe1vuewo31mvp7rahw` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`),
  CONSTRAINT `FKok4aw9cf1yjqtn8320m33yd16` FOREIGN KEY (`room_id`) REFERENCES `facility` (`facility_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes`
--

LOCK TABLES `classes` WRITE;
/*!40000 ALTER TABLE `classes` DISABLE KEYS */;
INSERT INTO `classes` VALUES (20,'08:00:00.000000',60,'2026-06-30','2026-05-27',5,1,'2026-05-27 22:05:38.682909',1,1,'2026-05-27 22:05:38.682909','TUESDAY,FRIDAY','YOGA',NULL,'ALL','','ACTIVE');
/*!40000 ALTER TABLE `classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `facility`
--

DROP TABLE IF EXISTS `facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facility` (
  `facility_id` bigint NOT NULL AUTO_INCREMENT,
  `facility_name` varchar(120) NOT NULL,
  `branch_id` bigint NOT NULL,
  `capacity` int NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `under_maintenance` tinyint(1) DEFAULT '0',
  `maintenance_reason` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`facility_id`),
  KEY `FK8nkvlhajwfamksywxa6fwyicp` (`branch_id`),
  CONSTRAINT `FK8nkvlhajwfamksywxa6fwyicp` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `fk_facility_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `facility`
--

LOCK TABLES `facility` WRITE;
/*!40000 ALTER TABLE `facility` DISABLE KEYS */;
INSERT INTO `facility` VALUES (1,'Aqua Pool',1,25,1,0,'No current maintenance','2026-01-01 09:00:00','2026-01-01 09:00:00'),(2,'Crossfit Arena',1,20,1,0,'No current maintenance','2026-01-01 09:00:00','2026-01-01 09:00:00'),(3,'Zumba Studio',2,30,1,0,'No current maintenance','2026-01-01 09:00:00','2026-01-01 09:00:00'),(4,'Pilates Room',2,16,1,0,'No current maintenance','2026-01-01 09:00:00','2026-01-01 09:00:00'),(5,'Power Cage Area',3,18,1,1,'Cable machine under repair ? est. back 2026-06-01','2026-02-01 09:00:00','2026-05-15 10:00:00'),(6,'Cardio Deck',4,35,1,0,'No current maintenance','2026-03-01 09:00:00','2026-03-01 09:00:00'),(7,'Wellness Studio',5,20,1,0,'No current maintenance','2026-03-15 09:00:00','2026-03-15 09:00:00');
/*!40000 ALTER TABLE `facility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature_flag`
--

DROP TABLE IF EXISTS `feature_flag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feature_flag` (
  `is_enabled` bit(1) NOT NULL,
  `flag_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `flag_name` varchar(255) NOT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`flag_id`),
  UNIQUE KEY `UK_8oo4o4qqh7wndcpnvbj172erm` (`flag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature_flag`
--

LOCK TABLES `feature_flag` WRITE;
/*!40000 ALTER TABLE `feature_flag` DISABLE KEYS */;
/*!40000 ALTER TABLE `feature_flag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_consent`
--

DROP TABLE IF EXISTS `health_consent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_consent` (
  `liability_acknowledged` bit(1) NOT NULL,
  `medical_acknowledged` bit(1) NOT NULL,
  `privacy_acknowledged` bit(1) NOT NULL,
  `acknowledged_at` datetime(6) NOT NULL,
  `consent_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `form_version` varchar(20) NOT NULL,
  `ip_address` varchar(45) NOT NULL,
  `parq_responses` text,
  `staff_notes` text,
  `status` enum('ACTIVE','EXPIRED','PENDING') NOT NULL,
  PRIMARY KEY (`consent_id`),
  KEY `FKlkrcrwo14y6yqkl00y1wxtc3u` (`member_id`),
  CONSTRAINT `FKlkrcrwo14y6yqkl00y1wxtc3u` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_consent`
--

LOCK TABLES `health_consent` WRITE;
/*!40000 ALTER TABLE `health_consent` DISABLE KEYS */;
INSERT INTO `health_consent` VALUES (_binary '',_binary '',_binary '','2026-05-27 22:10:27.568383',1,'2026-05-27 22:10:27.568383','2027-05-27 22:10:27.568383',16,'2026-05-27 22:10:27.568383','v1.0','0:0:0:0:0:0:0:1','{\"heartCondition\":false,\"chestPain\":false,\"dizziness\":false,\"boneJointProblem\":false,\"bloodPressureMedication\":false,\"otherReason\":false,\"pregnancy\":false}',NULL,'ACTIVE');
/*!40000 ALTER TABLE `health_consent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `discount` decimal(10,2) DEFAULT NULL,
  `wallet_credit_applied` decimal(10,2) NOT NULL DEFAULT '0.00',
  `final_amount` decimal(10,2) NOT NULL,
  `mrp` decimal(10,2) NOT NULL,
  `outstanding` decimal(10,2) DEFAULT NULL,
  `paid_amount` decimal(10,2) DEFAULT NULL,
  `promise_to_pay_date` date DEFAULT NULL,
  `taxes` decimal(10,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `invoice_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `membership_id` bigint DEFAULT NULL,
  `promo_code` varchar(30) DEFAULT NULL,
  `invoice_number` varchar(40) NOT NULL,
  `plan_name` varchar(100) DEFAULT NULL,
  `status` enum('DRAFT','ISSUED','PAID','OVERDUE','VOID','PENDING','UNPAID') NOT NULL,
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `UK_t6xkdjx1qtd5whp2iljdfn2yj` (`invoice_number`),
  KEY `FK9mgjds63m99hhuwk66k3lyxq5` (`member_id`),
  KEY `FKrgvplmyyhctu6xjvuxul6e1y3` (`membership_id`),
  CONSTRAINT `FK9mgjds63m99hhuwk66k3lyxq5` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKrgvplmyyhctu6xjvuxul6e1y3` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`mem_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (0.00,0.00,17999.00,17999.00,0.00,17999.00,NULL,0.00,'2026-05-27 21:23:21.579264',1,16,1,NULL,'INV-37D69460',NULL,'PAID'),(119.85,0.00,713.11,799.00,-500.11,1213.22,NULL,33.96,'2026-05-27 21:34:26.336456',2,16,NULL,'WELCOME8349','INV-811C019D','Senior Wellness Monthly','PAID'),(0.00,0.00,2948.82,2499.00,0.00,2948.82,NULL,449.82,'2026-05-27 22:07:39.213484',3,16,NULL,NULL,'INV-49D11564','Weekend Warrior','PAID');
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login_attempt`
--

DROP TABLE IF EXISTS `login_attempt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_attempt` (
  `attempts` int NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_attempt` datetime(6) DEFAULT NULL,
  `locked_until` datetime(6) DEFAULT NULL,
  `ip_address` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mm48fmvtlg6vs1vx61vq8s62v` (`ip_address`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login_attempt`
--

LOCK TABLES `login_attempt` WRITE;
/*!40000 ALTER TABLE `login_attempt` DISABLE KEYS */;
INSERT INTO `login_attempt` VALUES (0,1,'2026-05-27 21:59:31.644996',NULL,'0:0:0:0:0:0:0:1');
/*!40000 ALTER TABLE `login_attempt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `mem_name` varchar(120) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `dob` date NOT NULL,
  `address` text NOT NULL,
  `emg_contact` varchar(120) NOT NULL,
  `emg_phone` varchar(20) NOT NULL,
  `referral_code` varchar(30) DEFAULT NULL,
  `corporate_code` varchar(30) DEFAULT NULL,
  `status` enum('PROSPECT','ACTIVE','SUSPENDED','DEACTIVATED') NOT NULL,
  `home_branch_id` bigint NOT NULL,
  `photo_path` varchar(255) DEFAULT NULL,
  `notes` text,
  `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pt_session_credits` int NOT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `UK_a9bw6sk85ykh4bacjpu0ju5f6` (`user_id`),
  UNIQUE KEY `UK_6ithqvsvrcawbi9dtxu0ttsny` (`phone`),
  UNIQUE KEY `UK_mbmcqelty0fbrvxp1q58dn57t` (`email`),
  KEY `FK5e1uhn4fu5k8soskrn0b8v6kq` (`created_by`),
  KEY `FKjmlatrnq4pf6u9bc2igxd9i05` (`home_branch_id`),
  CONSTRAINT `FK5e1uhn4fu5k8soskrn0b8v6kq` FOREIGN KEY (`created_by`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `FKgaqfv56tk2dbjtwfgvnlsbvus` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `FKjmlatrnq4pf6u9bc2igxd9i05` FOREIGN KEY (`home_branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `member_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `member_ibfk_2` FOREIGN KEY (`home_branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `member_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,6,'Kiran Bhat','member16@fitness.com','9845012301','1988-04-14','9 Richmond Road, Bengaluru, Karnataka','Suma Bhat','9845012302','REF1003','CORP200','ACTIVE',1,'photos/member16.jpg','Interested in aqua fitness program',5,'2026-03-10 10:00:00','2026-03-10 10:00:00',0),(2,7,'Anjali Desai','member17@fitness.com','9845012303','1995-07-22','14 Cunningham Road, Bengaluru, Karnataka','Prakash Desai','9845012304','REF1004','NONE','ACTIVE',2,'photos/member17.jpg','Prefers early morning Pilates',5,'2026-03-15 11:00:00','2026-03-15 11:00:00',0),(3,8,'Rohan Kulkarni','member18@fitness.com','9845012305','1993-11-09','28 Bannerghatta Road, Bengaluru, Karnataka','Savita Kulkarni','9845012306','NONE','NONE','ACTIVE',3,'photos/member18.jpg','Weight loss goal ? 10 kg in 6 months',5,'2026-04-01 09:00:00','2026-04-01 09:00:00',0),(4,9,'Meena Iyer','member19@fitness.com','9845012307','1997-03-05','67 Dollars Colony, Bengaluru, Karnataka','Venkat Iyer','9845012308','NONE','CORP300','PROSPECT',4,'photos/member19.jpg','Enquired about Diamond Annual plan',5,'2026-04-10 08:30:00','2026-04-10 08:30:00',0),(5,10,'Tarun Goswami','member20@fitness.com','9845012309','1985-09-17','3 Sadashivanagar Circle, Bengaluru, Karnataka','Rekha Goswami','9845012310','REF1005','NONE','ACTIVE',5,'photos/member20.jpg','Senior member ? prefer low-impact classes',5,'2026-05-01 10:00:00','2026-05-01 10:00:00',0),(6,14,'Ashok Verma','ashokverma@gmail.com','918147614828','2002-01-01','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','Ravi','8147614828',NULL,NULL,'ACTIVE',5,'uploads\\photos\\member_6_1779644161847.jpg',NULL,12,'2026-05-24 23:06:02','2026-05-24 23:06:02',10),(7,15,'Raghav','ragav@gmail.com','8147614827','2003-01-01','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','Vivek','8147614828',NULL,NULL,'ACTIVE',5,NULL,NULL,12,'2026-05-24 23:08:48','2026-05-24 23:08:49',10),(8,17,'Meera','meera@gmail.com','8147614826','2004-02-01','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','Aman','7523819239',NULL,NULL,'ACTIVE',5,'uploads\\photos\\member_8_1779650873670.jpg',NULL,12,'2026-05-25 00:57:53','2026-05-25 03:10:06',8),(9,18,'Kriti','kriti@gmail.com','8147614828','2001-11-11','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','Aman','8147614828',NULL,NULL,'ACTIVE',5,NULL,NULL,12,'2026-05-25 01:14:22','2026-05-25 01:14:22',10),(10,19,'Arjun Mehta','arjun.mehta@gmail.com','9876543210','1990-04-12','12 Residency Road, Bengaluru, Karnataka','Rohit Mehta','9876543211','DIAMOND10','CORP-INFY','PROSPECT',1,NULL,'Interested in muscle gain and evening workouts',11,'2026-05-25 03:48:13','2026-05-25 03:48:13',10),(11,20,'Sneha Reddy','sneha.reddy@gmail.com','9845011122','1990-04-12','45 MG Road, Bengaluru, Karnataka','Lakshmi Reddy','9845011123','EARLYBIRD','CORP-WIPRO','PROSPECT',1,NULL,'Prefers yoga and functional fitness sessions',11,'2026-05-25 03:48:14','2026-05-25 03:48:14',10),(12,21,'Rahul Sharma','rahul.sharma@gmail.com','9812345678','1990-04-12','78 Indiranagar 2nd Stage, Bengaluru, Karnataka','Anita Sharma','9812345679','FAMILY200','CORP-TCS','PROSPECT',2,NULL,'Recovering from shoulder injury, requires trainer supervision',11,'2026-05-25 03:48:14','2026-05-25 03:48:14',10),(13,22,'Siri','siri01@gmail.com','7147614828','2000-02-25','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','kiriti','7523819239',NULL,'CORP-INFY','PROSPECT',5,'uploads\\photos\\member_13_1779664819984.jpg',NULL,12,'2026-05-25 04:50:20','2026-05-25 04:50:20',10),(14,23,'Aman Kumar Chauhan','amanchauhan10122004@gmail.com','918147614841','2004-10-12','H NO 1-52/A/S-1\n3ND BLOCK , NEAR WOMENS HOSTEL\nASIAN GARDEN, GULBARGA PO:C J COLONY','Poonam','9703918022',NULL,NULL,'PROSPECT',5,'uploads\\photos\\member_14_1779671215615.jpg',NULL,12,'2026-05-25 06:36:48','2026-05-25 06:36:56',10),(15,24,'Rahul Chauhan','amankumarchauhan.1si22cs015@gmail.com','8147444828','2004-01-03','Village-Larewan','AMAN KUMAR CHAUHAN','9703918022',NULL,NULL,'PROSPECT',5,'uploads\\photos\\member_15_1779674394181.png',NULL,12,'2026-05-25 07:29:40','2026-05-25 07:29:54',10),(16,29,'Hritik','lilemox724@bittnex.com','918147614829','2005-01-13','Village-Larewan','Poonam','8147614828',NULL,NULL,'PROSPECT',5,'uploads\\photos\\member_16_1779897201526.jpg','HeadAce',28,'2026-05-27 21:23:10','2026-05-27 21:23:22',10);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership`
--

DROP TABLE IF EXISTS `membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership` (
  `discount_amount` decimal(10,2) DEFAULT NULL,
  `duration` int NOT NULL,
  `end_date` date NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `start_date` date NOT NULL,
  `branch_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `mem_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `promo_code_used` varchar(30) DEFAULT NULL,
  `status` enum('ACTIVE','EXPIRED','PENDING','DUNNING','SUSPENDED') NOT NULL,
  PRIMARY KEY (`mem_id`),
  KEY `FKik5tek3qutsrw9pt9ejl1hlha` (`branch_id`),
  KEY `FKej551mo6x4epei474ys7ojb7c` (`member_id`),
  KEY `FK2l7koqyyb5vywbwgs0n4p0sr7` (`plan_id`),
  CONSTRAINT `FK2l7koqyyb5vywbwgs0n4p0sr7` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`plan_id`),
  CONSTRAINT `FKej551mo6x4epei474ys7ojb7c` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKik5tek3qutsrw9pt9ejl1hlha` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership`
--

LOCK TABLES `membership` WRITE;
/*!40000 ALTER TABLE `membership` DISABLE KEYS */;
INSERT INTO `membership` VALUES (0.00,365,'2027-05-27',17999.00,'2026-05-27',5,'2026-05-27 21:23:21.498189',1,16,3,'2026-05-27 22:08:22.844141',NULL,'ACTIVE');
/*!40000 ALTER TABLE `membership` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `notif_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `body` text NOT NULL,
  `deep_link` varchar(500) DEFAULT NULL,
  `channel` enum('IN_APP','EMAIL') NOT NULL,
  `delivery_status` enum('SENT','FAILED','PENDING','DELIVERED') NOT NULL,
  `type` enum('BOOKING','CANCELLATION','RENEWAL','SCHEDULE_CHANGE','DUNNING','CHECK_IN','GENERAL') NOT NULL,
  PRIMARY KEY (`notif_id`),
  KEY `FKsq5f70nvburtgf507rf560jd7` (`user_id`),
  CONSTRAINT `FKsq5f70nvburtgf507rf560jd7` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_policy`
--

DROP TABLE IF EXISTS `password_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_policy` (
  `lockout_duration_min` int NOT NULL,
  `max_failed_attempts` int NOT NULL,
  `min_password_length` int NOT NULL,
  `require_number` bit(1) NOT NULL,
  `require_special_char` bit(1) NOT NULL,
  `require_uppercase` bit(1) NOT NULL,
  `session_timeout_min` int NOT NULL,
  `policy_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `last_updated_by` varchar(80) NOT NULL,
  PRIMARY KEY (`policy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_policy`
--

LOCK TABLES `password_policy` WRITE;
/*!40000 ALTER TABLE `password_policy` DISABLE KEYS */;
INSERT INTO `password_policy` VALUES (30,5,8,_binary '',_binary '',_binary '',60,1,NULL,'SYSTEM');
/*!40000 ALTER TABLE `password_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_token`
--

DROP TABLE IF EXISTS `password_reset_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_token` (
  `used` bit(1) NOT NULL,
  `expiry_date` datetime(6) NOT NULL,
  `token_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(100) NOT NULL,
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `UK_g0guo4k8krgpwuagos61oc06j` (`token`),
  KEY `FK1lrxqcemiojij5cb50rf1d7kg` (`user_id`),
  CONSTRAINT `FK1lrxqcemiojij5cb50rf1d7kg` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_token`
--

LOCK TABLES `password_reset_token` WRITE;
/*!40000 ALTER TABLE `password_reset_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `amount_paid` decimal(10,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `invoice_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `payment_date` datetime(6) NOT NULL,
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `refund_by` bigint DEFAULT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `refund_reason` text,
  `payment_method` enum('CASH','UPI','CARD') NOT NULL,
  `payment_status` enum('SUCCESS','FAILED','PENDING','REFUNDED') NOT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `FKsb24p8f52refbb80qwp4gem9n` (`invoice_id`),
  KEY `FK4pswry4r5sx6j57cdeulh1hx8` (`member_id`),
  KEY `FKon2d2h5t40gl4h7v7r4b338ru` (`refund_by`),
  CONSTRAINT `FK4pswry4r5sx6j57cdeulh1hx8` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKon2d2h5t40gl4h7v7r4b338ru` FOREIGN KEY (`refund_by`) REFERENCES `system_user` (`user_id`),
  CONSTRAINT `FKsb24p8f52refbb80qwp4gem9n` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (500.11,'2026-05-27 21:34:34.185963',2,16,'2026-05-27 21:34:34.171278',1,NULL,NULL,NULL,'CARD','SUCCESS'),(713.11,'2026-05-27 22:07:10.528735',2,16,'2026-05-27 22:07:10.496265',2,NULL,NULL,NULL,'CARD','SUCCESS'),(2948.82,'2026-05-27 22:07:40.648099',3,16,'2026-05-27 22:07:40.626790',3,NULL,NULL,NULL,'CARD','SUCCESS'),(17999.00,'2026-05-27 22:08:22.753392',1,16,'2026-05-27 22:08:22.733746',4,NULL,NULL,NULL,'CARD','SUCCESS');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan`
--

DROP TABLE IF EXISTS `plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan` (
  `plan_id` bigint NOT NULL AUTO_INCREMENT,
  `plan_name` varchar(120) NOT NULL,
  `duration_days` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `access_start` time NOT NULL,
  `access_end` time NOT NULL,
  `eligibility_type` enum('GENERAL','STUDENT','SENIOR','CORPORATE') NOT NULL,
  `proration_rule` varchar(50) DEFAULT NULL,
  `tax_percent` decimal(5,2) DEFAULT '0.00',
  `version` int DEFAULT '1',
  `effective_from` date NOT NULL,
  `branch_visibility` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `facilities_included` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan`
--

LOCK TABLES `plan` WRITE;
/*!40000 ALTER TABLE `plan` DISABLE KEYS */;
INSERT INTO `plan` VALUES (1,'Senior Wellness Monthly',30,799.00,'07:00:00','20:00:00','SENIOR','PRO_RATA',5.00,1,'2026-01-01','ALL',1,'2026-01-01 09:00:00','2026-01-01 09:00:00',NULL),(2,'Weekend Warrior',90,2499.00,'08:00:00','20:00:00','GENERAL','FULL',18.00,1,'2026-01-01','1,2,3',1,'2026-01-01 09:00:00','2026-01-01 09:00:00',NULL),(3,'Diamond Annual',365,17999.00,'04:00:00','23:59:00','GENERAL','FULL',18.00,1,'2026-04-01','ALL',1,'2026-04-01 09:00:00','2026-04-01 09:00:00',NULL),(4,'Family Duo Monthly',30,2499.00,'06:00:00','22:00:00','GENERAL','PRO_RATA',18.00,1,'2026-05-01','ALL',1,'2026-05-01 09:00:00','2026-05-01 09:00:00',NULL),(5,'Early Bird Quarterly',90,2999.00,'05:00:00','10:00:00','GENERAL','PRO_RATA',18.00,1,'2026-05-01','1,4,5',1,'2026-05-01 09:00:00','2026-05-01 09:00:00',NULL);
/*!40000 ALTER TABLE `plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_addon`
--

DROP TABLE IF EXISTS `plan_addon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_addon` (
  `is_included` bit(1) NOT NULL,
  `addon_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKolcaw3hgeuduef3iq4tgkejct` (`addon_id`),
  KEY `FKp6sxv16u0mxgk9f5iwj0p0n7r` (`plan_id`),
  CONSTRAINT `FKolcaw3hgeuduef3iq4tgkejct` FOREIGN KEY (`addon_id`) REFERENCES `add_on` (`addon_id`),
  CONSTRAINT `FKp6sxv16u0mxgk9f5iwj0p0n7r` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_addon`
--

LOCK TABLES `plan_addon` WRITE;
/*!40000 ALTER TABLE `plan_addon` DISABLE KEYS */;
/*!40000 ALTER TABLE `plan_addon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_facility`
--

DROP TABLE IF EXISTS `plan_facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_facility` (
  `plan_id` bigint NOT NULL,
  `facility_id` bigint NOT NULL,
  PRIMARY KEY (`plan_id`,`facility_id`),
  KEY `idx_plan` (`plan_id`),
  KEY `idx_facility` (`facility_id`),
  CONSTRAINT `FK_PLANFACILITY_FACILITY` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`facility_id`),
  CONSTRAINT `FK_PLANFACILITY_PLAN` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_facility`
--

LOCK TABLES `plan_facility` WRITE;
/*!40000 ALTER TABLE `plan_facility` DISABLE KEYS */;
INSERT INTO `plan_facility` VALUES (1,4),(1,6),(1,7),(2,1),(2,3),(2,6),(3,1),(3,2),(3,3),(3,4),(3,5),(3,6),(3,7),(4,3),(4,4),(4,7),(5,1),(5,2),(5,6);
/*!40000 ALTER TABLE `plan_facility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promo_code`
--

DROP TABLE IF EXISTS `promo_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promo_code` (
  `discount_value` decimal(10,2) NOT NULL,
  `expiry_date` date NOT NULL,
  `is_active` bit(1) NOT NULL,
  `per_member_limit` int NOT NULL,
  `usage_limit` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `promo_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `code` varchar(30) NOT NULL,
  `discount_type` enum('PERCENT','FLAT') NOT NULL,
  `eligibility` enum('ALL','NEW','RETURNING','CORPORATE','STUDENT') NOT NULL,
  PRIMARY KEY (`promo_id`),
  UNIQUE KEY `UK_fplc11dewa94eib758xs5mrg9` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promo_code`
--

LOCK TABLES `promo_code` WRITE;
/*!40000 ALTER TABLE `promo_code` DISABLE KEYS */;
INSERT INTO `promo_code` VALUES (15.00,'2026-06-03',_binary '',1,1,'2026-05-27 21:23:10.106703',1,'2026-05-27 21:23:10.106703','WELCOME8349','PERCENT','NEW');
/*!40000 ALTER TABLE `promo_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promo_code_usage`
--

DROP TABLE IF EXISTS `promo_code_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promo_code_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `promo_id` bigint NOT NULL,
  `used_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKalbqpub4sqm6fvprskwf2kcma` (`invoice_id`),
  KEY `FKjkrdcrh3kvuqpescd1c81yok1` (`member_id`),
  KEY `FKhncw6moktwwrk61c751inpc0u` (`promo_id`),
  CONSTRAINT `FKalbqpub4sqm6fvprskwf2kcma` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`),
  CONSTRAINT `FKhncw6moktwwrk61c751inpc0u` FOREIGN KEY (`promo_id`) REFERENCES `promo_code` (`promo_id`),
  CONSTRAINT `FKjkrdcrh3kvuqpescd1c81yok1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promo_code_usage`
--

LOCK TABLES `promo_code_usage` WRITE;
/*!40000 ALTER TABLE `promo_code_usage` DISABLE KEYS */;
/*!40000 ALTER TABLE `promo_code_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pt_session`
--

DROP TABLE IF EXISTS `pt_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pt_session` (
  `duration_mins` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `member_id` bigint NOT NULL,
  `scheduled_at` datetime(6) NOT NULL,
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `trainer_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `trainer_notes` text,
  `status` enum('REQUESTED','ACCEPTED','DECLINED','COMPLETED','CANCELLED','APPROVED') NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `FK8lriyy4rlf36fs6dvo86vfe2q` (`member_id`),
  KEY `FKenhuq5sl5dw7at4625eqc96nb` (`trainer_id`),
  CONSTRAINT `FK8lriyy4rlf36fs6dvo86vfe2q` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKenhuq5sl5dw7at4625eqc96nb` FOREIGN KEY (`trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pt_session`
--

LOCK TABLES `pt_session` WRITE;
/*!40000 ALTER TABLE `pt_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `pt_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt`
--

DROP TABLE IF EXISTS `receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt` (
  `amount` decimal(10,2) NOT NULL,
  `tax_amount` decimal(10,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `invoice_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `payment_id` bigint NOT NULL,
  `receipt_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `receipt_number` varchar(40) NOT NULL,
  `notes` text,
  `status` enum('ISSUED','EMAILED','PRINTED') NOT NULL,
  PRIMARY KEY (`receipt_id`),
  UNIQUE KEY `UK_tbqak8g0j5h1f1bbonfylcc06` (`receipt_number`),
  KEY `FK4rljok7h7j2x9v0p25bawd9d0` (`invoice_id`),
  KEY `FK2s7sws87bynqjgo121cgaqas5` (`member_id`),
  KEY `FKol02cxv1xmyc8j9o2npop6bs5` (`payment_id`),
  CONSTRAINT `FK2s7sws87bynqjgo121cgaqas5` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK4rljok7h7j2x9v0p25bawd9d0` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`),
  CONSTRAINT `FKol02cxv1xmyc8j9o2npop6bs5` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt`
--

LOCK TABLES `receipt` WRITE;
/*!40000 ALTER TABLE `receipt` DISABLE KEYS */;
INSERT INTO `receipt` VALUES (500.11,23.82,500.11,'2026-05-27 21:34:34.234828',2,16,1,1,'2026-05-27 21:34:34.234828','RCP-B59863B9',NULL,'ISSUED'),(713.11,33.96,713.11,'2026-05-27 22:07:10.558342',2,16,2,2,'2026-05-27 22:07:10.558342','RCP-A14F335C',NULL,'ISSUED'),(2948.82,449.82,2948.82,'2026-05-27 22:07:40.667787',3,16,3,3,'2026-05-27 22:07:40.667787','RCP-B4F001CE',NULL,'ISSUED'),(17999.00,0.00,17999.00,'2026-05-27 22:08:22.777311',1,16,4,4,'2026-05-27 22:08:22.777311','RCP-C6808CC7',NULL,'ISSUED');
/*!40000 ALTER TABLE `receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_user`
--

DROP TABLE IF EXISTS `system_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(80) NOT NULL,
  `full_name` varchar(150) DEFAULT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('MEMBER','ADMIN','FRONT_DESK','TRAINER','MANAGER') NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `failed_attempts` int DEFAULT '0',
  `locked_until` datetime DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `branch_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `UK_74y7xiqrvp39wycn0ron4xq4h` (`username`),
  UNIQUE KEY `UK_3ypdb9457wfdya51dfk3ul642` (`email`),
  KEY `FKlf0nkyfnp53boyaxsrc2dksmy` (`branch_id`),
  CONSTRAINT `FKlf0nkyfnp53boyaxsrc2dksmy` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_user`
--

LOCK TABLES `system_user` WRITE;
/*!40000 ALTER TABLE `system_user` DISABLE KEYS */;
INSERT INTO `system_user` VALUES (1,'trainer4','Sneha Pilates','trainer4@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','TRAINER',1,0,NULL,'2026-05-27 21:55:05','2026-01-15 09:00:00','2026-05-27 21:55:05',5),(2,'trainer5','Rahul Aqua','trainer5@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','TRAINER',1,0,NULL,'2026-05-25 04:21:05','2026-02-01 09:00:00','2026-05-25 04:29:34',5),(3,'trainer6','Kavya Zumba','trainer6@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','TRAINER',1,0,NULL,'2026-05-12 10:00:00','2026-03-01 10:00:00','2026-05-25 01:10:40',2),(4,'trainer7','Suresh Boxing','trainer7@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','TRAINER',1,0,NULL,'2026-05-09 19:00:00','2026-04-01 08:00:00','2026-05-25 01:10:40',3),(5,'manager2','Divya Operations','manager2@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MANAGER',1,0,NULL,'2026-05-27 22:03:44','2026-01-01 08:00:00','2026-05-27 22:03:43',5),(6,'member16','Kiran Bhat','member16@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MEMBER',1,0,NULL,'2026-05-18 07:00:00','2026-03-10 10:00:00','2026-05-25 01:10:40',1),(7,'member17','Anjali Desai','member17@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MEMBER',1,0,NULL,'2026-05-25 04:09:39','2026-03-15 11:00:00','2026-05-25 04:09:39',2),(8,'member18','Rohan Kulkarni','member18@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MEMBER',1,0,NULL,'2026-05-17 06:45:00','2026-04-01 09:00:00','2026-05-25 01:10:40',3),(9,'member19','Meena Iyer','member19@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MEMBER',1,0,NULL,'2026-05-16 09:00:00','2026-04-10 08:30:00','2026-05-25 01:10:40',4),(10,'member20','Tarun Goswami','member20@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','MEMBER',1,0,NULL,'2026-05-15 18:00:00','2026-05-01 10:00:00','2026-05-25 01:10:40',5),(11,'admin1','Arjun Verma','admin1@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','ADMIN',1,0,NULL,'2026-05-27 21:59:41','2026-01-01 08:00:00','2026-05-27 21:59:40',NULL),(12,'frontdesk1','Pooja Sharma','frontdesk1@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','FRONT_DESK',1,0,NULL,'2026-05-25 07:28:08','2026-02-01 09:00:00','2026-05-25 07:28:08',2),(13,'frontdesk2','Rahul Nair','frontdesk2@fitness.com','$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6','FRONT_DESK',1,0,NULL,'2026-05-18 08:15:00','2026-03-01 09:00:00','2026-05-25 01:10:40',3),(14,'ashokverma','Ashok Verma','ashokverma@gmail.com','$2a$10$VnyXJmqHOnPqRqcV4ChI4ud1GhDcGI7LyHq7XHTfcBUFGXOiQc55i','MEMBER',1,0,NULL,NULL,'2026-05-24 23:06:01','2026-05-24 23:51:29',5),(15,'ragav','Raghav','ragav@gmail.com','$2a$10$dtD8j2xRzBUX7iN39FXcY.eWHblkrzMcjrDN81ZfIsDOc1WumQI32','MEMBER',1,0,NULL,'2026-05-24 23:11:41','2026-05-24 23:08:48','2026-05-24 23:51:29',5),(16,'rachinrao123','Rachin Rao','rachinrao123@gmail.com','$2a$10$Wc03Kz3QyDSemBzKohbZ.uhCtMjxcfrC38W9vyNhWoR/6pfsc.7se','FRONT_DESK',1,0,NULL,'2026-05-25 00:38:13','2026-05-24 23:28:01','2026-05-25 00:38:13',5),(17,'meera','Meera','meera@gmail.com','$2a$10$VxU2LZqCDi8cU65K9Kiur.TNoWUpDgfZhk7nL/UShYCGAUCE3uwfW','MEMBER',1,0,NULL,'2026-05-25 04:21:59','2026-05-25 00:57:53','2026-05-25 04:21:58',5),(18,'kriti','Kriti','kriti@gmail.com','$2a$10$McZZG3JJW.KFc2iPOO66Kuf5NCgJeLEKC6MDSzN06pC2ay8ztG6IC','MEMBER',1,0,NULL,NULL,'2026-05-25 01:14:21','2026-05-25 01:21:05',5),(19,'arjun.mehta','Arjun Mehta','arjun.mehta@gmail.com','$2a$10$k1cYhtv55uO5gROgc0SLu.dkuqKIafvLzdWjRL3bniZCnuMabJyoS','MEMBER',1,0,NULL,NULL,'2026-05-25 03:48:13','2026-05-25 03:48:13',1),(20,'sneha.reddy','Sneha Reddy','sneha.reddy@gmail.com','$2a$10$k6Z7Uh0je/e0.jPvFK4N.OPY/yB6egNAUx30Xw/pRIeoeHbkWMPSS','MEMBER',1,0,NULL,NULL,'2026-05-25 03:48:13','2026-05-25 03:48:13',1),(21,'rahul.sharma','Rahul Sharma','rahul.sharma@gmail.com','$2a$10$GcmPF/KOoaxiEII3NXrIauR4eTXKguFedS7Qiv5UMo2UYqFijbXM2','MEMBER',1,0,NULL,NULL,'2026-05-25 03:48:13','2026-05-25 03:48:13',2),(22,'siri01','Siri','siri01@gmail.com','$2a$10$UCKmb6PK/cKEImp1j5X.VOV.9IwGijSjbyRiYGFxQ8NifEqEthwfu','MEMBER',1,0,NULL,'2026-05-25 04:51:52','2026-05-25 04:50:19','2026-05-25 04:51:52',5),(23,'amanchauhan10122004','Aman Kumar Chauhan','amanchauhan10122004@gmail.com','$2a$10$8BjNNq7rWTnqoWJ7n.WU7.4dgSOz72yFsiKdYZeoZh9k6gVuKj7/O','MEMBER',1,0,NULL,NULL,'2026-05-25 06:36:47','2026-05-25 06:36:47',5),(24,'amankumarchauhan.1si22cs015','Rahul Chauhan','amankumarchauhan.1si22cs015@gmail.com','$2a$10$hmJvh/FihLyfulMxDI2F7ekRwjKMLPWEGvhO1MRIDjPIPBiM2LqRW','MEMBER',1,0,NULL,'2026-05-27 21:25:41','2026-05-25 07:29:40','2026-05-27 21:25:40',5),(26,'bharat','Bharat','bharat@gmail.com','$2a$10$nYxAgfQrH7inWwvCbxPU/eoXoXQBmhwMajtnUrBOM6DSNIxSBBOES','FRONT_DESK',1,0,NULL,'2026-05-27 21:26:25','2026-05-27 08:47:03','2026-05-27 21:26:25',5),(27,'venkatesh01','Venkatesh Kumar','venkatesh01@fitness.com','$2a$10$EP9IwLqMqhRxw3XF/GbETOxgR9AHQKGDA7QAmYcz7tyUApLl3UNIK','FRONT_DESK',1,0,NULL,NULL,'2026-05-27 21:08:03','2026-05-27 21:08:03',5),(28,'pavanmj01','Pavan Yadav','pavanmj01@gmail.com','$2a$10$vBPUSA8wjAyCrELgkPuHteMVLIqyL1dlgrap9WizkPbcx96T6SsIC','FRONT_DESK',1,0,NULL,'2026-05-27 21:20:01','2026-05-27 21:16:09','2026-05-27 21:27:32',5),(29,'lilemox724','Hritik','lilemox724@bittnex.com','$2a$10$1k3Y0rjxttn0mXwonalAsuFaDEU1aSGQZ7uFs9pECtEAvsTTp4FIK','MEMBER',1,0,NULL,'2026-05-27 22:05:50','2026-05-27 21:23:10','2026-05-27 22:05:50',5),(30,'ajay01','Ajay Mehta','ajay01@gmail.com','$2a$10$7OiTGwBzWo4rP5g8Rtg3R.bCpvJ8ECf.wW3iaEUHIjFa94XA6s7Ku','TRAINER',1,0,NULL,'2026-05-27 22:01:10','2026-05-27 22:00:46','2026-05-27 22:01:10',5);
/*!40000 ALTER TABLE `system_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_user_branches`
--

DROP TABLE IF EXISTS `system_user_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_user_branches` (
  `user_id` bigint NOT NULL,
  `branch_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`branch_id`),
  KEY `idx_system_user_branches_branch` (`branch_id`),
  CONSTRAINT `fk_system_user_branches_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `fk_system_user_branches_user` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_user_branches`
--

LOCK TABLES `system_user_branches` WRITE;
/*!40000 ALTER TABLE `system_user_branches` DISABLE KEYS */;
INSERT INTO `system_user_branches` (`user_id`, `branch_id`)
SELECT `user_id`, `branch_id` FROM `system_user` WHERE `branch_id` IS NOT NULL;
/*!40000 ALTER TABLE `system_user_branches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer`
--

DROP TABLE IF EXISTS `trainer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer` (
  `accepting_pt_clients` bit(1) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `rating` double NOT NULL,
  `branch_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `trainer_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `availability` text,
  `bio` text,
  `certifications` varchar(255) DEFAULT NULL,
  `specialties` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`trainer_id`),
  UNIQUE KEY `UK_geuofcxp00v2rcu9fj9g7olue` (`user_id`),
  KEY `FK9g1upx79ns59tdoka8s8r8k14` (`branch_id`),
  CONSTRAINT `FK9g1upx79ns59tdoka8s8r8k14` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `FKn3v2igf6xyrp9it8ehr1s6q7y` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer`
--

LOCK TABLES `trainer` WRITE;
/*!40000 ALTER TABLE `trainer` DISABLE KEYS */;
INSERT INTO `trainer` VALUES (_binary '',_binary '',5,5,'2026-05-27 22:00:46.678542',1,'2026-05-27 22:03:15.111239',30,'MONDAY-10:00 AM, TUESDAY-10:00 AM, WEDNESDAY-10:00 AM, THURSDAY-10:00 AM, FRIDAY-10:00 AM, SATURDAY-10:00 AM, SUNDAY-10:00 AM','New trainer ready to help you achieve your goals.','NASM-CT (Pending Manager Verification)','General Fitness');
/*!40000 ALTER TABLE `trainer` ENABLE KEYS */;
UNLOCK TABLES;
 
--
-- Schema compatibility patch
-- (Keep dumps aligned with JPA entity mappings when ddl-auto=none)
--

CREATE TABLE IF NOT EXISTS `system_config` (
  `config_key` varchar(255) NOT NULL,
  `config_value` varchar(255) NOT NULL,
  `version` int NOT NULL DEFAULT 1,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `branch` ADD COLUMN IF NOT EXISTS `branch_code` varchar(20) DEFAULT NULL;
ALTER TABLE `member` ADD COLUMN IF NOT EXISTS `my_referral_code` varchar(30) DEFAULT NULL;
ALTER TABLE `member` ADD COLUMN IF NOT EXISTS `wallet_balance` decimal(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE `notification` ADD COLUMN IF NOT EXISTS `deep_link` varchar(500) DEFAULT NULL;
ALTER TABLE `invoice` ADD COLUMN IF NOT EXISTS `wallet_credit_applied` decimal(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE `classes` MODIFY COLUMN `weekdays` varchar(100) NOT NULL;
CREATE TABLE IF NOT EXISTS `system_user_branches` (
  `user_id` bigint NOT NULL,
  `branch_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`branch_id`),
  KEY `idx_system_user_branches_branch` (`branch_id`),
  CONSTRAINT `fk_system_user_branches_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`branch_id`),
  CONSTRAINT `fk_system_user_branches_user` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT IGNORE INTO `system_user_branches` (`user_id`, `branch_id`)
SELECT `user_id`, `branch_id` FROM `system_user` WHERE `branch_id` IS NOT NULL;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-28 14:48:21
