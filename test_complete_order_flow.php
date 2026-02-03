<?php
// 测试完整的订餐流程

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
    echo "  角色: {$user['role']}\n";
    echo "  部门: {$user['department']}\n";
    echo "  部门ID: " . ($user['departmentId'] ?? '未设置') . "\n";
    echo "  餐厅ID: " . ($user['restaurantId'] ?? '未设置') . "\n\n";
} else {
    echo "❌ 登录失败\n";
    echo "响应：{$response}\n";
    exit(1);
}

echo "=== 查询餐食类型 ===\n\n";

$url = $baseUrl . '/admin/meal-type/list';
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
    echo "✅ 查询餐食类型成功\n";
    echo "餐食类型总数：{$data['total']}\n\n";

    if (count($data['data']) > 0) {
        echo "餐食类型列表：\n";
        foreach ($data['data'] as $mealType) {
            echo "  餐食类型ID: {$mealType['id']}\n";
            echo "  餐食类型名称: {$mealType['name']}\n";
            echo "  价格: {$mealType['price']}\n";
            echo "  ---\n";
        }
    }
} else {
    echo "❌ 查询餐食类型失败\n";
    echo "响应：{$response}\n";
}

echo "\n=== 测试批量订餐（带餐厅ID） ===\n\n";

$today = date('Y-m-d');
$tomorrow = date('Y-m-d', strtotime('+1 day'));

$orderUrl = $baseUrl . '/order/batch-create';
$orderData = json_encode([
    'mealTypeId' => 1,
    'orderDates' => [$today, $tomorrow],
    'restaurantId' => 1
]);

echo "订餐数据：\n";
echo "  餐食类型ID: 1\n";
echo "  订餐日期: {$today}, {$tomorrow}\n";
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
?>
