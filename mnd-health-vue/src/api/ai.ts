import axios from "axios";

/**
 * 与 AI 助手对话
 *
 * @param message 用户输入的消息
 * @param conversationId 会话ID（可选，首次传空）
 * @returns AI 回复内容 + 会话ID
 */
export interface AiChatResponse {
  reply: string;
  conversationId: string | null;
}

export async function chatWithAi(
  message: string,
  conversationId?: string | null,
): Promise<AiChatResponse> {
  const token = localStorage.getItem("token");
  const response = await axios.post(
    "/api/front/ai/chat",
    {
      message,
      conversationId: conversationId || null,
    },
    {
      headers: {
        satoken: token,
      },
    },
  );
  if (response.data.code !== 200) {
    throw new Error(response.data.msg || "AI 助手请求失败");
  }
  return response.data.data.result as AiChatResponse;
}
