<?php
//Comprova si un correu ja està registrat
ini_set('display_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$response = array();
$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($input['email'])) {
    $email = $input['email'];

    // consultem si existeix
    $stmt = $conn->prepare("SELECT id FROM usuaris WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $response['status'] = 'exists';
        $response['message'] = 'Aquest correu ja està registrat.';
    } else {
        $response['status'] = 'available';
        $response['message'] = 'Correu disponible.';
    }
    $stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Dades incorrectes.';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>