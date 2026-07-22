<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type Component } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowLeft, BookOpen, Bot, BrainCircuit, Expand, History, LayoutDashboard, Minimize, ShieldCheck } from 'lucide-vue-next'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import AiConversationPanel from '@/components/ai/AiConversationPanel.vue'
import AiKnowledgePanel from '@/components/ai/AiKnowledgePanel.vue'
import AiOverviewPanel from '@/components/ai/AiOverviewPanel.vue'
import { useAuthStore } from '@/stores/auth'

type WorkspaceTab = 'overview' | 'chat' | 'knowledge' | 'conversations'

const auth = useAuthStore()
const activeTab = ref<WorkspaceTab>('overview')
const isFullscreen = ref(Boolean(document.fullscreenElement))
const currentTime = ref('')
let clockTimer: ReturnType<typeof setInterval> | null = null

const tabs: Array<{ id: WorkspaceTab; label: string; description: string; icon: Component }> = [
  { id: 'overview', label: '智能工作台', description: 'AI 能力总览', icon: LayoutDashboard },
  { id: 'chat', label: 'AI 助手', description: '问答与智能填单', icon: Bot },
  { id: 'knowledge', label: '知识库', description: '企业知识管理', icon: BookOpen },
  { id: 'conversations', label: '会话历史', description: '历史上下文', icon: History }
]

const componentMap: Record<WorkspaceTab, Component> = {
  overview: AiOverviewPanel,
  chat: AiChatPanel,
  knowledge: AiKnowledgePanel,
  conversations: AiConversationPanel
}

const activeComponent = computed(() => componentMap[activeTab.value])
const activeLabel = computed(() => tabs.find((tab) => tab.id === activeTab.value)?.label || '智能工作台')
const userRole = computed(() => (auth.user?.roles || []).map((role) => role.replace('ROLE_', '')).join(' / ') || 'EMPLOYEE')

function updateTime() {
  currentTime.value = new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  }).format(new Date())
}

async function toggleFullscreen() {
  if (!document.fullscreenElement) await document.documentElement.requestFullscreen()
  else await document.exitFullscreen()
}

function syncFullscreen() {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

onMounted(() => {
  updateTime()
  clockTimer = setInterval(updateTime, 1000)
  document.addEventListener('fullscreenchange', syncFullscreen)
})

onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer)
  document.removeEventListener('fullscreenchange', syncFullscreen)
})
</script>

<template>
  <div class="ai-workspace">
    <div class="grid-texture" aria-hidden="true" />
    <span class="data-line line-one" aria-hidden="true" />
    <span class="data-line line-two" aria-hidden="true" />

    <header class="workspace-header">
      <div class="brand-zone">
        <RouterLink class="header-button" to="/dashboard" aria-label="返回工作台" title="返回工作台"><ArrowLeft /></RouterLink>
        <span class="brand-mark"><BrainCircuit /></span>
        <div class="brand-copy"><span>OA INTELLIGENCE</span><strong>企业智能协同中心</strong></div>
      </div>
      <div class="header-center"><span class="header-rule" /><div><span>ACTIVE MODULE</span><strong>{{ activeLabel }}</strong></div><span class="header-rule reverse" /></div>
      <div class="header-tools">
        <div class="engine-status"><i /><span><small>AI ENGINE</small><strong>READY</strong></span></div>
        <time>{{ currentTime }}</time>
        <button class="header-button" type="button" :aria-label="isFullscreen ? '退出全屏' : '进入全屏'" :title="isFullscreen ? '退出全屏' : '进入全屏'" @click="toggleFullscreen"><Minimize v-if="isFullscreen" /><Expand v-else /></button>
      </div>
    </header>

    <div class="workspace-body">
      <aside class="workspace-sidebar">
        <nav aria-label="AI 工作台导航">
          <button v-for="tab in tabs" :key="tab.id" type="button" :class="{ active: activeTab === tab.id }" :title="tab.label" @click="activeTab = tab.id">
            <span class="nav-icon"><component :is="tab.icon" /></span>
            <span class="nav-copy"><strong>{{ tab.label }}</strong><small>{{ tab.description }}</small></span>
          </button>
        </nav>
        <div class="sidebar-foot">
          <div class="security-state"><ShieldCheck /><span><small>SECURE ACCESS</small><strong>权限已验证</strong></span></div>
          <div class="user-state"><span class="user-avatar">{{ auth.displayName.slice(0, 1) }}</span><span><strong>{{ auth.displayName }}</strong><small>{{ userRole }}</small></span></div>
        </div>
      </aside>

      <main class="workspace-content">
        <Transition name="module" mode="out-in">
          <KeepAlive><component :is="activeComponent" :key="activeTab" :display-name="auth.displayName" @navigate="activeTab = $event" /></KeepAlive>
        </Transition>
      </main>
    </div>

    <nav class="mobile-module-nav" aria-label="AI 模块导航">
      <button v-for="tab in tabs" :key="tab.id" type="button" :class="{ active: activeTab === tab.id }" @click="activeTab = tab.id"><component :is="tab.icon" /><span>{{ tab.label }}</span></button>
    </nav>
  </div>
