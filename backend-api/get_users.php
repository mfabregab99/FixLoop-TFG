<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

// Intentem rebre l'ID de l'usuari que fa la petició (si està loguejat)
$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);
$current_user_id = isset($input['user_id']) ? intval($input['user_id']) : 0;

// Consulta avançada amb LEFT JOIN a favorits
// El truc és fer un LEFT JOIN amb la taula 'favorits' filtrant pel nostre usuari.
// Si troba una fila (f.reparador_id no és NULL), vol dir que és favorit.

$sql = "SELECT 
            u.id, u.nom_complet, u.email, u.tipus, u.es_pro, u.foto_perfil, 
            u.latitud, u.longitud,
            GROUP_CONCAT(c.nom SEPARATOR ', ') as llista_categories,
            (f.reparador_id IS NOT NULL) as es_favorit
        FROM usuaris u
        LEFT JOIN reparador_categories rc ON u.id = rc.usuari_id
        LEFT JOIN categories c ON rc.categoria_id = c.id
        LEFT JOIN favorits f ON u.id = f.reparador_id AND f.usuari_id = ?
        WHERE u.tipus = 'reparador'
        GROUP BY u.id";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $current_user_id);
$stmt->execute();
$result = $stmt->get_result();

$users = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $row['es_pro'] = (bool)$row['es_pro'];
        
        // Ara el camp 'is_favorite' ve directament de la BD (1 o 0)
        $row['is_favorite'] = (bool)$row['es_favorit']; 
        
        if ($row['llista_categories'] == null) {
            $row['llista_categories'] = "General";
        }
        
        $users[] = $row;
    }
}

echo json_encode($users, JSON_UNESCAPED_UNICODE);
$conn->close();
?>