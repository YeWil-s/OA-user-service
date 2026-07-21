package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.user.dto.MenuDTO;
import com.oa.user.entity.SysMenu;
import com.oa.user.mapper.SysMenuMapper;
import com.oa.user.service.ISysMenuService;
import com.oa.user.vo.MenuTreeVO;
import com.oa.user.vo.RouterVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysMenuMapper sysMenuMapper;

    public SysMenuServiceImpl(SysMenuMapper sysMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> allMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1).orderByAsc(SysMenu::getSortOrder));

        List<MenuTreeVO> vos = allMenus.stream().map(menu -> {
            MenuTreeVO vo = new MenuTreeVO();
            vo.setId(menu.getId());
            vo.setParentId(menu.getParentId());
            vo.setMenuName(menu.getMenuName());
            vo.setMenuType(menu.getMenuType());
            vo.setPath(menu.getPath());
            vo.setComponent(menu.getComponent());
            vo.setPermissionCode(menu.getPermissionCode());
            vo.setIcon(menu.getIcon());
            vo.setSortOrder(menu.getSortOrder());
            vo.setVisible(menu.getVisible());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, List<MenuTreeVO>> childrenMap = vos.stream()
                .filter(v -> v.getParentId() != null && v.getParentId() != 0)
                .collect(Collectors.groupingBy(MenuTreeVO::getParentId));

        vos.forEach(v -> v.setChildren(childrenMap.getOrDefault(v.getId(), new ArrayList<>())));

        return vos.stream().filter(v -> v.getParentId() == null || v.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMenu(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermissionCode(dto.getPermissionCode());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : 1);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(menu);
    }

    @Override
    @Transactional
    public void updateMenu(Long id, MenuDTO dto) {
        SysMenu menu = this.getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermissionCode(dto.getPermissionCode());
        menu.setIcon(dto.getIcon());
        if (dto.getSortOrder() != null) menu.setSortOrder(dto.getSortOrder());
        if (dto.getVisible() != null) menu.setVisible(dto.getVisible());
        if (dto.getStatus() != null) menu.setStatus(dto.getStatus());
        this.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        if (this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)) > 0) {
            throw new BusinessException(ResultCode.MENU_HAS_CHILDREN);
        }
        this.removeById(id);
    }

    @Override
    public List<RouterVO> getUserRouters(Long userId) {
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        return buildRouterTree(menus);
    }

    private List<RouterVO> buildRouterTree(List<SysMenu> menus) {
        List<RouterVO> allRouters = menus.stream()
                .filter(m -> m.getVisible() == 1 && m.getMenuType() <= 2)
                .map(this::toRouterVO)
                .collect(Collectors.toList());

        Map<Long, List<RouterVO>> childrenMap = allRouters.stream()
                .filter(r -> {
                    return menus.stream().anyMatch(m ->
                            r.getPath() != null && m.getPath() != null
                                    && r.getPath().equals(m.getPath()) && m.getParentId() != null && m.getParentId() > 0);
                })
                .collect(Collectors.groupingBy(r -> menus.stream()
                        .filter(m -> m.getPath() != null && m.getPath().equals(r.getPath()))
                        .findFirst().map(SysMenu::getParentId).orElse(0L)));

        allRouters.forEach(r -> r.setChildren(childrenMap.getOrDefault(
                menus.stream().filter(m -> m.getPath() != null && m.getPath().equals(r.getPath()))
                        .findFirst().map(SysMenu::getId).orElse(0L), new ArrayList<>())));

        return allRouters.stream()
                .filter(r -> menus.stream().anyMatch(m ->
                        m.getPath() != null && m.getPath().equals(r.getPath())
                                && (m.getParentId() == null || m.getParentId() == 0)))
                .collect(Collectors.toList());
    }

    private RouterVO toRouterVO(SysMenu menu) {
        RouterVO vo = new RouterVO();
        vo.setName(menu.getPath());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setMenuType(menu.getMenuType());
        RouterVO.Meta meta = new RouterVO.Meta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setHidden(menu.getVisible() != null && menu.getVisible() == 0);
        vo.setMeta(meta);
        return vo;
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        return sysMenuMapper.selectMenusByUserId(userId);
    }
}
