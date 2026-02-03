<?php
// 测试API分页功能（带认证）

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

echo "登录响应：\n";
echo $response . "\n\n";

$loginResult = json_decode($response, true);
$token = null;
if ($loginResult && isset($loginResult['token'])) {
    $token = $loginResult['token'];
    echo "✅ 登录成功，获取到Token\n";
    echo "Token：{$token}\n\n";
} else {
    echo "❌ 登录失败\n";
    exit(1);
}

echo "=== 测试订单管理API分页功能 ===\n\n";

// 测试订单列表API（第1页，每页10条）
echo "=== 测试订单列表API（第1页，每页10条）===\n";
$url = $baseUrl . '/admin/order/list?page=1&pageSize=10';
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

echo "HTTP状态码：{$httpCode}\n";
echo "响应数据：\n";
echo $response . "\n\n";

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 请求成功\n";
    echo "总记录数：{$data['total']}\n";
    echo "返回记录数：" . count($data['data']) . "\n";
} else {
    echo "❌ 请求失败\n";
}
echo "\n";

// 测试订单列表API（第2页，每页10条）
echo "=== 测试订单列表API（第2页，每页10条）===\n";
$url = $baseUrl . '/admin/order/list?page=2&pageSize=10';
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

echo "HTTP状态码：{$httpCode}\n";
echo "响应数据：\n";
echo $response . "\n\n";

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 请求成功\n";
    echo "总记录数：{$data['total']}\n";
    echo "返回记录数：" . count($data['data']) . "\n";
} else {
    echo "❌ 请求失败\n";
}
echo "\n";

echo "=== 测试操作日志API分页功能 ===\n\n";

// 测试操作日志API（第1页，每页10条）
echo "=== 测试操作日志API（第1页，每页10条）===\n";
$url = $baseUrl . '/admin/operation-log/list?page=1&pageSize=10';
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

echo "HTTP状态码：{$httpCode}\n";
echo "响应数据：\n";
echo $response . "\n\n";

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 请求成功\n";
    echo "总记录数：{$data['total']}\n";
    echo "返回记录数：" . count($data['data']) . "\n";
} else {
    echo "❌ 请求失败\n";
}
echo "\n";

// 测试操作日志API（第2页，每页10条）
echo "=== 测试操作日志API（第2页，每页10条）===\n";
$url = $baseUrl . '/admin/operation-log/list?page=2&pageSize=10';
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

echo "HTTP状态码：{$httpCode}\n";
echo "响应数据：\n";
echo $response . "\n\n";

$data = json_decode($response, true);
if ($data && isset($data['success']) && $data['success']) {
    echo "✅ 请求成功\n";
    echo "总记录数：{$data['total']}\n";
    echo "返回记录数：" . count($data['data']) . "\n";
} else {
    echo "❌ 请求失败\n";
}
echo "\n";

echo "✅ API分页功能测试完成！\n";
?>
