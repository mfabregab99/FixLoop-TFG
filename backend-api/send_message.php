<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Acceptem JSON o POST normal
    $inputJSON = file_get_contents('php://input');
    $input = json_decode($inputJSON, TRUE);
    
    // Si ve per JSON
    if ($input) {
        $xat_id = $input['xat_id'];
        $emissor_id = $input['emissor_id'];
        $text = $input['contingut'];
        $tipus = isset($input['tipus']) ? $input['tipus'] : 'text';
    } 
    // Si ve per POST (per si en el futur enviem imatges multipart)
    else {
        $xat_id = $_POST['xat_id'];
        $emissor_id = $_POST['emissor_id'];
        $text = $_POST['contingut'];
        $tipus = isset($_POST['tipus']) ? $_POST['tipus'] : 'text';
    }

    if ($xat_id && $emissor_id && $text) {
        
        // 1. Insertar Missatge
        $sql = "INSERT INTO missatges (xat_id, emissor_id, contingut, tipus, data_enviament, llegit) VALUES (?, ?, ?, ?, NOW(), 0)";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iiss", $xat_id, $emissor_id, $text, $tipus);
        
        if ($stmt->execute()) {
            // 2. Actualitzar data del Xat (perquè surti primer a la llista)
            $update_sql = "UPDATE xats SET data_ultima_activitat = NOW() WHERE id = ?";
            $update_stmt = $conn->prepare($update_sql);
            $update_stmt->bind_param("i", $xat_id);
            $update_stmt->execute();
            $update_stmt->close();

            $response['status'] = 'success';
            $response['id'] = $stmt->insert_id;
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Error SQL: ' . $stmt->error;
        }
        $stmt->close();

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Dades incompletes.';
    }

} else {
    $response['status'] = 'error';
    $response['message'] = 'Mètode no permès.';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>