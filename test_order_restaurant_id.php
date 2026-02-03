<?php
// 测试订单创建时是否正确保存 restaurantId

header('Content-Type: text/html; charset=utf-8');

// 数据库配置
$host = 'localhost';
$dbname = 'canteen_db';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "<h2>测试订单创建 - restaurantId 保存验证</h2>";

    // 1. 查询最近的订单记录
    echo "<h3>1. 查询最近的订单记录（包含 restaurantId）</h3>";
    $stmt = $pdo->query("
        SELECT 
            o.id,
            o.user_id,
            o.meal_type_id,
            o.restaurant_id,
            o.order_date,
            o.status,
            o.created_at,
            u.name as user_name,
            mt.name as meal_type_name,
            r.name as restaurant_name
        FROM `order` o
        LEFT JOIN user u ON o.user_id = u.id
        LEFT JOIN meal_type mt ON o.meal_type_id = mt.id
        LEFT JOIN restaurant r ON o.restaurant_id = r.id
        ORDER BY o.created_at DESC
        LIMIT 10
    ");

    $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($orders) > 0) {
        echo "<table border='1' cellpadding='5' style='border-collapse: collapse;'>";
        echo "<tr style='background-color: #f0f0f0;'>";
        echo "<th>ID</th>";
        echo "<th>用户</th>";
        echo "<th>餐食类型</th>";
        echo "<th>餐厅ID</th>";
        echo "<th>餐厅名称</th>";
        echo "<th>订单日期</th>";
        echo "<th>状态</th>";
        echo "<th>创建时间</th>";
        echo "</tr>";

        foreach ($orders as $order) {
            $restaurantId = $order['restaurant_id'];
            $restaurantName = $order['restaurant_name'];
            $statusClass = $order['status'] == 1 ? 'color: green;' : 'color: red;';
            $statusText = $order['status'] == 1 ? '有效' : '已取消';

            echo "<tr>";
            echo "<td>{$order['id']}</td>";
            echo "<td>{$order['user_name']}</td>";
            echo "<td>{$order['meal_type_name']}</td>";
            echo "<td style='font-weight: bold; color: blue;'>{$restaurantId}</td>";
            echo "<td>{$restaurantName}</td>";
            echo "<td>{$order['order_date']}</td>";
            echo "<td style='{$statusClass}'>{$statusText}</td>";
            echo "<td>{$order['created_at']}</td>";
            echo "</tr>";
        }
        echo "</table>";

        // 统计 restaurant_id 为 NULL 的订单
        $nullCount = 0;
        $validCount = 0;
        foreach ($orders as $order) {
            if ($order['restaurant_id'] === null) {
                $nullCount++;
            } else {
                $validCount++;
            }
        }

        echo "<h3>2. 统计结果</h3>";
        echo "<ul>";
        echo "<li>有效订单（有 restaurant_id）：<strong style='color: green;'>{$validCount}</strong></li>";
        echo "<li>无效订单（restaurant_id 为 NULL）：<strong style='color: red;'>{$nullCount}</strong></li>";
        echo "</ul>";

        if ($nullCount > 0) {
            echo "<div style='background-color: #fff3cd; padding: 10px; margin-top: 10px;'>";
            echo "<strong>⚠️ 警告：</strong>存在 {$nullCount} 条订单的 restaurant_id 为 NULL，请检查前端是否正确发送了 restaurantId 参数。";
            echo "</div>";
        } else {
            echo "<div style='background-color: #d4edda; padding: 10px; margin-top: 10px;'>";
            echo "<strong>✅ 成功：</strong>所有订单都正确保存了 restaurant_id！";
            echo "</div>";
        }
    } else {
        echo "<p>暂无订单记录</p>";
    }

    // 3. 查询所有订单中 restaurant_id 的分布情况
    echo "<h3>3. 所有订单中 restaurant_id 的分布情况</h3>";
    $stmt = $pdo->query("
        SELECT 
            restaurant_id,
            r.name as restaurant_name,
            COUNT(*) as order_count
        FROM `order` o
        LEFT JOIN restaurant r ON o.restaurant_id = r.id
        GROUP BY restaurant_id, r.name
        ORDER BY order_count DESC
    ");

    $distribution = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<table border='1' cellpadding='5' style='border-collapse: collapse;'>";
    echo "<tr style='background-color: #f0f0f0;'>";
    echo "<th>餐厅ID</th>";
    echo "<th>餐厅名称</th>";
    echo "<th>订单数量</th>";
    echo "</tr>";

    foreach ($distribution as $item) {
        $restaurantId = $item['restaurant_id'];
        $restaurantName = $item['restaurant_name'];
        $count = $item['order_count'];

        echo "<tr>";
        echo "<td>{$restaurantId}</td>";
        echo "<td>{$restaurantName}</td>";
        echo "<td style='font-weight: bold;'>{$count}</td>";
        echo "</tr>";
    }
    echo "</table>";

} catch (PDOException $e) {
    echo "<div style='color: red;'>";
    echo "<strong>数据库连接错误：</strong>" . $e->getMessage();
    echo "</div>";
}
?>
