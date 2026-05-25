DROP DATABASE IF EXISTS fitness_db;
CREATE DATABASE fitness_db;
USE fitness_db;



CREATE TABLE BRANCH (
  branch_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  branch_name VARCHAR(120) NOT NULL,
  address TEXT NOT NULL,
  contact VARCHAR(20) NOT NULL,
  op_hours VARCHAR(100) NOT NULL,
  timezone VARCHAR(60) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE SYSTEM_USER (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    full_name VARCHAR(150),
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    last_login DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP,
    branch_id BIGINT,
    
    CONSTRAINT fk_system_user_branch
        FOREIGN KEY (branch_id)
        REFERENCES branch(branch_id)
);

CREATE TABLE PLAN (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_name VARCHAR(120) NOT NULL,
    duration_days INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    access_start TIME NOT NULL,
    access_end TIME NOT NULL,
    eligibility_type VARCHAR(50) NOT NULL,
    proration_rule VARCHAR(50) NULL,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    version INT DEFAULT 1,
    effective_from DATE NOT NULL,
    branch_visibility VARCHAR(255) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE FACILITY (
  facility_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_name VARCHAR(120) NOT NULL,
  branch_id BIGINT NOT NULL,
  capacity INT NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  under_maintenance BOOLEAN DEFAULT FALSE,
  maintenance_reason TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_facility_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);


CREATE TABLE ADD_ON(
  addon_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  addon_name VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  capacity INT NULL,
  addon_type VARCHAR(30) NOT NULL,
  tax_percent DECIMAL(5,2) DEFAULT 0,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
); 


INSERT INTO PROMO_CODE
(code, discount_type, discount_value, expiry_date, usage_limit, per_member_limit, eligibility, is_active)
VALUES
('SENIOR5', 'PERCENT', 5.00, '2026-12-31', 100, 1, 'SENIOR', TRUE),

('WELCOME300', 'FLAT', 300.00, '2026-09-30', 150, 1, 'ALL', TRUE),

('DIAMOND10', 'PERCENT', 10.00, '2026-12-31', 40, 1, 'ALL', TRUE),

('FAMILY200', 'FLAT', 200.00, '2026-12-31', 200, 2, 'ALL', TRUE),

('EARLYBIRD', 'PERCENT', 12.00, '2026-07-31', 300, 1, 'ALL', TRUE),

('CORP-INFY', 'PERCENTAGE', 15.00, '2030-12-31', 1000, 1, 'CORPORATE', TRUE),

('CORP-WIPRO', 'PERCENTAGE', 15.00, '2030-12-31', 1000, 1, 'CORPORATE', TRUE),

('CORP-TCS', 'PERCENTAGE', 15.00, '2030-12-31', 1000, 1, 'CORPORATE', TRUE),

('WELCOME7446', 'PERCENT', 15.00, '2026-06-01', 1, 1, 'NEW', TRUE);


CREATE TABLE TRAINER(
  trainer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  bio TEXT,
  certifications VARCHAR(255),
  specialties VARCHAR(255),
  rating DECIMAL(3,2) DEFAULT 0,
  branch_id BIGINT NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_trainer_user FOREIGN KEY (user_id) REFERENCES system_user(user_id),
  CONSTRAINT fk_trainer_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

create table MEMBER (
    member_id bigint primary key auto_increment,
    user_id bigint not null unique,
    mem_name varchar(120) not null,
    email varchar(150) not null unique,
    phone varchar(20) not null unique,
    dob date not null,
    address text not null,
    emg_contact varchar(120) not null,
    emg_phone varchar(20) not null,
    referral_code varchar(30),
    corporate_code varchar(30),
    status varchar(50) not null,
    home_branch_id bigint not null,
    photo_path varchar(255),
    notes text,
    created_by bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key(user_id) references SYSTEM_USER(user_id),
    foreign key(home_branch_id) references BRANCH(branch_id),
    foreign key(created_by) references SYSTEM_USER(user_id)
);

CREATE TABLE PLAN_FACILITY (
    plan_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    PRIMARY KEY (plan_id, facility_id),
    CONSTRAINT FK_PLANFACILITY_PLAN FOREIGN KEY (plan_id) REFERENCES PLAN(plan_id),
    CONSTRAINT FK_PLANFACILITY_FACILITY FOREIGN KEY (facility_id) REFERENCES FACILITY(facility_id),
    INDEX idx_plan (plan_id),
    INDEX idx_facility (facility_id)
);


create table CLASSES(
class_id bigint auto_increment primary key,
class_name varchar(120) not null,
trainer_id bigint not null,
room_id bigint not null,
branch_id bigint not null,
start_date date not null,
end_date date not null,
weekdays varchar(20) not null,
class_time time not null,
duration_mins int not null,
capacity int not null,
prerequisites text null,
plan_eligibility varchar(255) null,
status varchar(20) default 'active',
cancel_reason text null,
created_at datetime not null default current_timestamp,
updated_at datetime not null default current_timestamp on update current_timestamp,
foreign key (trainer_id) references trainer(trainer_id),
foreign key (room_id) references facility(facility_id),
foreign key (branch_id) references branch(branch_id)
);

create table MEMBERSHIP(
    mem_id bigint primary key auto_increment,
    member_id bigint not null,
    plan_id bigint not null,
    start_date date not null,
    end_date date not null,
    status varchar(50) not null,
    duration int not null,
    price decimal(10,2) not null,
    discount_amount decimal(10,2) default 0,
    promo_code_used varchar(30),
    branch_id bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key(member_id) references MEMBER(member_id),
    foreign key(plan_id) references PLAN(plan_id),
    foreign key(branch_id) references BRANCH(branch_id)
);

CREATE TABLE INVOICE (
    invoice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    membership_id BIGINT NULL,
    mrp DECIMAL(10,2) NOT NULL,
    taxes DECIMAL(10,2) DEFAULT 0,
    discount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    outstanding DECIMAL(10,2) DEFAULT 0,
    promo_code VARCHAR(30) NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_INVOICE_MEMBER FOREIGN KEY (member_id) REFERENCES MEMBER(member_id),
    CONSTRAINT FK_INVOICE_MEMBERSHIP FOREIGN KEY (membership_id) REFERENCES MEMBERSHIP(mem_id),
    CONSTRAINT CHK_OUTSTANDING CHECK (outstanding = final_amount - paid_amount)
);


CREATE TABLE PAYMENT (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(250) NULL,
    refund_by BIGINT NULL,
    refund_reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_PAYMENT_INVOICE FOREIGN KEY (invoice_id) REFERENCES INVOICE(invoice_id),
    CONSTRAINT FK_PAYMENT_MEMBER FOREIGN KEY (member_id) REFERENCES MEMBER(member_id),
    CONSTRAINT FK_PAYMENT_REFUND_BY FOREIGN KEY (refund_by) REFERENCES SYSTEM_USER(user_id)
);

create table CLASS_BOOKING(
    booking_id bigint primary key auto_increment,
    class_id bigint not null,
    member_id bigint not null,
    booking_status varchar(50) not null,
    waitlist_position int,
    cancelled_at datetime,
    override_by bigint,
    override_reason text,
    created_at datetime not null default current_timestamp,
    unique key uk_class_member (class_id, member_id),
    foreign key (class_id) references CLASSES(class_id),
    foreign key (member_id) references MEMBER(member_id),
    foreign key (override_by) references SYSTEM_USER(user_id)
);

create table PT_SESSION(
session_id bigint auto_increment primary key,
member_id bigint not null,
trainer_id bigint not null,
scheduled_at datetime not null,
duration_mins int not null,
status varchar(20) not null,
trainer_notes text null,
created_at datetime not null default current_timestamp,
updated_at datetime not null default current_timestamp on update current_timestamp,
foreign key(member_id) references member(member_id),
foreign key (trainer_id) references trainer(trainer_id)
);


create table ATTENDANCE(
log_id bigint auto_increment primary key,
member_id bigint not null,
branch_id bigint not null,
check_in_time datetime not null,
check_out_time datetime null,
alert_flag boolean default false,
scan_method varchar(20) not null,
sync_status varchar(20) not null,
class_id bigint null,
override_by bigint null,
override_reason text null,
created_at datetime not null default current_timestamp,
foreign key (member_id) references member(member_id),
foreign key (branch_id) references branch(branch_id),
foreign key (class_id) references classes(class_id),
foreign key (override_by) references system_user(user_id)
);

create table HEALTH_CONSENT(
consent_id bigint auto_increment primary key,
member_id bigint not null,
form_version varchar(20) not null,
parq_responses text null,
medical_acknowledged boolean not null default false,
liability_acknowledged boolean not null default false,
privacy_acknowledged boolean not null default false,
acknowledged_at datetime not null,
expires_at datetime null,
ip_address varchar(45) not null,
status varchar(20) not null,
staff_notes text null,
created_at datetime not null default current_timestamp,
updated_at datetime not null default current_timestamp on update current_timestamp,
foreign key (member_id) references member(member_id)
);


CREATE TABLE NOTIFICATION (
  notif_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(40) NOT NULL,
  channel VARCHAR(30) NOT NULL,
  title VARCHAR(200) NOT NULL,
  body TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  delivery_status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES system_user(user_id)
);



CREATE TABLE AUDIT_LOG(
  audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  performed_by BIGINT NOT NULL,
  entity_name VARCHAR(60) NOT NULL,
  entity_id BIGINT NOT NULL,
  action VARCHAR(30) NOT NULL,
  old_value JSON,
  new_value JSON,
  created_at DATETIME NOT NULL,

  CONSTRAINT fk_audit_user FOREIGN KEY (performed_by) REFERENCES system_user(user_id)
);

CREATE TABLE FEATURE_FLAG (
  flag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  flag_name VARCHAR(100) NOT NULL UNIQUE,
  is_enabled BOOLEAN DEFAULT FALSE,
  last_modified_by VARCHAR(80),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- ============================================================
--  FITNESS MEMBERSHIP MANAGEMENT SYSTEM — NEW INSERT BATCH
--  Extends existing data (IDs 16+). All FKs reference valid
--  existing PKs or new PKs introduced in this same script.
--  No NULL values used. Runs after the original seed script.
-- ============================================================
 
-- ----------------------------------------------------------------
-- 1. SYSTEM_USER  (IDs 16–25: 4 trainers, 1 manager, 5 members)
-- ----------------------------------------------------------------

INSERT INTO SYSTEM_USER
  (user_id, username, full_name, email, password_hash, role, active, failed_attempts, locked_until, last_login, created_at, updated_at)
VALUES
(1, 'trainer4',   'Sneha Pilates',       'trainer4@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'TRAINER', 1, 0, '2026-01-01 00:00:00', '2026-05-10 08:00:00', '2026-01-15 09:00:00', '2026-05-10 08:00:00'),

(2, 'trainer5',   'Rahul Aqua',          'trainer5@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'TRAINER', 1, 0, '2026-01-01 00:00:00', '2026-05-11 07:30:00', '2026-02-01 09:00:00', '2026-05-11 07:30:00'),

(3, 'trainer6',   'Kavya Zumba',         'trainer6@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'TRAINER', 1, 0, '2026-01-01 00:00:00', '2026-05-12 10:00:00', '2026-03-01 10:00:00', '2026-05-12 10:00:00'),

(4, 'trainer7',   'Suresh Boxing',       'trainer7@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'TRAINER', 1, 0, '2026-01-01 00:00:00', '2026-05-09 19:00:00', '2026-04-01 08:00:00', '2026-05-09 19:00:00'),

(5, 'manager2',   'Divya Operations',    'manager2@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MANAGER', 1, 0, '2026-01-01 00:00:00', '2026-05-20 09:15:00', '2026-01-01 08:00:00', '2026-05-20 09:15:00'),

(6, 'member16',   'Kiran Bhat',          'member16@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MEMBER', 1, 0, '2026-01-01 00:00:00', '2026-05-18 07:00:00', '2026-03-10 10:00:00', '2026-05-18 07:00:00'),

(7, 'member17',   'Anjali Desai',        'member17@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MEMBER', 1, 0, '2026-01-01 00:00:00', '2026-05-19 08:30:00', '2026-03-15 11:00:00', '2026-05-19 08:30:00'),

(8, 'member18',   'Rohan Kulkarni',      'member18@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MEMBER', 1, 0, '2026-01-01 00:00:00', '2026-05-17 06:45:00', '2026-04-01 09:00:00', '2026-05-17 06:45:00'),

(9, 'member19',   'Meena Iyer',          'member19@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MEMBER', 1, 0, '2026-01-01 00:00:00', '2026-05-16 09:00:00', '2026-04-10 08:30:00', '2026-05-16 09:00:00'),

(10, 'member20',  'Tarun Goswami',       'member20@fitness.com',   '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6', 'MEMBER', 1, 0, '2026-01-01 00:00:00', '2026-05-15 18:00:00', '2026-05-01 10:00:00', '2026-05-15 18:00:00'),

(11, 'admin1', 'Arjun Verma',
 'admin1@fitness.com',
 '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6',
 'ADMIN',
 1, 0,
 '2026-01-01 00:00:00',
 '2026-05-20 08:00:00',
 '2026-01-01 08:00:00',
 '2026-05-20 08:00:00'),

(12, 'frontdesk1', 'Pooja Sharma',
 'frontdesk1@fitness.com',
 '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6',
 'FRONT_DESK',
 1, 0,
 '2026-01-01 00:00:00',
 '2026-05-19 07:30:00',
 '2026-02-01 09:00:00',
 '2026-05-19 07:30:00'),

(13, 'frontdesk2', 'Rahul Nair',
 'frontdesk2@fitness.com',
 '$2a$12$xPo16zbpRRn8QeWdZ.Bnd.N5E9WAjPrNVbGU.3uA1bKUrMEOoYLu6',
 'FRONT_DESK',
 1, 0,
 '2026-01-01 00:00:00',
 '2026-05-18 08:15:00',
 '2026-03-01 09:00:00',
 '2026-05-18 08:15:00');
-- ----------------------------------------------------------------
-- 2. BRANCH  (IDs 4–6)
-- ----------------------------------------------------------------
INSERT INTO BRANCH
  (branch_id, branch_name, address, contact, op_hours, timezone, is_active, created_at)
VALUES
(1, 'East End Fitness',     '22 Marathahalli Bridge Road, Bengaluru, Karnataka', '+91-9876543213', '06:00 - 22:00', 'Asia/Kolkata', 1, '2026-01-01 08:00:00'),

(2, 'West Wing Wellness',   '10 Rajajinagar Main Road, Bengaluru, Karnataka',    '+91-9876543214', '05:30 - 23:00', 'Asia/Kolkata', 1, '2026-01-01 08:00:00'),

(3, 'Central Power Zone',   '3 Cubbon Park Road, Bengaluru, Karnataka',          '+91-9876543215', '06:00 - 21:30', 'Asia/Kolkata', 1, '2026-02-01 08:00:00'),

(4, 'Tech Park Gym',        '77 Electronic City Phase 1, Bengaluru, Karnataka',  '+91-9876543216', '05:00 - 23:00', 'Asia/Kolkata', 1, '2026-03-01 08:00:00'),

(5, 'Lakeside Health Club', '5 Ulsoor Lake Road, Bengaluru, Karnataka',          '+91-9876543217', '06:30 - 21:00', 'Asia/Kolkata', 1, '2026-03-15 08:00:00');
 
-- ----------------------------------------------------------------
-- 3. PLAN  (IDs 6–10)
-- ----------------------------------------------------------------
INSERT INTO PLAN
  (plan_id, plan_name, duration_days, price, access_start, access_end, eligibility_type, proration_rule, tax_percent, version, effective_from, branch_visibility, is_active, created_at, updated_at)
VALUES
(1, 'Senior Wellness Monthly', 30, 799.00, '07:00:00', '20:00:00', 'SENIOR', 'PRO_RATA', 5.00, 1, '2026-01-01', 'ALL', 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(2, 'Weekend Warrior', 90, 2499.00, '08:00:00', '20:00:00', 'GENERAL', 'FULL', 18.00, 1, '2026-01-01', '1,2,3', 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(3, 'Diamond Annual', 365, 17999.00, '04:00:00', '23:59:00', 'GENERAL', 'FULL', 18.00, 1, '2026-04-01', 'ALL', 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),

(4, 'Family Duo Monthly', 30, 2499.00, '06:00:00', '22:00:00', 'GENERAL', 'PRO_RATA', 18.00, 1, '2026-05-01', 'ALL', 1, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),

(5, 'Early Bird Quarterly', 90, 2999.00, '05:00:00', '10:00:00', 'GENERAL', 'PRO_RATA', 18.00, 1, '2026-05-01', '1,4,5', 1, '2026-05-01 09:00:00', '2026-05-01 09:00:00');
 
-- ----------------------------------------------------------------
-- 4. FACILITY  (IDs 9–15)
-- ----------------------------------------------------------------
INSERT INTO FACILITY
  (facility_id, facility_name, branch_id, capacity, is_active, under_maintenance, maintenance_reason, created_at, updated_at)
VALUES
(1, 'Aqua Pool',       1, 25, 1, 0, 'No current maintenance', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(2, 'Crossfit Arena',  1, 20, 1, 0, 'No current maintenance', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(3, 'Zumba Studio',    2, 30, 1, 0, 'No current maintenance', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(4, 'Pilates Room',    2, 16, 1, 0, 'No current maintenance', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(5, 'Power Cage Area', 3, 18, 1, 1, 'Cable machine under repair — est. back 2026-06-01', '2026-02-01 09:00:00', '2026-05-15 10:00:00'),

(6, 'Cardio Deck',     4, 35, 1, 0, 'No current maintenance', '2026-03-01 09:00:00', '2026-03-01 09:00:00'),

(7, 'Wellness Studio', 5, 20, 1, 0, 'No current maintenance', '2026-03-15 09:00:00', '2026-03-15 09:00:00');
 
-- ----------------------------------------------------------------
-- 5. ADD_ON  (IDs 7–11)
-- ----------------------------------------------------------------
INSERT INTO ADD_ON
  (addon_id, addon_name, price, capacity, addon_type, tax_percent, is_active, created_at, updated_at)
VALUES
(1, 'Swimming Pool Access (Monthly)', 800.00, 25, 'FACILITY', 18.00, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(2, 'Sauna & Steam (Monthly)', 600.00, 10, 'FACILITY', 5.00, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(3, 'Diet Meal Plan (Monthly)', 2500.00, 50, 'SERVICE', 18.00, 1, '2026-02-01 09:00:00', '2026-02-01 09:00:00'),

(4, 'PT Package (20 Sessions)', 12000.00, 10, 'SERVICE', 18.00, 1, '2026-03-01 09:00:00', '2026-03-01 09:00:00'),

(5, 'Guest Day Pass (Per Visit)', 350.00, 30, 'SERVICE', 18.00, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00');
 
-- ----------------------------------------------------------------
-- 6. PROMO_CODE  (IDs 6–10)
-- ----------------------------------------------------------------
INSERT INTO PROMO_CODE
  (promo_id, code, discount_type, discount_value, expiry_date, usage_limit, per_member_limit, eligibility, is_active, created_at, updated_at)
VALUES
(1, 'SENIOR5', 'PERCENT', 5.00, '2026-12-31', 100, 1, 'SENIOR', 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),

(2, 'WELCOME300', 'FLAT', 300.00, '2026-09-30', 150, 1, 'ALL', 1, '2026-02-01 09:00:00', '2026-02-01 09:00:00'),

(3, 'DIAMOND10', 'PERCENT', 10.00, '2026-12-31', 40, 1, 'ALL', 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),

(4, 'FAMILY200', 'FLAT', 200.00, '2026-12-31', 200, 2, 'ALL', 1, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),

(5, 'EARLYBIRD', 'PERCENT', 12.00, '2026-07-31', 300, 1, 'ALL', 1, '2026-05-01 09:00:00', '2026-05-01 09:00:00');
 
-- ----------------------------------------------------------------
-- 7. TRAINER  (IDs 4–7 — reference new SYSTEM_USER 16–19 and
--              branches 2, 3, 4, 5)
-- ----------------------------------------------------------------
INSERT INTO trainer
(trainer_id, user_id, bio, certifications, specialties, rating,
 branch_id, is_active, created_at, updated_at,
 accepting_pt_clients, availability)
VALUES

(1, 1,
'Certified Pilates instructor with 6 years of studio experience.',
'STOTT Pilates Certified, FMS Level 1',
'Pilates, Posture Correction, Core Strength',
4.75,
5,
1,
'2026-01-15 09:00:00',
'2026-05-25 02:45:53',
1,
'WEDNESDAY-08:00 AM, FRIDAY-10:00 AM'),

(2, 2,
'Aquatic fitness coach specialised in rehabilitation and lap swimming.',
'AEA Certified, Lifeguard Pro, NASM CPT',
'Aqua Fitness, Swimming, Rehab',
4.65,
5,
1,
'2026-02-01 09:00:00',
'2026-05-25 04:32:28',
1,
NULL),

(3, 3,
'High-energy Zumba and Latin dance fitness instructor.',
'Zumba B1, ZIN Instructor, ACE Group Fitness',
'Zumba, Dance Fitness, Aerobics',
4.85,
2,
1,
'2026-03-01 09:00:00',
'2026-03-01 09:00:00',
0,
NULL),

(4, 4,
'Former amateur boxer with expertise in functional strength and combat.',
'WBC Trainer Level 2, NSCA CSCS',
'Boxing, Functional Strength, Agility',
4.60,
3,
1,
'2026-04-01 09:00:00',
'2026-04-01 09:00:00',
0,
NULL);
 
-- ----------------------------------------------------------------
-- 8. MEMBER  (IDs 9–13 — reference new SYSTEM_USER 21–25,
--             branches 4/5/6/7/8, created_by staff 2 or 12)
-- ----------------------------------------------------------------
INSERT INTO MEMBER
  (member_id, user_id, mem_name, email, phone, dob, address, emg_contact, emg_phone, referral_code, corporate_code, status, home_branch_id, photo_path, notes, created_by, created_at, updated_at)
VALUES
(1, 6, 'Kiran Bhat', 'member16@fitness.com', '9845012301', '1988-04-14',
 '9 Richmond Road, Bengaluru, Karnataka',
 'Suma Bhat', '9845012302',
 'REF1003', 'CORP200', 'ACTIVE',
 1, 'photos/member16.jpg',
 'Interested in aqua fitness program',
 5, '2026-03-10 10:00:00', '2026-03-10 10:00:00'),

(2, 7, 'Anjali Desai', 'member17@fitness.com', '9845012303', '1995-07-22',
 '14 Cunningham Road, Bengaluru, Karnataka',
 'Prakash Desai', '9845012304',
 'REF1004', 'NONE', 'ACTIVE',
 2, 'photos/member17.jpg',
 'Prefers early morning Pilates',
 5, '2026-03-15 11:00:00', '2026-03-15 11:00:00'),

(3, 8, 'Rohan Kulkarni', 'member18@fitness.com', '9845012305', '1993-11-09',
 '28 Bannerghatta Road, Bengaluru, Karnataka',
 'Savita Kulkarni', '9845012306',
 'NONE', 'NONE', 'ACTIVE',
 3, 'photos/member18.jpg',
 'Weight loss goal — 10 kg in 6 months',
 5, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),

(4, 9, 'Meena Iyer', 'member19@fitness.com', '9845012307', '1997-03-05',
 '67 Dollars Colony, Bengaluru, Karnataka',
 'Venkat Iyer', '9845012308',
 'NONE', 'CORP300', 'PROSPECT',
 4, 'photos/member19.jpg',
 'Enquired about Diamond Annual plan',
 5, '2026-04-10 08:30:00', '2026-04-10 08:30:00'),

(5, 10, 'Tarun Goswami', 'member20@fitness.com', '9845012309', '1985-09-17',
 '3 Sadashivanagar Circle, Bengaluru, Karnataka',
 'Rekha Goswami', '9845012310',
 'REF1005', 'NONE', 'ACTIVE',
 5, 'photos/member20.jpg',
 'Senior member — prefer low-impact classes',
 5, '2026-05-01 10:00:00', '2026-05-01 10:00:00');
 
-- ----------------------------------------------------------------
-- 9. PLAN_FACILITY  (new plan–facility mappings)
-- ----------------------------------------------------------------
INSERT INTO PLAN_FACILITY (plan_id, facility_id) VALUES

-- Senior Wellness Monthly → Wellness Studio, Pilates Room, Cardio Deck
(1, 7), (1, 4), (1, 6),

-- Weekend Warrior
(2, 1), (2, 3), (2, 6),

-- Diamond Annual
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7),

-- Family Duo Monthly → Zumba Studio, Pilates Room, Wellness Studio
(4, 3), (4, 4), (4, 7),

-- Early Bird Quarterly → Aqua Pool, Crossfit Arena, Cardio Deck
(5, 1), (5, 2), (5, 6);
 
-- ----------------------------------------------------------------
-- 10. CLASSES  (IDs 16–22 — trainers 4–7, rooms from new facilities,
--              branches 4/5/6/7/8)
-- ----------------------------------------------------------------
INSERT INTO CLASSES
  (class_id, class_name, trainer_id, room_id, branch_id, start_date, end_date, weekdays, class_time, duration_mins, capacity, prerequisites, plan_eligibility, status, cancel_reason, created_at, updated_at)
VALUES

(1, 'Aqua Aerobics',
 2, 1, 1,
 '2026-05-01', '2026-12-31',
 'Tue,Thu,Sat', '07:00:00',
 50, 25,
 'Basic swimming ability',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(2, 'Zumba Fiesta',
 3, 3, 2,
 '2026-05-01', '2026-12-31',
 'Mon,Wed,Fri', '18:30:00',
 45, 30,
 'None',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(3, 'Advanced Pilates',
 1, 4, 2,
 '2026-05-01', '2026-12-31',
 'Mon,Wed', '09:00:00',
 60, 16,
 'Completed Pilates Core',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(4, 'Combat Boxing Drills',
 4, 5, 3,
 '2026-05-01', '2026-12-31',
 'Tue,Thu', '17:00:00',
 60, 12,
 'Boxing Fundamentals class',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(5, 'Morning CrossFit',
 2, 2, 1,
 '2026-05-01', '2026-12-31',
 'Mon,Tue,Wed', '06:00:00',
 50, 20,
 'Intermediate fitness',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(6, 'Senior Stretch & Tone',
 1, 7, 5,
 '2026-05-01', '2026-12-31',
 'Tue,Thu', '10:00:00',
 45, 20,
 'None',
 'GENERAL',
 'ACTIVE',
 'No cancellation',
 '2026-04-15 09:00:00',
 '2026-04-15 09:00:00'),

(7, 'Evening Tabata Blast',
 3, 6, 4,
 '2026-05-01', '2026-09-30',
 'Mon,Wed,Fri', '19:00:00',
 40, 35,
 'None',
 'GENERAL',
 'CANCELLED',
 'Facility under capacity review',
 '2026-04-15 09:00:00',
 '2026-05-10 12:00:00');
 
-- ----------------------------------------------------------------
-- 11. MEMBERSHIP  (IDs 16–22)
-- ----------------------------------------------------------------
INSERT INTO MEMBERSHIP
  (mem_id, member_id, plan_id, start_date, end_date, status, duration, price, discount_amount, promo_code_used, branch_id, created_at, updated_at)
VALUES

(1, 1, 3,
 '2026-04-01', '2027-04-01',
 'ACTIVE',
 365,
 17999.00,
 1799.90,
 'DIAMOND10',
 1,
 '2026-04-01 10:00:00',
 '2026-04-01 10:00:00'),

(2, 2, 1,
 '2026-04-01', '2026-05-01',
 'EXPIRED',
 30,
 799.00,
 0.00,
 'NONE',
 2,
 '2026-04-01 10:00:00',
 '2026-05-01 10:00:00'),

(3, 2, 1,
 '2026-05-01', '2026-06-01',
 'ACTIVE',
 30,
 799.00,
 39.95,
 'SENIOR5',
 2,
 '2026-05-01 10:00:00',
 '2026-05-01 10:00:00'),

(4, 3, 2,
 '2026-05-01', '2026-07-30',
 'ACTIVE',
 90,
 2499.00,
 300.00,
 'WELCOME300',
 3,
 '2026-05-01 10:00:00',
 '2026-05-01 10:00:00'),

(5, 4, 3,
 '2026-05-01', '2027-05-01',
 'PENDING',
 365,
 17999.00,
 0.00,
 'NONE',
 4,
 '2026-05-01 10:00:00',
 '2026-05-01 10:00:00'),

(6, 5, 1,
 '2026-05-01', '2026-06-01',
 'ACTIVE',
 30,
 799.00,
 0.00,
 'NONE',
 5,
 '2026-05-01 10:00:00',
 '2026-05-01 10:00:00'),

(7, 1, 4,
 '2026-05-01', '2026-06-01',
 'ACTIVE',
 30,
 2499.00,
 200.00,
 'FAMILY200',
 1,
 '2026-05-01 10:00:00',
 '2026-05-01 10:00:00');
 

 INSERT INTO INVOICE
  (invoice_id, invoice_number, member_id, membership_id, mrp, taxes, discount, final_amount, paid_amount, outstanding, promo_code, status, created_at)
VALUES

-- member 1, Diamond Annual with DIAMOND10
(1, 'INV-2026-001',
 1, 1,
 17999.00,
 2915.84,
 1799.90,
 19114.94,
 19114.94,
 0.00,
 'DIAMOND10',
 'PAID',
 '2026-04-01 10:30:00'),

-- member 2, Senior Monthly expired
(2, 'INV-2026-002',
 2, 2,
 799.00,
 39.95,
 0.00,
 838.95,
 838.95,
 0.00,
 'NONE',
 'PAID',
 '2026-04-01 11:00:00'),

-- member 2, Senior Monthly renewed with SENIOR5
(3, 'INV-2026-003',
 2, 3,
 799.00,
 37.95,
 39.95,
 797.00,
 797.00,
 0.00,
 'SENIOR5',
 'PAID',
 '2026-05-01 10:30:00'),

-- member 3, Weekend Warrior with WELCOME300
(4, 'INV-2026-004',
 3, 4,
 2499.00,
 395.82,
 300.00,
 2594.82,
 2594.82,
 0.00,
 'WELCOME300',
 'PAID',
 '2026-05-01 11:00:00'),

-- member 4, Diamond Annual pending
(5, 'INV-2026-005',
 4, 5,
 17999.00,
 3239.82,
 0.00,
 21238.82,
 0.00,
 21238.82,
 'NONE',
 'UNPAID',
 '2026-05-01 12:00:00'),

-- member 5, Senior Monthly
(6, 'INV-2026-006',
 5, 6,
 799.00,
 39.95,
 0.00,
 838.95,
 838.95,
 0.00,
 'NONE',
 'PAID',
 '2026-05-01 10:00:00'),

-- member 1, Family Duo Monthly with FAMILY200
(7, 'INV-2026-007',
 1, 7,
 2499.00,
 413.82,
 200.00,
 2712.82,
 2712.82,
 0.00,
 'FAMILY200',
 'PAID',
 '2026-05-01 10:15:00');
-- ----------------------------------------------------------------
-- 13. PAYMENT  (IDs 15–22)
-- ----------------------------------------------------------------
INSERT INTO PAYMENT
  (payment_id, invoice_id, member_id, payment_method, amount_paid, payment_date, payment_status, failure_reason, refund_by, refund_reason, created_at)
VALUES

(1, 1, 1,
 'UPI',
 19114.94,
 '2026-04-01 10:35:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-04-01 10:35:00'),

(2, 2, 2,
 'CASH',
 838.95,
 '2026-04-01 11:10:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-04-01 11:10:00'),

(3, 3, 2,
 'UPI',
 797.00,
 '2026-05-01 10:35:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-05-01 10:35:00'),

(4, 4, 3,
 'CARD',
 2594.82,
 '2026-05-01 11:05:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-05-01 11:05:00'),

(5, 5, 4,
 'CARD',
 0.00,
 '2026-05-01 12:10:00',
 'FAILED',
 'Card declined by bank',
 1,
 'No refund applicable',
 '2026-05-01 12:10:00'),

(6, 5, 4,
 'UPI',
 0.00,
 '2026-05-02 09:00:00',
 'FAILED',
 'UPI timeout error',
 1,
 'No refund applicable',
 '2026-05-02 09:00:00'),

(7, 6, 5,
 'CASH',
 838.95,
 '2026-05-01 10:05:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-05-01 10:05:00'),

(8, 7, 1,
 'CARD',
 2712.82,
 '2026-05-01 10:20:00',
 'SUCCESS',
 'No failure',
 1,
 'No refund applicable',
 '2026-05-01 10:20:00');
-- ----------------------------------------------------------------
-- 14. CLASS_BOOKING  (IDs 16–22 — new members + new classes)
-- ----------------------------------------------------------------
INSERT INTO CLASS_BOOKING
  (booking_id, class_id, member_id, booking_status, waitlist_position, cancelled_at, override_by, override_reason, created_at)
VALUES

(1, 1, 1,
 'CONFIRMED',
 0,
 '2026-01-01 00:00:00',
 1,
 'Regular booking',
 '2026-04-28 09:00:00'),

(2, 2, 2,
 'CONFIRMED',
 0,
 '2026-01-01 00:00:00',
 1,
 'Regular booking',
 '2026-04-28 09:05:00'),

(3, 3, 2,
 'CONFIRMED',
 0,
 '2026-01-01 00:00:00',
 1,
 'Regular booking',
 '2026-04-28 09:10:00'),

(4, 4, 3,
 'CONFIRMED',
 0,
 '2026-01-01 00:00:00',
 1,
 'Regular booking',
 '2026-04-28 09:15:00'),

(5, 6, 5,
 'CONFIRMED',
 0,
 '2026-01-01 00:00:00',
 1,
 'Senior class — priority entry',
 '2026-04-28 09:20:00'),

(6, 1, 3,
 'WAITLISTED',
 1,
 '2026-01-01 00:00:00',
 1,
 'Waitlisted — pool near cap',
 '2026-04-29 10:00:00'),

(7, 2, 5,
 'CANCELLED',
 0,
 '2026-05-04 08:30:00',
 5,
 'Member requested cancellation',
 '2026-04-29 10:05:00');
-- ----------------------------------------------------------------
-- 15. PT_SESSION  (IDs 7–12)
-- ----------------------------------------------------------------
INSERT INTO PT_SESSION
  (session_id, member_id, trainer_id, scheduled_at, duration_mins, status, trainer_notes, created_at, updated_at)
VALUES

(1, 1, 2,
 '2026-05-13 07:00:00',
 60,
 'APPROVED',
 'Aqua resistance exercises — shoulder rehab focus',
 '2026-05-08 09:00:00',
 '2026-05-08 09:00:00'),

(2, 2, 1,
 '2026-05-14 09:00:00',
 45,
 'REQUESTED',
 'Initial Pilates assessment and flexibility baseline',
 '2026-05-09 10:00:00',
 '2026-05-09 10:00:00'),

(3, 3, 4,
 '2026-05-15 17:00:00',
 60,
 'APPROVED',
 'Shadow boxing and footwork drills for beginner level',
 '2026-05-09 11:00:00',
 '2026-05-09 11:00:00'),

(4, 5, 1,
 '2026-05-16 10:00:00',
 45,
 'COMPLETED',
 'Low-impact stretching; improved hip mobility noted',
 '2026-05-10 08:00:00',
 '2026-05-16 11:00:00'),

(5, 1, 2,
 '2026-05-20 07:00:00',
 60,
 'COMPLETED',
 'Lap swimming — endurance up by 2 lengths this week',
 '2026-05-10 09:00:00',
 '2026-05-20 08:00:00'),

(6, 4, 3,
 '2026-05-22 18:30:00',
 45,
 'REQUESTED',
 'Dance fitness orientation — assess coordination',
 '2026-05-12 10:00:00',
 '2026-05-12 10:00:00');
 
-- ----------------------------------------------------------------
-- 16. ATTENDANCE  (IDs 16–22)
-- ----------------------------------------------------------------
INSERT INTO ATTENDANCE
  (log_id, member_id, branch_id, check_in_time, check_out_time, alert_flag, scan_method, sync_status, class_id, override_by, override_reason, created_at)
VALUES

(1, 1, 1,
 '2026-05-13 06:55:00',
 '2026-05-13 08:00:00',
 0,
 'QR',
 'SYNCED',
 1,
 1,
 'Regular entry',
 '2026-05-13 06:55:00'),

(2, 2, 2,
 '2026-05-14 08:50:00',
 '2026-05-14 09:50:00',
 0,
 'CARD',
 'SYNCED',
 3,
 1,
 'Regular entry',
 '2026-05-14 08:50:00'),

(3, 3, 3,
 '2026-05-15 16:55:00',
 '2026-05-15 18:05:00',
 0,
 'QR',
 'SYNCED',
 4,
 1,
 'Regular entry',
 '2026-05-15 16:55:00'),

(4, 5, 5,
 '2026-05-16 09:55:00',
 '2026-05-16 10:55:00',
 0,
 'MANUAL',
 'SYNCED',
 6,
 1,
 'Regular entry',
 '2026-05-16 09:55:00'),

(5, 4, 4,
 '2026-05-17 07:00:00',
 '2026-05-17 08:15:00',
 1,
 'QR',
 'SYNCED',
 5,
 1,
 'PROSPECT status — access override by staff',
 '2026-05-17 07:00:00'),

(6, 1, 1,
 '2026-05-20 06:50:00',
 '2026-05-20 08:00:00',
 0,
 'QR',
 'SYNCED',
 1,
 1,
 'Regular entry',
 '2026-05-20 06:50:00'),

(7, 3, 1,
 '2026-05-19 17:30:00',
 '2026-05-19 18:45:00',
 0,
 'CARD',
 'SYNCED',
 2,
 1,
 'Cross-branch visit authorised by plan',
 '2026-05-19 17:30:00');
 
-- ----------------------------------------------------------------
-- 17. HEALTH_CONSENT  (IDs 6–10)
-- ----------------------------------------------------------------
INSERT INTO HEALTH_CONSENT
  (consent_id, member_id, form_version, parq_responses, medical_acknowledged, liability_acknowledged, privacy_acknowledged, acknowledged_at, expires_at, ip_address, status, staff_notes, created_at, updated_at)
VALUES

(1, 1, 'v1.1',
 '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":true,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}',
 1, 1, 1,
 '2026-04-01 09:30:00',
 '2027-04-01 09:30:00',
 '192.168.2.10',
 'ACTIVE',
 'Shoulder condition noted — cleared by physio',
 '2026-04-01 09:30:00',
 '2026-04-01 09:30:00'),

(2, 2, 'v1.1',
 '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}',
 1, 1, 1,
 '2026-04-01 10:00:00',
 '2027-04-01 10:00:00',
 '192.168.2.11',
 'ACTIVE',
 'All clear on health screening',
 '2026-04-01 10:00:00',
 '2026-04-01 10:00:00'),

(3, 3, 'v1.1',
 '{"heartCondition":false,"chestPain":false,"dizziness":true,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}',
 1, 1, 1,
 '2026-04-01 09:00:00',
 '2027-04-01 09:00:00',
 '192.168.2.12',
 'ACTIVE',
 'Mild dizziness reported — advised low-intensity',
 '2026-04-01 09:00:00',
 '2026-04-01 09:00:00'),

(4, 4, 'v1.1',
 '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":true,"otherReason":false,"pregnancy":false}',
 1, 1, 1,
 '2026-04-10 08:45:00',
 '2027-04-10 08:45:00',
 '192.168.2.13',
 'ACTIVE',
 'On BP medication — doctor clearance attached',
 '2026-04-10 08:45:00',
 '2026-04-10 08:45:00'),

(5, 5, 'v1.1',
 '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}',
 1, 1, 1,
 '2026-05-01 10:15:00',
 '2027-05-01 10:15:00',
 '192.168.2.14',
 'ACTIVE',
 'Senior member — full clearance received',
 '2026-05-01 10:15:00',
 '2026-05-01 10:15:00');
 
-- ----------------------------------------------------------------
-- 18. NOTIFICATION  (IDs 11–17)
-- ----------------------------------------------------------------
INSERT INTO NOTIFICATION
  (notif_id, user_id, type, channel, title, body, is_read, delivery_status, created_at)
VALUES

(1, 6, 'MEMBERSHIP', 'EMAIL',
 'Diamond Plan Activated',
 'Your Diamond Annual membership is active from 2026-04-01 to 2027-04-01. Welcome aboard!',
 1, 'DELIVERED',
 '2026-04-01 10:40:00'),

(2, 7, 'MEMBERSHIP', 'IN_APP',
 'Membership Renewal Reminder',
 'Your Senior Wellness plan expires on 2026-05-01. Renew now to continue uninterrupted access.',
 0, 'DELIVERED',
 '2026-04-28 09:00:00'),

(3, 8, 'CLASS', 'IN_APP',
 'Class Booking Confirmed',
 'Your booking for Combat Boxing Drills on 2026-05-15 at 17:00 is confirmed. Please arrive 5 minutes early.',
 0, 'DELIVERED',
 '2026-04-28 09:15:00'),

(4, 9, 'PAYMENT', 'EMAIL',
 'Invoice Payment Pending',
 'Invoice INV-2026-020 of Rs.21238.82 is unpaid. Please complete the payment to activate your membership.',
 0, 'DELIVERED',
 '2026-05-01 12:15:00'),

(5, 10, 'TRAINER', 'SMS',
 'PT Session Reminder',
 'Your PT session with Sneha Pilates is scheduled for 2026-05-16 at 10:00. Reply CANCEL to reschedule.',
 0, 'DELIVERED',
 '2026-05-15 08:00:00'),

(6, 6, 'CLASS', 'IN_APP',
 'Waitlist Position Update',
 'Good news! You have moved to position 1 on the waitlist for Aqua Aerobics on Tuesday. We will notify you shortly.',
 0, 'DELIVERED',
 '2026-04-30 11:00:00'),

(7, 1, 'SYSTEM', 'EMAIL',
 'Weekly Operations Report',
 'Branch summary: 7 new memberships, 22 attendance records, and 3 pending invoices recorded this week.',
 1, 'DELIVERED',
 '2026-05-18 07:00:00');
 
-- ----------------------------------------------------------------
-- 19. AUDIT_LOG  (IDs 11–17)
-- ----------------------------------------------------------------
INSERT INTO AUDIT_LOG
  (audit_id, performed_by, entity_name, entity_id, action, old_value, new_value, created_at)
VALUES

(1, 1, 'SYSTEM_USER', 6,
 'CREATE',
 '{"note":"new_user"}',
 '{"username":"member16","role":"MEMBER"}',
 '2026-03-10 10:00:00'),

(2, 5, 'MEMBER', 1,
 'UPDATE',
 '{"status":"PROSPECT"}',
 '{"status":"ACTIVE"}',
 '2026-04-01 10:30:00'),

(3, 1, 'INVOICE', 5,
 'UPDATE',
 '{"status":"UNPAID"}',
 '{"status":"UNPAID","note":"Second payment attempt failed"}',
 '2026-05-02 09:05:00'),

(4, 3, 'PLAN', 3,
 'CREATE',
 '{"note":"new_plan"}',
 '{"plan_name":"Diamond Annual","price":17999.00}',
 '2026-04-01 08:00:00'),

(5, 5, 'MEMBERSHIP', 2,
 'UPDATE',
 '{"status":"ACTIVE"}',
 '{"status":"EXPIRED"}',
 '2026-05-01 09:55:00'),

(6, 1, 'CLASS', 7,
 'UPDATE',
 '{"status":"ACTIVE"}',
 '{"status":"CANCELLED","cancel_reason":"Facility under capacity review"}',
 '2026-05-10 12:00:00'),

(7, 5, 'ATTENDANCE', 5,
 'CREATE',
 '{"note":"new_record"}',
 '{"member_id":4,"alert_flag":true,"override_reason":"PROSPECT status"}',
 '2026-05-17 07:01:00');
-- ----------------------------------------------------------------
-- 20. FEATURE_FLAG  (5 new flags)
-- ----------------------------------------------------------------
INSERT INTO FEATURE_FLAG (flag_name, is_enabled, last_modified_by) VALUES
('Multi-Branch Access',      1, 'Admin'),
('Waitlist Auto-Promote',    1, 'Admin'),
('Online Class Booking',     1, 'Admin'),
('Refund Workflow',          0, 'Admin'),
('Member Mobile App Sync',   1, 'Admin');

-- ----------------------------------------------------------------
-- 21. PASSWORD_POLICY  (single-row table, admin-managed)
--     Used by: member registration (front-desk), password reset (member)
--     Only ADMIN role may UPDATE this table via the API.
-- ----------------------------------------------------------------
CREATE TABLE PASSWORD_POLICY (
  policy_id             BIGINT       PRIMARY KEY AUTO_INCREMENT,

  -- Password complexity rules
  min_password_length   INT          NOT NULL DEFAULT 8
                          COMMENT 'Minimum number of characters required',
  require_uppercase     BOOLEAN      NOT NULL DEFAULT TRUE
                          COMMENT 'At least one uppercase letter (A-Z)',
  require_number        BOOLEAN      NOT NULL DEFAULT TRUE
                          COMMENT 'At least one numeric digit (0-9)',
  require_special_char  BOOLEAN      NOT NULL DEFAULT TRUE
                          COMMENT 'At least one special character (!@#$% etc.)',

  -- Session & lockout settings
  session_timeout_min   INT          NOT NULL DEFAULT 60
                          COMMENT 'JWT / session expiry in minutes',
  max_failed_attempts   INT          NOT NULL DEFAULT 5
                          COMMENT 'Failed logins before account is locked',
  lockout_duration_min  INT          NOT NULL DEFAULT 30
                          COMMENT 'How long the account stays locked (minutes)',

  -- Audit trail
  last_updated_by       VARCHAR(80)  NOT NULL DEFAULT 'SYSTEM'
                          COMMENT 'Username of the admin who last changed the policy',
  updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP
);

-- Default row — enforced until admin explicitly changes it via the UI.
-- Only one row should ever exist (policy_id = 1).
INSERT INTO PASSWORD_POLICY (
  policy_id,
  min_password_length,
  require_uppercase,
  require_number,
  require_special_char,
  session_timeout_min,
  max_failed_attempts,
  lockout_duration_min,
  last_updated_by
) VALUES (
  1,       -- fixed primary key so there is always exactly one row
  8,       -- min 8 characters (same as original default across the app)
  TRUE,    -- uppercase required
  TRUE,    -- number required
  TRUE,    -- special character required
  60,      -- 60-minute session timeout
  5,       -- 5 failed attempts before lockout
  30,      -- 30-minute lockout window
  'SYSTEM' -- set by system seed; admin changes this via the UI
);