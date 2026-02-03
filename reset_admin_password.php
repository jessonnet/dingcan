<?php
// 重置admin密码为admin123

$host = 'localhost';
$user = 'root';
$pass = '123456';
$dbname = 'canteen_ordering_system';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "=== 重置admin密码 ===\n\n";
    
    // 生成BCrypt密码
    $password = 'admin123';
    $hash = password_hash($password, PASSWORD_BCRYPT);
    
    echo "新密码：{$password}\n";
    echo "加密后的密码：{$hash}\n\n";
    
    // 更新admin用户的密码
    $stmt = $pdo->prepare("UPDATE user SET password = ? WHERE username = 'admin'");
    $result = $stmt->execute([$hash]);
    
    if ($result) {
        echo "✅ 密码重置成功！\n";
        echo "用户名：admin\n";
        echo "密码：admin123\n";
    } else {
        echo "❌ 密码重置失败\n";
    }
    
} catch (PDOException $e) {
    echo "错误: " . $e->getMessage() . "\n";
    exit(1);
}
?>
