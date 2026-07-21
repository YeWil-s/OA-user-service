package com.oa.ai.service.impl;

import com.oa.ai.service.PromptService;
import com.oa.ai.vo.SourceRefVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptServiceImpl implements PromptService {

    @Override
    public String buildRagSystemPrompt(List<SourceRefVO> sources, List<String> roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是OA办公系统的智能助手。你**只能**根据下方\"参考资料\"中的内容回答问题。\n\n");
        sb.append("## 严格规则\n");
        sb.append("1. 只使用参考资料中的信息，不得使用任何外部知识。\n");
        sb.append("2. 如果参考资料中没有答案，必须回答：\"抱歉，我目前的知识库中没有相关信息，请联系HR或管理员获取帮助。\"\n");
        sb.append("3. 不要猜测、推测、或编造任何信息。\n");
        sb.append("4. 回答时注明引用的文档标题，例如【请假制度】。\n");
        sb.append("5. 对于流程类问题，按步骤列出。\n\n");
        sb.append("## 用户身份\n");
        sb.append("角色：").append(String.join("、", roles)).append("\n\n");
        sb.append("## 参考资料\n");

        for (int i = 0; i < sources.size(); i++) {
            SourceRefVO src = sources.get(i);
            sb.append("---\n");
            sb.append("文档").append(i + 1).append("：").append(src.getTitle()).append("\n");
            sb.append(src.getSnippet()).append("\n");
        }
        sb.append("---\n");

        return sb.toString();
    }

    @Override
    public String buildIntentPrompt() {
        return """
            你是一个意图分类器。分析用户消息，返回严格的JSON格式结果。

            ## 意图类型
            - FORM_FILLING: 用户想要填写或提交申请单（请假、加班、外出等）
            - KNOWLEDGE_QA: 用户询问公司制度、流程、政策等知识性问题
            - DATA_ANALYSIS: 用户想要数据分析或报告生成
            - UNKNOWN: 无法归类到以上任何类型

            ## 关键识别规则
            - 包含"请假"、"调休"、"年假"、"病假"、"事假"、"加班"、"外出"、"帮我填"、"帮我申请"、"提交" -> FORM_FILLING
            - 提示词包含"确认"、"是的"、"好的"、"可以"、"提交吧"且对话上下文中有待确认的申请单 -> FORM_FILLING (action=confirm)
            - 包含"怎么"、"如何"、"规定"、"政策"、"制度"、"流程"、"规则"、"是什么"、"什么是" -> KNOWLEDGE_QA
            - 包含"分析"、"报告"、"统计"、"数据" -> DATA_ANALYSIS

            ## 输出格式（仅输出JSON，不要任何其他文字）
            {"intent": "FORM_FILLING", "confidence": 0.95, "appType": "LEAVE", "reasoning": "..."}
            """;
    }

    @Override
    public String buildExtractionPrompt(String currentDate) {
        return """
            你是一个表单填写助手。从用户消息中提取申请单所需字段。

            ## 申请类型(appType)
            1=请假, 2=加班, 3=外出

            ## 请假子类型(leaveType, 仅appType=1时需要)
            1=年假, 2=事假, 3=病假, 4=婚假, 5=产假

            ## 当前时间：%s

            ## 字段提取规则
            - startTime/endTime: 格式YYYY-MM-DD HH:mm:ss。如果用户说"明天"基于当前时间推算。如果只说日期不说到时间，请假默认为全天(09:00-18:00)，加班默认为18:00-21:00。
            - duration: 时长(天)，自动计算保留1位小数。
            - reason: 用户原话或总结。
            - 如果用户没有明确说明具体日期，needClarification设为true。

            ## 输出格式（仅输出JSON）
            {"appType":1,"leaveType":3,"startTime":"...","endTime":"...","duration":1.0,"reason":"...","missingFields":[],"needClarification":false,"clarificationQuestion":""}
            """.formatted(currentDate);
    }

    @Override
    public String buildRagUserPrompt(String question, List<SourceRefVO> sources) {
        return "## 用户问题\n" + question + "\n\n## 你的回答";
    }
}
