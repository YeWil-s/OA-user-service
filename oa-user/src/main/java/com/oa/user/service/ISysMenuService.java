package com.oa.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.user.dto.MenuDTO;
import com.oa.user.entity.SysMenu;
import com.oa.user.vo.MenuTreeVO;
import com.oa.user.vo.RouterVO;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    List<MenuTreeVO> getMenuTree();

    void addMenu(MenuDTO dto);

    void updateMenu(Long id, MenuDTO dto);

    void deleteMenu(Long id);

    List<RouterVO> getUserRouters(Long userId);

    List<SysMenu> getUserMenus(Long userId);
}
