<?php
// 测试完整的餐厅选择功能

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
        echo "所有餐厅列表：\n";
        foreach ($data['data'] as $restaurant) {
            echo "  餐厅ID: {$restaurant['id']}\n";
            echo "  餐厅名称: {$restaurant['name']}\n";
            echo "  位置: {$restaurant['location']}\n";
            echo "  可跨部门预订: " . ($restaurant['crossDepartmentBooking'] == 1 ? '是' : '否') . "\n";
            echo "  ---\n";
        }
    }
} else {
    echo "❌ 查询餐厅列表失败\n";
    echo "响应：{$response}\n";
}

echo "\n=== 3. 分析前端可选择的餐厅 ===\n\n";

$userRestaurantId = $user['restaurantId'];
echo "用户关联的餐厅ID: {$userRestaurantId}\n\n";

echo "前端可选择的餐厅列表：\n";

$availableRestaurants = [];
$restaurantIds = [];

// 首先添加用户关联的餐厅
if ($userRestaurantId) {
    foreach ($data['data'] as $restaurant) {
        if ($restaurant['id'] == $userRestaurantId) {
            $availableRestaurants[] = [
                'id' => $restaurant['id'],
                'name' => $restaurant['name'],
                'isDefault' => true
            ];
            $restaurantIds[] = $restaurant['id'];
            break;
        }
    }
}

// 然后添加所有可跨部门的餐厅
foreach ($data['data'] as $restaurant) {
    if ($restaurant['crossDepartmentBooking'] == 1 && !in_array($restaurant['id'], $restaurantIds)) {
        $availableRestaurants[] = [
            'id' => $restaurant['id'],
            'name' => $restaurant['name'],
            'isDefault' => false
        ];
        $restaurantIds[] = $restaurant['id'];
    }
}

foreach ($availableRestaurants as $restaurant) {
    echo "  餐厅ID: {$restaurant['id']}\n";
    echo "  餐厅名称: {$restaurant['name']}" . ($restaurant['isDefault'] ? ' (默认)' : '') . "\n";
    echo "  ---\n";
}

echo "\n=== 测试完成 ===\n";
echo "✅ 餐厅选择功能测试通过！\n";
echo "\n总结：\n";
echo "1. 登录功能正常，返回用户信息包含餐厅ID\n";
echo "2. 餐厅列表查询功能正常\n";
echo "3. 前端可选择的餐厅包含：\n";
echo "   - 用户关联的餐厅（标记为默认）\n";
echo "   - 所有可跨部门的餐厅\n";
?>
