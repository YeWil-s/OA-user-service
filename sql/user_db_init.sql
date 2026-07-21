-- ============================================================
-- user_db 初始化 SQL
-- 数据库：user_db (用户、组织、权限)
-- ============================================================

CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db;

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '上级部门ID，0=根部门',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(30) NOT NULL COMMENT '部门编码，唯一',
    leader_id BIGINT COMMENT '部门负责人ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0=停用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB COMMENT='部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    position_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    position_code VARCHAR(30) NOT NULL COMMENT '岗位编码，唯一',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0=停用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_position_code (position_code)
) ENGINE=InnoDB COMMENT='岗位表';

-- 用户/员工表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(30) NOT NULL COMMENT '登录账号（工号），唯一',
    password VARCHAR(200) NOT NULL COMMENT 'BCrypt加密密码',
    real_name VARCHAR(30) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(50) COMMENT '邮箱',
    gender TINYINT DEFAULT 1 COMMENT '性别：0=女，1=男',
    dept_id BIGINT COMMENT '所属部门ID',
    position_id BIGINT COMMENT '岗位ID',
    avatar_url VARCHAR(200) COMMENT '头像URL',
    entry_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0=离职，1=在职，2=冻结',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_username (username),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(30) NOT NULL COMMENT '角色编码，唯一',
    role_desc VARCHAR(200) COMMENT '角色描述',
    data_scope TINYINT DEFAULT 3 COMMENT '数据权限范围：0=全部，1=本部门及下级，2=本部门，3=本人',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0=停用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0=根',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT DEFAULT 1 COMMENT '类型：1=目录，2=菜单，3=按钮',
    path VARCHAR(200) COMMENT '前端路由path',
    component VARCHAR(200) COMMENT '前端组件路径',
    permission_code VARCHAR(100) COMMENT '权限标识（如 user:list, user:add）',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    visible TINYINT DEFAULT 1 COMMENT '是否可见：0=隐藏，1=显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0=停用，1=启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='菜单/权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 内置角色
INSERT INTO sys_role (role_name, role_code, role_desc, data_scope, sort_order) VALUES
('超级管理员', 'ROLE_ADMIN', '全部权限', 0, 1),
('HR', 'ROLE_HR', '员工管理、考勤查看、公告发布', 0, 2),
('部门主管', 'ROLE_LEADER', '下属考勤查看、审批处理、部门员工管理', 1, 3),
('普通员工', 'ROLE_EMPLOYEE', '个人打卡、请假申请、公告查看、个人信息', 3, 4);

-- 默认菜单（三级结构：目录 → 菜单 → 按钮）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order, visible) VALUES
-- 系统管理（目录）
(1, 0, '系统管理', 1, '/system', NULL, NULL, 'Setting', 1, 1),
-- 员工管理（菜单）
(2, 1, '员工管理', 2, '/system/employee', 'system/employee/index', NULL, 'User', 1, 1),
-- 员工管理按钮
(3, 2, '新增员工', 3, NULL, NULL, 'user:add', NULL, 1, 1),
(4, 2, '编辑员工', 3, NULL, NULL, 'user:edit', NULL, 2, 1),
(5, 2, '删除员工', 3, NULL, NULL, 'user:delete', NULL, 3, 1),
(6, 2, '重置密码', 3, NULL, NULL, 'user:reset-pwd', NULL, 4, 1),
-- 部门管理（菜单）
(7, 1, '部门管理', 2, '/system/dept', 'system/dept/index', NULL, 'OfficeBuilding', 2, 1),
(8, 7, '新增部门', 3, NULL, NULL, 'dept:add', NULL, 1, 1),
(9, 7, '编辑部门', 3, NULL, NULL, 'dept:edit', NULL, 2, 1),
(10, 7, '删除部门', 3, NULL, NULL, 'dept:delete', NULL, 3, 1),
-- 岗位管理（菜单）
(11, 1, '岗位管理', 2, '/system/position', 'system/position/index', NULL, 'Briefcase', 3, 1),
(12, 11, '新增岗位', 3, NULL, NULL, 'position:add', NULL, 1, 1),
(13, 11, '编辑岗位', 3, NULL, NULL, 'position:edit', NULL, 2, 1),
(14, 11, '删除岗位', 3, NULL, NULL, 'position:delete', NULL, 3, 1),
-- 角色管理（菜单）
(15, 1, '角色管理', 2, '/system/role', 'system/role/index', NULL, 'UserFilled', 4, 1),
(16, 15, '新增角色', 3, NULL, NULL, 'role:add', NULL, 1, 1),
(17, 15, '编辑角色', 3, NULL, NULL, 'role:edit', NULL, 2, 1),
(18, 15, '删除角色', 3, NULL, NULL, 'role:delete', NULL, 3, 1),
(19, 15, '分配权限', 3, NULL, NULL, 'role:assign-menu', NULL, 4, 1),
-- 菜单管理（菜单）
(20, 1, '菜单管理', 2, '/system/menu', 'system/menu/index', NULL, 'Menu', 5, 1),
(21, 20, '新增菜单', 3, NULL, NULL, 'menu:add', NULL, 1, 1),
(22, 20, '编辑菜单', 3, NULL, NULL, 'menu:edit', NULL, 2, 1),
(23, 20, '删除菜单', 3, NULL, NULL, 'menu:delete', NULL, 3, 1);

-- 为超级管理员分配所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 默认管理员账号（admin/123456）
INSERT INTO sys_user (username, password, real_name, phone, email, gender, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '管理员', '13800000000', 'admin@oa.com', 1, 1);

-- 管理员关联超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
