<?php
// create_sollicitud.php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Validar camps obligatoris enviats des de l'Android
    if (isset($_POST['user_id']) && isset($_POST['titol']) && isset($_POST['descripcio']) && isset($_POST['categoria_id'])) {
        
        $usuari_id = $_POST['user_id']; 
        $titol = $_POST['titol'];
        $descripcio = $_POST['descripcio'];
        $categoria_id = $_POST['categoria_id'];
        
        // Gestió de la Imatge
        $db_filename = ""; // Guardarem el nom del fitxer
        if (isset($_FILES['imatge']) && $_FILES['imatge']['error'] === UPLOAD_ERR_OK) {
            $tmp_name = $_FILES['imatge']['tmp_name'];
            $name = basename($_FILES['imatge']['name']);
            $ext = pathinfo($name, PATHINFO_EXTENSION);
            
            // Generem un nom únic per al fitxer
            $filename = "sollicitud_" . time() . "_" . uniqid() . "." . $ext;
            
            $upload_dir = __DIR__ . "/uploads/";
            if (!is_dir($upload_dir)) mkdir($upload_dir, 0755, true);
            
            $target_file = $upload_dir . $filename;
            
            if (move_uploaded_file($tmp_name, $target_file)) {
                $db_filename = $filename;
            }
        }

        // Insertar a la Base de Dades
        $sql = "INSERT INTO sollicituds (usuari_id, categoria_id, titol, descripcio, foto_url, data_creacio, estat) 
                VALUES (?, ?, ?, ?, ?, NOW(), 'oberta')";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iisss", $usuari_id, $categoria_id, $titol, $descripcio, $db_filename);
        
        if ($stmt->execute()) {
            $response['status'] = 'success';
            $response['message'] = 'Sol·licitud creada correctament';
            $response['id'] = $stmt->insert_id;
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Error SQL: ' . $stmt->error;
        }
        $stmt->close();

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Falten dades obligatòries.';
    }

} else {
    $response['status'] = 'error';
    $response['message'] = 'Mètode no permès.';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>