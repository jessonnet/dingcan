<?php
// 查询部门和餐厅的关系

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询部门列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, name FROM department ORDER BY id");
    $departments = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($departments) > 0) {
        echo "部门列表：\n";
        foreach ($departments as $dept) {
            echo "  部门ID: {$dept['id']}\n";
            echo "  部门名称: {$dept['name']}\n";
            echo "  ---\n";
        }
    }

    echo "\n=== 查询餐厅列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, name, location, cross_department_booking FROM restaurant ORDER BY id");
    $restaurants = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($restaurants) > 0) {
        echo "餐厅列表：\n";
        foreach ($restaurants as $restaurant) {
            echo "  餐厅ID: {$restaurant['id']}\n";
            echo "  餐厅名称: {$restaurant['name']}\n";
            echo "  位置: {$restaurant['location']}\n";
            echo "  可跨部门预订: " . ($restaurant['cross_department_booking'] == 1 ? '是' : '否') . "\n";
            echo "  ---\n";
        }
    }

    echo "\n=== 查询用户列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, username, name, department_id, restaurant_id FROM user WHERE role_id = 3 ORDER BY id");
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($users) > 0) {
        echo "员工列表：\n";
        foreach ($users as $user) {
            echo "  用户ID: {$user['id']}\n";
            echo "  用户名: {$user['username']}\n";
            echo "  姓名: {$user['name']}\n";
            echo "  部门ID: " . ($user['department_id'] ?? '未设置') . "\n";
            echo "  餐厅ID: " . ($user['restaurant_id'] ?? '未设置') . "\n";
            echo "  ---\n";
        }
    }

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
