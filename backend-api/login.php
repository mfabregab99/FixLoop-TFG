<?php
//Mostrar errors per depuració
ini_set('display_errors', 1);
error_reporting(E_ALL);

header('Content-Type: application/json; charset=utf-8');

//la connexió a la bd
require_once 'db_connect.php';

$response = array();

//rebre les dades
$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    if (isset($input['email']) && isset($input['password'])) {
        
        $email = $input['email'];
        $password = $input['password'];

        //busquem el usuari
        $stmt = $conn->prepare("SELECT id, email, password_hash, nom_complet, tipus, es_pro, foto_perfil FROM usuaris WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();

        // Si l'usuari existeix
        if ($stmt->num_rows > 0) {
            $stmt->bind_result($id, $emailDB, $password_hash, $nom, $tipus, $es_pro, $foto);
            $stmt->fetch();

            // verificar contrasenya
            //password_verify compara el text pla amb el hash guardat
            if (password_verify($password, $password_hash)) {
                
                $response['status'] = 'success';
                $response['message'] = 'Login correcte.';
                
                // Retornem les dades de l'usuari per guardar-les a l'app
                $response['user'] = array(
                    'id' => $id,
                    'email' => $emailDB,
                    'nom_complet' => $nom,
                    'tipus' => $tipus,
                    'es_pro' => (bool)$es_pro,
                    'foto_perfil' => $foto
                );

            } else {
                $response['status'] = 'error';
                $response['message'] = 'Contrasenya incorrecta.';
            }

        } else {
            $response['status'] = 'error';
            $response['message'] = 'No existeix cap usuari amb aquest correu.';
        }
        $stmt->close();

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Falten dades (email o password).';
    }

} else {
    $response['status'] = 'error';
    $response['message'] = 'Mètode no permès.';
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
$conn->close();
?>