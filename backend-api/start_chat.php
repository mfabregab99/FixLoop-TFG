<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $inputJSON = file_get_contents('php://input');
    $input = json_decode($inputJSON, TRUE);

    $emissor_id = isset($input['emissor_id']) ? intval($input['emissor_id']) : 0;
    $receptor_id = isset($input['receptor_id']) ? intval($input['receptor_id']) : 0;
    
    // TRUC: Si rebem 0 o no rebem res, ho convertim a NULL
    $sollicitud_val = (isset($input['sollicitud_id']) && intval($input['sollicitud_id']) > 0) ? intval($input['sollicitud_id']) : null;

    if ($emissor_id > 0 && $receptor_id > 0) {
        
        // 1. Determinar qui és client i qui reparador
        $stmt_rol = $conn->prepare("SELECT tipus FROM usuaris WHERE id = ?");
        $stmt_rol->bind_param("i", $emissor_id);
        $stmt_rol->execute();
        $stmt_rol->bind_result($tipus_emissor);
        $stmt_rol->fetch();
        $stmt_rol->close();

        $client_id = 0;
        $reparador_id = 0;

        if ($tipus_emissor === 'client') {
            $client_id = $emissor_id;
            $reparador_id = $receptor_id;
        } else {
            $client_id = $receptor_id;
            $reparador_id = $emissor_id;
        }

        // 2. Comprovar si ja existeix el xat
        // Fem servir dues consultes diferents per evitar problemes amb NULL en SQL
        if ($sollicitud_val === null) {
            // Busquem xat general (sollicitud_id IS NULL)
            $sql_check = "SELECT id FROM xats WHERE client_id = ? AND reparador_id = ? AND sollicitud_id IS NULL";
            $stmt_check = $conn->prepare($sql_check);
            $stmt_check->bind_param("ii", $client_id, $reparador_id);
        } else {
            // Busquem xat específic
            $sql_check = "SELECT id FROM xats WHERE client_id = ? AND reparador_id = ? AND sollicitud_id = ?";
            $stmt_check = $conn->prepare($sql_check);
            $stmt_check->bind_param("iii", $client_id, $reparador_id, $sollicitud_val);
        }
        
        $stmt_check->execute();
        $stmt_check->store_result();

        if ($stmt_check->num_rows > 0) {
            // JA EXISTEIX -> Retornem l'ID
            $stmt_check->bind_result($xat_id);
            $stmt_check->fetch();
            $response['status'] = 'success';
            $response['xat_id'] = $xat_id;
            $response['message'] = 'Xat recuperat';
        } else {
            // NO EXISTEIX -> CREEM UN NOU
            $stmt_check->close();
            
            $sql_insert = "INSERT INTO xats (client_id, reparador_id, sollicitud_id, data_ultima_activitat) VALUES (?, ?, ?, NOW())";
            $stmt_insert = $conn->prepare($sql_insert);
            $stmt_insert->bind_param("iii", $client_id, $reparador_id, $sollicitud_val);
            
            if ($stmt_insert->execute()) {
                $response['status'] = 'success';
                $response['xat_id'] = $stmt_insert->insert_id;
                $response['message'] = 'Xat creat';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Error SQL: ' . $stmt_insert->error;
            }
            $stmt_insert->close();
        }

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Falten IDs';
    }

} else {
    $response['status'] = 'error';
    $response['message'] = 'Mètode incorrecte';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>