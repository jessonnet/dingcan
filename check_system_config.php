<?php
// 查询系统配置

$host = 'localhost';
$dbname = 'canteen_ordering_system';
$username = 'root';
$password = '123456';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "=== 查询系统配置 ===\n\n";

    $stmt = $pdo->query("SELECT config_key, config_value FROM system_config");
    $configs = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($configs) > 0) {
        echo "系统配置：\n";
        foreach ($configs as $config) {
            echo "  {$config['config_key']}: {$config['config_value']}\n";
        }
    } else {
        echo "没有找到系统配置\n";
    }

    echo "\n当前时间: " . date('Y-m-d H:i:s') . "\n";

} catch (PDOException $e) {
    echo "数据库错误: " . $e->getMessage() . "\n";
}
?>
