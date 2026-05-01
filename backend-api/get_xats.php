<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);
$my_id = isset($input['user_id']) ? intval($input['user_id']) : 0;

if ($my_id > 0) {
    
    // Utilitzem LEFT JOIN per 'sollicituds' perquè ara pot ser NULL.
    // Utilitzem COALESCE(s.titol, 'Consulta General') per si no hi ha sol·licitud.
    
    $sql = "SELECT 
                x.id as xat_id,
                x.sollicitud_id,
                x.data_ultima_activitat,
                COALESCE(s.titol, 'Consulta General') as titol_sollicitud,
                CASE 
                    WHEN x.client_id = ? THEN r.nom_complet 
                    ELSE c.nom_complet 
                END as nom_altre_usuari,
                CASE 
                    WHEN x.client_id = ? THEN r.foto_perfil 
                    ELSE c.foto_perfil 
                END as foto_altre_usuari
            FROM xats x
            LEFT JOIN sollicituds s ON x.sollicitud_id = s.id
            JOIN usuaris c ON x.client_id = c.id
            JOIN usuaris r ON x.reparador_id = r.id
            WHERE x.client_id = ? OR x.reparador_id = ?
            ORDER BY x.data_ultima_activitat DESC";

    $stmt = $conn->prepare($sql);
    // Passem el meu ID 4 vegades (pels 4 interrogants de la consulta)
    $stmt->bind_param("iiii", $my_id, $my_id, $my_id, $my_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $xats = array();
    while($row = $result->fetch_assoc()) {
        $xats[] = $row;
    }
    
    echo json_encode($xats, JSON_UNESCAPED_UNICODE);
    $stmt->close();

} else {
    echo json_encode(array());
}
$conn->close();
?>