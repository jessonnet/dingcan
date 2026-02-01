package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogService extends IService<OperationLog> {

    void saveLog(OperationLog log);

    List<OperationLog> queryLogs(Long userId, String username, String operationType, 
                                String module, LocalDateTime startTime, LocalDateTime endTime, 
                                Integer status, Integer page, Integer pageSize);

    Long countLogs(Long userId, String username, String operationType, 
                 String module, LocalDateTime startTime, LocalDateTime endTime, 
                 Integer status);

    void deleteLogsBeforeDate(LocalDateTime date);
}
