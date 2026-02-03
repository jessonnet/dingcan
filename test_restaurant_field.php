<?php
// 测试订单列表API是否返回餐厅字段

$baseUrl = 'http://localhost:8080/api';

echo "=== 登录获取Token ===\n\n";

// 先登录获取token
$loginUrl = $baseUrl . '/auth/login';
$loginData = json_encode([
    'username' => 'admin',
    'password' => 'admin123'
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
    echo "✅ 登录成功\n\n";
} else {
    echo "❌ 登录失败\n";
    echo "响应：{$response}\n";
    exit(1);
}

echo "=== 测试订单列表API（第1页，每页1条）===\n\n";
$url = $baseUrl . '/admin/order/list?page=1&pageSize=1';
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
    echo "总记录数：{$data['total']}\n";
    echo "返回记录数：" . count($data['data']) . "\n\n";
    
    if (count($data['data']) > 0) {
        $order = $data['data'][0];
        echo "=== 第一条订单详情 ===\n";
        echo "订单ID：{$order['id']}\n";
        echo "用户名：{$order['username']}\n";
        echo "真实姓名：{$order['realName']}\n";
        echo "部门：{$order['departmentName']}\n";
        echo "餐厅ID：" . (isset($order['restaurantId']) ? $order['restaurantId'] : '未设置') . "\n";
        echo "餐厅名称：" . (isset($order['restaurantName']) ? $order['restaurantName'] : '未设置') . "\n";
        echo "餐食类型：{$order['mealTypeName']}\n";
        echo "价格：¥{$order['mealPrice']}\n";
        
        if (isset($order['restaurantName']) && !empty($order['restaurantName'])) {
            echo "\n✅ 餐厅字段已正确返回！\n";
        } else {
            echo "\n❌ 餐厅字段未返回或为空！\n";
        }
    }
} else {
    echo "❌ 请求失败\n";
    echo "响应：{$response}\n";
}
?>
