package com.canteen.service.impl;

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
        int offset = (page - 1) * pageSize;
        return baseMapper.queryLogs(userId, username, operationType, module, 
                                  startTime, endTime, status, offset, pageSize);
    }

    @Override
    public Long countLogs(Long userId, String username, String operationType, 
                        String module, LocalDateTime startTime, LocalDateTime endTime, 
                        Integer status) {
        return baseMapper.countLogs(userId, username, operationType, module, 
                                 startTime, endTime, status);
    }

    @Override
    public void deleteLogsBeforeDate(LocalDateTime date) {
        baseMapper.delete(null);
    }
}
