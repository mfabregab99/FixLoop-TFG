<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

// Aquest endpoint retorna totes les sol·licituds obertes (pels reparadors)
// Opcionalment podríem filtrar per categoria, però per ara mostrem totes.

$sql = "SELECT 
            s.id, s.usuari_id, s.categoria_id, s.titol, s.descripcio, s.foto_url, s.data_creacio, s.estat,
            c.nom as categoria_nom,
            u.nom_complet as usuari_nom,
            u.foto_perfil as usuari_foto
        FROM sollicituds s
        JOIN categories c ON s.categoria_id = c.id
        JOIN usuaris u ON s.usuari_id = u.id
        WHERE s.estat = 'oberta'
        ORDER BY s.data_creacio DESC";

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