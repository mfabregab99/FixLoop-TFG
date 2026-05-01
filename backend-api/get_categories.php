<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$sql = "SELECT id, nom FROM categories ORDER BY nom ASC";
$result = $conn->query($sql);

$categories = array();

// Afegim manualment la opció "Tot" al principi
$categories[] = array("id" => -1, "nom" => "Tot");

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $categories[] = $row;
    }
}

echo json_encode($categories, JSON_UNESCAPED_UNICODE);
$conn->close();
?>