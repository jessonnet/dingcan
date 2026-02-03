<?php
// 测试订单删除功能

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

echo "=== 查询订单总数（删除前）===\n\n";
$url = $baseUrl . '/admin/order/list?page=1&pageSize=1';

$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $token,
    'Content-Type: application/json'
]);
$response = curl_exec($ch);
$data = json_decode($response, true);

if ($data && isset($data['success']) && $data['success']) {
    $totalBefore = $data['total'];
    echo "删除前订单总数：{$totalBefore}\n";
    
    if (count($data['data']) > 0) {
        $orderId = $data['data'][0]['id'];
        echo "准备删除的订单ID：{$orderId}\n\n";
        
        echo "=== 执行删除操作 ===\n\n";
        $deleteUrl = $baseUrl . "/admin/order/delete/{$orderId}";
        echo "删除URL：{$deleteUrl}\n";
        
        $ch = curl_init($deleteUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Authorization: Bearer ' . $token,
            'Content-Type: application/json'
        ]);
        $deleteResponse = curl_exec($ch);
        $deleteHttpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        
        echo "HTTP状态码：{$deleteHttpCode}\n";
        echo "删除响应：{$deleteResponse}\n\n";
        
        $deleteResult = json_decode($deleteResponse, true);
        if ($deleteResult && isset($deleteResult['success']) && $deleteResult['success']) {
            echo "✅ 删除操作成功\n\n";
            
            echo "=== 查询订单总数（删除后）===\n\n";
            $ch = curl_init($url);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_HTTPHEADER, [
                'Authorization: Bearer ' . $token,
                'Content-Type: application/json'
            ]);
            $response = curl_exec($ch);
            $data = json_decode($response, true);
            
            if ($data && isset($data['success']) && $data['success']) {
                $totalAfter = $data['total'];
                echo "删除后订单总数：{$totalAfter}\n\n";
                
                if ($totalAfter == $totalBefore - 1) {
                    echo "✅ 物理删除成功！订单总数减少了1条\n";
                    echo "删除前：{$totalBefore} 条\n";
                    echo "删除后：{$totalAfter} 条\n";
                    echo "减少：1 条\n";
                } else {
                    echo "❌ 删除操作可能不是物理删除\n";
                    echo "删除前：{$totalBefore} 条\n";
                    echo "删除后：{$totalAfter} 条\n";
                }
            }
        } else {
            echo "❌ 删除操作失败\n";
        }
    }
} else {
    echo "❌ 获取订单列表失败\n";
}
?>
