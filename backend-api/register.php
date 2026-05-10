<?php
// register.php
// --- CONFIGURACIÓ ---
ini_set('display_errors', 0); 
ini_set('log_errors', 1);
error_reporting(E_ALL);

ini_set('memory_limit', '256M');
ini_set('post_max_size', '64M');
ini_set('upload_max_filesize', '64M');

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

function write_log($text) {
    $logfile = '/tmp/fixloop_debug.log'; 
    file_put_contents($logfile, date('Y-m-d H:i:s') . " - " . $text . "\n", FILE_APPEND);
}

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    if (isset($_POST['email']) && isset($_POST['password'])) {
        
        $email = $_POST['email'];
        $password = $_POST['password'];
        $nom_complet = $_POST['nom_complet'];
        $tipus = $_POST['tipus'];
        
        $descripcio = isset($_POST['descripcio']) ? $_POST['descripcio'] : "";
        $categories_str = isset($_POST['categories']) ? $_POST['categories'] : ""; 

        write_log("Registre iniciat: $email ($tipus). Categories: $categories_str");

        $stmt = $conn->prepare("SELECT id FROM usuaris WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $response['status'] = 'error';
            $response['message'] = 'Correu ja registrat.';
            $stmt->close();
        } else {
            $stmt->close();

            // --- GESTIÓ D'IMATGE  ---
            $db_photo_name = ""; 
            if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {
                $tmp_name = $_FILES['foto']['tmp_name'];
                $name = basename($_FILES['foto']['name']);
                $ext = pathinfo($name, PATHINFO_EXTENSION);
                $filename = "profile_" . time() . "_" . uniqid() . "." . $ext;
                
                $upload_dir = __DIR__ . "/uploads/";
                $target_file = $upload_dir . $filename;

                if (!is_dir($upload_dir)) mkdir($upload_dir, 0755, true);

                if (move_uploaded_file($tmp_name, $target_file)) {
                    $db_photo_name = $filename;
                    write_log("Foto guardada al servidor: $filename");
                } else {
                    write_log("Error movent fitxer a $target_file");
                }
            }

            // --- INSERTAR USUARI ---
            $password_hash = password_hash($password, PASSWORD_DEFAULT);
            
            $insert_stmt = $conn->prepare("INSERT INTO usuaris (email, password_hash, nom_complet, tipus, foto_perfil, descripcio) VALUES (?, ?, ?, ?, ?, ?)");
            $insert_stmt->bind_param("ssssss", $email, $password_hash, $nom_complet, $tipus, $db_photo_name, $descripcio);

            if ($insert_stmt->execute()) {
                $user_id = $insert_stmt->insert_id;
                write_log("Usuari creat amb ID: $user_id");

                if (!empty($categories_str) && $tipus === 'reparador') {
                    $cat_ids = explode(",", $categories_str);
                    $cat_stmt = $conn->prepare("INSERT INTO reparador_categories (usuari_id, categoria_id) VALUES (?, ?)");
                    
                    foreach ($cat_ids as $cat_id) {
                        $cat_id_int = intval(trim($cat_id));
                        if ($cat_id_int > 0) {
                            $cat_stmt->bind_param("ii", $user_id, $cat_id_int);
                            $cat_stmt->execute();
                        }
                    }
                    $cat_stmt->close();
                }

                $response['status'] = 'success';
                $response['message'] = 'Usuari registrat!';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Error BD: ' . $conn->error;
            }
            $insert_stmt->close();
        }
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Falten dades POST.';
    }
} else {
    $response['status'] = 'error';
    $response['message'] = 'Mètode no permès.';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>