-- ============================================================
-- OA办公管理系统 - user_db + ai_db 建库脚本
-- 负责模块: user-service / ai-service / Android(复用后端API)
-- 版本: v1.0
-- ============================================================

-- ============================================================
-- PART 1: 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ai_db   DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- PART 2: user_db — 用户、组织、权限
-- ============================================================
USE user_db;

-- 2.1 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0  COMMENT '上级部门ID，0=根部门(顶级，无上级)',
    dept_name   VARCHAR(50)  NOT NULL             COMMENT '部门名称',
    dept_code   VARCHAR(30)  NOT NULL             COMMENT '部门编码',
    leader_id   BIGINT       DEFAULT NULL         COMMENT '负责人ID',
    sort_order  INT          NOT NULL DEFAULT 0   COMMENT '排序号，数字越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='部门表';

-- 2.2 岗位表
DROP TABLE IF EXISTS sys_position;
CREATE TABLE sys_position (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    position_name VARCHAR(50)  NOT NULL             COMMENT '岗位名称',
    position_code VARCHAR(30)  NOT NULL             COMMENT '岗位编码',
    dept_id       BIGINT       DEFAULT NULL         COMMENT '所属部门ID',
    sort_order    INT          NOT NULL DEFAULT 0   COMMENT '排序号',
    status        TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_code (position_code)
) ENGINE=InnoDB COMMENT='岗位表';

-- 2.3 用户/员工表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(30)  NOT NULL             COMMENT '登录账号(工号)',
    password        VARCHAR(200) NOT NULL             COMMENT 'BCrypt加密密码',
    real_name       VARCHAR(30)  NOT NULL             COMMENT '真实姓名',
    phone           VARCHAR(20)  DEFAULT NULL         COMMENT '手机号',
    email           VARCHAR(50)  DEFAULT NULL         COMMENT '邮箱',
    gender          TINYINT      DEFAULT NULL         COMMENT '性别: 0=女, 1=男',
    dept_id         BIGINT       DEFAULT NULL         COMMENT '所属部门ID',
    position_id     BIGINT       DEFAULT NULL         COMMENT '岗位ID',
    avatar_url      VARCHAR(200) DEFAULT NULL         COMMENT '头像URL',
    entry_date      DATE         DEFAULT NULL         COMMENT '入职日期',
    status          TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=离职, 1=在职, 2=冻结',
    last_login_time DATETIME     DEFAULT NULL         COMMENT '最后登录时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='用户/员工表';

-- 2.4 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(30)  NOT NULL             COMMENT '角色名称',
    role_code   VARCHAR(30)  NOT NULL             COMMENT '角色编码(ROLE_ADMIN/ROLE_HR/ROLE_LEADER/ROLE_EMPLOYEE)',
    role_desc   VARCHAR(200) DEFAULT NULL         COMMENT '角色描述',
    data_scope  TINYINT      NOT NULL DEFAULT 3   COMMENT '数据权限: 0=全部, 1=本部门及下级, 2=本部门, 3=本人',
    sort_order  INT          NOT NULL DEFAULT 0   COMMENT '排序号',
    status      TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 2.5 菜单/权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       BIGINT       NOT NULL DEFAULT 0   COMMENT '父菜单ID，0=根',
    menu_name       VARCHAR(50)  NOT NULL             COMMENT '菜单名称',
    menu_type       TINYINT      NOT NULL             COMMENT '类型: 1=目录(侧边栏分组), 2=菜单(可点击页面), 3=按钮(页面内操作权限)',
    path            VARCHAR(200) DEFAULT NULL         COMMENT '前端路由path(菜单型填)',
    component       VARCHAR(200) DEFAULT NULL         COMMENT '前端组件路径(菜单型填)',
    permission_code VARCHAR(100) DEFAULT NULL         COMMENT '权限标识(如 user:employee:add)',
    icon            VARCHAR(50)  DEFAULT NULL         COMMENT '菜单图标(Element Plus图标名)',
    sort_order      INT          NOT NULL DEFAULT 0   COMMENT '排序号',
    visible         TINYINT      NOT NULL DEFAULT 1   COMMENT '是否可见: 0=隐藏, 1=显示',
    status          TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='菜单/权限表';

-- 2.6 用户-角色关联表 (多对多)
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 2.7 角色-菜单关联表 (多对多)
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';


-- ============================================================
-- PART 3: ai_db — AI智能办公
-- ============================================================
USE ai_db;

-- 3.1 AI对话记录表
DROP TABLE IF EXISTS ai_conversation;
CREATE TABLE ai_conversation (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT      NOT NULL             COMMENT '用户ID',
    session_id  VARCHAR(50) NOT NULL             COMMENT '会话ID(多轮对话关联)',
    question    TEXT        DEFAULT NULL         COMMENT '用户问题',
    answer      TEXT        DEFAULT NULL         COMMENT 'AI回答',
    category    TINYINT     NOT NULL             COMMENT '类别: 1=智能填单, 2=数据分析, 3=知识问答',
    tokens_used INT         NOT NULL DEFAULT 0   COMMENT '消耗Token数',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT     NOT NULL DEFAULT 0   COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_session (session_id)
) ENGINE=InnoDB COMMENT='AI对话记录表';

