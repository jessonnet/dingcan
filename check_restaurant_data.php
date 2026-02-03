<?php
// 查询餐厅列表数据

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询餐厅列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, name, location, status, cross_department_booking, created_at FROM restaurant ORDER BY id");
    $restaurants = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($restaurants) > 0) {
        echo "找到 " . count($restaurants) . " 个餐厅：\n\n";
        foreach ($restaurants as $restaurant) {
            echo "餐厅ID: {$restaurant['id']}\n";
            echo "餐厅名称: {$restaurant['name']}\n";
            echo "位置: {$restaurant['location']}\n";
            echo "状态: " . ($restaurant['status'] == 1 ? '启用' : '禁用') . "\n";
            echo "可跨部门预订: " . ($restaurant['cross_department_booking'] == 1 ? '是' : '否') . "\n";
            echo "创建时间: {$restaurant['created_at']}\n";
            echo "---\n";
        }
    } else {
        echo "没有找到餐厅记录\n";
    }

    echo "\n=== 查询用户餐厅信息 ===\n\n";

    $stmt = $pdo->query("SELECT id, username, name, restaurant_id FROM user WHERE role_id = 3 ORDER BY id");
    $employees = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($employees) > 0) {
        echo "找到 " . count($employees) . " 个员工：\n\n";
        foreach ($employees as $employee) {
            echo "用户ID: {$employee['id']}\n";
            echo "用户名: {$employee['username']}\n";
            echo "姓名: {$employee['name']}\n";
            echo "餐厅ID: " . ($employee['restaurant_id'] ? $employee['restaurant_id'] : '未设置') . "\n";
            echo "---\n";
        }
    } else {
        echo "没有找到员工记录\n";
    }

} catch (PDOException $e) {
    echo "数据库连接失败: " . $e->getMessage() . "\n";
}
?>
