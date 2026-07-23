<script setup lang="ts">
import { ArrowRight, BookOpen, Bot, BrainCircuit, FileCheck2, History, Network, ShieldCheck, Sparkles } from 'lucide-vue-next'

defineProps<{ displayName: string }>()
const emit = defineEmits<{ navigate: [tab: 'chat' | 'knowledge' | 'conversations'] }>()
</script>

<template>
  <section class="overview-layout">
    <section class="hero-zone">
      <div class="hero-copy">
        <span class="eyebrow"><Sparkles />AI-ENABLED OFFICE</span>
        <h2>{{ displayName }}，欢迎进入智能工作台</h2>
        <p>在一个上下文中完成企业知识检索、制度问答与业务申请发起。</p>
        <button type="button" @click="emit('navigate', 'chat')"><Bot />开始智能对话<ArrowRight /></button>
      </div>

      <div class="intelligence-map" aria-hidden="true">
        <span class="orbit orbit-one" /><span class="orbit orbit-two" /><span class="orbit orbit-three" />
        <span class="node node-main"><BrainCircuit /></span>
        <span class="node node-a"><BookOpen /></span>
        <span class="node node-b"><FileCheck2 /></span>
        <span class="node node-c"><Network /></span>
        <i class="beam beam-a" /><i class="beam beam-b" /><i class="beam beam-c" />
      </div>
    </section>

    <section class="capability-grid">
      <button class="capability" type="button" @click="emit('navigate', 'chat')">
        <span class="capability-icon blue"><Bot /></span>
        <span class="capability-copy"><small>RAG ASSISTANT</small><strong>智能问答</strong><em>基于企业知识库的流式回答</em></span>
        <ArrowRight class="arrow" />
      </button>
      <button class="capability" type="button" @click="emit('navigate', 'chat')">
        <span class="capability-icon violet"><FileCheck2 /></span>
        <span class="capability-copy"><small>AI AGENT</small><strong>智能填单</strong><em>自然语言提取并确认业务字段</em></span>
        <ArrowRight class="arrow" />
      </button>
      <button class="capability" type="button" @click="emit('navigate', 'knowledge')">
        <span class="capability-icon cyan"><BookOpen /></span>
        <span class="capability-copy"><small>KNOWLEDGE BASE</small><strong>知识库维护</strong><em>管理制度、流程与业务文档</em></span>
        <ArrowRight class="arrow" />
      </button>
      <button class="capability" type="button" @click="emit('navigate', 'conversations')">
        <span class="capability-icon pink"><History /></span>
        <span class="capability-copy"><small>CONTEXT MEMORY</small><strong>会话历史</strong><em>追溯当前用户的历史上下文</em></span>
        <ArrowRight class="arrow" />
      </button>
    </section>

    <section class="system-strip">
      <div><span class="status-dot" /><p><small>AI SERVICE</small><strong>服务待命</strong></p></div>
      <div><ShieldCheck /><p><small>ACCESS CONTROL</small><strong>权限隔离</strong></p></div>
      <div><Network /><p><small>STREAM CHANNEL</small><strong>SSE 实时响应</strong></p></div>
      <div><BrainCircuit /><p><small>KNOWLEDGE ENGINE</small><strong>RAG 检索增强</strong></p></div>
    </section>
  </section>
</template>

