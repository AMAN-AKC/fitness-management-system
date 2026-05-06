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
    CONSTRAINT FK_INVOICE_MEMBERSHIP FOREIGN KEY (membership_id) REFERENCES MEMBERSHIP(membership_id),
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
acknowledged_at datetime not null,
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
  action VARCHAR(30) NOT NULL,
  old_value JSON,
  new_value JSON,
  created_at DATETIME NOT NULL,

  CONSTRAINT fk_audit_user FOREIGN KEY (performed_by) REFERENCES system_user(user_id)

);


