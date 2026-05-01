<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($input['user_id']) && isset($input['reparador_id'])) {
    
    $user_id = $input['user_id'];
    $reparador_id = $input['reparador_id'];

    // 1. Comprovem si ja existeix
    $check_stmt = $conn->prepare("SELECT * FROM favorits WHERE usuari_id = ? AND reparador_id = ?");
    $check_stmt->bind_param("ii", $user_id, $reparador_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows > 0) {
        // JA EXISTEIX -> ESBORREM (Remove favorite)
        $delete_stmt = $conn->prepare("DELETE FROM favorits WHERE usuari_id = ? AND reparador_id = ?");
        $delete_stmt->bind_param("ii", $user_id, $reparador_id);
        if ($delete_stmt->execute()) {
            echo json_encode(array("status" => "removed", "message" => "Eliminat de favorits"));
        } else {
            echo json_encode(array("status" => "error", "message" => "Error eliminant"));
        }
        $delete_stmt->close();

    } else {
        // NO EXISTEIX -> AFEGIM (Add favorite)
        $insert_stmt = $conn->prepare("INSERT INTO favorits (usuari_id, reparador_id) VALUES (?, ?)");
        $insert_stmt->bind_param("ii", $user_id, $reparador_id);
        if ($insert_stmt->execute()) {
            echo json_encode(array("status" => "added", "message" => "Afegit a favorits"));
        } else {
            echo json_encode(array("status" => "error", "message" => "Error afegint"));
        }
        $insert_stmt->close();
    }
    $check_stmt->close();

} else {
    echo json_encode(array("status" => "error", "message" => "Dades incompletes"));
}
$conn->close();
?>