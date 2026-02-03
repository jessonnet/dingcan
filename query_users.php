<?php
// 查询管理员账号

$host = 'localhost';
$user = 'root';
$pass = '123456';
$dbname = 'canteen_ordering_system';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "=== 查询用户表 ===\n\n";
    
    $stmt = $pdo->query("SELECT id, username, name, role_id FROM user ORDER BY id");
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "用户列表：\n";
    echo "ID\t用户名\t姓名\t角色ID\n";
    echo "--------------------------------\n";
    foreach ($users as $user) {
        echo "{$user['id']}\t{$user['username']}\t{$user['name']}\t{$user['role_id']}\n";
    }
    echo "\n";
    
    echo "=== 查询角色表 ===\n\n";
    
    $stmt = $pdo->query("SELECT id, name, description FROM role");
    $roles = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "角色列表：\n";
    echo "ID\t角色名称\t描述\n";
    echo "--------------------------------\n";
    foreach ($roles as $role) {
        echo "{$role['id']}\t{$role['name']}\t{$role['description']}\n";
    }
    
} catch (PDOException $e) {
    echo "错误: " . $e->getMessage() . "\n";
    exit(1);
}
?>