</template>

<style scoped>
.ai-workspace{position:fixed;inset:0;z-index:100;min-width:320px;min-height:100vh;display:grid;grid-template-rows:66px minmax(0,1fr);overflow:hidden;background:radial-gradient(ellipse at 50% -20%,rgba(37,99,235,.18),transparent 46%),linear-gradient(150deg,#070d1a,#030711 62%,#08101d);color:#dbe7f7;font-family:Inter,"Source Han Sans SC","Microsoft YaHei",sans-serif;animation:workspace-enter .35s ease-out both}
.ai-workspace::before{position:fixed;inset:0;z-index:-2;pointer-events:none;background-image:radial-gradient(circle,rgba(125,211,252,.15) 0 1px,transparent 1.5px);background-size:46px 46px;opacity:.16;content:""}
.grid-texture{position:fixed;inset:66px 0 0;z-index:-1;pointer-events:none;background-image:linear-gradient(rgba(59,130,246,.025) 1px,transparent 1px),linear-gradient(90deg,rgba(59,130,246,.025) 1px,transparent 1px);background-size:54px 54px;mask-image:linear-gradient(to bottom,black,transparent 92%)}
.data-line{position:fixed;z-index:-1;width:34vw;height:1px;pointer-events:none;background:linear-gradient(90deg,transparent,rgba(34,211,238,.42),transparent);opacity:.22;animation:data-flow 9s linear infinite}.line-one{top:27%;left:-35vw}.line-two{right:-35vw;bottom:19%;animation-direction:reverse;animation-delay:-4s}
.workspace-header{position:relative;z-index:5;display:grid;grid-template-columns:minmax(270px,1fr) minmax(240px,.8fr) minmax(320px,1fr);align-items:center;gap:16px;padding:0 16px;border-bottom:1px solid rgba(96,165,250,.16);background:linear-gradient(90deg,rgba(8,15,29,.92),rgba(15,23,42,.78),rgba(8,15,29,.92));box-shadow:0 14px 36px rgba(2,6,23,.34),inset 0 -1px 0 rgba(255,255,255,.025);backdrop-filter:blur(16px) saturate(125%)}
.workspace-header::after{position:absolute;right:25%;bottom:-1px;left:25%;height:1px;background:linear-gradient(90deg,transparent,#38bdf8,#8b5cf6,transparent);box-shadow:0 0 14px rgba(56,189,248,.35);content:""}
.brand-zone,.header-tools,.header-center,.engine-status,.security-state,.user-state{display:flex;align-items:center}.brand-zone{gap:10px}
.header-button{width:34px;height:34px;display:grid;place-items:center;border:1px solid rgba(148,163,184,.15);border-radius:6px;background:rgba(148,163,184,.05);color:#8fa2ba;cursor:pointer;transition:transform .15s ease,color .18s ease,border-color .18s ease,background .18s ease}.header-button:hover{border-color:rgba(56,189,248,.35);background:rgba(56,189,248,.08);color:#7dd3fc}.header-button:active{transform:scale(.97)}.header-button svg{width:16px;height:16px}
.brand-mark{width:38px;height:38px;display:grid;place-items:center;border:1px solid rgba(96,165,250,.28);border-radius:7px;background:linear-gradient(145deg,rgba(37,99,235,.34),rgba(124,58,237,.22));color:#bfe8ff;box-shadow:0 0 24px rgba(59,130,246,.18),inset 0 1px 0 rgba(255,255,255,.08)}.brand-mark svg{width:21px}.brand-copy{display:grid;gap:2px}.brand-copy span{color:#526982;font-size:7px;font-weight:850;letter-spacing:1.8px}.brand-copy strong{color:#edf5ff;font-size:14px;font-weight:760}
.header-center{justify-content:center;gap:12px;text-align:center}.header-center>div{display:grid;gap:3px}.header-center span{color:#4d6179;font-size:7px;font-weight:800;letter-spacing:1.3px}.header-center strong{color:#bfdbfe;font-size:11px}.header-rule{width:clamp(28px,4vw,68px);height:1px;background:linear-gradient(90deg,transparent,rgba(56,189,248,.35))}.header-rule.reverse{transform:rotate(180deg)}
.header-tools{justify-content:flex-end;gap:12px}.header-tools time{color:#71849d;font-family:ui-monospace,SFMono-Regular,Consolas,monospace;font-size:9px;font-variant-numeric:tabular-nums}.engine-status{gap:7px}.engine-status i{width:6px;height:6px;border-radius:50%;background:#22c55e;box-shadow:0 0 10px rgba(34,197,94,.65);animation:engine-pulse 1.8s infinite}.engine-status span{display:grid;gap:2px}.engine-status small{color:#4f6076;font-size:6px;letter-spacing:1px}.engine-status strong{color:#72d691;font-size:8px}
.workspace-body{min-height:0;display:grid;grid-template-columns:214px minmax(0,1fr)}.workspace-sidebar{min-height:0;display:grid;grid-template-rows:minmax(0,1fr) auto;padding:14px 10px;border-right:1px solid rgba(148,163,184,.11);background:linear-gradient(180deg,rgba(8,15,29,.76),rgba(5,9,20,.86));box-shadow:14px 0 38px rgba(2,6,23,.2),inset -1px 0 0 rgba(255,255,255,.02);backdrop-filter:blur(14px)}
.workspace-sidebar nav{display:grid;align-content:start;gap:5px}.workspace-sidebar nav button{position:relative;min-width:0;height:54px;display:flex;align-items:center;gap:10px;padding:0 10px;border:1px solid transparent;border-radius:7px;background:transparent;color:#63738a;cursor:pointer;text-align:left;transition:transform .16s ease,color .18s ease,border-color .18s ease,background .18s ease}.workspace-sidebar nav button::before{position:absolute;left:-10px;width:2px;height:22px;border-radius:0 3px 3px 0;background:transparent;content:""}.workspace-sidebar nav button:hover{border-color:rgba(96,165,250,.13);background:rgba(59,130,246,.06);color:#bdcadb;transform:translateX(3px)}.workspace-sidebar nav button:active{transform:scale(.97)}.workspace-sidebar nav button.active{border-color:rgba(96,165,250,.2);background:linear-gradient(90deg,rgba(37,99,235,.18),rgba(124,58,237,.08));color:#7dd3fc;box-shadow:inset 0 1px 0 rgba(255,255,255,.035),0 8px 24px rgba(37,99,235,.08)}.workspace-sidebar nav button.active::before{background:linear-gradient(180deg,#22d3ee,#8b5cf6);box-shadow:0 0 12px rgba(34,211,238,.55)}
.nav-icon{width:32px;height:32px;flex:0 0 32px;display:grid;place-items:center;border-radius:6px;background:rgba(148,163,184,.05)}.nav-icon svg{width:16px;height:16px}.nav-copy{min-width:0;display:grid;gap:4px}.nav-copy strong{color:inherit;font-size:10px}.nav-copy small{overflow:hidden;color:#4f6076;font-size:8px;text-overflow:ellipsis;white-space:nowrap}
.sidebar-foot{display:grid;gap:9px}.security-state{gap:8px;padding:10px;border:1px solid rgba(34,197,94,.12);border-radius:6px;background:rgba(34,197,94,.035);color:#66c889}.security-state svg{width:15px}.security-state span,.user-state>span:last-child{display:grid;gap:3px;min-width:0}.security-state small,.user-state small{color:#496176;font-size:7px;letter-spacing:.8px}.security-state strong,.user-state strong{overflow:hidden;color:#8194aa;font-size:9px;text-overflow:ellipsis;white-space:nowrap}.user-state{gap:9px;padding:8px 6px}.user-avatar{width:30px;height:30px;flex:0 0 30px;display:grid;place-items:center;border:1px solid rgba(96,165,250,.22);border-radius:6px;background:linear-gradient(145deg,rgba(37,99,235,.22),rgba(124,58,237,.14));color:#bfdbfe;font-size:11px;font-weight:800}
.workspace-content{min-width:0;min-height:0;padding:12px;overflow:hidden}.workspace-content>:deep(*){height:100%}.mobile-module-nav{display:none}.module-enter-active,.module-leave-active{transition:opacity .18s ease,transform .18s ease}.module-enter-from{opacity:0;transform:translateY(7px)}.module-leave-to{opacity:0;transform:translateY(-4px)}
@keyframes workspace-enter{from{opacity:0;transform:translateY(8px)}}@keyframes engine-pulse{50%{opacity:.45;transform:scale(1.2)}}@keyframes data-flow{to{transform:translateX(170vw)}}
@media(max-width:900px){.workspace-header{grid-template-columns:1fr auto}.header-center{display:none}.workspace-body{grid-template-columns:78px minmax(0,1fr)}.workspace-sidebar{padding-inline:9px}.workspace-sidebar nav button{justify-content:center;padding:0}.workspace-sidebar nav button::before{left:-9px}.nav-copy,.security-state span,.user-state>span:last-child{display:none}.security-state,.user-state{justify-content:center;padding-inline:0}}
@media(max-width:640px){.ai-workspace{grid-template-rows:58px minmax(0,1fr) 64px}.workspace-header{padding:0 10px}.brand-mark{width:34px;height:34px}.brand-copy span,.header-tools time,.engine-status{display:none}.brand-copy strong{font-size:12px}.workspace-body{display:block;min-height:0}.workspace-sidebar{display:none}.workspace-content{height:100%;padding:8px;overflow-y:auto}.mobile-module-nav{z-index:6;display:grid;grid-template-columns:repeat(4,minmax(0,1fr));padding:5px 6px max(5px,env(safe-area-inset-bottom));border-top:1px solid rgba(96,165,250,.15);background:rgba(5,9,20,.92);backdrop-filter:blur(16px)}.mobile-module-nav button{position:relative;display:grid;place-items:center;align-content:center;gap:3px;border:0;border-radius:5px;background:transparent;color:#5f7087;font:inherit;font-size:8px;cursor:pointer}.mobile-module-nav button.active{background:rgba(59,130,246,.1);color:#7dd3fc}.mobile-module-nav button.active::before{position:absolute;top:0;width:20px;height:2px;border-radius:2px;background:#38bdf8;box-shadow:0 0 9px rgba(56,189,248,.5);content:""}.mobile-module-nav svg{width:17px;height:17px}}
@media(prefers-reduced-motion:reduce){.ai-workspace,.data-line,.engine-status i{animation:none}.module-enter-active,.module-leave-active{transition:none}}
</style>
