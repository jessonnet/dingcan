<?php
// 直接查询数据库确认wang用户的restaurant_id

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询wang用户的restaurant_id ===\n\n";

    $stmt = $pdo->query("SELECT id, username, name, restaurant_id FROM user WHERE username = 'wang'");
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        echo "数据库中的用户信息：\n";
        echo "  用户ID: {$user['id']}\n";
        echo "  用户名: {$user['username']}\n";
        echo "  姓名: {$user['name']}\n";
        echo "  restaurant_id: " . ($user['restaurant_id'] ? $user['restaurant_id'] : 'NULL') . "\n\n";

        if (!$user['restaurant_id']) {
            echo "restaurant_id 为 NULL，需要更新\n\n";

            $stmt = $pdo->prepare("UPDATE user SET restaurant_id = 1 WHERE username = 'wang'");
            $stmt->execute();
            $affectedRows = $stmt->rowCount();

            echo "更新了 {$affectedRows} 行\n\n";

            $stmt = $pdo->query("SELECT id, username, name, restaurant_id FROM user WHERE username = 'wang'");
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

            echo "更新后的用户信息：\n";
            echo "  用户ID: {$user['id']}\n";
            echo "  用户名: {$user['username']}\n";
            echo "  姓名: {$user['name']}\n";
            echo "  restaurant_id: {$user['restaurant_id']}\n";
        }
    } else {
        echo "未找到wang用户\n";
    }

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
