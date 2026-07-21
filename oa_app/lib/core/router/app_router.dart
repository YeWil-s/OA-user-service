import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/pages/login_page.dart';
import '../../features/home/pages/home_page.dart';
import '../../features/messages/pages/messages_page.dart';
import '../../features/approval/pages/applications_page.dart';
import '../../features/approval/pages/submit_application_page.dart';
import '../../features/approval/pages/pending_page.dart';
import '../../features/approval/pages/application_detail_page.dart';
import '../../features/profile/pages/profile_page.dart';
import '../../features/ai_chat/pages/ai_chat_page.dart';
import '../../app.dart';

/// 登录态变化通知器，用作 GoRouter 的 refreshListenable
final authChangeNotifier = ValueNotifier<bool>(false);

final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    refreshListenable: authChangeNotifier,
    initialLocation: '/login',
    redirect: (context, state) {
      final isLoggedIn = authChangeNotifier.value;
      final isLoginPage = state.matchedLocation == '/login';

      if (!isLoggedIn && !isLoginPage) return '/login';
      if (isLoggedIn && isLoginPage) return '/home';
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: '/home',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: HomePage(),
            ),
          ),
          GoRoute(
            path: '/messages',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: MessagesPage(),
            ),
          ),
          GoRoute(
            path: '/applications',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ApplicationsPage(),
            ),
          ),
          GoRoute(
            path: '/applications/submit',
            builder: (context, state) => const SubmitApplicationPage(),
          ),
          GoRoute(
            path: '/applications/:id',
            builder: (context, state) {
              final id = int.parse(state.pathParameters['id']!);
              return ApplicationDetailPage(id: id);
            },
          ),
          GoRoute(
            path: '/pending',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: PendingPage(),
            ),
          ),
          GoRoute(
            path: '/profile',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ProfilePage(),
            ),
          ),
          GoRoute(
            path: '/ai/chat',
            builder: (context, state) => const AiChatPage(),
          ),
        ],
      ),
    ],
  );
});
