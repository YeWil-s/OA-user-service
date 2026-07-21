import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/chat_provider.dart';
import '../models/chat_message.dart';

class AiChatPage extends ConsumerStatefulWidget {
  const AiChatPage({super.key});

  @override
  ConsumerState<AiChatPage> createState() => _AiChatPageState();
}

class _AiChatPageState extends ConsumerState<AiChatPage> {
  final _textController = TextEditingController();
  final _scrollController = ScrollController();
  final _focusNode = FocusNode();

  static const _suggestions = [
    '请假流程是什么？',
    '年假有多少天？',
    '帮我请个假，家里有事',
    '如何提交加班申请？',
  ];

  @override
  void dispose() {
    _textController.dispose();
    _scrollController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _sendMessage() {
    final text = _textController.text.trim();
    if (text.isEmpty) return;
    _textController.clear();
    _focusNode.requestFocus();
    ref.read(chatProvider.notifier).sendMessage(text);
    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(chatProvider);
    final messages = chatState.messages;
    final theme = Theme.of(context);

    if (messages.isNotEmpty) {
      _scrollToBottom();
    }

    return Scaffold(
      backgroundColor: theme.colorScheme.surfaceContainerLowest,
      resizeToAvoidBottomInset: true,
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
                ),
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.auto_awesome, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('AI 智能助手', style: TextStyle(fontSize: 16)),
                Text(
                  chatState.isStreaming ? '正在输入...' : '在线 · 支持填单',
                  style: TextStyle(
                    fontSize: 11,
                    color: chatState.isStreaming ? theme.colorScheme.primary : Colors.green,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ],
        ),
        actions: [
          if (messages.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.add_comment_outlined),
              tooltip: '新对话',
              onPressed: () => ref.read(chatProvider.notifier).clearMessages(),
            ),
        ],
      ),
      body: GestureDetector(
        onTap: () => _focusNode.unfocus(),
        child: Column(
          children: [
            Expanded(
              child: messages.isEmpty
                  ? _buildWelcomeScreen(theme)
                  : _buildMessageList(theme),
            ),
            // 填单确认按钮
            if (chatState.formConfirmation != null && !chatState.isStreaming)
              _buildConfirmBar(theme),
            _buildInputBar(theme, chatState.isStreaming),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageList(ThemeData theme) {
    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      itemBuilder: (context, index) {
        final msg = ref.read(chatProvider).messages[index];
        final messages = ref.read(chatProvider).messages;
        final showAvatar =
            index == 0 || messages[index - 1].role != msg.role;
        return _ChatBubble(message: msg, showAvatar: showAvatar);
      },
      itemCount: ref.watch(chatProvider).messages.length,
    );
  }

  Widget _buildConfirmBar(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: SizedBox(
        width: double.infinity,
        height: 42,
        child: FilledButton.icon(
          onPressed: () => ref.read(chatProvider.notifier).confirmForm(),
          icon: const Icon(Icons.check, size: 20),
          label: const Text('确认提交表单', style: TextStyle(fontSize: 15)),
          style: FilledButton.styleFrom(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        ),
      ),
    );
  }

  Widget _buildWelcomeScreen(ThemeData theme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 80,
                height: 80,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
                  ),
                  borderRadius: BorderRadius.circular(22),
                  boxShadow: [
                    BoxShadow(
                      color: theme.colorScheme.primary.withValues(alpha: 0.3),
                      blurRadius: 20,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: const Icon(Icons.auto_awesome, color: Colors.white, size: 36),
              ),
              const SizedBox(height: 24),
              Text(
                'OA 智能助手',
                style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                '智能问答 · 一键填单 · 政策咨询',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.outline,
                ),
              ),
              const SizedBox(height: 32),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _suggestions.map((s) {
                  return ActionChip(
                    avatar: const Icon(Icons.lightbulb_outline, size: 16),
                    label: Text(s, style: const TextStyle(fontSize: 13)),
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    onPressed: () {
                      _textController.text = s;
                      _sendMessage();
                    },
                  );
                }).toList(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInputBar(ThemeData theme, bool isStreaming) {
    return Container(
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        border: Border(
          top: BorderSide(
            color: theme.colorScheme.outlineVariant.withValues(alpha: 0.3),
          ),
        ),
      ),
      child: MediaQuery.of(context).viewInsets.bottom > 0
          ? _buildInputRow(theme, isStreaming)
          : SafeArea(
              child: _buildInputRow(theme, isStreaming),
            ),
    );
  }

  Widget _buildInputRow(ThemeData theme, bool isStreaming) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        Expanded(
          child: Container(
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
              borderRadius: BorderRadius.circular(24),
            ),
            child: TextField(
              controller: _textController,
              focusNode: _focusNode,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => _sendMessage(),
              enabled: !isStreaming,
              maxLines: 4,
              minLines: 1,
              style: theme.textTheme.bodyMedium,
              decoration: InputDecoration(
                hintText: '输入消息...',
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                suffixIcon: _textController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.close, size: 20),
                        onPressed: () {
                          _textController.clear();
                          _focusNode.requestFocus();
                        },
                      )
                    : null,
              ),
              onChanged: (_) => setState(() {}),
            ),
          ),
        ),
        const SizedBox(width: 8),
        AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            gradient: isStreaming
                ? null
                : LinearGradient(
                    colors: [
                      theme.colorScheme.primary,
                      theme.colorScheme.primary.withValues(alpha: 0.7),
                    ],
                  ),
            color: isStreaming ? theme.colorScheme.surfaceContainerHighest : null,
            shape: BoxShape.circle,
          ),
          child: IconButton(
            icon: isStreaming
                ? SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: theme.colorScheme.primary,
                    ),
                  )
                : const Icon(Icons.arrow_upward, color: Colors.white, size: 22),
            onPressed: (_textController.text.trim().isNotEmpty && !isStreaming)
                ? _sendMessage
                : null,
          ),
        ),
      ],
    );
  }
}

