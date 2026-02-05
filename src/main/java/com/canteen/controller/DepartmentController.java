package com.canteen.controller;

import com.canteen.annotation.Log;
import com.canteen.entity.Department;
import com.canteen.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门控制器
 */
@RestController
@RequestMapping("/api/admin/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取部门列表
     * @return 结果
     */
    @GetMapping("/list")
    public Map<String, Object> getDepartmentList() {
        Map<String, Object> result = new HashMap<>();
        List<Department> departments = departmentService.list();
        result.put("success", true);
        result.put("data", departments);
        return result;
    }

    /**
     * 获取启用的部门列表（用于下拉选择）
     * @return 结果
     */
    @GetMapping("/enabled")
    public Map<String, Object> getEnabledDepartments() {
        Map<String, Object> result = new HashMap<>();
        List<Department> departments = departmentService.list().stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .toList();
        result.put("success", true);
        result.put("data", departments);
        return result;
    }

    /**
     * 添加部门
     * @param department 部门信息
     * @return 结果
     */
    @Log(module = "部门管理", description = "添加部门", operationType = "CREATE")
    @PostMapping("/add")
    public Map<String, Object> addDepartment(@RequestBody Department department) {
        Map<String, Object> result = new HashMap<>();
        boolean success = departmentService.save(department);
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return result;
    }

    /**
     * 修改部门
     * @param department 部门信息
     * @return 结果
     */
    @Log(module = "部门管理", description = "修改部门", operationType = "UPDATE")
    @PutMapping("/update")
    public Map<String, Object> updateDepartment(@RequestBody Department department) {
        Map<String, Object> result = new HashMap<>();
        boolean success = departmentService.updateById(department);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return result;
    }

    /**
     * 删除部门
     * @param id 部门ID
     * @return 结果
     */
    @Log(module = "部门管理", description = "删除部门", operationType = "DELETE")
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteDepartment(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = departmentService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

}
