<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

// Rebrem l'ID de l'usuari per POST
$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

// Si no ens envien JSON, mirem si ve per POST normal (per si de cas)
$user_id = isset($input['user_id']) ? $input['user_id'] : (isset($_POST['user_id']) ? $_POST['user_id'] : null);

if ($user_id) {
    // Consulta amb JOIN per obtenir les dades dels reparadors favorits
    // També fem el GROUP_CONCAT per les categories
    $sql = "SELECT 
                u.id, u.nom_complet, u.email, u.tipus, u.es_pro, u.foto_perfil, 
                u.latitud, u.longitud,
                GROUP_CONCAT(c.nom SEPARATOR ', ') as llista_categories
            FROM favorits f
            JOIN usuaris u ON f.reparador_id = u.id
            LEFT JOIN reparador_categories rc ON u.id = rc.usuari_id
            LEFT JOIN categories c ON rc.categoria_id = c.id
            WHERE f.usuari_id = ?
            GROUP BY u.id";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $users = array();

    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $row['es_pro'] = (bool)$row['es_pro'];
            $row['is_favorite'] = true; // Òbviament, si està aquí és favorit
            
            if ($row['llista_categories'] == null) {
                $row['llista_categories'] = "General";
            }
            $users[] = $row;
        }
    }
    echo json_encode($users, JSON_UNESCAPED_UNICODE);
    $stmt->close();

} else {
    echo json_encode(array()); // Retornem llista buida si no hi ha ID
}

$conn->close();
?>