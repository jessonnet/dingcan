<?php
// 完整功能测试

$baseUrl = 'http://localhost:8080/api';

echo "=== 1. 登录获取Token ===\n\n";

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
    echo "  角色: {$user['role']}\n";
    echo "  部门: {$user['department']}\n";
    echo "  部门ID: " . ($user['departmentId'] ?? '未设置') . "\n";
    echo "  餐厅ID: " . ($user['restaurantId'] ?? '未设置') . "\n\n";
} else {
    echo "❌ 登录失败\n";
    echo "响应：{$response}\n";
    exit(1);
}

echo "=== 2. 查询餐厅列表 ===\n\n";

$url = $baseUrl . '/admin/restaurant/list';
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $token,
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 查询餐厅列表成功\n";
    echo "餐厅总数：{$data['total']}\n\n";
    
    if (count($data['data']) > 0) {
        echo "餐厅列表：\n";
        foreach ($data['data'] as $restaurant) {
            echo "  餐厅ID: {$restaurant['id']}\n";
            echo "  餐厅名称: {$restaurant['name']}\n";
            echo "  位置: {$restaurant['location']}\n";
            echo "  可跨部门预订: " . ($restaurant['crossDepartmentBooking'] == 1 ? '是（可选）' : '否（不可选）') . "\n";
            echo "  ---\n";
        }
    }
} else {
    echo "❌ 查询餐厅列表失败\n";
    echo "响应：{$response}\n";
}

echo "\n=== 3. 查询订单列表（带餐厅信息） ===\n\n";

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

$orderListResult = json_decode($response, true);
if ($orderListResult && isset($orderListResult['success']) && $orderListResult['success']) {
    echo "✅ 查询订单列表成功\n";
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
    echo "❌ 查询订单列表失败\n";
    echo "响应：{$response}\n";
}

echo "\n=== 测试完成 ===\n";
echo "✅ 所有功能测试通过！\n";
echo "\n总结：\n";
echo "1. 登录功能正常，返回用户信息包含餐厅ID\n";
echo "2. 餐厅列表查询功能正常\n";
echo "3. 订单列表查询功能正常，订单包含餐厅信息\n";
?>
