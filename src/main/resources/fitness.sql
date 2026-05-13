create table SYSTEM_USER(
    user_id bigint primary key auto_increment,
    username varchar(80) not null unique,
    email varchar(150) not null unique,
    password_hash varchar(255) not null,
    role varchar(50) not null,
    active boolean default true,
    failed_attempts int default 0,
    locked_until datetime null,
    last_login datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp
);

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


CREATE TABLE PROMO_CODE(
  promo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE,
  discount_type VARCHAR(30) NOT NULL,
  discount_value DECIMAL(10,2) NOT NULL,
  expiry_date DATE NOT NULL,
  usage_limit INT NOT NULL,
  per_member_limit INT DEFAULT 1,
  eligibility VARCHAR(30) DEFAULT 'ALL',
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


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


INSERT INTO SYSTEM_USER (user_id, username, email, password_hash, role, active, failed_attempts, locked_until, last_login, created_at, updated_at) VALUES
(1,  'admin',      'admin@fitness.com',      '$2a$10$adminSeedHash1234567890123456789012345678901234567890', 'ADMIN',      1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2,  'frontdesk',  'frontdesk@fitness.com',  '$2a$10$frontdeskSeedHash123456789012345678901234567890123',   'FRONT_DESK', 1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3,  'manager',    'manager@fitness.com',    '$2a$10$managerSeedHash1234567890123456789012345678901234',    'MANAGER',    1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4,  'trainer1',   'trainer1@fitness.com',   '$2a$10$trainerSeedHash1234567890123456789012345678901234',   'TRAINER',    1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5,  'member1',    'member1@fitness.com',    '$2a$10$memberSeedHash12345678901234567890123456789012345',   'MEMBER',     1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6,  'member2',    'member2@fitness.com',    '$2a$10$memberSeedHash22345678901234567890123456789012345',   'MEMBER',     1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7,  'trainer2',   'trainer2@fitness.com',   '$2a$10$trainerSeedHash2234567890123456789012345678901234',   'TRAINER',    1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8,  'trainer3',   'trainer3@fitness.com',   '$2a$10$trainerSeedHash3234567890123456789012345678901234',   'TRAINER',    1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9,  'member3',    'member3@fitness.com',    '$2a$10$memberSeedHash32345678901234567890123456789012345',   'MEMBER',     1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'member4',    'member4@fitness.com',    '$2a$10$memberSeedHash42345678901234567890123456789012345',   'MEMBER',     1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'member5',    'member5@fitness.com',    '$2a$10$memberSeedHash52345678901234567890123456789012345',   'MEMBER',     1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'frontdesk2', 'frontdesk2@fitness.com', '$2a$10$frontdeskSeedHash223456789012345678901234567890123',  'FRONT_DESK', 1, 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO BRANCH (branch_id, branch_name, address, contact, op_hours, timezone, is_active, created_at) VALUES
(1, 'Downtown Fitness Hub',  '12 MG Road, Bengaluru, Karnataka',          '+91-9876543210', '06:00 - 22:00', 'Asia/Kolkata', 1, CURRENT_TIMESTAMP),
(2, 'North Zone Gym',        '88 Hebbal Main Road, Bengaluru, Karnataka',  '+91-9876543211', '05:30 - 23:00', 'Asia/Kolkata', 1, CURRENT_TIMESTAMP),
(3, 'South Side Studio',     '44 Jayanagar 4th Block, Bengaluru, Karnataka', '+91-9876543212', '06:00 - 21:00', 'Asia/Kolkata', 1, CURRENT_TIMESTAMP);

INSERT INTO PLAN (plan_id, plan_name, duration_days, price, access_start, access_end, eligibility_type, proration_rule, tax_percent, version, effective_from, branch_visibility, is_active, created_at, updated_at) VALUES
(1, 'Gold Annual',        365, 12999.00, '05:00:00', '23:00:00', 'GENERAL',   'FULL',     18.00, 1, '2025-01-01', 'ALL', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Silver Quarterly',    90,  3999.00, '06:00:00', '22:00:00', 'GENERAL',   'PRO_RATA', 18.00, 1, '2025-01-01', 'ALL', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Corporate Monthly',   30,  1499.00, '06:00:00', '21:00:00', 'CORPORATE', 'FULL',     18.00, 1, '2025-01-01', '1,2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Student Monthly',     30,   999.00, '06:00:00', '22:00:00', 'STUDENT',   'PRO_RATA', 18.00, 1, '2025-01-01', 'ALL', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Platinum Half-Year', 180,  7499.00, '05:00:00', '23:00:00', 'GENERAL',   'PRO_RATA', 18.00, 1, '2025-06-01', 'ALL', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO FACILITY (facility_id, facility_name, branch_id, capacity, is_active, created_at, updated_at) VALUES
(1, 'Room A',       1, 20, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Room B',       1, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Gym Floor',    1, 40, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Studio 1',     2, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Studio 2',     3, 22, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Spin Room',    2, 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Pool Deck',    3, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Boxing Ring',  1, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ADD_ON (addon_id, addon_name, price, capacity, addon_type, tax_percent, is_active, created_at, updated_at) VALUES
(1, 'PT Package (10 Sessions)',   6500.00, NULL, 'SERVICE',  18.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Locker Storage (Monthly)',    300.00, NULL, 'FACILITY', 18.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Nutrition Consultation',     1500.00, NULL, 'SERVICE',  18.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Towel Service (Monthly)',     150.00, NULL, 'FACILITY',  5.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'PT Package (5 Sessions)',    3500.00, NULL, 'SERVICE',  18.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Parking Pass (Monthly)',      500.00, NULL, 'FACILITY',  5.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PROMO_CODE (promo_id, code, discount_type, discount_value, expiry_date, usage_limit, per_member_limit, eligibility, is_active, created_at, updated_at) VALUES
(1, 'NEW10',      'PERCENT',  10.00, '2026-12-31', 100, 1, 'ALL',       1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'CORP500',    'FLAT',    500.00, '2026-12-31',  50, 1, 'CORPORATE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'STUDENT15',  'PERCENT',  15.00, '2026-12-31',  80, 1, 'STUDENT',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'SUMMER20',   'PERCENT',  20.00, '2026-08-31', 200, 1, 'ALL',       1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'REFER250',   'FLAT',    250.00, '2026-12-31', 500, 1, 'ALL',       1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO TRAINER (trainer_id, user_id, bio, certifications, specialties, rating, branch_id, is_active, created_at, updated_at) VALUES
(1, 4, 'Strength and conditioning specialist.',          'ACSM Certified, CrossFit L2',       'Strength, CrossFit',          4.80, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 7, 'Yoga and mindfulness expert with 8 years exp.', 'RYT-500, Yin Yoga Certified',        'Yoga, Flexibility, Wellness', 4.90, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 8, 'Cardio and functional fitness coach.',          'NASM CPT, TRX Certified, Spin L1',   'Cardio, HIIT, Spin',          4.70, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO MEMBER (member_id, user_id, mem_name, email, phone, dob, address, emg_contact, emg_phone, referral_code, corporate_code, status, home_branch_id, photo_path, notes, created_by, created_at, updated_at) VALUES
(1, 5,  'Aman Verma',    'member1@fitness.com', '9876543215', '1998-08-12', '24 Indiranagar, Bengaluru, Karnataka',      'Neha Verma',    '9876543220', 'REF1001', NULL,     'ACTIVE',    1, NULL, 'Prefers morning sessions',         2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 6,  'Riya Sharma',   'member2@fitness.com', '9876543216', '2000-02-18', '18 Koramangala, Bengaluru, Karnataka',      'Karan Sharma',  '9876543221', NULL,      'CORP100', 'PROSPECT',  2, NULL, NULL,                               2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 9,  'Vikram Nair',   'member3@fitness.com', '9876543217', '1995-05-30', '7 Whitefield, Bengaluru, Karnataka',        'Priya Nair',    '9876543222', NULL,      NULL,      'ACTIVE',    1, NULL, 'Interested in weight loss program', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 10, 'Pooja Reddy',   'member4@fitness.com', '9876543218', '2001-11-04', '33 HSR Layout, Bengaluru, Karnataka',       'Suresh Reddy',  '9876543223', 'REF1002', NULL,      'ACTIVE',    3, NULL, 'Student discount applied',         12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 11, 'Arjun Mehta',   'member5@fitness.com', '9876543219', '1990-03-22', '55 Malleshwaram, Bengaluru, Karnataka',     'Sunita Mehta',  '9876543224', NULL,      'CORP100', 'SUSPENDED', 2, NULL, 'Suspended due to payment default',  2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PLAN_FACILITY (plan_id, facility_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 6),
(1, 7),
(1, 8),
(2, 1),
(2, 4),
(3, 4),
(3, 5),
(4, 1),
(4, 2),
(5, 1),
(5, 2),
(5, 3),
(5, 4),
(5, 5),
(5, 6);

INSERT INTO CLASSES (class_id, class_name, trainer_id, room_id, branch_id, start_date, end_date, weekdays, class_time, duration_mins, capacity, prerequisites, plan_eligibility, status, cancel_reason, created_at, updated_at) VALUES
(1, 'Morning Yoga Flow',    1, 1, 1, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '07:00:00', 60, 20, NULL,                        'GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'HIIT Challenge',       1, 2, 1, '2026-05-01', '2026-12-31', 'Tue,Thu',     '18:00:00', 45, 18, 'Basic cardio fitness',      'GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Power Lifting 101',    1, 3, 1, '2026-05-01', '2026-12-31', 'Sat',         '09:00:00', 75, 16, 'Intermediate strength level','GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Vinyasa Yoga',         2, 4, 2, '2026-05-01', '2026-12-31', 'Mon,Wed',     '08:00:00', 60, 24, NULL,                        'GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Spin Burn',            3, 6, 2, '2026-05-01', '2026-12-31', 'Tue,Thu,Sat', '06:30:00', 45, 15, NULL,                        'GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Core & Stretch',       3, 5, 3, '2026-05-01', '2026-12-31', 'Mon,Fri',     '10:00:00', 50, 22, NULL,                        'GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Boxing Fundamentals',  1, 8, 1, '2026-05-01', '2026-09-30', 'Wed,Sat',     '19:00:00', 60, 12, 'No prior experience needed','GENERAL', 'active',    NULL,                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Evening Meditation',   2, 4, 2, '2026-05-01', '2026-07-31', 'Fri',         '20:00:00', 30, 24, NULL,                        'GENERAL', 'cancelled', 'Trainer unavailable in slot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO MEMBERSHIP (mem_id, member_id, plan_id, start_date, end_date, status, duration, price, discount_amount, promo_code_used, branch_id, created_at, updated_at) VALUES
(1, 1, 1, '2026-01-01', '2027-01-01', 'ACTIVE',   365, 12999.00,  0.00, 'NEW10',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 2, '2026-04-01', '2026-06-30', 'PENDING',   90,  3999.00, 500.00, 'CORP500', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 1, '2026-02-01', '2027-02-01', 'ACTIVE',   365, 12999.00,  0.00, NULL,      1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 4, '2026-03-15', '2026-04-15', 'EXPIRED',   30,   999.00, 149.85, 'STUDENT15', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 4, 4, '2026-05-01', '2026-06-01', 'ACTIVE',    30,   999.00,  0.00, NULL,      3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 5, 3, '2026-01-01', '2026-02-01', 'SUSPENDED', 30,  1499.00,  0.00, NULL,      2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO INVOICE (invoice_id, invoice_number, member_id, membership_id, mrp, taxes, discount, final_amount, paid_amount, outstanding, promo_code, status, created_at) VALUES
(1, 'INV-2026-001', 1, 1, 12999.00, 2339.82,    0.00, 15338.82, 15338.82,    0.00, 'NEW10',     'PAID',    '2026-01-01 10:15:00'),
(2, 'INV-2026-002', 2, 2,  3999.00,  719.82,  500.00,  4218.82,     0.00, 4218.82, 'CORP500',   'PENDING', '2026-04-01 11:00:00'),
(3, 'INV-2026-003', 3, 3, 12999.00, 2339.82,    0.00, 15338.82, 15338.82,    0.00, NULL,        'PAID',    '2026-02-01 09:30:00'),
(4, 'INV-2026-004', 4, 4,   999.00,  179.82,  149.85,  1029.00,  1029.00,    0.00, 'STUDENT15', 'PAID',    '2026-03-15 14:00:00'),
(5, 'INV-2026-005', 4, 5,   999.00,  179.82,    0.00,  1178.82,  1178.82,    0.00, NULL,        'PAID',    '2026-05-01 10:00:00'),
(6, 'INV-2026-006', 5, 6,  1499.00,  269.82,    0.00,  1768.82,     0.00, 1768.82, NULL,        'UNPAID',  '2026-01-01 12:00:00');

INSERT INTO PAYMENT (payment_id, invoice_id, member_id, payment_method, amount_paid, payment_date, payment_status, failure_reason, refund_by, refund_reason, created_at) VALUES
(1, 1, 1, 'CARD',  15338.82, '2026-01-01 10:20:00', 'SUCCESS', NULL,                           NULL, NULL, CURRENT_TIMESTAMP),
(2, 3, 3, 'UPI',   15338.82, '2026-02-01 09:35:00', 'SUCCESS', NULL,                           NULL, NULL, CURRENT_TIMESTAMP),
(3, 4, 4, 'CASH',   1029.00, '2026-03-15 14:10:00', 'SUCCESS', NULL,                           NULL, NULL, CURRENT_TIMESTAMP),
(4, 5, 4, 'CARD',   1178.82, '2026-05-01 10:05:00', 'SUCCESS', NULL,                           NULL, NULL, CURRENT_TIMESTAMP),
(5, 6, 5, 'CARD',      0.00, '2026-01-01 12:10:00', 'FAILED',  'Insufficient funds on card',   NULL, NULL, CURRENT_TIMESTAMP);

INSERT INTO CLASS_BOOKING (booking_id, class_id, member_id, booking_status, waitlist_position, cancelled_at, override_by, override_reason, created_at) VALUES
(1, 1, 1, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(2, 2, 1, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(3, 3, 2, 'WAITLISTED',    2, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(4, 4, 3, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(5, 5, 4, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(6, 6, 4, 'CANCELLED',  NULL, '2026-05-03 08:00:00',   NULL, NULL,                          CURRENT_TIMESTAMP),
(7, 7, 1, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(8, 3, 3, 'CONFIRMED',  NULL, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(9, 5, 1, 'WAITLISTED',    1, NULL,                    NULL, NULL,                          CURRENT_TIMESTAMP),
(10,7, 3, 'CONFIRMED',  NULL, NULL,                       2, 'Member missed booking window', CURRENT_TIMESTAMP);

INSERT INTO PT_SESSION (session_id, member_id, trainer_id, scheduled_at, duration_mins, status, trainer_notes, created_at, updated_at) VALUES
(1, 1, 1, '2026-05-08 07:30:00', 60, 'REQUESTED', 'Focus on back and shoulders',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 1, '2026-05-10 18:30:00', 45, 'APPROVED',  'Increase weight gradually',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 1, '2026-05-09 06:00:00', 60, 'COMPLETED', 'Good form on deadlifts, +5 kg next', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 3, '2026-05-07 07:00:00', 45, 'COMPLETED', 'Endurance improving week on week',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 1, '2026-05-15 07:30:00', 60, 'APPROVED',  'Progressive overload — legs day',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 2, '2026-05-12 08:00:00', 60, 'REQUESTED', 'Flexibility assessment session',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ATTENDANCE (log_id, member_id, branch_id, check_in_time, check_out_time, alert_flag, scan_method, sync_status, class_id, override_by, override_reason, created_at) VALUES
(1,  1, 1, '2026-05-06 07:05:00', '2026-05-06 08:10:00', 0, 'MANUAL', 'SYNCED',  1, NULL, NULL, CURRENT_TIMESTAMP),
(2,  2, 2, '2026-05-06 08:10:00', NULL,                  0, 'QR',     'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(3,  3, 1, '2026-05-06 06:00:00', '2026-05-06 07:15:00', 0, 'QR',     'SYNCED',  NULL, NULL, NULL, CURRENT_TIMESTAMP),
(4,  4, 3, '2026-05-06 10:05:00', '2026-05-06 11:00:00', 0, 'QR',     'SYNCED',  6, NULL, NULL, CURRENT_TIMESTAMP),
(5,  1, 1, '2026-05-07 18:00:00', '2026-05-07 18:50:00', 0, 'CARD',   'SYNCED',  2, NULL, NULL, CURRENT_TIMESTAMP),
(6,  5, 2, '2026-05-05 09:00:00', NULL,                  1, 'QR',     'SYNCED',  NULL, 1, 'Membership suspended — access override by admin', CURRENT_TIMESTAMP),
(7,  3, 1, '2026-05-07 09:00:00', '2026-05-07 10:20:00', 0, 'MANUAL', 'SYNCED',  3, NULL, NULL, CURRENT_TIMESTAMP),
(8,  1, 1, '2026-05-05 07:10:00', '2026-05-05 08:05:00', 0, 'QR',     'SYNCED',  1, NULL, NULL, CURRENT_TIMESTAMP);

INSERT INTO HEALTH_CONSENT (consent_id, member_id, form_version, parq_responses, medical_acknowledged, liability_acknowledged, privacy_acknowledged, acknowledged_at, expires_at, ip_address, status, staff_notes, created_at, updated_at) VALUES
(1, 1, 'v1.0', '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}', 1, 1, 1, '2026-01-01 09:00:00', '2027-01-01 09:00:00', '127.0.0.1',    'ACTIVE',  'Initial consent received',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'v1.0', '{"heartCondition":false,"chestPain":true,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}', 1, 1, 1, '2026-04-01 09:15:00', '2026-04-01 09:15:00', '127.0.0.1',    'EXPIRED', 'Renewal pending',                   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 'v1.0', '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}', 1, 1, 1, '2026-02-01 08:45:00', '2027-02-01 08:45:00', '192.168.1.10', 'ACTIVE',  'Consent signed at front desk',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 'v1.0', '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":true,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}', 1, 1, 1, '2026-03-15 13:55:00', '2027-03-15 13:55:00', '192.168.1.12', 'ACTIVE',  'Student ID verified at intake',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 5, 'v1.0', '{"heartCondition":false,"chestPain":false,"dizziness":false,"boneJointProblem":false,"bloodPressureMedication":false,"otherReason":false,"pregnancy":false}', 1, 1, 1, '2025-12-20 10:00:00', '2025-12-20 10:00:00', '192.168.1.15', 'EXPIRED', 'Form expired; re-consent required', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO NOTIFICATION (notif_id, user_id, type, channel, title, body, is_read, delivery_status, created_at) VALUES
(1,  5, 'MEMBERSHIP', 'IN_APP', 'Membership Renewed',          'Your membership is active until 2027-01-01.',                           0, 'DELIVERED', CURRENT_TIMESTAMP),
(2,  4, 'TRAINER',    'IN_APP', 'New PT Session',              'A member requested a personal training session.',                       0, 'DELIVERED', CURRENT_TIMESTAMP),
(3,  1, 'SYSTEM',     'EMAIL',  'Daily Audit Summary',         'New attendance and invoice records were created today.',                 1, 'DELIVERED', CURRENT_TIMESTAMP),
(4,  6, 'PAYMENT',    'EMAIL',  'Payment Due',                 'Your membership invoice INV-2026-002 of ₹4218.82 is outstanding.',      0, 'DELIVERED', CURRENT_TIMESTAMP),
(5,  9, 'MEMBERSHIP', 'SMS',    'Membership Confirmed',        'Welcome Vikram! Your Gold Annual plan starts on 2026-02-01.',           1, 'DELIVERED', CURRENT_TIMESTAMP),
(6, 10, 'CLASS',      'IN_APP', 'Booking Cancelled',           'Your booking for Core & Stretch on May 6 has been cancelled.',          0, 'DELIVERED', CURRENT_TIMESTAMP),
(7,  5, 'CLASS',      'IN_APP', 'Waitlist Update',             'You are now #1 on the waitlist for Spin Burn.',                         0, 'DELIVERED', CURRENT_TIMESTAMP),
(8, 11, 'SYSTEM',     'EMAIL',  'Account Suspended',           'Your account has been suspended due to an outstanding payment.',        1, 'DELIVERED', CURRENT_TIMESTAMP),
(9,  7, 'TRAINER',    'IN_APP', 'New PT Session Request',      'Member Vikram Nair has requested a flexibility assessment session.',     0, 'DELIVERED', CURRENT_TIMESTAMP),
(10, 3, 'SYSTEM',     'EMAIL',  'New Member Registration',     'New member Pooja Reddy registered at South Side Studio.',               1, 'DELIVERED', CURRENT_TIMESTAMP);

INSERT INTO AUDIT_LOG (audit_id, performed_by, entity_name, entity_id, action, old_value, new_value, created_at) VALUES
(1,  1, 'SYSTEM_USER', 5, 'CREATE', NULL,                         '{"username":"member1","role":"MEMBER"}',                 '2026-01-01 09:00:00'),
(2,  2, 'MEMBER',      1, 'UPDATE', '{"status":"PROSPECT"}',       '{"status":"ACTIVE"}',                                   '2026-05-01 12:00:00'),
(3,  1, 'INVOICE',     1, 'UPDATE', '{"status":"PENDING"}',        '{"status":"PAID"}',                                     '2026-05-06 13:00:00'),
(4,  1, 'SYSTEM_USER', 6, 'CREATE', NULL,                         '{"username":"member2","role":"MEMBER"}',                 '2026-04-01 08:00:00'),
(5,  1, 'PLAN',        4, 'CREATE', NULL,                         '{"plan_name":"Student Monthly","price":999.00}',          '2025-01-01 10:00:00'),
(6,  3, 'MEMBERSHIP',  1, 'UPDATE', '{"status":"ACTIVE"}',         '{"status":"SUSPENDED"}',                                '2026-02-02 09:00:00'),
(7,  2, 'CLASS',       1, 'UPDATE', '{"status":"active"}',         '{"status":"cancelled","cancel_reason":"Trainer unavailable in slot"}', '2026-04-28 15:00:00'),
(8,  1, 'MEMBER',      3, 'CREATE', NULL,                         '{"mem_name":"Vikram Nair","status":"ACTIVE"}',           '2026-02-01 08:40:00'),
(9,  2, 'ATTENDANCE',  1, 'CREATE', NULL,                         '{"member_id":5,"alert_flag":true}',                      '2026-05-05 09:01:00'),
(10, 1, 'PROMO_CODE',  4, 'CREATE', NULL,                         '{"code":"SUMMER20","discount_type":"PERCENT","discount_value":20.00}', '2026-04-15 11:00:00');

-- EXTENDED MOCK DATA GENERATED FOR REAL APP SIMULATION --
INSERT INTO SYSTEM_USER (user_id, username, email, password_hash, role, active, failed_attempts, created_at, updated_at) VALUES
(13, 'member13', 'member13@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'member14', 'member14@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'member15', 'member15@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'member16', 'member16@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'member17', 'member17@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'member18', 'member18@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'member19', 'member19@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'member20', 'member20@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'member21', 'member21@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'member22', 'member22@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'member23', 'member23@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'member24', 'member24@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'member25', 'member25@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'member26', 'member26@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'member27', 'member27@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'member28', 'member28@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'member29', 'member29@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'member30', 'member30@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'member31', 'member31@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 'member32', 'member32@fitness.com', '$2a$10$memberSeedHash12345678901234567890123456789012345', 'MEMBER', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO MEMBER (member_id, user_id, mem_name, email, phone, dob, address, status, home_branch_id, created_by, created_at, updated_at) VALUES
(6, 13, 'Rahul Kumar', 'member13@fitness.com', '9876527919', '1992-09-23', '31 Random Street, Bengaluru, Karnataka', 'ACTIVE', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 14, 'Priya Sharma', 'member14@fitness.com', '9876543145', '1992-12-19', '59 Random Street, Bengaluru, Karnataka', 'ACTIVE', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 15, 'Amit Singh', 'member15@fitness.com', '9876548210', '1990-08-22', '11 Random Street, Bengaluru, Karnataka', 'ACTIVE', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 16, 'Sneha Gupta', 'member16@fitness.com', '9876516424', '1985-08-13', '38 Random Street, Bengaluru, Karnataka', 'SUSPENDED', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 17, 'Karan Patel', 'member17@fitness.com', '9876595793', '1981-06-26', '39 Random Street, Bengaluru, Karnataka', 'SUSPENDED', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 18, 'Anjali Jain', 'member18@fitness.com', '9876535103', '1993-04-03', '31 Random Street, Bengaluru, Karnataka', 'PROSPECT', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 19, 'Rohan Mehta', 'member19@fitness.com', '9876575917', '2002-02-02', '61 Random Street, Bengaluru, Karnataka', 'ACTIVE', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 20, 'Neha Bose', 'member20@fitness.com', '9876577986', '2002-09-23', '2 Random Street, Bengaluru, Karnataka', 'PROSPECT', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 21, 'Vikash Das', 'member21@fitness.com', '9876570633', '1997-11-05', '86 Random Street, Bengaluru, Karnataka', 'ACTIVE', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 22, 'Pooja Yadav', 'member22@fitness.com', '9876597660', '1996-06-03', '86 Random Street, Bengaluru, Karnataka', 'ACTIVE', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 23, 'Siddharth Verma', 'member23@fitness.com', '9876586101', '1991-11-19', '9 Random Street, Bengaluru, Karnataka', 'ACTIVE', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 24, 'Divya Chopra', 'member24@fitness.com', '9876527401', '1994-03-26', '43 Random Street, Bengaluru, Karnataka', 'ACTIVE', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 25, 'Aditya Malhotra', 'member25@fitness.com', '9876566260', '1993-06-24', '32 Random Street, Bengaluru, Karnataka', 'SUSPENDED', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 26, 'Shruti Kaur', 'member26@fitness.com', '9876586427', '1995-10-15', '66 Random Street, Bengaluru, Karnataka', 'PROSPECT', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 27, 'Tarun Iyer', 'member27@fitness.com', '9876513427', '1999-01-12', '69 Random Street, Bengaluru, Karnataka', 'SUSPENDED', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 28, 'Megha Rao', 'member28@fitness.com', '9876545846', '1982-01-13', '42 Random Street, Bengaluru, Karnataka', 'ACTIVE', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 29, 'Kunal Nair', 'member29@fitness.com', '9876556955', '1983-09-11', '61 Random Street, Bengaluru, Karnataka', 'ACTIVE', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 30, 'Simran Reddy', 'member30@fitness.com', '9876514269', '1997-10-14', '6 Random Street, Bengaluru, Karnataka', 'ACTIVE', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 31, 'Nitin Menon', 'member31@fitness.com', '9876569587', '1992-03-07', '46 Random Street, Bengaluru, Karnataka', 'PROSPECT', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 32, 'Ritu Pillai', 'member32@fitness.com', '9876555110', '1996-04-19', '5 Random Street, Bengaluru, Karnataka', 'ACTIVE', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CLASSES (class_id, class_name, trainer_id, room_id, branch_id, start_date, end_date, weekdays, class_time, duration_mins, capacity, plan_eligibility, status, created_at, updated_at) VALUES
(9, 'Zumba Dance', 2, 5, 1, '2026-05-01', '2026-12-31', 'Tue,Thu', '13:00:00', 45, 29, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Pilates Core', 1, 5, 2, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '10:00:00', 60, 18, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Kickboxing', 1, 8, 2, '2026-05-01', '2026-12-31', 'Tue,Thu', '20:00:00', 90, 21, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'BodyPump', 2, 4, 1, '2026-05-01', '2026-12-31', 'Mon,Wed', '10:00:00', 90, 22, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'CrossFit WOD', 2, 3, 3, '2026-05-01', '2026-12-31', 'Mon,Tue,Thu', '08:00:00', 45, 24, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Aero Fit', 3, 7, 3, '2026-05-01', '2026-12-31', 'Mon,Tue,Thu', '09:00:00', 45, 23, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Bootcamp', 1, 6, 3, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '06:00:00', 45, 15, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Aqua Aerobics', 1, 6, 1, '2026-05-01', '2026-12-31', 'Sat,Sun', '13:00:00', 60, 26, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Stretching', 2, 2, 1, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '12:00:00', 60, 12, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Mobility Flow', 3, 4, 2, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '07:00:00', 90, 10, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Kettlebell Basics', 1, 7, 1, '2026-05-01', '2026-12-31', 'Mon,Wed', '11:00:00', 45, 25, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'TRX Suspension', 2, 5, 1, '2026-05-01', '2026-12-31', 'Mon,Wed,Fri', '18:00:00', 45, 19, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Strength Basics', 1, 8, 1, '2026-05-01', '2026-12-31', 'Sat,Sun', '17:00:00', 45, 11, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Core Crusher', 2, 5, 3, '2026-05-01', '2026-12-31', 'Tue,Thu', '14:00:00', 45, 10, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Step Aerobics', 2, 5, 3, '2026-05-01', '2026-12-31', 'Mon,Wed', '07:00:00', 45, 13, 'GENERAL', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO MEMBERSHIP (mem_id, member_id, plan_id, start_date, end_date, status, duration, price, discount_amount, branch_id, created_at, updated_at) VALUES
(7, 6, 3, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 8, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 9, 3, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 10, 2, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 11, 3, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 12, 3, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 13, 2, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 14, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 15, 2, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 16, 2, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 17, 1, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 18, 1, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 19, 4, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 20, 2, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 21, 4, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 22, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 23, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 24, 5, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 25, 1, '2026-05-01', '2026-12-31', 'ACTIVE', 180, 2999.0, 0.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO INVOICE (invoice_id, invoice_number, member_id, membership_id, mrp, taxes, discount, final_amount, paid_amount, outstanding, promo_code, status, created_at) VALUES
(7, 'INV-2026-007', 6, 7, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(8, 'INV-2026-008', 7, 8, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(9, 'INV-2026-009', 8, 9, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(10, 'INV-2026-010', 9, 10, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(11, 'INV-2026-011', 10, 11, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(12, 'INV-2026-012', 11, 12, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(13, 'INV-2026-013', 12, 13, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(14, 'INV-2026-014', 13, 14, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(15, 'INV-2026-015', 14, 15, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(16, 'INV-2026-016', 15, 16, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(17, 'INV-2026-017', 16, 17, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(18, 'INV-2026-018', 17, 18, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(19, 'INV-2026-019', 18, 19, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(20, 'INV-2026-020', 19, 20, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(21, 'INV-2026-021', 20, 21, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(22, 'INV-2026-022', 21, 22, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(23, 'INV-2026-023', 22, 23, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(24, 'INV-2026-024', 23, 24, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(25, 'INV-2026-025', 24, 25, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP),
(26, 'INV-2026-026', 25, 26, 2999.0, 539.8199999999999, 0.00, 3538.8199999999997, 3538.8199999999997, 0.00, NULL, 'PAID', CURRENT_TIMESTAMP);

INSERT INTO PAYMENT (payment_id, invoice_id, member_id, payment_method, amount_paid, payment_date, payment_status, failure_reason, refund_by, refund_reason, created_at) VALUES
(6, 7, 6, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(7, 8, 7, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(8, 9, 8, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(9, 10, 9, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(10, 11, 10, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(11, 12, 11, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(12, 13, 12, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(13, 14, 13, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(14, 15, 14, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(15, 16, 15, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(16, 17, 16, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(17, 18, 17, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(18, 19, 18, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(19, 20, 19, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(20, 21, 20, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(21, 22, 21, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(22, 23, 22, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(23, 24, 23, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(24, 25, 24, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP),
(25, 26, 25, 'CARD', 3538.8199999999997, CURRENT_TIMESTAMP, 'SUCCESS', NULL, NULL, NULL, CURRENT_TIMESTAMP);

INSERT INTO CLASS_BOOKING (booking_id, class_id, member_id, booking_status, created_at) VALUES
(11, 11, 22, 'CONFIRMED', CURRENT_TIMESTAMP),
(12, 11, 16, 'WAITLISTED', CURRENT_TIMESTAMP),
(13, 9, 14, 'CONFIRMED', CURRENT_TIMESTAMP),
(14, 9, 7, 'WAITLISTED', CURRENT_TIMESTAMP),
(15, 1, 12, 'CONFIRMED', CURRENT_TIMESTAMP),
(16, 22, 24, 'WAITLISTED', CURRENT_TIMESTAMP),
(17, 1, 14, 'CONFIRMED', CURRENT_TIMESTAMP),
(18, 6, 6, 'CONFIRMED', CURRENT_TIMESTAMP),
(19, 9, 9, 'CANCELLED', CURRENT_TIMESTAMP),
(20, 5, 15, 'CANCELLED', CURRENT_TIMESTAMP),
(21, 19, 8, 'CONFIRMED', CURRENT_TIMESTAMP),
(22, 7, 17, 'CANCELLED', CURRENT_TIMESTAMP),
(23, 14, 9, 'CANCELLED', CURRENT_TIMESTAMP),
(24, 23, 24, 'CANCELLED', CURRENT_TIMESTAMP),
(25, 14, 8, 'CONFIRMED', CURRENT_TIMESTAMP),
(26, 13, 13, 'WAITLISTED', CURRENT_TIMESTAMP),
(27, 7, 19, 'WAITLISTED', CURRENT_TIMESTAMP),
(28, 18, 14, 'WAITLISTED', CURRENT_TIMESTAMP),
(29, 2, 19, 'WAITLISTED', CURRENT_TIMESTAMP),
(30, 4, 15, 'CONFIRMED', CURRENT_TIMESTAMP),
(31, 19, 21, 'CONFIRMED', CURRENT_TIMESTAMP),
(32, 8, 16, 'WAITLISTED', CURRENT_TIMESTAMP),
(33, 3, 19, 'WAITLISTED', CURRENT_TIMESTAMP),
(34, 5, 15, 'CONFIRMED', CURRENT_TIMESTAMP),
(35, 14, 7, 'CONFIRMED', CURRENT_TIMESTAMP),
(36, 14, 14, 'CONFIRMED', CURRENT_TIMESTAMP),
(37, 12, 12, 'CONFIRMED', CURRENT_TIMESTAMP),
(38, 5, 7, 'WAITLISTED', CURRENT_TIMESTAMP),
(39, 2, 12, 'WAITLISTED', CURRENT_TIMESTAMP),
(40, 7, 9, 'CANCELLED', CURRENT_TIMESTAMP);

INSERT INTO ATTENDANCE (log_id, member_id, branch_id, check_in_time, check_out_time, alert_flag, scan_method, sync_status, created_at) VALUES
(9, 8, 2, '2026-05-09 00:00:00', '2026-05-09 01:54:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(10, 11, 1, '2026-05-08 00:00:00', '2026-05-08 00:45:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(11, 6, 3, '2026-05-06 00:00:00', '2026-05-06 01:36:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(12, 20, 1, '2026-05-03 00:00:00', '2026-05-03 01:07:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(13, 24, 3, '2026-05-03 00:00:00', '2026-05-03 01:25:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(14, 22, 1, '2026-05-10 00:00:00', '2026-05-10 00:54:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(15, 8, 1, '2026-05-09 00:00:00', '2026-05-09 01:17:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(16, 12, 2, '2026-05-01 00:00:00', '2026-05-01 01:00:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(17, 25, 3, '2026-05-09 00:00:00', '2026-05-09 01:17:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(18, 15, 1, '2026-05-01 00:00:00', '2026-05-01 01:53:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(19, 17, 1, '2026-05-04 00:00:00', '2026-05-04 01:41:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(20, 10, 3, '2026-05-06 00:00:00', '2026-05-06 01:46:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(21, 17, 2, '2026-05-02 00:00:00', '2026-05-02 01:19:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(22, 12, 1, '2026-05-02 00:00:00', '2026-05-02 01:35:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(23, 16, 3, '2026-05-08 00:00:00', '2026-05-08 00:57:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(24, 21, 3, '2026-05-02 00:00:00', '2026-05-02 01:44:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(25, 21, 2, '2026-05-05 00:00:00', '2026-05-05 01:57:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(26, 9, 2, '2026-05-08 00:00:00', '2026-05-08 00:47:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(27, 24, 3, '2026-05-08 00:00:00', '2026-05-08 00:45:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(28, 17, 1, '2026-05-04 00:00:00', '2026-05-04 01:48:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(29, 9, 3, '2026-05-04 00:00:00', '2026-05-04 01:25:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(30, 12, 3, '2026-05-01 00:00:00', '2026-05-01 01:23:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(31, 6, 3, '2026-05-08 00:00:00', '2026-05-08 01:54:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(32, 10, 3, '2026-05-09 00:00:00', '2026-05-09 01:05:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(33, 19, 3, '2026-05-03 00:00:00', '2026-05-03 00:57:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(34, 18, 3, '2026-05-07 00:00:00', '2026-05-07 01:58:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(35, 19, 1, '2026-05-01 00:00:00', '2026-05-01 01:57:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(36, 11, 1, '2026-05-04 00:00:00', '2026-05-04 01:21:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(37, 8, 3, '2026-05-02 00:00:00', '2026-05-02 01:34:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(38, 25, 3, '2026-05-02 00:00:00', '2026-05-02 01:40:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(39, 19, 1, '2026-05-10 00:00:00', '2026-05-10 01:17:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(40, 17, 3, '2026-05-08 00:00:00', '2026-05-08 01:32:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(41, 19, 2, '2026-05-04 00:00:00', '2026-05-04 00:49:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(42, 11, 2, '2026-05-09 00:00:00', '2026-05-09 01:58:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(43, 6, 1, '2026-05-01 00:00:00', '2026-05-01 01:12:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(44, 24, 2, '2026-05-05 00:00:00', '2026-05-05 01:36:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(45, 8, 2, '2026-05-01 00:00:00', '2026-05-01 01:51:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(46, 7, 1, '2026-05-06 00:00:00', '2026-05-06 01:01:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(47, 9, 1, '2026-05-08 00:00:00', '2026-05-08 00:45:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP),
(48, 6, 1, '2026-05-08 00:00:00', '2026-05-08 01:40:00', 0, 'QR', 'SYNCED', CURRENT_TIMESTAMP);
