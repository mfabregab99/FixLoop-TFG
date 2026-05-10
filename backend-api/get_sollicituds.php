<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

// Consulta optimitzada per a l'Adapter de l'Android
$sql = "SELECT 
            s.id, s.usuari_id, s.categoria_id, s.titol, s.descripcio, s.foto_url, s.data_creacio, s.estat,
            c.nom as categoria_nom, 
            u.nom_complet as usuari_nom, 
            u.foto_perfil as usuari_foto
        FROM sollicituds s
        LEFT JOIN categories c ON s.categoria_id = c.id
        LEFT JOIN usuaris u ON s.usuari_id = u.id
        WHERE s.estat = 'oberta'
        ORDER BY s.id DESC"; // Ordenem per ID per veure les més recents a dalt

$result = $conn->query($sql);
$sollicituds = array();

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $sollicituds[] = $row;
    }
}

echo json_encode($sollicituds, JSON_UNESCAPED_UNICODE);
$conn->close();
?>