package com.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.Department;
import com.canteen.mapper.DepartmentMapper;
import com.canteen.service.DepartmentService;
import org.springframework.stereotype.Service;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

}
