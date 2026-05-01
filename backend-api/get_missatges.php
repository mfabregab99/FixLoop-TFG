<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$xat_id = isset($input['xat_id']) ? intval($input['xat_id']) : 0;
$last_id = isset($input['last_id']) ? intval($input['last_id']) : 0; // El "cursor"

if ($xat_id > 0) {
    
    if ($last_id == 0) {
        // CÀRREGA INICIAL: Els últims 50 missatges
        // Ordenem DESC per agafar els últims, però després el mòbil els haurà d'ordenar bé
        $sql = "SELECT * FROM missatges WHERE xat_id = ? ORDER BY id DESC LIMIT 50";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $xat_id);
    } else {
        // REFRESH (SCROLL AVALL): Missatges nous (més nous que l'últim que tinc)
        $sql = "SELECT * FROM missatges WHERE xat_id = ? AND id > ? ORDER BY id ASC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $xat_id, $last_id);
    }

    $stmt->execute();
    $result = $stmt->get_result();

    $missatges = array();
    while($row = $result->fetch_assoc()) {
        // Convertim l'ID a enter per si de cas
        $row['id'] = intval($row['id']);
        $row['emissor_id'] = intval($row['emissor_id']);
        $missatges[] = $row;
    }
    
    // Si era càrrega inicial (DESC), els girem per enviar-los cronològics (1, 2, 3...)
    if ($last_id == 0) {
        $missatges = array_reverse($missatges);
    }

    echo json_encode($missatges, JSON_UNESCAPED_UNICODE);
    $stmt->close();

} else {
    echo json_encode(array());
}
$conn->close();
?>