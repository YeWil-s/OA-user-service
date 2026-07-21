/// 全局 401 回调，避免 provider 循环依赖
typedef LogoutCallback = void Function();

LogoutCallback? _onLogout;

void setLogoutCallback(LogoutCallback callback) {
  _onLogout = callback;
}

void triggerLogout() {
  _onLogout?.call();
}
