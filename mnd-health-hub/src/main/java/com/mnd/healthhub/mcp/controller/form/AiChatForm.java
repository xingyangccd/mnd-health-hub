package com.mnd.healthhub.mcp.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI 助手对话请求参数")
public class AiChatForm {

    @Schema(description = "用户输入的消息", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "帮我查一下我的订单")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字")
    private String message;

    @Schema(description = "会话ID（可选）。首次对话可传空，后续传同一会话ID可保持上下文",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "conv_abc123")
    private String conversationId;
}