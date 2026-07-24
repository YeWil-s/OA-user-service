-- ============================================================
-- OA办公管理系统 - 全量数据库初始化脚本
-- 版本: v1.4
-- 说明: 依次创建7个微服务数据库及全部表结构，并插入预设数据
-- 使用: 直接在本机MySQL中执行此文件即可
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS attendance_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS approval_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notice_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS asset_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ai_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS statistics_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 2. user_db: 用户、组织、权限
-- ============================================================
USE user_db;

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0  COMMENT '上级部门ID，0=根部门',
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

-- 岗位表
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

-- 用户/员工表
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

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(30)  NOT NULL             COMMENT '角色名称',
    role_code   VARCHAR(30)  NOT NULL             COMMENT '角色编码',
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

-- 菜单/权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       BIGINT       NOT NULL DEFAULT 0   COMMENT '父菜单ID，0=根',
    menu_name       VARCHAR(50)  NOT NULL             COMMENT '菜单名称',
    menu_type       TINYINT      NOT NULL             COMMENT '类型: 1=目录, 2=菜单, 3=按钮',
    path            VARCHAR(200) DEFAULT NULL         COMMENT '前端路由path',
    component       VARCHAR(200) DEFAULT NULL         COMMENT '前端组件路径',
    permission_code VARCHAR(100) DEFAULT NULL         COMMENT '权限标识(如 user:list)',
    icon            VARCHAR(50)  DEFAULT NULL         COMMENT '菜单图标',
    sort_order      INT          NOT NULL DEFAULT 0   COMMENT '排序号',
    visible         TINYINT      NOT NULL DEFAULT 1   COMMENT '是否可见: 0=隐藏, 1=显示',
    status          TINYINT      NOT NULL DEFAULT 1   COMMENT '状态',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='菜单/权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- 岗位角色关联表
DROP TABLE IF EXISTS sys_position_role;
CREATE TABLE sys_position_role (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    position_id BIGINT NOT NULL COMMENT '岗位ID',
    role_id     BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_role (position_id, role_id)
) ENGINE=InnoDB COMMENT='岗位角色关联表';

-- ============================================================
-- 3. attendance_db: 考勤打卡
-- ============================================================
USE attendance_db;

-- 班次表（模板）
DROP TABLE IF EXISTS att_shift;
CREATE TABLE att_shift (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    shift_name  VARCHAR(50) NOT NULL             COMMENT '班次名称',
    start_time  TIME        NOT NULL             COMMENT '上班标准时间',
    end_time    TIME        NOT NULL             COMMENT '下班标准时间',
    flex_start  TIME        DEFAULT NULL         COMMENT '弹性打卡起始时间(弹性班才填)',
    flex_end    TIME        DEFAULT NULL         COMMENT '弹性打卡截止时间(弹性班才填)',
    status      TINYINT     NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='班次表';

-- 打卡流水表
DROP TABLE IF EXISTS att_record;
CREATE TABLE att_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT       NOT NULL             COMMENT '员工ID',
    record_date    DATE         NOT NULL             COMMENT '打卡日期',
    punch_in_time  DATETIME     DEFAULT NULL         COMMENT '上班打卡时间',
    punch_out_time DATETIME     DEFAULT NULL         COMMENT '下班打卡时间',
    punch_type     TINYINT      NOT NULL DEFAULT 1   COMMENT '打卡类型: 1=现场, 2=外勤',
    device_info    VARCHAR(100) DEFAULT NULL         COMMENT '设备信息/IP',
    location       VARCHAR(200) DEFAULT NULL         COMMENT '打卡地点名称',
    latitude       DECIMAL(10,7) DEFAULT NULL        COMMENT '纬度',
    longitude      DECIMAL(10,7) DEFAULT NULL        COMMENT '经度',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB COMMENT='打卡流水表';

-- 每日考勤统计表（XXL-Job定时任务产出）
DROP TABLE IF EXISTS att_daily_summary;
CREATE TABLE att_daily_summary (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT      NOT NULL             COMMENT '员工ID',
    dept_id         BIGINT      NOT NULL             COMMENT '部门ID(记录统计时刻的部门)',
    summary_date    DATE        NOT NULL             COMMENT '统计日期',
    shift_id        BIGINT      DEFAULT NULL         COMMENT '班次ID',
    punch_in_time   DATETIME    DEFAULT NULL         COMMENT '实际上班打卡时间',
    punch_out_time  DATETIME    DEFAULT NULL         COMMENT '实际下班打卡时间',
    status          TINYINT     NOT NULL             COMMENT '考勤状态: 1=正常, 2=迟到, 3=早退, 4=旷工, 5=缺卡, 6=请假, 7=加班',
    late_minutes    INT         NOT NULL DEFAULT 0   COMMENT '迟到分钟数',
    early_minutes   INT         NOT NULL DEFAULT 0   COMMENT '早退分钟数',
    work_hours      DECIMAL(5,1) NOT NULL DEFAULT 0  COMMENT '出勤工时',
    overtime_hours  DECIMAL(5,1) NOT NULL DEFAULT 0  COMMENT '加班工时',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, summary_date),
    KEY idx_dept_date (dept_id, summary_date)
) ENGINE=InnoDB COMMENT='每日考勤统计表';

