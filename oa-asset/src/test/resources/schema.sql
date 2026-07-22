DROP TABLE IF EXISTS ast_asset_record;
DROP TABLE IF EXISTS ast_asset;
DROP TABLE IF EXISTS ast_staff_change;
DROP TABLE IF EXISTS ast_employee_archive;

CREATE TABLE ast_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name VARCHAR(100) NOT NULL,
    asset_code VARCHAR(50) NOT NULL UNIQUE,
    category TINYINT NOT NULL,
    model VARCHAR(50),
    purchase_date DATE,
    purchase_price DECIMAL(10,2),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ast_asset_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    expect_return_date DATE,
    actual_return_date DATE,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ast_employee_archive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    id_card VARCHAR(18), education TINYINT, major VARCHAR(50), graduate_school VARCHAR(50),
    address VARCHAR(200), emergency_contact VARCHAR(30), emergency_phone VARCHAR(20),
    contract_start DATE, contract_end DATE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ast_staff_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL, change_type TINYINT NOT NULL,
    before_dept BIGINT, after_dept BIGINT, before_position BIGINT, after_position BIGINT,
    change_date DATE NOT NULL, remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
