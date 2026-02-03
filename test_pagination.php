<?php
// 测试分页功能

$host = 'localhost';
$user = 'root';
$pass = '123456';
$dbname = 'canteen_ordering_system';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "=== 测试订单表分页功能 ===\n\n";
    
    // 获取订单总数
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM `order`");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $totalOrders = $result['total'];
    echo "订单总数：{$totalOrders}\n\n";
    
    // 测试分页查询（第1页，每页10条）
    echo "=== 第1页（每页10条）===\n";
    $stmt = $pdo->query("SELECT * FROM `order` ORDER BY created_at DESC LIMIT 0, 10");
    $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "返回记录数：" . count($orders) . "\n";
    if (count($orders) > 0) {
        echo "第一条记录ID：" . $orders[0]['id'] . "\n";
        echo "最后一条记录ID：" . $orders[count($orders)-1]['id'] . "\n";
    }
    echo "\n";
    
    // 测试分页查询（第2页，每页10条）
    if ($totalOrders > 10) {
        echo "=== 第2页（每页10条）===\n";
        $stmt = $pdo->query("SELECT * FROM `order` ORDER BY created_at DESC LIMIT 10, 10");
        $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo "返回记录数：" . count($orders) . "\n";
        if (count($orders) > 0) {
            echo "第一条记录ID：" . $orders[0]['id'] . "\n";
            echo "最后一条记录ID：" . $orders[count($orders)-1]['id'] . "\n";
        }
        echo "\n";
    }
    
    echo "=== 测试操作日志表分页功能 ===\n\n";
    
    // 获取日志总数
    $stmt = $pdo->query("SELECT COUNT(*) as total FROM operation_log");
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $totalLogs = $result['total'];
    echo "日志总数：{$totalLogs}\n\n";
    
    // 测试分页查询（第1页，每页10条）
    echo "=== 第1页（每页10条）===\n";
    $stmt = $pdo->query("SELECT * FROM operation_log ORDER BY created_at DESC LIMIT 0, 10");
    $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "返回记录数：" . count($logs) . "\n";
    if (count($logs) > 0) {
        echo "第一条记录ID：" . $logs[0]['id'] . "\n";
        echo "最后一条记录ID：" . $logs[count($logs)-1]['id'] . "\n";
    }
    echo "\n";
    
    // 测试分页查询（第2页，每页10条）
    if ($totalLogs > 10) {
        echo "=== 第2页（每页10条）===\n";
        $stmt = $pdo->query("SELECT * FROM operation_log ORDER BY created_at DESC LIMIT 10, 10");
        $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo "返回记录数：" . count($logs) . "\n";
        if (count($logs) > 0) {
            echo "第一条记录ID：" . $logs[0]['id'] . "\n";
            echo "最后一条记录ID：" . $logs[count($logs)-1]['id'] . "\n";
        }
        echo "\n";
    }
    
    echo "✅ 数据库分页功能测试完成！\n";
    
} catch (PDOException $e) {
    echo "错误: " . $e->getMessage() . "\n";
    exit(1);
}
?>