-- 用户班次关联表
DROP TABLE IF EXISTS user_shift;
CREATE TABLE user_shift (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL             COMMENT '员工ID',
    shift_id    BIGINT   NOT NULL             COMMENT '班次ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB COMMENT='用户班次关联表';

-- 每日排班表
DROP TABLE IF EXISTS att_schedule;
CREATE TABLE att_schedule (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT   NOT NULL             COMMENT '员工ID',
    schedule_date  DATE     NOT NULL             COMMENT '排班日期',
    shift_id       BIGINT   NOT NULL             COMMENT '班次ID',
    status         TINYINT  DEFAULT 1            COMMENT '1=正常 2=请假',
    overtime_hours DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '加班小时数(审批通过后写入)',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, schedule_date)
) ENGINE=InnoDB COMMENT='每日排班表';

-- ============================================================
-- 4. approval_db: 审批流程
-- ============================================================
USE approval_db;

-- 申请单表
DROP TABLE IF EXISTS app_application;
CREATE TABLE app_application (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    application_no      VARCHAR(50)  NOT NULL             COMMENT '申请单号(如 LV20260720001)',
    user_id             BIGINT       NOT NULL             COMMENT '申请人ID',
    dept_id             BIGINT       NOT NULL             COMMENT '申请人部门ID',
    app_type            TINYINT      NOT NULL             COMMENT '申请类型: 1=请假, 2=加班, 3=外出, 4=调岗, 5=资产领用',
    leave_type          TINYINT      DEFAULT NULL         COMMENT '请假子类型: 1=年假, 2=事假, 3=病假, 4=婚假, 5=产假(仅app_type=1时有效)',
    start_time          DATETIME     DEFAULT NULL         COMMENT '开始时间(app_type=1/2/3时必填)',
    end_time            DATETIME     DEFAULT NULL         COMMENT '结束时间(app_type=1/2/3时必填)',
    duration            DECIMAL(5,1) DEFAULT NULL         COMMENT '时长(天/小时, app_type=1/2/3时必填)',
    target_dept_id      BIGINT       DEFAULT NULL         COMMENT '目标部门ID(app_type=4时)',
    target_position_id  BIGINT       DEFAULT NULL         COMMENT '目标岗位ID(app_type=4时)',
    asset_id            BIGINT       DEFAULT NULL         COMMENT '资产ID(app_type=5时)',
    recipient_id        BIGINT       DEFAULT NULL         COMMENT '领取人ID(app_type=5时，默认同申请人)',
    expect_return_date  DATE         DEFAULT NULL         COMMENT '预计归还日期(app_type=5时)',
    reason              VARCHAR(500) DEFAULT NULL         COMMENT '申请原因',
    attachments         VARCHAR(500) DEFAULT NULL         COMMENT '附件URL,逗号分隔',
    status              TINYINT      NOT NULL DEFAULT 0   COMMENT '状态: 0=草稿, 1=审批中, 2=已通过, 3=已驳回, 4=已撤销',
    current_approver_id BIGINT       DEFAULT NULL         COMMENT '当前审批人ID',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_no (application_no),
    KEY idx_user_status (user_id, status),
    KEY idx_approver (current_approver_id)
) ENGINE=InnoDB COMMENT='申请单表';

