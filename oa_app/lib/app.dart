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

    // 审批 Tab 所有人可见（我的申请+提交申请），待审批入口在页面内部做权限控制
    final navItems = <NavItem>[
      const NavItem(icon: Icons.home_outlined, selectedIcon: Icons.home, label: '首页', path: '/home'),
      const NavItem(icon: Icons.notifications_outlined, selectedIcon: Icons.notifications, label: '消息', path: '/messages'),
      const NavItem(icon: Icons.assignment_outlined, selectedIcon: Icons.assignment, label: '审批', path: '/applications', altPath: '/pending'),
      const NavItem(icon: Icons.person_outlined, selectedIcon: Icons.person, label: '我的', path: '/profile'),
    ];

    int currentIndex = -1;
    for (int i = 0; i < navItems.length; i++) {
      if (navItems[i].matches(currentRoute)) {
        currentIndex = i;
        break;
      }
    }

    return Scaffold(
      body: child,
      bottomNavigationBar: currentIndex >= 0
          ? NavigationBar(
              selectedIndex: currentIndex,
              onDestinationSelected: (index) {
                context.go(navItems[index].path);
              },
              destinations: navItems
                  .map((n) => NavigationDestination(icon: Icon(n.icon), selectedIcon: Icon(n.selectedIcon), label: n.label))
                  .toList(),
            )
          : null,
    );
  }
}

class NavItem {
  final IconData icon;
  final IconData selectedIcon;
  final String label;
  final String path;
  final String? altPath;

  const NavItem({required this.icon, required this.selectedIcon, required this.label, required this.path, this.altPath});

  bool matches(String route) => route == path || route == altPath;
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
