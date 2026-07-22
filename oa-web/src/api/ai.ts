const API_BASE = import.meta.env.VITE_API_BASE || ''

export type AiEvent = {
  type?: string
  content?: string
  data?: unknown
  fields?: Record<string, unknown>
  sessionId?: string
  applicationNo?: string
  [key: string]: unknown
}

export type AiEventHandlers = Record<string, ((event: AiEvent) => void) | undefined> & {
  onEvent?: (event: AiEvent) => void
}

function getToken() {
  return localStorage.getItem('oa_token')
}

function normalizeEvent(raw: string) {
  const lines = raw
    .split(/\r?\n/)
    .map((line) => line.replace(/^data:\s?/, '').trim())
    .filter(Boolean)

  return lines.join('\n').trim()
}

function parseEvent(raw: string): AiEvent | null {
  const text = normalizeEvent(raw)
  if (!text || text === '[DONE]') return null

  try {
    return JSON.parse(text) as AiEvent
  } catch {
    return { type: 'token', content: text }
  }
}

export async function streamAi(path: string, body: unknown, handlers: AiEventHandlers = {}) {
  const token = getToken()
  const response = await fetch(path, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(body)
  })

  if (!response.ok || !response.body) {
    throw new Error(`AI 流式请求失败：${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split(/\n\n|\r\n\r\n/)
    buffer = parts.pop() || ''

    for (const part of parts) {
      const event = parseEvent(part)
      if (!event) continue
      handlers.onEvent?.(event)
      if (event.type) handlers[event.type]?.(event)
    }
  }

  const tail = parseEvent(buffer)
  if (tail) {
    handlers.onEvent?.(tail)
    if (tail.type) handlers[tail.type]?.(tail)
  }
}

export function streamChat(
  { question, sessionId }: { question: string; sessionId: string },
  handlers: AiEventHandlers
) {
  return streamAi('/api/ai/chat/stream', { question, sessionId }, handlers)
}

export function streamAgent(
  { message, sessionId, action }: { message: string; sessionId: string; action: string },
  handlers: AiEventHandlers
) {
  return streamAi('/api/ai/agent/stream', { message, sessionId, action }, handlers)
}

function buildUrl(path: string, params?: Record<string, unknown>) {
  const url = new URL(`${API_BASE}${path}`, window.location.origin)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  return url
}

async function request<T>(
  path: string,
  options: Omit<RequestInit, 'body'> & { params?: Record<string, unknown>; body?: BodyInit | Record<string, unknown> } = {}
): Promise<T> {
  const token = getToken()
  const headers = new Headers(options.headers || {})

  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(buildUrl(path, options.params), {
    ...options,
    headers,
    body: options.body && typeof options.body !== 'string' ? JSON.stringify(options.body) : options.body
  })

  if (response.status === 401) {
    localStorage.removeItem('oa_token')
    localStorage.removeItem('oa_user')
    window.location.href = '/login'
    throw new Error('登录已失效，请重新登录')
  }

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败：${response.status}`)
  }
  if (payload && typeof payload === 'object' && payload.code !== 200) {
    throw new Error(payload.message || '业务处理失败')
  }
  return (payload?.data ?? payload) as T
}

export const aiApi = {
  knowledge: {
    list: (params: Record<string, unknown>) => request<Record<string, unknown>>('/api/ai/knowledge', { params }),
    detail: (id: string | number) => request<Record<string, unknown>>(`/api/ai/knowledge/${id}`),
    create: (body: Record<string, unknown>) => request('/api/ai/knowledge', { method: 'POST', body }),
    update: (id: string | number, body: Record<string, unknown>) => request(`/api/ai/knowledge/${id}`, { method: 'PUT', body }),
    remove: (id: string | number) => request(`/api/ai/knowledge/${id}`, { method: 'DELETE' }),
    reindex: () => request('/api/ai/knowledge/reindex', { method: 'POST' }),
    tags: () => request<Array<Record<string, unknown>>>('/api/ai/knowledge/tags')
  },
  conversations: {
    list: (params: Record<string, unknown>) => request<Record<string, unknown>>('/api/ai/conversations', { params }),
    detail: (sessionId: string) => request<Array<Record<string, unknown>>>(`/api/ai/conversations/${sessionId}`),
    remove: (sessionId: string) => request(`/api/ai/conversations/${sessionId}`, { method: 'DELETE' })
  }
}
