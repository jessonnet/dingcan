<?php
// 添加一个可跨部门的餐厅用于测试

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询当前餐厅列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, name, location, cross_department_booking FROM restaurant ORDER BY id");
    $restaurants = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($restaurants) > 0) {
        echo "当前餐厅列表：\n";
        foreach ($restaurants as $restaurant) {
            echo "  餐厅ID: {$restaurant['id']}\n";
            echo "  餐厅名称: {$restaurant['name']}\n";
            echo "  位置: {$restaurant['location']}\n";
            echo "  可跨部门预订: " . ($restaurant['cross_department_booking'] == 1 ? '是' : '否') . "\n";
            echo "  ---\n";
        }
    }

    echo "\n=== 添加测试餐厅（可跨部门） ===\n\n";

    $stmt = $pdo->prepare("INSERT INTO restaurant (name, location, cross_department_booking, created_at, updated_at) VALUES (?, ?, 1, NOW(), NOW())");
    $stmt->execute(['测试食堂', '测试地址']);
    $newRestaurantId = $pdo->lastInsertId();

    echo "✅ 添加成功\n";
    echo "新餐厅ID: {$newRestaurantId}\n";
    echo "餐厅名称: 测试食堂\n";
    echo "位置: 测试地址\n";
    echo "可跨部门预订: 是\n\n";

    echo "=== 更新后的餐厅列表 ===\n\n";

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

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
