<?php
// create_sollicitud.php
header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // 1. Validar camps obligatoris
    // L'Android ens envia 'user_id', 'titol', 'descripcio' i 'categoria_id'
    if (isset($_POST['user_id']) && isset($_POST['titol']) && isset($_POST['descripcio']) && isset($_POST['categoria_id'])) {
        
        $usuari_id = $_POST['user_id']; // Mapeig: App(user_id) -> BD(usuari_id)
        $titol = $_POST['titol'];
        $descripcio = $_POST['descripcio'];
        $categoria_id = $_POST['categoria_id'];
        
        // 2. Gestió de la Imatge
        $foto_url = ""; 
        if (isset($_FILES['imatge']) && $_FILES['imatge']['error'] === UPLOAD_ERR_OK) {
            $tmp_name = $_FILES['imatge']['tmp_name'];
            $name = basename($_FILES['imatge']['name']);
            $ext = pathinfo($name, PATHINFO_EXTENSION);
            // Generem un nom únic
            $filename = "sollicitud_" . time() . "_" . uniqid() . "." . $ext;
            
            $upload_dir = __DIR__ . "/uploads/";
            if (!is_dir($upload_dir)) mkdir($upload_dir, 0755, true);
            
            $target_file = $upload_dir . $filename;
            
            if (move_uploaded_file($tmp_name, $target_file)) {
                // Construïm la URL completa
                $foto_url = "https://eimtcms.eimt.uoc.edu/~mfabregab99/api/uploads/" . $filename;
            }
        }

        // 3. Insertar a la Base de Dades
        // Utilitzem els noms REALS de les teves columnes: usuari_id, foto_url
        $sql = "INSERT INTO sollicituds (usuari_id, categoria_id, titol, descripcio, foto_url, data_creacio, estat) VALUES (?, ?, ?, ?, ?, NOW(), 'oberta')";
        
        $stmt = $conn->prepare($sql);
        // "iisss" vol dir: integer, integer, string, string, string
        $stmt->bind_param("iisss", $usuari_id, $categoria_id, $titol, $descripcio, $foto_url);
        
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