-- 审批记录表
DROP TABLE IF EXISTS app_approval_record;
CREATE TABLE app_approval_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    application_id BIGINT       NOT NULL             COMMENT '申请单ID',
    approver_id    BIGINT       NOT NULL             COMMENT '审批人ID',
    action         TINYINT      NOT NULL             COMMENT '操作: 1=同意, 2=驳回',
    comment        VARCHAR(500) DEFAULT NULL         COMMENT '审批意见',
    action_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_application (application_id)
) ENGINE=InnoDB COMMENT='审批记录表';

-- ============================================================
-- 5. notice_db: 公告通知
-- ============================================================
USE notice_db;

-- 公告表
DROP TABLE IF EXISTS ntc_notice;
CREATE TABLE ntc_notice (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(200) NOT NULL             COMMENT '公告标题',
    content      TEXT         DEFAULT NULL         COMMENT '公告内容(富文本)',
    publisher_id BIGINT       NOT NULL             COMMENT '发布人ID',
    notice_type  TINYINT      NOT NULL DEFAULT 1   COMMENT '类型: 1=公司公告, 2=部门通知, 3=系统通知',
    target_type  TINYINT      NOT NULL DEFAULT 1   COMMENT '发布范围: 1=全公司, 2=指定部门, 3=指定人',
    target_ids   VARCHAR(500) DEFAULT NULL         COMMENT '目标ID列表,逗号分隔(target_type非1时有效)',
    start_time   DATETIME     DEFAULT NULL         COMMENT '生效时间',
    end_time     DATETIME     DEFAULT NULL         COMMENT '失效时间',
    status       TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=草稿, 1=已发布, 2=已下架',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status_time (status, create_time)
) ENGINE=InnoDB COMMENT='公告表';

-- 已读状态表
DROP TABLE IF EXISTS ntc_read_status;
CREATE TABLE ntc_read_status (
    id        BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    notice_id BIGINT   NOT NULL             COMMENT '公告ID',
    user_id   BIGINT   NOT NULL             COMMENT '用户ID',
    is_read   TINYINT  NOT NULL DEFAULT 0   COMMENT '是否已读: 0=未读, 1=已读',
    read_time DATETIME DEFAULT NULL         COMMENT '阅读时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_user (notice_id, user_id)
) ENGINE=InnoDB COMMENT='已读状态表';

-- 站内消息表
DROP TABLE IF EXISTS ntc_message;
CREATE TABLE ntc_message (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL             COMMENT '接收人ID',
    title       VARCHAR(200) NOT NULL             COMMENT '消息标题',
    content     VARCHAR(500) DEFAULT NULL         COMMENT '消息内容',
    msg_type    TINYINT      NOT NULL             COMMENT '消息类型: 1=审批通知, 2=考勤通知, 3=系统通知',
    related_id  BIGINT       DEFAULT NULL         COMMENT '关联业务ID',
    is_read     TINYINT      NOT NULL DEFAULT 0   COMMENT '是否已读: 0=未读, 1=已读',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read)
) ENGINE=InnoDB COMMENT='站内消息表';

-- ============================================================
-- 6. asset_db: 资产与人事
-- ============================================================
USE asset_db;

