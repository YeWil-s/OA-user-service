-- ============================================================
-- OA办公管理系统 - AI智能助手服务 数据库初始化脚本
-- 负责模块: ai-service (oa-ai)
-- 版本: v1.1
-- 变更记录:
--   v1.0 - 初始建表：知识库表 + 种子数据
--   v1.1 - 补充 ai_conversation 表（update_time+is_deleted）、
--          新增 approval_db.app_application 表
-- ============================================================

-- ============================================================
-- PART 1: 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS ai_db       DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS approval_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- PART 2: ai_db — 知识库 + 对话记录
-- ============================================================
USE ai_db;

-- 2.1 知识文档表
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

-- 如果数据库中已有旧版 ai_knowledge_doc 表，执行以下 ALTER 进行升级：
-- ALTER TABLE ai_knowledge_doc ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT '所属部门ID' AFTER category;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN access_positions VARCHAR(500) DEFAULT NULL COMMENT '可访问岗位(JSON数组)' AFTER access_roles;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN access_depts VARCHAR(500) DEFAULT NULL COMMENT '可访问部门(JSON数组)' AFTER access_positions;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN access_mode TINYINT DEFAULT 0 COMMENT '访问模式: 0=全部可见, 1=按角色, 2=按部门岗位' AFTER access_depts;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN version INT DEFAULT 1 COMMENT '版本号' AFTER access_mode;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN vector_status TINYINT DEFAULT 0 COMMENT '向量状态: 0=未同步, 1=已同步, 2=同步失败' AFTER version;
-- ALTER TABLE ai_knowledge_doc ADD COLUMN vector_error VARCHAR(500) DEFAULT NULL COMMENT '向量同步错误信息' AFTER vector_status;
-- ALTER TABLE ai_knowledge_doc ADD INDEX idx_vector_status (vector_status);

-- 2.2 知识标签表
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

-- 2.3 知识文档-标签关联表
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

-- 2.4 AI对话记录表
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

-- 2.5 AI分析报告表（预留）
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
-- PART 3: ai_db — 审批申请单（临时）
-- ============================================================
-- 说明: 当前 oa-approval 模块尚未开发，oa-ai 暂存申请单于本地表。
-- 待 oa-approval 模块上线后，由该模块负责创建 approval_db.app_application，
-- oa-ai 改为通过 Nacos 服务发现 + HTTP 调用审批接口。
-- ============================================================

DROP TABLE IF EXISTS app_application;
CREATE TABLE app_application (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    application_no      VARCHAR(50)  NOT NULL             COMMENT '申请单号(如 LV20260721001)',
    user_id             BIGINT       NOT NULL             COMMENT '申请人ID',
    dept_id             BIGINT       NOT NULL             COMMENT '申请人部门ID',
    app_type            TINYINT      NOT NULL             COMMENT '申请类型: 1=请假, 2=加班, 3=外出',
    leave_type          TINYINT      DEFAULT NULL         COMMENT '请假子类型: 1=年假, 2=事假, 3=病假, 4=婚假, 5=产假(仅app_type=1时有效)',
    start_time          DATETIME     NOT NULL             COMMENT '开始时间',
    end_time            DATETIME     NOT NULL             COMMENT '结束时间',
    duration            DECIMAL(5,1) NOT NULL             COMMENT '时长(天)',
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
) ENGINE=InnoDB COMMENT='审批申请单表';

-- ============================================================
-- PART 4: 种子数据（ai_db）
-- ============================================================
USE ai_db;

-- 预设标签
INSERT INTO ai_knowledge_tag (id, tag_name, tag_code, tag_desc) VALUES
(1,  '考勤',    'ATTENDANCE',    '考勤打卡、请假、加班相关制度'),
(2,  '审批',    'APPROVAL',      '审批流程、表单填写规范'),
(3,  '人事',    'HR',            '招聘、入职、离职、转正流程'),
(4,  '财务',    'FINANCE',       '报销、借款、费用标准'),
(5,  'IT',      'IT',            '系统使用、账号申请、故障报修'),
(6,  '行政',    'ADMIN',         '会议室、办公用品、接待规范'),
(7,  '安全',    'SECURITY',      '信息安全、保密制度、操作安全');

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
