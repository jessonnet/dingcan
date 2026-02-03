<?php
// 执行SQL脚本修复历史订单的餐厅字段

$host = 'localhost';
$user = 'root';
$pass = '123456';
$dbname = 'canteen_ordering_system';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "开始修复历史订单的餐厅字段...\n";
    
    // 检查是否有餐厅记录
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM restaurant");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $restaurantCount = $result['count'];
    
    echo "餐厅表中共有 {$restaurantCount} 条记录\n";
    
    if ($restaurantCount == 0) {
        echo "警告：餐厅表中没有记录，无法修复订单的餐厅字段\n";
        exit(1);
    }
    
    // 获取第一条餐厅记录
    $stmt = $pdo->query("SELECT id, name FROM restaurant LIMIT 1");
    $firstRestaurant = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo "使用第一条餐厅记录：ID={$firstRestaurant['id']}, 名称={$firstRestaurant['name']}\n";
    
    // 检查需要修复的订单数量
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM `order` WHERE restaurant_id IS NULL");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $nullCount = $result['count'];
    
    echo "需要修复的订单数量：{$nullCount}\n";
    
    if ($nullCount == 0) {
        echo "没有需要修复的订单，所有订单的餐厅字段都已填写\n";
        exit(0);
    }
    
    // 更新所有restaurant_id为NULL的订单
    $updateSql = "UPDATE `order` SET restaurant_id = {$firstRestaurant['id']} WHERE restaurant_id IS NULL";
    $affectedRows = $pdo->exec($updateSql);
    
    echo "成功修复了 {$affectedRows} 条订单记录\n";
    
    // 验证修复结果
    $stmt = $pdo->query("SELECT 
        COUNT(*) as total_orders,
        SUM(CASE WHEN restaurant_id IS NULL THEN 1 ELSE 0 END) as null_restaurant_count,
        SUM(CASE WHEN restaurant_id IS NOT NULL THEN 1 ELSE 0 END) as has_restaurant_count
        FROM `order`");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo "\n验证结果：\n";
    echo "总订单数：{$result['total_orders']}\n";
    echo "餐厅字段为空的订单数：{$result['null_restaurant_count']}\n";
    echo "餐厅字段已填充的订单数：{$result['has_restaurant_count']}\n";
    
    if ($result['null_restaurant_count'] == 0) {
        echo "\n✓ 所有订单的餐厅字段修复成功！\n";
    } else {
        echo "\n⚠ 仍有 {$result['null_restaurant_count']} 条订单的餐厅字段为空\n";
    }
    
} catch (PDOException $e) {
    echo "错误: " . $e->getMessage() . "\n";
    exit(1);
}
?>
