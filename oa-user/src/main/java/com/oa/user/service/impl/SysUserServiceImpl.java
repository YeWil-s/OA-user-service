package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.common.utils.JwtUtils;
import com.oa.common.utils.RedisUtils;
import com.oa.user.client.AssetServiceClient;
import com.oa.user.client.AttendanceServiceClient;
import com.oa.user.dto.EmployeeDTO;
import com.oa.user.dto.ResetPasswordDTO;
import com.oa.user.entity.SysDept;
import com.oa.user.entity.SysPosition;
import com.oa.user.entity.SysPositionRole;
import com.oa.user.entity.SysUser;
import com.oa.user.entity.SysUserRole;
import com.oa.user.mapper.SysDeptMapper;
import com.oa.user.mapper.SysPositionMapper;
import com.oa.user.mapper.SysPositionRoleMapper;
import com.oa.user.mapper.SysUserMapper;
import com.oa.user.mapper.SysUserRoleMapper;
import com.oa.user.service.ISysUserService;
import com.oa.user.vo.CurrentUserVO;
import com.oa.user.vo.LoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final AssetServiceClient assetServiceClient;
    private final AttendanceServiceClient attendanceServiceClient;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysPositionMapper sysPositionMapper;
    private final SysPositionRoleMapper sysPositionRoleMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, JwtUtils jwtUtils,
                              BCryptPasswordEncoder passwordEncoder, RedisUtils redisUtils,
                              AssetServiceClient assetServiceClient,
                              AttendanceServiceClient attendanceServiceClient,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysDeptMapper sysDeptMapper,
                              SysPositionMapper sysPositionMapper,
                              SysPositionRoleMapper sysPositionRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.redisUtils = redisUtils;
        this.assetServiceClient = assetServiceClient;
        this.attendanceServiceClient = attendanceServiceClient;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysPositionMapper = sysPositionMapper;
        this.sysPositionRoleMapper = sysPositionRoleMapper;
    }

    @Override
    public LoginVO login(String username, String password) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(user.getId());
        List<String> roleNames = sysUserMapper.selectRoleNamesByUserId(user.getId());

        String token = jwtUtils.generateToken(user.getId(), user.getDeptId(), user.getUsername(), roles, permissions);

        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        LoginVO vo = new LoginVO(token, user.getId(), user.getUsername(), user.getRealName(), user.getAvatarUrl(), roles, permissions);
        vo.setRoleNames(roleNames);
        vo.setDeptId(user.getDeptId());
        vo.setPositionId(user.getPositionId());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        if (user.getEntryDate() != null) {
            vo.setEntryDate(user.getEntryDate().toString());
        }
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        if (user.getPositionId() != null) {
            SysPosition pos = sysPositionMapper.selectById(user.getPositionId());
            if (pos != null) vo.setPositionName(pos.getPositionName());
        }
        return vo;
    }

    @Override
    public void logout(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        long remainingTtl = jwtUtils.getRemainingTtl(token);
        if (remainingTtl > 0) {
            redisUtils.set("jwt:blacklist:" + token, "1", java.time.Duration.ofMillis(remainingTtl));
        }
    }

    @Override
    public CurrentUserVO getCurrentUser(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Long userId = jwtUtils.getUserId(token);
        SysUser user = this.getById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(userId);
        List<String> roleNames = sysUserMapper.selectRoleNamesByUserId(userId);

        CurrentUserVO vo = new CurrentUserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setDeptId(user.getDeptId());
        vo.setPositionId(user.getPositionId());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        if (user.getEntryDate() != null) {
            vo.setEntryDate(user.getEntryDate().toString());
        }
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        if (user.getPositionId() != null) {
            SysPosition pos = sysPositionMapper.selectById(user.getPositionId());
            if (pos != null) vo.setPositionName(pos.getPositionName());
        }
        vo.setRoles(roles);
        vo.setRoleNames(roleNames);
        vo.setPermissions(permissions);
        return vo;
    }

    @Override
    public IPage<SysUser> pageEmployees(Integer pageNum, Integer pageSize,
                                         Long deptId, Long positionId, String realName, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) wrapper.eq(SysUser::getDeptId, deptId);
        if (positionId != null) wrapper.eq(SysUser::getPositionId, positionId);
        if (StringUtils.hasText(realName)) wrapper.like(SysUser::getRealName, realName);
        if (status != null) wrapper.eq(SysUser::getStatus, status);
        wrapper.orderByDesc(SysUser::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SysUser getEmployeeDetail(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void addEmployee(EmployeeDTO dto) {
        SysUser exist = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new BusinessException(ResultCode.USER_EXISTED);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setDeptId(dto.getDeptId());
        user.setPositionId(dto.getPositionId());
        user.setEntryDate(dto.getEntryDate());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(user);

        Long newUserId = user.getId();
        assignRolesByPosition(newUserId, dto.getPositionId());
        try {
            assetServiceClient.createArchive(newUserId);
        } catch (Exception e) {
            log.warn("自动创建员工档案失败: userId={}, error={}", newUserId, e.getMessage());
        }
        try {
            attendanceServiceClient.assignDefaultShift(newUserId);
        } catch (Exception e) {
            log.warn("自动分配默认班次失败: userId={}, error={}", newUserId, e.getMessage());
        }
    }

    private void assignRolesByPosition(Long userId, Long positionId) {
        if (positionId == null) return;
        List<Long> roleIds = sysPositionRoleMapper.selectRoleIdsByPositionId(positionId);
        if (roleIds.isEmpty()) {
            // fallback: default employee role
            roleIds = List.of(4L);
        }
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private void reassignRolesByPosition(Long userId, Long positionId) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        assignRolesByPosition(userId, positionId);
    }

    @Override
    @Transactional
    public void updateEmployee(Long id, EmployeeDTO dto) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        boolean positionChanged = dto.getPositionId() != null
                && !dto.getPositionId().equals(user.getPositionId());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setDeptId(dto.getDeptId());
        user.setPositionId(dto.getPositionId());
        user.setEntryDate(dto.getEntryDate());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        this.updateById(user);
        if (positionChanged) {
            reassignRolesByPosition(user.getId(), dto.getPositionId());
        }
    }

    @Override
    @Transactional
    public void updateEmployeeDeptPosition(Long id, Long deptId, Long positionId) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        boolean positionChanged = positionId != null
                && !positionId.equals(user.getPositionId());
        user.setDeptId(deptId);
        user.setPositionId(positionId);
        this.updateById(user);
        if (positionChanged) {
            reassignRolesByPosition(user.getId(), positionId);
        }
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(0);
        this.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode("123456"));
        this.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, ResetPasswordDTO dto) {
        SysUser user = this.getById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return sysUserMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return sysUserMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public List<SysUser> listByDeptId(Long deptId) {
        return this.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId).eq(SysUser::getStatus, 1));
    }
}