-- 3.2 AI分析报告表
DROP TABLE IF EXISTS ai_analysis_report;
CREATE TABLE ai_analysis_report (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    report_type     TINYINT      NOT NULL             COMMENT '报告类型: 1=考勤异常分析, 2=效能分析, 3=风险预警',
    target_user_id  BIGINT       DEFAULT NULL         COMMENT '目标员工ID(个人报告)',
    target_dept_id  BIGINT       DEFAULT NULL         COMMENT '目标部门ID(部门报告)',
    analysis_period VARCHAR(20)  NOT NULL             COMMENT '分析周期(如 2026-07)',
    content         TEXT         DEFAULT NULL         COMMENT '报告内容(JSON格式)',
    summary         VARCHAR(500) DEFAULT NULL         COMMENT '报告摘要',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_period (analysis_period),
    KEY idx_type (report_type)
) ENGINE=InnoDB COMMENT='AI分析报告表';


-- ============================================================
-- PART 4: 预设数据
-- ============================================================

-- ---------- user_db 初始化 ----------
USE user_db;

-- 部门
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order) VALUES
(1, 0, '总公司',     'ROOT',        1),
(2, 1, '技术部',     'TECH',        1),
(3, 1, '人事部',     'HR',          2),
(4, 1, '财务部',     'FINANCE',     3),
(5, 1, '市场部',     'MARKET',      4);

-- 岗位
INSERT INTO sys_position (id, position_name, position_code, dept_id, sort_order) VALUES
(1, '总经理',       'GM',             1, 1),
(2, '技术总监',     'TECH_DIRECTOR',  2, 1),
(3, 'Java开发工程师','JAVA_DEV',      2, 2),
(4, '前端开发工程师','FE_DEV',        2, 3),
(5, 'HR经理',       'HR_MANAGER',     3, 1),
(6, 'HR专员',       'HR_SPECIALIST',  3, 2),
(7, '财务经理',     'FINANCE_MANAGER',4, 1),
(8, '市场经理',     'MARKET_MANAGER', 5, 1),
(9, '市场专员',     'MARKET_SPECIALIST',5,2);

-- 用户（密码均为 123456 的BCrypt哈希，运行时用BCryptPasswordEncoder加密替换）
INSERT INTO sys_user (id, username, password, real_name, phone, email, gender, dept_id, position_id, entry_date, status) VALUES
(1, 'admin',    '$2a$10$ibW83Nv5NmQh1e1TlU8tdOazMzoe4aPsQRIJ2y9N6fqXuc7.FxiRK', '系统管理员', '13800000000', 'admin@oa.com',    1, 1, 1, '2024-01-01', 1),
(2, 'zhangsan', '$2a$10$ibW83Nv5NmQh1e1TlU8tdOazMzoe4aPsQRIJ2y9N6fqXuc7.FxiRK', '张三',       '13800000001', 'zhangsan@oa.com', 1, 2, 3, '2024-06-01', 1),
(3, 'lisi',     '$2a$10$ibW83Nv5NmQh1e1TlU8tdOazMzoe4aPsQRIJ2y9N6fqXuc7.FxiRK', '李四',       '13800000002', 'lisi@oa.com',     1, 3, 5, '2024-08-15', 1),
(4, 'wangwu',   '$2a$10$ibW83Nv5NmQh1e1TlU8tdOazMzoe4aPsQRIJ2y9N6fqXuc7.FxiRK', '王五',       '13800000003', 'wangwu@oa.com',   1, 2, 4, '2025-01-10', 1);

-- 角色
INSERT INTO sys_role (id, role_name, role_code, role_desc, data_scope, sort_order) VALUES
(1, '超级管理员', 'ROLE_ADMIN',    '系统全部权限',     0, 1),
(2, 'HR',        'ROLE_HR',       '人事管理与考勤查看', 1, 2),
(3, '部门主管',   'ROLE_LEADER',   '部门管理与审批',    2, 3),
(4, '普通员工',   'ROLE_EMPLOYEE', '个人办公功能',      3, 4);

-- 菜单（三级: 目录/菜单/按钮）
-- 一级目录(parent_id=0)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(1,  0, '系统管理',   1, '/system',     NULL,              NULL,              'Setting',         1),
(2,  0, '考勤管理',   1, '/attendance', NULL,              NULL,              'Clock',           2),
(3,  0, '审批管理',   1, '/approval',   NULL,              NULL,              'Document',        3),
(4,  0, '公告通知',   1, '/notice',     NULL,              NULL,              'Bell',            4),
(5,  0, '资产管理',   1, '/asset',      NULL,              NULL,              'Box',             5),
(6,  0, 'AI助手',    1, '/ai',         NULL,              NULL,              'ChatDotRound',    6),
(7,  0, '数据大屏',   1, '/visual',     NULL,              NULL,              'DataAnalysis',    7);