-- 员工档案表
DROP TABLE IF EXISTS ast_employee_archive;
CREATE TABLE ast_employee_archive (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id           BIGINT       NOT NULL             COMMENT '关联用户ID',
    id_card           VARCHAR(18)  DEFAULT NULL         COMMENT '身份证号',
    education         TINYINT      DEFAULT NULL         COMMENT '学历: 1=高中, 2=大专, 3=本科, 4=硕士, 5=博士',
    major             VARCHAR(50)  DEFAULT NULL         COMMENT '专业',
    graduate_school   VARCHAR(50)  DEFAULT NULL         COMMENT '毕业院校',
    address           VARCHAR(200) DEFAULT NULL         COMMENT '现住址',
    emergency_contact VARCHAR(30)  DEFAULT NULL         COMMENT '紧急联系人',
    emergency_phone   VARCHAR(20)  DEFAULT NULL         COMMENT '紧急联系电话',
    contract_start    DATE         DEFAULT NULL         COMMENT '合同开始日期',
    contract_end      DATE         DEFAULT NULL         COMMENT '合同结束日期',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB COMMENT='员工档案表';

-- 人事变动记录表
DROP TABLE IF EXISTS ast_staff_change;
CREATE TABLE ast_staff_change (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT       NOT NULL             COMMENT '员工ID',
    change_type    TINYINT      NOT NULL             COMMENT '变动类型: 1=入职, 2=转正, 3=调岗, 4=离职',
    before_dept    BIGINT       DEFAULT NULL         COMMENT '变动前部门ID',
    after_dept     BIGINT       DEFAULT NULL         COMMENT '变动后部门ID',
    before_position BIGINT      DEFAULT NULL         COMMENT '变动前岗位ID',
    after_position BIGINT       DEFAULT NULL         COMMENT '变动后岗位ID',
    change_date    DATE         NOT NULL             COMMENT '变动日期',
    remark         VARCHAR(500) DEFAULT NULL         COMMENT '备注',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='人事变动记录表';

-- 资产信息表
DROP TABLE IF EXISTS ast_asset;
CREATE TABLE ast_asset (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    asset_name     VARCHAR(100) NOT NULL             COMMENT '资产名称',
    asset_code     VARCHAR(50)  NOT NULL             COMMENT '资产编码',
    category       TINYINT      NOT NULL             COMMENT '分类: 1=固定资产, 2=办公用品, 3=电子设备',
    model          VARCHAR(50)  DEFAULT NULL         COMMENT '规格型号',
    purchase_date  DATE         DEFAULT NULL         COMMENT '购置日期',
    purchase_price DECIMAL(10,2) DEFAULT NULL        COMMENT '购置价格',
    status         TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=报废, 1=可领用, 2=已领用',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_code (asset_code)
) ENGINE=InnoDB COMMENT='资产信息表';

-- 资产领用记录表
DROP TABLE IF EXISTS ast_asset_record;
CREATE TABLE ast_asset_record (
    id                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    asset_id           BIGINT   NOT NULL             COMMENT '资产ID',
    user_id            BIGINT   NOT NULL             COMMENT '领用人ID',
    borrow_date        DATE     NOT NULL             COMMENT '领用日期',
    expect_return_date DATE     DEFAULT NULL         COMMENT '预计归还日期',
    actual_return_date DATE     DEFAULT NULL         COMMENT '实际归还日期',
    status             TINYINT  NOT NULL DEFAULT 1   COMMENT '状态: 1=领用中, 2=已归还',
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='资产领用记录表';

-- ============================================================
-- 7. ai_db: AI服务
-- ============================================================
USE ai_db;

-- AI对话记录表
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

-- AI分析报告表
DROP TABLE IF EXISTS ai_analysis_report;
CREATE TABLE ai_analysis_report (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    report_type     TINYINT      NOT NULL             COMMENT '报告类型: 1=考勤异常, 2=效能分析, 3=风险预警',
    target_user_id  BIGINT       DEFAULT NULL         COMMENT '目标员工ID(个人级别报告)',
    target_dept_id  BIGINT       DEFAULT NULL         COMMENT '目标部门ID(部门级别报告)',
    analysis_period VARCHAR(20)  NOT NULL             COMMENT '分析周期(如 2026-07)',
    content         TEXT         DEFAULT NULL         COMMENT '报告内容(JSON格式)',
    summary         VARCHAR(500) DEFAULT NULL         COMMENT '报告摘要',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_period (analysis_period)
) ENGINE=InnoDB COMMENT='AI分析报告表';

-- 知识文档表
DROP TABLE IF EXISTS ai_knowledge_doc;
CREATE TABLE ai_knowledge_doc (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title            VARCHAR(200) NOT NULL             COMMENT '文档标题',
    content          LONGTEXT     NOT NULL             COMMENT '文档正文(Markdown格式)',
    summary          VARCHAR(500) DEFAULT NULL         COMMENT '文档摘要(用于列表卡片展示)',
    category         TINYINT      NOT NULL             COMMENT '分类: 1=公司制度, 2=操作流程, 3=HR政策, 4=财务制度, 5=IT规范, 6=其他',
    dept_id          BIGINT       DEFAULT NULL         COMMENT '所属部门ID',
    access_roles     VARCHAR(200) NOT NULL DEFAULT '["ROLE_EMPLOYEE","ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]' COMMENT '可访问角色(JSON数组)',
    access_positions VARCHAR(500) DEFAULT NULL         COMMENT '可访问岗位(JSON数组)',
    access_depts     VARCHAR(500) DEFAULT NULL         COMMENT '可访问部门(JSON数组)',
    access_mode      TINYINT      DEFAULT 0            COMMENT '访问模式: 0=全部可见, 1=按角色, 2=按部门岗位',
    version          INT          DEFAULT 1            COMMENT '版本号',
    vector_status    TINYINT      DEFAULT 0            COMMENT '向量状态: 0=未同步, 1=已同步, 2=同步失败',
    vector_error     VARCHAR(500) DEFAULT NULL         COMMENT '向量同步错误信息',
    status           TINYINT      NOT NULL DEFAULT 1   COMMENT '状态: 0=停用, 1=启用',
    create_by        BIGINT       DEFAULT NULL         COMMENT '创建人ID',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_vector_status (vector_status)
) ENGINE=InnoDB COMMENT='知识文档表';

-- 知识标签表
DROP TABLE IF EXISTS ai_knowledge_tag;
CREATE TABLE ai_knowledge_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tag_name    VARCHAR(50)  NOT NULL             COMMENT '标签名称',
    tag_code    VARCHAR(30)  NOT NULL             COMMENT '标签编码(唯一标识)',
    tag_desc    VARCHAR(200) DEFAULT NULL         COMMENT '标签描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0   COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_code (tag_code)
) ENGINE=InnoDB COMMENT='知识标签表';

-- 知识文档-标签关联表
DROP TABLE IF EXISTS ai_knowledge_doc_tag;
CREATE TABLE ai_knowledge_doc_tag (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id  BIGINT NOT NULL COMMENT '文档ID',
    tag_id  BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_tag (doc_id, tag_id),
    KEY idx_doc_id (doc_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB COMMENT='知识文档-标签关联表';

-- ============================================================
-- 8. statistics_db: 可视化统计数据
-- ============================================================
USE statistics_db;

-- 月度考勤统计
DROP TABLE IF EXISTS stat_attendance_monthly;
CREATE TABLE stat_attendance_monthly (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    stat_month       VARCHAR(7)   NOT NULL             COMMENT '统计月份(如 2026-07)',
    dept_id          BIGINT       NOT NULL             COMMENT '部门ID',
    total_employees  INT          NOT NULL DEFAULT 0   COMMENT '部门总人数',
    normal_count     INT          NOT NULL DEFAULT 0   COMMENT '正常出勤人次',
    late_count       INT          NOT NULL DEFAULT 0   COMMENT '迟到达人次',
    early_count      INT          NOT NULL DEFAULT 0   COMMENT '早退人次',
    absent_count     INT          NOT NULL DEFAULT 0   COMMENT '旷工人次',
    leave_count      INT          NOT NULL DEFAULT 0   COMMENT '请假人次',
    overtime_total   DECIMAL(10,1) NOT NULL DEFAULT 0  COMMENT '加班总时长',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_month_dept (stat_month, dept_id)
) ENGINE=InnoDB COMMENT='月度考勤统计';

-- 审批数据汇总
DROP TABLE IF EXISTS stat_approval_summary;
CREATE TABLE stat_approval_summary (
    id                 BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    stat_month         VARCHAR(7)  NOT NULL             COMMENT '统计月份',
    dept_id            BIGINT      NOT NULL             COMMENT '部门ID',
    total_applications INT         NOT NULL DEFAULT 0   COMMENT '申请总数',
    approved_count     INT         NOT NULL DEFAULT 0   COMMENT '通过数',
    rejected_count     INT         NOT NULL DEFAULT 0   COMMENT '驳回数',
    pending_count      INT         NOT NULL DEFAULT 0   COMMENT '待审批数',
    avg_approval_hours DECIMAL(5,1) NOT NULL DEFAULT 0  COMMENT '平均审批耗时(小时)',
    create_time        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_month_dept (stat_month, dept_id)
) ENGINE=InnoDB COMMENT='审批数据汇总';

-- 部门概览统计
DROP TABLE IF EXISTS stat_dept_overview;
CREATE TABLE stat_dept_overview (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    stat_month       VARCHAR(7)   NOT NULL             COMMENT '统计月份',
    dept_id          BIGINT       NOT NULL             COMMENT '部门ID',
    active_employees INT          NOT NULL DEFAULT 0   COMMENT '在职人数',
    new_hires        INT          NOT NULL DEFAULT 0   COMMENT '入职人数',
    resignations     INT          NOT NULL DEFAULT 0   COMMENT '离职人数',
    attendance_rate  DECIMAL(5,2) NOT NULL DEFAULT 0   COMMENT '出勤率(%)',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_month_dept (stat_month, dept_id)
) ENGINE=InnoDB COMMENT='部门概览统计';


-- ============================================================
-- 9. 预设数据 (初始化)
-- ============================================================

-- ---------- user_db ----------
USE user_db;

-- 部门
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order, leader_id) VALUES
(1, 0, '总公司',     'ROOT',        1, 1),
(2, 1, '技术部',     'TECH',        1, 2),
(3, 1, '人事部',     'HR',          2, 3),
(4, 1, '财务部',     'FINANCE',     3, 4),
(5, 1, '市场部',     'MARKET',      4, 1);

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

-- 账号: admin / 密码: admin123
-- 账号: zhangsan / 密码: 123456
-- 账号: lisi / 密码: 123456
-- 账号: wangwu / 密码: 123456
-- BCrypt在线生成, 10轮加密
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

-- 菜单（三级: 目录 / 菜单 / 按钮）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission_code, icon, sort_order) VALUES
-- 一级目录
(1,  0, '系统管理',   1, '/system',     NULL,              NULL,            'Setting',     1),
(2,  0, '考勤管理',   1, '/attendance', NULL,              NULL,            'Clock',       2),
(3,  0, '审批管理',   1, '/approval',   NULL,              NULL,            'Document',    3),
(4,  0, '公告通知',   1, '/notice',     NULL,              NULL,            'Bell',        4),
(5,  0, '资产管理',   1, '/asset',      NULL,              NULL,            'Box',         5),
(6,  0, 'AI助手',    1, '/ai',         NULL,              NULL,            'ChatDotRound',6),
(7,  0, '数据大屏',   1, '/visual',     NULL,              NULL,            'DataAnalysis',7),
-- 系统管理子菜单
(11, 1, '员工管理',   2, 'employee',    'system/employee',  'user:employee:list', 'User',   1),
(12, 1, '部门管理',   2, 'dept',        'system/dept',      'user:dept:list',     'OfficeBuilding', 2),
(13, 1, '岗位管理',   2, 'position',    'system/position',  'user:position:list', 'Briefcase', 3),
(14, 1, '角色管理',   2, 'role',        'system/role',      'user:role:list',     'Avatar',    4),
(15, 1, '菜单管理',   2, 'menu',        'system/menu',      'user:menu:list',     'Menu',      5),
-- 员工管理按钮
(111, 11, '新增员工', 3, NULL, NULL, 'user:employee:add',    NULL, 1),
(112, 11, '编辑员工', 3, NULL, NULL, 'user:employee:edit',   NULL, 2),
(113, 11, '删除员工', 3, NULL, NULL, 'user:employee:delete', NULL, 3),
-- 考勤管理子菜单
(21, 2, '打卡页面',   2, 'punch',      'attendance/punch',  'attendance:punch',  'Check',  1),
(22, 2, '考勤记录',   2, 'record',     'attendance/record', 'attendance:record', 'Tickets',2),
(23, 2, '班次管理',   2, 'shift',      'attendance/shift',  'attendance:shift',  'Timer',  3),
-- 审批管理子菜单
(31, 3, '我的申请',   2, 'my-apply',   'approval/my-apply', 'approval:my',      'Edit',   1),
(32, 3, '提交申请',   2, 'submit',     'approval/submit',   'approval:submit',  'Plus',   2),
(33, 3, '待审批',     2, 'pending',    'approval/pending',  'approval:pending', 'Clock',  3),
(34, 3, '已办审批',   2, 'processed',  'approval/processed','approval:processed','Select',4),
-- 公告通知子菜单
(41, 4, '公告列表',   2, 'list',       'notice/list',       'notice:list',       'Notebook', 1),
(42, 4, '消息中心',   2, 'message',    'notice/message',    'notice:message',    'ChatLineSquare', 2),
-- 资产管理子菜单
(51, 5, '资产台账',   2, 'assets',     'asset/assets',      'asset:list',        'Package',   1),
(52, 5, '人事变动',   2, 'staff',      'asset/staff',       'asset:staff',       'Connection', 2),
-- AI助手子菜单
(61, 6, 'AI助手',     2, 'assistant',  'ai/assistant',      'ai:assistant',      'Bot',         1),
-- 数据大屏子菜单
(71, 7, '数据大屏',   2, 'screen',     'visual/screen',     'visual:screen',     'Monitor',   1),
(72, 7, '数据报表',   2, 'reports',    'visual/reports',    'visual:reports',    'DataLine',  2);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin = 超级管理员
(2, 4),  -- zhangsan = 普通员工
(3, 3),  -- lisi = 部门主管(HR经理)
(4, 4);  -- wangwu = 普通员工

-- 岗位角色关联
INSERT INTO sys_position_role (position_id, role_id) VALUES
(1, 1),   -- 总经理 → 超级管理员
(5, 2),   -- HR经理 → HR管理员
(2, 3),   -- 技术总监 → 部门主管
(7, 3),   -- 财务经理 → 部门主管
(8, 3);   -- 市场经理 → 部门主管

-- 角色菜单关联（超级管理员拥有全部菜单）
-- 简化写法: 超级管理员(role_id=1)绑定所有页面级菜单(menu_type=1或2)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_type IN (1, 2);

-- 普通员工(role_id=4)只看考勤打卡、审批、公告、AI助手
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 2), (4, 21), (4, 22),   -- 考勤管理 + 打卡 + 记录
(4, 3), (4, 31), (4, 32),   -- 审批管理 + 我的申请 + 提交
(4, 4), (4, 41), (4, 42),   -- 公告通知
(4, 6), (4, 61);             -- AI助手

-- ---------- attendance_db ----------
USE attendance_db;

-- 班次模板数据
INSERT INTO att_shift (id, shift_name, start_time, end_time, flex_start, flex_end) VALUES
(1, '标准白班',   '09:00', '18:00', '08:30', '09:30'),
(2, '弹性白班',   '09:00', '18:00', '08:00', '10:00'),
(3, '晚班',       '14:00', '22:00', NULL,    NULL);

-- 用户班次分配（默认所有员工标准白班）
INSERT INTO user_shift (user_id, shift_id) VALUES
(1, 1), (2, 1), (3, 1), (4, 1);

-- ---------- approval_db ----------
USE approval_db;

-- 暂无预设数据，运行时由业务逻辑动态生成

-- ---------- notice_db ----------
USE notice_db;

-- 一条示例公告
INSERT INTO ntc_notice (id, title, content, publisher_id, notice_type, target_type, start_time, status) VALUES
(1, '欢迎使用OA办公管理系统', '<p>系统已正式上线运行，请大家及时完善个人信息，并熟悉各项功能模块。</p>', 1, 1, 1, NOW(), 1);

-- ---------- ai_db ----------
USE ai_db;

-- 预设标签
INSERT INTO ai_knowledge_tag (id, tag_name, tag_code, tag_desc) VALUES
(1, '考勤',   'ATTENDANCE',  '考勤打卡、请假、加班相关制度'),
(2, '审批',   'APPROVAL',    '审批流程、表单填写规范'),
(3, '人事',   'HR',          '招聘、入职、离职、转正流程'),
(4, '财务',   'FINANCE',     '报销、借款、费用标准'),
(5, 'IT',     'IT',          '系统使用、账号申请、故障报修'),
(6, '行政',   'ADMIN',       '会议室、办公用品、接待规范'),
(7, '安全',   'SECURITY',    '信息安全、保密制度、操作安全');

-- 预设知识文档示例
INSERT INTO ai_knowledge_doc (id, title, content, summary, category, access_roles, status, create_by) VALUES
(1, '请假制度与流程',
'# 请假制度与流程

## 请假类型
- 年假：员工入职满一年后享有5天年假
- 事假：需提前1天申请，每月不超过3天
- 病假：需提供医院证明，紧急情况可事后补交
- 婚假：3天，需提供结婚证复印件
- 产假：98天，需提前1个月申请

## 申请流程
1. 登录OA系统，进入"审批管理" → "提交申请"
2. 选择请假类型，填写开始/结束时间
3. 填写请假原因
4. 提交后由直属主管审批
5. 超过3天的请假需HR二次审批

## 注意事项
- 紧急请假需电话通知主管，事后在系统补单
- 年假可分段使用，每次最少半天',
'公司请假类型说明及申请流程', 1, '["ROLE_EMPLOYEE","ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]', 1, 1),

(2, '加班管理制度',
'# 加班管理制度

## 加班申请
1. 加班需提前在OA系统提交加班申请
2. 填写加班起止时间、加班事由
3. 由部门主管审批

## 加班费标准
- 工作日加班：按1.5倍工资计算
- 休息日加班：按2倍工资计算
- 法定节假日加班：按3倍工资计算

## 调休制度
- 工作日加班可累积调休时长
- 每月调休不超过2天
- 调休需提前1天申请',
'加班申请流程及费用标准说明', 1, '["ROLE_EMPLOYEE","ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]', 1, 1),

(3, 'OA系统操作指南',
'# OA系统操作指南

## 常用功能入口
- 考勤打卡：首页"考勤管理" → "打卡页面"
- 请假申请："审批管理" → "提交申请" → 选择请假
- 查看公告："公告通知" → "公告列表"
- 个人设置：右上角头像 → "个人中心"

## 考勤打卡
1. 工作时间 9:00-18:00
2. 弹性打卡窗口 8:30-9:30
3. 每日需打上班卡和下班卡
4. 忘记打卡需联系HR补卡

## 密码修改
1. 右上角头像 → "个人中心"
2. 选择"修改密码"
3. 输入旧密码和新密码
4. 密码长度不少于8位，需包含字母和数字',
'OA系统各功能模块使用说明', 2, '["ROLE_EMPLOYEE","ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]', 1, 1),

(4, '财务报销制度（主管级可见）',
'# 财务报销制度

## 报销范围
- 差旅费：交通、住宿、餐饮
- 办公用品：文具、耗材
- 业务招待费：需提前申请

## 报销标准
- 差旅住宿：一线城市不超过500元/晚，其他城市不超过350元/晚
- 餐饮补贴：出差期间每天80元
- 交通：市内交通实报实销，跨市交通二等座/经济舱标准

## 报销流程
1. 在OA系统中填写报销单
2. 上传发票照片/电子发票
3. 提交部门主管审批
4. 超过2000元的报销需财务经理加签
5. 每月25日统一处理报销打款',
'报销范围、标准及申请流程', 4, '["ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]', 1, 1),

(5, '信息安全与保密制度',
'# 信息安全与保密制度

## 数据分类
- 公开信息：可对外发布的公司信息
- 内部信息：仅限公司内部使用
- 机密信息：仅限相关人员访问

## 操作规范
- 离开工位时锁屏
- 不将公司设备带离办公场所（除便携设备）
- 不使用个人存储设备接入公司电脑
- 密码定期更换（90天）

## 违规处理
- 首次违规：口头警告
- 二次违规：书面警告
- 严重违规：解除劳动合同',
'公司数据安全分类及员工操作规范', 6, '["ROLE_EMPLOYEE","ROLE_LEADER","ROLE_HR","ROLE_ADMIN"]', 1, 1);

-- 文档-标签关联
INSERT INTO ai_knowledge_doc_tag (doc_id, tag_id) VALUES
(1, 1), (1, 2),   -- 请假制度: 考勤 + 审批
(2, 1), (2, 2),   -- 加班管理: 考勤 + 审批
(3, 5),            -- OA操作: IT
(4, 4), (4, 2),   -- 财务报销: 财务 + 审批
(5, 7);            -- 信息安全: 安全

-- ---------- asset_db / statistics_db ----------
-- 无预设数据，运行时动态生成
