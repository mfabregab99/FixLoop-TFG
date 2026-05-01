<?php
// Activem report d'errors per veure warnings en pantalla si cal
error_reporting(E_ALL);
ini_set('display_errors', 1);

$target_dir = "imatges/"; 
$resposta = array();
$resposta['debug_path'] = getcwd() . '/' . $target_dir; // Per veure la ruta real

// DIAGNÒSTIC DE CARPETA
if (!file_exists($target_dir)) {
    // Intentem crear-la nosaltres (PHP) en lloc de tu (SFTP)
    if (!mkdir($target_dir, 0777, true)) {
        $resposta['status'] = 'error';
        $resposta['message'] = 'PHP no pot crear la carpeta. Error de permisos al directori pare.';
        echo json_encode($resposta);
        exit;
    }
}

// Comprovem si és "writable" realment
if (!is_writable($target_dir)) {
    $resposta['status'] = 'error';
    $resposta['message'] = 'La carpeta existeix però PHP diu que NO hi pot escriure (is_writable=false).';
    $resposta['permissions'] = substr(sprintf('%o', fileperms($target_dir)), -4);
    echo json_encode($resposta);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_FILES['foto'])) {
        
        // Mirem si hi ha hagut error a la pujada (abans de moure)
        if ($_FILES['foto']['error'] !== UPLOAD_ERR_OK) {
             $resposta['status'] = 'error';
             $resposta['message'] = 'Error de pujada PHP (Codi: ' . $_FILES['foto']['error'] . ')';
             // Codi 1 vol dir que la imatge és massa gran pel php.ini
             echo json_encode($resposta);
             exit;
        }

        $nom_fitxer = basename($_FILES["foto"]["name"]);
        $ruta_desti = $target_dir . $nom_fitxer;

        if (move_uploaded_file($_FILES["foto"]["tmp_name"], $ruta_desti)) {
            $resposta['status'] = 'success';
            $resposta['message'] = 'Imatge pujada correctament';
            $resposta['url'] = $ruta_desti;
        } else {
            $resposta['status'] = 'error';
            $resposta['message'] = 'Error final al moure (move_uploaded_file).';
            // Capturem l'últim error del sistema
            $error = error_get_last();
            $resposta['system_error'] = $error['message']; 
        }
    } else {
        $resposta['status'] = 'error';
        $resposta['message'] = 'No s\'ha rebut cap fitxer';
    }
} else {
    $resposta['status'] = 'error';
    $resposta['message'] = 'Mètode incorrecte';
}

header('Content-Type: application/json');
echo json_encode($resposta);
?>