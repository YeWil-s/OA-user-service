import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/providers/auth_providers.dart';

class AppShell extends ConsumerWidget {
  final Widget child;

  const AppShell({super.key, required this.child});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentRoute = GoRouterState.of(context).matchedLocation;

    int currentIndex;
    switch (currentRoute) {
      case '/home':
        currentIndex = 0;
        break;
      case '/messages':
        currentIndex = 1;
        break;
      case '/applications':
      case '/pending':
        currentIndex = 2;
        break;
      case '/profile':
        currentIndex = 3;
        break;
      default:
        currentIndex = -1;
    }

    return Scaffold(
      body: child,
      bottomNavigationBar: currentIndex >= 0
          ? NavigationBar(
              selectedIndex: currentIndex,
              onDestinationSelected: (index) {
                switch (index) {
                  case 0:
                    context.go('/home');
                    break;
                  case 1:
                    context.go('/messages');
                    break;
                  case 2:
                    context.go('/applications');
                    break;
                  case 3:
                    context.go('/profile');
                    break;
                }
              },
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.home_outlined),
                  selectedIcon: Icon(Icons.home),
                  label: '首页',
                ),
                NavigationDestination(
                  icon: Icon(Icons.notifications_outlined),
                  selectedIcon: Icon(Icons.notifications),
                  label: '消息',
                ),
                NavigationDestination(
                  icon: Icon(Icons.assignment_outlined),
                  selectedIcon: Icon(Icons.assignment),
                  label: '审批',
                ),
                NavigationDestination(
                  icon: Icon(Icons.person_outlined),
                  selectedIcon: Icon(Icons.person),
                  label: '我的',
                ),
              ],
            )
          : null,
    );
  }
}

class App extends ConsumerStatefulWidget {
  const App({super.key});

  @override
  ConsumerState<App> createState() => _AppState();
}

class _AppState extends ConsumerState<App> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(authProvider.notifier).checkAutoLogin());
  }

  @override
  Widget build(BuildContext context) {
    // 登录态变化时通知 GoRouter 的 refreshListenable，不重建 Router
    ref.listen(authProvider, (prev, next) {
      final wasLoggedIn = prev?.valueOrNull != null;
      final isLoggedIn = next.valueOrNull != null;
      if (wasLoggedIn != isLoggedIn) {
        authChangeNotifier.value = isLoggedIn;
      }
    });

    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'OA 管理系统',
      theme: AppTheme.light,
      routerConfig: router,
      debugShowCheckedModeBanner: false,
    );
  }
}
