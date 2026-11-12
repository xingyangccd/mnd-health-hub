<template>
  <!-- 右下角悬浮球 -->
  <div
    class="ai-float-btn"
    :class="{ 'ai-float-btn--active': visible }"
    @click="toggleVisible"
  >
    <el-icon v-if="!visible" :size="26">
      <ChatDotRound />
    </el-icon>
    <el-icon v-else :size="26">
      <Close />
    </el-icon>
    <span v-if="!visible" class="ai-float-badge"></span>
  </div>

  <!-- 对话窗口 -->
  <transition name="ai-slide">
    <div v-show="visible" class="ai-panel">
      <!-- 头部 -->
      <div class="ai-header">
        <div class="ai-header__avatar">
          <el-icon :size="22"><Service /></el-icon>
        </div>
        <div class="ai-header__info">
          <div class="ai-header__title">AI 健康助手</div>
          <div class="ai-header__sub">为您解答体检、订单、预约等问题</div>
        </div>
        <el-icon class="ai-header__close" :size="20" @click="toggleVisible">
          <Close />
        </el-icon>
      </div>

      <!-- 消息列表 -->
      <div ref="messageListRef" class="ai-messages">
        <!-- 欢迎语 -->
        <div v-if="messages.length === 0" class="ai-welcome">
          <div class="ai-welcome__icon">
            <el-icon :size="42"><Service /></el-icon>
          </div>
          <div class="ai-welcome__title">你好，我是 AI 健康助手</div>
          <div class="ai-welcome__desc">我可以帮你：</div>
          <ul class="ai-welcome__list">
            <li>查询体检套餐、订单、预约信息</li>
            <li>下单购买体检套餐</li>
            <li>创建体检预约、申请退款</li>
            <li>解答体检相关问题</li>
          </ul>
        </div>

        <!-- 消息气泡 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="ai-message"
          :class="msg.role === 'user' ? 'ai-message--user' : 'ai-message--ai'"
        >
          <div v-if="msg.role === 'ai'" class="ai-message__avatar">
            <el-icon :size="18"><Service /></el-icon>
          </div>
          <div class="ai-message__bubble">
            <div v-if="msg.loading" class="ai-message__loading">
              <span></span><span></span><span></span>
            </div>
            <div
              v-else
              class="ai-message__content"
              v-html="renderContent(msg.content)"
            ></div>
          </div>
          <div v-if="msg.role === 'user'" class="ai-message__avatar">
            <el-icon :size="18"><User /></el-icon>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="ai-input">
        <textarea
          v-model="input"
          class="ai-input__textarea"
          placeholder="输入您的问题，Enter 发送，Shift+Enter 换行"
          rows="2"
          maxlength="2000"
          @keydown.enter.exact.prevent="send"
          @keydown.enter.shift.exact.stop
        ></textarea>
        <el-button
          type="primary"
          size="default"
          :loading="sending"
          :disabled="!input.trim() || sending"
          class="ai-input__send"
          @click="send"
        >
          发送
        </el-button>
      </div>

      <!-- 底部提示 -->
      <div class="ai-footer">
        <span>AI 助手可能出错，请核实重要信息</span>
        <span
          v-if="conversationId"
          class="ai-footer__new"
          @click="startNewChat"
        >
          开启新会话
        </span>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { ChatDotRound, Close, Service, User } from "@element-plus/icons-vue";
import { chatWithAi } from "@/api/ai";

import MarkdownIt from "markdown-it";

// 创建 Markdown 渲染器（html: false 防止 XSS）
const md = new MarkdownIt({
  html: false, // 禁止 HTML 标签，防止 XSS
  linkify: true, // 自动识别链接
  breaks: true, // 单个换行转为 <br>
  typographer: true, // 优化排版
});

/** 面板是否可见 */
const visible = ref(false);

/** 对话消息列表 */
interface Message {
  role: "user" | "ai";
  content: string;
  loading?: boolean;
}
const messages = ref<Message[]>([]);

/** 输入内容 */
const input = ref("");

/** 是否正在发送 */
const sending = ref(false);

/** 会话ID */
const conversationId = ref<string | null>(null);

/** 消息列表 DOM 引用 */
const messageListRef = ref<HTMLElement | null>(null);

/** 切换面板可见性 */
const toggleVisible = () => {
  visible.value = !visible.value;
  if (visible.value) {
    // 打开时滚动到底部
    scrollToBottom();
  }
};

