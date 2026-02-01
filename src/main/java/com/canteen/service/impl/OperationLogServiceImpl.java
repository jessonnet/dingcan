package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.OperationLog;
import com.canteen.mapper.OperationLogMapper;
import com.canteen.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void saveLog(OperationLog log) {
        save(log);
    }

    @Override
    public List<OperationLog> queryLogs(Long userId, String username, String operationType, 
                                       String module, LocalDateTime startTime, LocalDateTime endTime, 
                                       Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.like(OperationLog::getDescription, module);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        Page<OperationLog> pageParam = new Page<>(page, pageSize);
        return page(pageParam, wrapper).getRecords();
    }

    @Override
    public Long countLogs(Long userId, String username, String operationType, 
                        String module, LocalDateTime startTime, LocalDateTime endTime, 
                        Integer status) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.like(OperationLog::getDescription, module);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        return count(wrapper);
    }

    @Override
    public void deleteLogsBeforeDate(LocalDateTime date) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(OperationLog::getCreatedAt, date);
        remove(wrapper);
    }
}
