<?php
// 测试后天订餐

$baseUrl = 'http://localhost:8080/api';

echo "=== 登录获取Token ===\n\n";

$loginUrl = $baseUrl . '/auth/login';
$loginData = json_encode([
    'username' => 'wang',
    'password' => '123456'
]);

$ch = curl_init($loginUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $loginData);
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

$loginResult = json_decode($response, true);
$token = null;
if ($loginResult && isset($loginResult['token'])) {
    $token = $loginResult['token'];
    $user = $loginResult['user'];
    echo "✅ 登录成功\n";
    echo "用户信息：\n";
    echo "  用户名: {$user['username']}\n";
    echo "  姓名: {$user['name']}\n";
    echo "  餐厅ID: " . ($user['restaurantId'] ?? '未设置') . "\n\n";
} else {
    echo "❌ 登录失败\n";
    echo "响应：{$response}\n";
    exit(1);
}

echo "=== 测试后天订餐（带餐厅ID） ===\n\n";

$dayAfterTomorrow = date('Y-m-d', strtotime('+2 days'));
$threeDaysLater = date('Y-m-d', strtotime('+3 days'));

$orderUrl = $baseUrl . '/order/batch-create';
$orderData = json_encode([
    'mealTypeId' => 1,
    'orderDates' => [$dayAfterTomorrow, $threeDaysLater],
    'restaurantId' => 1
]);

echo "订餐数据：\n";
echo "  餐食类型ID: 1\n";
echo "  订餐日期: {$dayAfterTomorrow}, {$threeDaysLater}\n";
echo "  餐厅ID: 1\n\n";

$ch = curl_init($orderUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $orderData);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $token,
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP状态码：{$httpCode}\n\n";

$orderResult = json_decode($response, true);
if ($orderResult && isset($orderResult['success']) && $orderResult['success']) {
    echo "✅ 订餐成功\n";
    echo "成功数量: {$orderResult['successCount']}\n";
    echo "失败数量: {$orderResult['failCount']}\n";
    if (isset($orderResult['failMessages']) && count($orderResult['failMessages']) > 0) {
        echo "失败原因：\n";
        foreach ($orderResult['failMessages'] as $message) {
            echo "  - {$message}\n";
        }
    }
} else {
    echo "❌ 订餐失败\n";
    echo "响应：{$response}\n";
}

echo "\n=== 查询订单列表 ===\n\n";

$orderListUrl = $baseUrl . '/order/list';
$ch = curl_init($orderListUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $token,
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP状态码：{$httpCode}\n\n";

$orderListResult = json_decode($response, true);
if ($orderListResult && isset($orderListResult['success']) && $orderListResult['success']) {
    echo "✅ 查询订单成功\n";
    echo "订单总数: " . count($orderListResult['data']) . "\n\n";

    if (count($orderListResult['data']) > 0) {
        echo "订单列表：\n";
        foreach ($orderListResult['data'] as $order) {
            echo "  订单ID: {$order['id']}\n";
            echo "  订餐日期: {$order['orderDate']}\n";
            echo "  餐食类型: {$order['mealTypeName']}\n";
            echo "  餐厅: " . ($order['restaurantName'] ?? '未设置') . "\n";
            echo "  状态: " . ($order['status'] == 1 ? '有效' : '无效') . "\n";
            echo "  ---\n";
        }
    }
} else {
    echo "❌ 查询订单失败\n";
    echo "响应：{$response}\n";
}
?>
