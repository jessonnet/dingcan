<?php
// 更新wang用户的部门ID

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询wang用户信息 ===\n\n";

    $stmt = $pdo->query("SELECT id, username, name, department_id, restaurant_id FROM user WHERE username = 'wang'");
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        echo "找到用户：\n";
        echo "  用户ID: {$user['id']}\n";
        echo "  用户名: {$user['username']}\n";
        echo "  姓名: {$user['name']}\n";
        echo "  部门ID: " . ($user['department_id'] ? $user['department_id'] : '未设置') . "\n";
        echo "  餐厅ID: " . ($user['restaurant_id'] ? $user['restaurant_id'] : '未设置') . "\n\n";

        if (!$user['department_id']) {
            echo "部门ID为NULL，更新为2（技术部）\n\n";

            $stmt = $pdo->prepare("UPDATE user SET department_id = 2 WHERE username = 'wang'");
            $stmt->execute();
            $affectedRows = $stmt->rowCount();

            echo "更新了 {$affectedRows} 行\n\n";

            $stmt = $pdo->query("SELECT id, username, name, department_id, restaurant_id FROM user WHERE username = 'wang'");
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

            echo "更新后的用户信息：\n";
            echo "  用户ID: {$user['id']}\n";
            echo "  用户名: {$user['username']}\n";
            echo "  姓名: {$user['name']}\n";
            echo "  部门ID: {$user['department_id']}\n";
            echo "  餐厅ID: {$user['restaurant_id']}\n";
        }
    } else {
        echo "未找到wang用户\n";
    }

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