/** 滚动到底部 */
const scrollToBottom = async () => {
  await nextTick();
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
};

/** 检查登录状态 */
const checkLogin = (): boolean => {
  const token = localStorage.getItem("token");
  if (!token) {
    ElMessage.warning("请先登录后再使用 AI 助手");
    return false;
  }
  return true;
};

/** 发送消息 */
const send = async () => {
  const text = input.value.trim();
  if (!text || sending.value) return;

  if (!checkLogin()) return;

  // 1. 添加用户消息
  messages.value.push({ role: "user", content: text });
  input.value = "";
  await scrollToBottom();

  // 2. 添加 AI 占位消息（加载中）
  const aiMsgIndex = messages.value.length;
  messages.value.push({ role: "ai", content: "", loading: true });
  sending.value = true;
  await scrollToBottom();

  try {
    // 3. 调用 AI 接口
    const res = await chatWithAi(text, conversationId.value);

    // 4. 更新会话ID
    if (res.conversationId) {
      conversationId.value = res.conversationId;
    }

    // 5. 更新 AI 回复
    messages.value[aiMsgIndex] = {
      role: "ai",
      content: res.reply || "抱歉，我没有得到有效的回复。",
      loading: false,
    };
  } catch (e: any) {
    // 6. 错误处理
    messages.value[aiMsgIndex] = {
      role: "ai",
      content:
        e?.response?.data?.msg ||
        e?.message ||
        "抱歉，AI 助手暂时无法回答，请稍后再试。",
      loading: false,
    };
  } finally {
    sending.value = false;
    await scrollToBottom();
  }
};

/** 开启新会话 */
const startNewChat = () => {
  messages.value = [];
  conversationId.value = null;
  ElMessage.success("已开启新会话");
};

const renderContent = (content: string): string => {
  if (!content) return "";
  return md.render(content);
};

/** 快捷键：Ctrl+K 或 Cmd+K 打开/关闭 */
const handleKeyDown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "k") {
    e.preventDefault();
    toggleVisible();
  }
};

onMounted(() => {
  document.addEventListener("keydown", handleKeyDown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeyDown);
});
</script>

<style lang="less" scoped>
/* ============ 悬浮球 ============ */
.ai-float-btn {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e40065 0%, #ff5b8d 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(228, 0, 101, 0.35);
  z-index: 9999;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 12px 32px rgba(228, 0, 101, 0.5);
  }

  &:active {
    transform: scale(0.95);
  }

  &--active {
    background: #909399;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);

    &:hover {
      background: #73767a;
    }
  }
}

.ai-float-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ffd900;
  animation: ai-pulse 2s infinite;
}

@keyframes ai-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.3);
  }
}

/* ============ 面板 ============ */
.ai-panel {
  position: fixed;
  right: 32px;
  bottom: 104px;
  width: 680px;
  height: 780px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9998;
}

/* ============ 头部 ============ */
.ai-header {
  display: flex;
  align-items: center;
  padding: 16px 18px;
  background: linear-gradient(135deg, #e40065 0%, #ff5b8d 100%);
  color: #fff;
  flex-shrink: 0;

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    margin-left: 12px;
    min-width: 0;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    line-height: 1.4;
  }

  &__sub {
    font-size: 12px;
    opacity: 0.85;
    line-height: 1.4;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__close {
    cursor: pointer;
    opacity: 0.8;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }
  }
}

/* ============ 消息区 ============ */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fa;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 3px;
  }
}

/* 欢迎语 */
.ai-welcome {
  text-align: center;
  padding: 24px 12px;
  color: #555;

  &__icon {
    width: 68px;
    height: 68px;
    margin: 0 auto 16px;
    border-radius: 50%;
    background: linear-gradient(135deg, #ffe5f0 0%, #fff0f5 100%);
    color: #e40065;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 8px;
  }

  &__list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: inline-block;
    text-align: left;

    li {
      font-size: 13px;
      color: #666;
      line-height: 1.9;

      &::before {
        content: "·";
        color: #e40065;
        font-weight: bold;
        margin-right: 6px;
      }
    }
  }
}

