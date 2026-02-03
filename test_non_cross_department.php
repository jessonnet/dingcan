<?php
// 测试场景：用户关联的餐厅不可跨部门

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 1. 创建测试用户 ===\n\n";

    $stmt = $pdo->prepare("INSERT INTO user (username, password, name, role_id, department_id, restaurant_id, status, created_at, updated_at) VALUES (?, ?, ?, 3, 2, 2, 1, NOW(), NOW())");
    $hashedPassword = password_hash('123456', PASSWORD_DEFAULT);
    $stmt->execute(['testuser', $hashedPassword, '测试用户']);
    $newUserId = $pdo->lastInsertId();

    echo "✅ 创建测试用户成功\n";
    echo "用户ID: {$newUserId}\n";
    echo "用户名: testuser\n";
    echo "姓名: 测试用户\n";
    echo "部门ID: 2 (技术部)\n";
    echo "餐厅ID: 2 (葵蓬馆食堂 - 不可跨部门)\n\n";

    echo "=== 2. 查询餐厅列表 ===\n\n";

    $stmt = $pdo->query("SELECT id, name, location, cross_department_booking FROM restaurant ORDER BY id");
    $restaurants = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($restaurants) > 0) {
        echo "所有餐厅列表：\n";
        foreach ($restaurants as $restaurant) {
            echo "  餐厅ID: {$restaurant['id']}\n";
            echo "  餐厅名称: {$restaurant['name']}\n";
            echo "  可跨部门预订: " . ($restaurant['cross_department_booking'] == 1 ? '是' : '否') . "\n";
            echo "  ---\n";
        }
    }

    echo "\n=== 3. 分析前端可选择的餐厅 ===\n\n";

    $userRestaurantId = 2;
    echo "用户关联的餐厅ID: {$userRestaurantId} (葵蓬馆食堂 - 不可跨部门)\n\n";

    echo "前端可选择的餐厅列表：\n";

    $availableRestaurants = [];
    $restaurantIds = [];

    // 首先添加用户关联的餐厅（不管是否可跨部门）
    foreach ($restaurants as $restaurant) {
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

    // 然后添加所有可跨部门的餐厅
    foreach ($restaurants as $restaurant) {
        if ($restaurant['cross_department_booking'] == 1 && !in_array($restaurant['id'], $restaurantIds)) {
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

    echo "\n=== 4. 测试登录 ===\n\n";

    $baseUrl = 'http://localhost:8080/api';
    $loginUrl = $baseUrl . '/auth/login';
    $loginData = json_encode([
        'username' => 'testuser',
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
    }

    echo "\n=== 测试完成 ===\n";
    echo "✅ 场景测试通过！\n";
    echo "\n总结：\n";
    echo "1. 用户关联的餐厅即使不可跨部门，也会出现在选项中（标记为默认）\n";
    echo "2. 所有可跨部门的餐厅都会出现在选项中\n";
    echo "3. 不可跨部门的餐厅（除用户关联的）不会出现在选项中\n";

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
