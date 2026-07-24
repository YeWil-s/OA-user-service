package com.oa.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.user.dto.PositionDTO;
import com.oa.user.entity.SysPosition;

import java.util.List;

public interface ISysPositionService extends IService<SysPosition> {

    IPage<SysPosition> pagePositions(Integer pageNum, Integer pageSize, Long deptId);

    void addPosition(PositionDTO dto);

    void updatePosition(Long id, PositionDTO dto);

    void deletePosition(Long id);

    List<Long> getRoleIdsByPositionId(Long positionId);
}
