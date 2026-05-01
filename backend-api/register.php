<?php
// --- CONFIGURACIÓ ---
ini_set('display_errors', 0); // Important: 0 per no trencar el JSON amb warnings
ini_set('log_errors', 1);
error_reporting(E_ALL);

// Límits per fitxers grans
ini_set('memory_limit', '256M');
ini_set('post_max_size', '64M');
ini_set('upload_max_filesize', '64M');

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

// Funció de log (escriu a /tmp/fixloop_debug.log per evitar problemes de permisos)
function write_log($text) {
    $logfile = '/tmp/fixloop_debug.log'; 
    file_put_contents($logfile, date('Y-m-d H:i:s') . " - " . $text . "\n", FILE_APPEND);
}

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // 1. RECOLLIM DADES BÀSIQUES
    if (isset($_POST['email']) && isset($_POST['password'])) {
        
        $email = $_POST['email'];
        $password = $_POST['password'];
        $nom_complet = $_POST['nom_complet'];
        $tipus = $_POST['tipus'];
        
        // 2. RECOLLIM DADES EXTRES (Poden ser buides si és Client)
        $descripcio = isset($_POST['descripcio']) ? $_POST['descripcio'] : "";
        $categories_str = isset($_POST['categories']) ? $_POST['categories'] : ""; // Ex: "1,4,5"

        write_log("Registre iniciat: $email ($tipus). Categories: $categories_str");

        // 3. CHECK DUPLICATS
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

            // 4. GESTIÓ D'IMATGE (MULTIPART)
            $foto_url = "";
            if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {
                $tmp_name = $_FILES['foto']['tmp_name'];
                $name = basename($_FILES['foto']['name']);
                $ext = pathinfo($name, PATHINFO_EXTENSION);
                $filename = "profile_" . time() . "_" . uniqid() . "." . $ext;
                
                // Ruta absoluta api/uploads
                $upload_dir = __DIR__ . "/uploads/";
                $target_file = $upload_dir . $filename;

                if (!is_dir($upload_dir)) mkdir($upload_dir, 0755, true);

                if (move_uploaded_file($tmp_name, $target_file)) {
                    $foto_url = "https://eimtcms.eimt.uoc.edu/~mfabregab99/api/uploads/" . $filename;
                    write_log("Foto guardada: $foto_url");
                } else {
                    write_log("Error movent fitxer a $target_file");
                }
            }

            // 5. INSERTAR USUARI (Amb descripció)
            $password_hash = password_hash($password, PASSWORD_DEFAULT);
            
            // Assegura't que la taula 'usuaris' té la columna 'descripcio'
            $insert_stmt = $conn->prepare("INSERT INTO usuaris (email, password_hash, nom_complet, tipus, foto_perfil, descripcio) VALUES (?, ?, ?, ?, ?, ?)");
            $insert_stmt->bind_param("ssssss", $email, $password_hash, $nom_complet, $tipus, $foto_url, $descripcio);

            if ($insert_stmt->execute()) {
                
                // 6. RECUPERAR ID DEL NOU USUARI
                $user_id = $insert_stmt->insert_id;
                write_log("Usuari creat amb ID: $user_id");

                // 7. INSERTAR CATEGORIES (TAULA RELACIONAL)
                if (!empty($categories_str) && $tipus === 'reparador') {
                    $cat_ids = explode(",", $categories_str);
                    
                    // Preparem la consulta per a la taula intermèdia
                    $cat_stmt = $conn->prepare("INSERT INTO reparador_categories (usuari_id, categoria_id) VALUES (?, ?)");
                    
                    foreach ($cat_ids as $cat_id) {
                        $cat_id_int = intval(trim($cat_id)); // Netejar espais
                        if ($cat_id_int > 0) {
                            $cat_stmt->bind_param("ii", $user_id, $cat_id_int);
                            if (!$cat_stmt->execute()) {
                                write_log("Error insertant categoria $cat_id_int: " . $cat_stmt->error);
                            }
                        }
                    }
                    $cat_stmt->close();
                    write_log("Categories processades.");
                }

                $response['status'] = 'success';
                $response['message'] = 'Usuari registrat!';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Error BD: ' . $conn->error;
                write_log("Error SQL Insert Usuari: " . $conn->error);
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