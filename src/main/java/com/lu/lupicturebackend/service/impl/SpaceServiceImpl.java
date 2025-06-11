package com.lu.lupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;

import com.lu.lupicturebackend.model.dto.space.SpaceAddRequest;
import com.lu.lupicturebackend.model.dto.space.SpaceQueryRequest;


import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.SpaceLevelEnum;
import com.lu.lupicturebackend.model.enums.SpaceTypeEnum;
import com.lu.lupicturebackend.model.vo.SpaceVO;
import com.lu.lupicturebackend.model.vo.UserVO;
import com.lu.lupicturebackend.service.SpaceService;
import com.lu.lupicturebackend.mapper.SpaceMapper;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 86186
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-06-05 10:16:19
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserServiceImpl userService;

    // 编程式事务
    @Resource
    private TransactionTemplate transactionTemplate;


    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
//    @Transactional  // 注解式在锁情况下会不起作用，因为它在整个方法执行完成后再提交事务
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1.填充参数
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 空间名
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        // 空间级别
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //  空间类型
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量大小
        this.fillSpaceBySpaceLevel(space);
        // 2.校验参数
        validSpace(space, true);

        // 3.权限校验
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (!userService.isAdmin(loginUser) && SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别空间");
        }
        // 获取锁防止用户创建多个空间，只能创建一个私有空间、以及一个团队空间
//        String lock = String.valueOf(userId).intern();
        Map<Long, Object> lockMap = new ConcurrentHashMap<>();
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());

        // 添加编程式事务
        Long newSpaceId = transactionTemplate.execute(transactionStatus -> {
            synchronized (lock) {
                try {
                    // 数据库操作
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceLevel, space.getSpaceLevel())
                            .exists();
                    if (exists) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已创建空间");
                    }
                    // 操作数据库
                    boolean result = this.save(space);
                    if (!result) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存到数据库失败");
                    }
                    return space.getId();
                } finally {
                    // 防止内存泄漏
                    lockMap.remove(userId);
                }
            }
//            synchronized (lock) {  //  锁对象
//                boolean exists = this.lambdaQuery()
//                        .eq(Space::getUserId, userId)
//                        .exists();
//                if (exists) {
//                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已创建空间");
//                }
//                // 操作数据库
//                boolean result = this.save(space);
//                if (!result) {
//                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存到数据库失败");
//                }
//                return space.getId();
//            }
        });
        return Optional.ofNullable(newSpaceId).orElse(-1L);

    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceTypeEnum == null && spaceType != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = spaceVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        Page<SpaceVO> SpaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        // 获取空间列表
        List<Space> SpaceList = spacePage.getRecords();
        if (CollUtil.isEmpty(SpaceList)) {
            return SpaceVOPage;
        }
        // 将空间列表转为空间VO列表
        List<SpaceVO> SpaceVOList = SpaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 获取用户信息
        Set<Long> collect = SpaceList.stream()
                .map(Space::getUserId)
                .collect(Collectors.toSet());
        // 查询用户信息
        Map<Long, List<User>> userMap = userService.listByIds(collect) // where id in (1,2,3)
                .stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        SpaceVOList.forEach(SpaceVO -> {
            Long userId = SpaceVO.getUserId();
            if (userId != null && userId > 0) {
                List<User> users = userMap.get(userId);
                if (CollUtil.isNotEmpty(users)) {
                    User user = users.get(0);
                    UserVO userVO = userService.getUserVO(user);
                    SpaceVO.setUser(userVO);
                }
            }
        });
        SpaceVOPage.setRecords(SpaceVOList);
        return SpaceVOPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (enumByValue != null) {
            if (space.getMaxSize() == null) {
                space.setMaxSize(enumByValue.getMaxSize());
            }
            if (space.getMaxCount() == null) {
                space.setMaxCount(enumByValue.getMaxCount());
            }
        }
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR);
        }
    }


}




