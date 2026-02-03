<?php
// 测试餐厅列表API和用户信息

$baseUrl = 'http://localhost:8080/api';

echo "=== 登录获取Token ===\n\n";

// 先登录获取token
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

echo "=== 测试餐厅列表API ===\n\n";
$url = $baseUrl . '/restaurant/list';
echo "请求URL：{$url}\n";

$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $token,
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP状态码：{$httpCode}\n\n";

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 请求成功\n";
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
    echo "❌ 请求失败\n";
    echo "响应：{$response}\n";
}
?>