class _ChatBubble extends StatelessWidget {
  final ChatMessage message;
  final bool showAvatar;

  const _ChatBubble({required this.message, required this.showAvatar});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isUser = message.role == 'user';
    final isEmpty = message.content.isEmpty && message.streaming;

    return Padding(
      padding: EdgeInsets.only(bottom: 16, top: showAvatar ? 8 : 0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: [
          if (!isUser && showAvatar) ...[
            _buildAssistantAvatar(theme),
            const SizedBox(width: 8),
          ] else if (!isUser) ...[
            const SizedBox(width: 44),
          ],
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: isUser
                    ? theme.colorScheme.primaryContainer
                    : theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(18),
                  topRight: const Radius.circular(18),
                  bottomLeft:
                      isUser ? const Radius.circular(18) : const Radius.circular(4),
                  bottomRight:
                      isUser ? const Radius.circular(4) : const Radius.circular(18),
                ),
                boxShadow: isUser
                    ? [
                        BoxShadow(
                          color: theme.colorScheme.primaryContainer.withValues(alpha: 0.4),
                          blurRadius: 4,
                          offset: const Offset(0, 2),
                        ),
                      ]
                    : null,
              ),
              child: isEmpty
                  ? SizedBox(
                      width: 60,
                      height: 20,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          _buildDot(theme, 0),
                          const SizedBox(width: 4),
                          _buildDot(theme, 1),
                          const SizedBox(width: 4),
                          _buildDot(theme, 2),
                        ],
                      ),
                    )
                  : Text(
                      message.content,
                      style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
                    ),
            ),
          ),
          if (isUser && showAvatar) ...[
            const SizedBox(width: 8),
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: theme.colorScheme.primary,
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.person, color: Colors.white, size: 20),
            ),
          ] else if (isUser) ...[
            const SizedBox(width: 44),
          ],
        ],
      ),
    );
  }

  static Widget _buildAssistantAvatar(ThemeData theme) {
    return Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
        ),
        borderRadius: BorderRadius.circular(10),
      ),
      child: const Icon(Icons.auto_awesome, color: Colors.white, size: 18),
    );
  }

  static Widget _buildDot(ThemeData theme, int index) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.5, end: 1.0),
      duration: const Duration(milliseconds: 600),
      curve: Curves.easeInOut,
      builder: (context, value, _) {
        return Container(
          width: 6 * value,
          height: 6 * value,
          decoration: BoxDecoration(
            color: theme.colorScheme.primary.withValues(alpha: 0.5 + (value - 0.5)),
            shape: BoxShape.circle,
          ),
        );
      },
    );
  }
}