<style scoped>
.overview-layout { min-height:0;display:grid;grid-template-rows:minmax(250px,1.15fr) minmax(170px,.8fr) auto;gap:12px;overflow-y:auto; }
.hero-zone,.capability,.system-strip { border:1px solid rgba(148,163,184,.14);border-radius:8px;background:linear-gradient(145deg,rgba(15,23,42,.74),rgba(8,15,29,.64));box-shadow:0 22px 58px rgba(2,6,23,.28),inset 0 1px 0 rgba(255,255,255,.045);backdrop-filter:blur(14px) saturate(125%); }
.hero-zone { position:relative;min-height:250px;display:grid;grid-template-columns:minmax(0,1fr) minmax(300px,.75fr);overflow:hidden;background:radial-gradient(circle at 78% 48%,rgba(59,130,246,.14),transparent 31%),linear-gradient(145deg,rgba(15,23,42,.84),rgba(8,15,29,.7)); }
.hero-zone::before { position:absolute;inset:0;pointer-events:none;background-image:linear-gradient(rgba(96,165,250,.035) 1px,transparent 1px),linear-gradient(90deg,rgba(96,165,250,.035) 1px,transparent 1px);background-size:32px 32px;content:"";mask-image:linear-gradient(90deg,transparent,black); }
.hero-copy { z-index:1;display:grid;align-content:center;justify-items:start;padding:clamp(26px,4vw,54px); }
.eyebrow { display:inline-flex;align-items:center;gap:7px;color:#67e8f9;font-size:8px;font-weight:850;letter-spacing:1.8px; }.eyebrow svg{width:13px}
h2 { max-width:650px;margin:12px 0 0;color:#f2f7ff;font-size:clamp(24px,3vw,40px);font-weight:760;line-height:1.22;letter-spacing:0; }
.hero-copy p { max-width:560px;margin:13px 0 0;color:#8291a7;font-size:12px;line-height:1.75; }
.hero-copy button { min-height:40px;display:inline-flex;align-items:center;gap:8px;margin-top:22px;padding:0 15px;border:1px solid rgba(96,165,250,.24);border-radius:6px;background:linear-gradient(135deg,#2563eb,#7c3aed);color:#fff;box-shadow:0 12px 28px rgba(37,99,235,.22);cursor:pointer;font:inherit;font-size:11px;font-weight:720;transition:transform .15s ease,filter .18s ease,box-shadow .18s ease; }
.hero-copy button:hover { filter:brightness(1.12);box-shadow:0 15px 32px rgba(59,130,246,.3);transform:translateY(-2px) }.hero-copy button:active{transform:scale(.97)}.hero-copy button svg{width:15px}
.intelligence-map { position:relative;display:grid;place-items:center;min-height:250px; }
.orbit { position:absolute;border:1px solid rgba(96,165,250,.16);border-radius:50%;animation:orbit-pulse 5s ease-in-out infinite }.orbit-one{width:110px;height:110px}.orbit-two{width:190px;height:190px;animation-delay:-1.5s}.orbit-three{width:270px;height:270px;animation-delay:-3s}
.node { position:absolute;width:46px;height:46px;display:grid;place-items:center;border:1px solid rgba(96,165,250,.22);border-radius:8px;background:rgba(15,23,42,.82);color:#7dd3fc;box-shadow:0 0 28px rgba(59,130,246,.14);backdrop-filter:blur(8px); }.node svg{width:20px}.node-main{width:64px;height:64px;background:linear-gradient(145deg,rgba(37,99,235,.44),rgba(124,58,237,.3));color:#fff;box-shadow:0 0 42px rgba(59,130,246,.3)}.node-main svg{width:29px}.node-a{transform:translate(-105px,-62px);color:#67e8f9}.node-b{transform:translate(108px,-42px);color:#c4b5fd}.node-c{transform:translate(30px,108px);color:#f9a8d4}
.beam { position:absolute;width:105px;height:1px;background:linear-gradient(90deg,transparent,#38bdf8,transparent);transform-origin:left;opacity:.36 }.beam-a{transform:rotate(30deg) translateX(-105px)}.beam-b{transform:rotate(160deg) translateX(-100px)}.beam-c{transform:rotate(270deg) translateX(-104px)}
.capability-grid { display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px; }
.capability { min-width:0;display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:11px;padding:14px;text-align:left;color:inherit;cursor:pointer;transition:transform .18s ease,border-color .18s ease,box-shadow .18s ease; }
.capability:hover { border-color:rgba(96,165,250,.3);box-shadow:0 24px 58px rgba(2,6,23,.38),0 0 22px rgba(59,130,246,.08),inset 0 1px 0 rgba(255,255,255,.06);transform:translateY(-2px) }.capability:active{transform:scale(.97)}
.capability-icon { width:38px;height:38px;display:grid;place-items:center;border-radius:7px; }.capability-icon svg{width:18px}.capability-icon.blue{background:rgba(59,130,246,.11);color:#7dd3fc}.capability-icon.violet{background:rgba(139,92,246,.11);color:#c4b5fd}.capability-icon.cyan{background:rgba(6,182,212,.1);color:#67e8f9}.capability-icon.pink{background:rgba(236,72,153,.09);color:#f9a8d4}
.capability-copy { min-width:0;display:grid;gap:4px }.capability-copy small{color:#4f6077;font-size:7px;font-weight:800;letter-spacing:1px}.capability-copy strong{color:#dce6f4;font-size:11px}.capability-copy em{overflow:hidden;color:#66768c;font-size:9px;font-style:normal;text-overflow:ellipsis;white-space:nowrap}.capability>.arrow{width:14px;color:#485a71;transition:transform .18s ease,color .18s ease}.capability:hover>.arrow{color:#7dd3fc;transform:translateX(3px)}
.system-strip { min-height:58px;display:grid;grid-template-columns:repeat(4,1fr);padding:8px 0; }
.system-strip>div { display:flex;align-items:center;justify-content:center;gap:9px;border-right:1px solid rgba(148,163,184,.1);color:#65c9df }.system-strip>div:last-child{border-right:0}.system-strip svg{width:15px}.system-strip p{display:grid;gap:3px;margin:0}.system-strip small{color:#4c5e75;font-size:7px;letter-spacing:1px}.system-strip strong{color:#93a4b9;font-size:9px}.status-dot{width:7px;height:7px;border-radius:50%;background:#22c55e;box-shadow:0 0 11px rgba(34,197,94,.65);animation:status-pulse 1.8s infinite}
@keyframes orbit-pulse{50%{opacity:.35;transform:scale(1.03)}}@keyframes status-pulse{50%{opacity:.45;transform:scale(1.18)}}
@media(max-width:1000px){.capability-grid{grid-template-columns:1fr 1fr}.overview-layout{grid-template-rows:auto auto auto}}
@media(max-width:700px){.hero-zone{grid-template-columns:1fr}.intelligence-map{min-height:210px}.capability-grid{grid-template-columns:1fr}.system-strip{grid-template-columns:1fr 1fr}.system-strip>div:nth-child(2){border-right:0}.system-strip>div{min-height:45px;border-bottom:1px solid rgba(148,163,184,.08)}h2{font-size:25px}}
@media(prefers-reduced-motion:reduce){.orbit,.status-dot{animation:none}}
</style>