/* 消息气泡 */
.ai-message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14px;

  &__avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__bubble {
    max-width: 92%;
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;
  }

  &__content {
    :deep(strong) {
      color: #e40065;
      font-weight: 600;
    }
  }

  /* AI 消息 */
  &--ai {
    .ai-message__avatar {
      background: linear-gradient(135deg, #ffe5f0 0%, #fff0f5 100%);
      color: #e40065;
      margin-right: 8px;
    }

    .ai-message__bubble {
      background: #fff;
      color: #333;
      border-top-left-radius: 4px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    }
  }

  /* 用户消息 */
  &--user {
    flex-direction: row-reverse;

    .ai-message__avatar {
      background: linear-gradient(135deg, #e40065 0%, #ff5b8d 100%);
      color: #fff;
      margin-left: 8px;
    }

    .ai-message__bubble {
      background: linear-gradient(135deg, #e40065 0%, #ff5b8d 100%);
      color: #fff;
      border-top-right-radius: 4px;
    }
  }
}

/* 加载动画 */
.ai-message__loading {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #e40065;
    animation: ai-loading 1.4s infinite ease-in-out;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
    &:nth-child(3) {
      animation-delay: 0s;
    }
  }
}

@keyframes ai-loading {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ============ 输入区 ============ */
.ai-input {
  display: flex;
  align-items: flex-end;
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  gap: 8px;

  &__textarea {
    flex: 1;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    padding: 8px 12px;
    font-size: 14px;
    font-family: inherit;
    line-height: 1.5;
    resize: none;
    outline: none;
    transition: border-color 0.2s;

    &:focus {
      border-color: #e40065;
    }

    &::placeholder {
      color: #bbb;
    }
  }

  &__send {
    height: 40px;
    flex-shrink: 0;
  }
}

/* ============ 底部 ============ */
.ai-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 14px 10px;
  font-size: 11px;
  color: #bbb;
  background: #fff;
  flex-shrink: 0;

  &__new {
    color: #e40065;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.7;
    }
  }
}

/* ============ 动画 ============ */
.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* ============ 响应式 ============ */
@media screen and (max-width: 500px) {
  .ai-panel {
    right: 12px;
    left: 12px;
    bottom: 92px;
    width: auto;
    height: 70vh;
  }

  .ai-float-btn {
    right: 20px;
    bottom: 20px;
  }
}

/* ============ Markdown 内容样式 ============ */
.ai-message__content {
  /* 表格 */
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 8px 0;
    font-size: 13px;
    background: #fff;
    border-radius: 6px;
    overflow: hidden;

    th,
    td {
      border: 1px solid #ebeef5;
      padding: 6px 10px;
      text-align: left;
      word-break: break-word;
    }

    th {
      background: #f5f7fa;
      font-weight: 600;
      color: #333;
    }

    tr:nth-child(even) td {
      background: #fafbfc;
    }
  }

  /* 有序/无序列表 */
  :deep(ul),
  :deep(ol) {
    margin: 6px 0;
    padding-left: 20px;
  }

  :deep(li) {
    margin: 4px 0;
    line-height: 1.6;
  }

  /* 标题 */
  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4) {
    margin: 10px 0 6px;
    font-weight: 600;
    line-height: 1.4;
  }

  :deep(h1) {
    font-size: 17px;
  }
  :deep(h2) {
    font-size: 16px;
  }
  :deep(h3) {
    font-size: 15px;
  }
  :deep(h4) {
    font-size: 14px;
  }

  /* 段落 */
  :deep(p) {
    margin: 6px 0;
    line-height: 1.7;
  }

  /* 加粗 */
  :deep(strong) {
    font-weight: 600;
    color: #e40065;
  }

  /* 行内代码 */
  :deep(code) {
    background: #f5f7fa;
    color: #e40065;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 12px;
    font-family: "Consolas", "Monaco", monospace;
  }

  /* 代码块 */
  :deep(pre) {
    background: #2d2d2d;
    color: #f8f8f2;
    padding: 10px 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 8px 0;
    font-size: 12px;

    code {
      background: transparent;
      color: inherit;
      padding: 0;
    }
  }

  /* 引用 */
  :deep(blockquote) {
    border-left: 3px solid #e40065;
    padding: 4px 12px;
    margin: 8px 0;
    background: #fff7fb;
    color: #666;
    border-radius: 0 6px 6px 0;
  }

  /* 链接 */
  :deep(a) {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  /* 分割线 */
  :deep(hr) {
    border: none;
    border-top: 1px solid #ebeef5;
    margin: 10px 0;
  }

  /* 用户消息里的内容（白字） */
  .ai-message--user & {
    :deep(strong),
    :deep(code) {
      color: #fff;
      background: rgba(255, 255, 255, 0.2);
    }

    :deep(table th) {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }

    :deep(table td) {
      border-color: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
  }
}
</style>