-- 系统管理子菜单(parent_id=1)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(11, 1, '员工管理',   2, 'employee',    'system/employee',  'user:employee:list',   'User',            1),
(12, 1, '部门管理',   2, 'dept',        'system/dept',      'user:dept:list',       'OfficeBuilding',  2),
(13, 1, '岗位管理',   2, 'position',    'system/position',  'user:position:list',   'Briefcase',       3),
(14, 1, '角色管理',   2, 'role',        'system/role',      'user:role:list',       'Avatar',          4),
(15, 1, '菜单管理',   2, 'menu',        'system/menu',      'user:menu:list',       'Menu',            5);

-- 员工管理按钮(parent_id=11)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(111, 11, '新增员工', 3, NULL, NULL, 'user:employee:add',    NULL, 1),
(112, 11, '编辑员工', 3, NULL, NULL, 'user:employee:edit',   NULL, 2),
(113, 11, '删除员工', 3, NULL, NULL, 'user:employee:delete', NULL, 3);

-- 考勤管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(21, 2, '打卡页面',   2, 'punch',      'attendance/punch',  'attendance:punch',    'Check',    1),
(22, 2, '考勤记录',   2, 'record',     'attendance/record', 'attendance:record',   'Tickets',  2),
(23, 2, '班次管理',   2, 'shift',      'attendance/shift',  'attendance:shift',    'Timer',    3);

-- 审批管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(31, 3, '我的申请',   2, 'my-apply',   'approval/my-apply', 'approval:my',         'Edit',     1),
(32, 3, '提交申请',   2, 'submit',     'approval/submit',   'approval:submit',     'Plus',     2),
(33, 3, '待审批',     2, 'pending',    'approval/pending',  'approval:pending',    'Clock',    3),
(34, 3, '已办审批',   2, 'processed',  'approval/processed','approval:processed',  'Select',   4);

-- 公告通知子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(41, 4, '公告列表',   2, 'list',       'notice/list',       'notice:list',         'Notebook',         1),
(42, 4, '消息中心',   2, 'message',    'notice/message',    'notice:message',      'ChatLineSquare',   2);

-- 资产管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(51, 5, '资产台账',   2, 'assets',     'asset/assets',      'asset:list',          'Package',     1),
(52, 5, '人事变动',   2, 'staff',      'asset/staff',       'asset:staff',         'Connection',  2);

-- AI助手子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(61, 6, 'AI助手',     2, 'assistant',  'ai/assistant',      'ai:assistant',        'Bot',         1);

-- 数据大屏子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
(71, 7, '数据大屏',   2, 'screen',     'visual/screen',     'visual:screen',       'Monitor',      1),
(72, 7, '数据报表',   2, 'reports',    'visual/reports',    'visual:reports',      'DataLine',     2);

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),   -- admin = 超级管理员
(2, 4),   -- zhangsan = 普通员工
(3, 3),   -- lisi = 部门主管
(4, 4);   -- wangwu = 普通员工

-- 角色-菜单关联
-- 超级管理员(role_id=1): 拥有所有菜单(menu_type=1或2)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_type IN (1, 2);

-- 普通员工(role_id=4): 打卡 + 申请 + 公告 + AI
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 2), (4, 21), (4, 22),   -- 考勤管理 + 打卡页面 + 考勤记录
(4, 3), (4, 31), (4, 32),   -- 审批管理 + 我的申请 + 提交申请
(4, 4), (4, 41), (4, 42),   -- 公告通知 + 公告列表 + 消息中心
(4, 6), (4, 61);             -- AI助手 + AI对话

-- 部门主管(role_id=3): 比普通员工多审批权限 + 部门考勤
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 2), (3, 21), (3, 22),   -- 考勤管理
(3, 3), (3, 31), (3, 32), (3, 33), (3, 34),  -- 审批管理(全)
(3, 4), (3, 41), (3, 42),   -- 公告通知
(3, 6), (3, 61);             -- AI助手

-- HR(role_id=2): 员工管理 + 考勤查看 + 公告发布 + 审批
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 11), (2, 12), (2, 13),    -- 系统管理(员工+部门+岗位)
(2, 2), (2, 21), (2, 22), (2, 23),    -- 考勤管理(全)
(2, 3), (2, 31), (2, 32), (2, 33), (2, 34),  -- 审批管理(全)
(2, 4), (2, 41), (2, 42),             -- 公告通知
(2, 5), (2, 51), (2, 52),             -- 资产管理(全)
(2, 7), (2, 71), (2, 72);             -- 数据大屏+报表


-- ---------- ai_db 无预设数据(运行时动态生成) ----------
USE ai_db;
-- ai_conversation 和 ai_analysis_report 均为空表，运行时由AI服务写入
