<template>
  <div class="chat-layout">
    <SideTab
      :active-tab="activeTab"
      @tab-change="(tab: any) => (activeTab = tab)"
    />

    <div class="list-panel">
      <ConversationList v-show="activeTab === 'conversations'" />
      <ContactList v-show="activeTab === 'contacts'" />
    </div>

    <div v-show="activeTab === 'conversations'" class="chat-panel">
      <Chat
        :conversationID="defaultOpenConversationID"
        :PlaceholderEmpty="EmptyChatTpl"
      >
        <ChatHeader>
          <template #ChatHeaderRight>
            <button class="icon-btn" @click="settingOpen = !settingOpen">
              <IconMenu size="20" />
            </button>
          </template>
        </ChatHeader>
        <MessageList />
        <MessageInput>
          <template #headerToolbar>
            <div class="toolbar">
              <div class="toolbar-left">
                <EmojiPicker />
                <FilePicker />
                <ImagePicker />
              </div>
              <button class="icon-btn" @click="searchOpen = !searchOpen">
                <IconSearch size="20" />
              </button>
            </div>
          </template>
        </MessageInput>
        <div v-if="settingOpen" class="float-sidebar">
          <ChatSetting @close="settingOpen = false" />
        </div>
      </Chat>
    </div>

    <div
      v-if="searchOpen && activeTab === 'conversations'"
      class="search-sidebar"
    >
      <div class="search-sidebar-header">
        <span class="search-sidebar-title">搜索</span>
        <button class="icon-btn" @click="searchOpen = false">✕</button>
      </div>
      <Search :variant="VariantType.EMBEDDED" />
    </div>

    <div v-show="activeTab === 'contacts'" class="detail-panel">
      <ContactInfo
        @send-message="activeTab = 'conversations'"
        @enter-group="activeTab = 'conversations'"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import {
  Chat,
  Search,
  ChatHeader,
  MessageList,
  MessageInput,
  ConversationList,
  ContactList,
  ContactInfo,
  ChatSetting,
  EmojiPicker,
  FilePicker,
  ImagePicker,
  VariantType,
  useChatContext,
} from "@tencentcloud/chat-uikit-vue3";
import { IconMenu, IconSearch } from "@tencentcloud/uikit-base-component-vue3";
import SideTab from "./SideTab.vue";

const emit = defineEmits<{
  conversationChange: [userId: string];
}>();

const { activeConversation } = useChatContext();

const activeTab = ref<"conversations" | "contacts">("conversations");
const settingOpen = ref(false);
const searchOpen = ref(false);
const defaultOpenConversationID = ref("");

// 监听会话切换，通知父组件
watch(
  () => activeConversation.value?.conversationID,
  (newConversationID) => {
    // 腾讯云IM即时通讯服务器中，默认的 newConversationID
    // 的值是：C2Ccustomer_1、C2Ccustomer_2.....
    // customer_1，customer_2这些值是我们springboot项目生成的IM账号
    settingOpen.value = false;
    searchOpen.value = false;
    if (newConversationID) {
      // 将 C2Ccustomer_1 转换成 customer_1
      const userId = newConversationID.replace("C2C", "");
      // 通过代码触发事件
      // 这个代码什么时候执行？会话变化的时候，这里的代码自然调用。
      emit("conversationChange", userId);
    }
  },
);

watch(activeTab, () => {
  settingOpen.value = false;
  searchOpen.value = false;
});

const EmptyChatTpl = {
  template: `
  <div class="empty-chat">
    <div style="font-size:48px;opacity:.3">💬</div>
    <div style="font-size:15px;font-weight:600;color:#6c757d">暂无会话</div>
    <div style="font-size:13px;color:#868e96">选择一个会话开始聊天</div>
  </div>
`,
};
</script>

<style scoped>
.chat-layout {
  height: 60vh;
  aspect-ratio: 16/9;
  display: flex;
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
}
@media (max-width: 1920px) {
  .chat-layout {
    height: 80vh;
  }
}
.list-panel {
  width: 240px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--stroke-color-primary);
  overflow: hidden;
}
.detail-panel {
  flex: 1;
}
.icon-btn {
  padding: 4px 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  font-size: 20px;
  color: var(--text-color-primary);
  cursor: pointer;
  outline: none;
}
.icon-btn:hover {
  background-color: var(--button-color-secondary-hover);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
}
.search-sidebar {
  border-left: 1px solid var(--stroke-color-primary);
  display: flex;
  flex-direction: column;
  flex: 0 0 300px;
}
.search-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--stroke-color-primary);
}
.search-sidebar-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color-primary);
}
.chat-panel {
  flex: 1;
  position: relative;
  display: flex;
}
.float-sidebar {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  min-width: 300px;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color-operate);
  box-shadow:
    -2px 0 8px rgba(0, 0, 0, 0.04),
    -4px 0 16px rgba(0, 0, 0, 0.06);
  overflow: auto;
  z-index: 1000;
}
.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: var(--bg-color-operate);
  border-left: 1px solid rgba(0, 0, 0, 0.08);
}
</style>

<style>
#app {
  text-align: start;
}

.chat-layout {
  height: 600px;
  width: 1050px;
  max-width: 100%;
  margin: 20px auto;
  display: flex;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}
</style>
