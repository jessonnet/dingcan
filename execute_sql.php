<?php
// 执行SQL脚本添加restaurant_id字段到订单表

$host = 'localhost';
$user = 'root';
$pass = '123456';
$dbname = 'canteen_ordering_system';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "开始执行SQL脚本...\n";
    
    // 读取SQL文件
    $sqlFile = __DIR__ . '/execute_add_restaurant_field.sql';
    if (!file_exists($sqlFile)) {
        die("SQL文件不存在: $sqlFile\n");
    }
    
    $sql = file_get_contents($sqlFile);
    
    // 执行SQL
    $pdo->exec($sql);
    
    echo "SQL脚本执行成功！\n";
    
    // 验证字段是否添加成功
    $stmt = $pdo->query("SHOW COLUMNS FROM `order` LIKE 'restaurant_id'");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($result) {
        echo "验证成功：restaurant_id字段已成功添加到订单表\n";
        echo "字段信息：\n";
        print_r($result);
    } else {
        echo "警告：restaurant_id字段未找到\n";
    }
    
} catch (PDOException $e) {
    echo "错误: " . $e->getMessage() . "\n";
    exit(1);
}
?>
