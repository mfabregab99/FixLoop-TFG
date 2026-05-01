<?php
// ==========================================
// CONFIGURACIÓ DE LA BASE DE DADES
// ==========================================
$servername = "eimtcms.eimt.uoc.edu";
$username   = "mfabregab99"; 
$password   = "nHFOZ8RF";
$dbname     = "mfabregab99";

// ==========================================
// 1. CREAR LA CONNEXIÓ
// ==========================================
$conn = new mysqli($servername, $username, $password, $dbname);

// Indiquem que volem que la resposta sigui JSON
header('Content-Type: application/json; charset=utf-8');

// ==========================================
// 2. VERIFICAR LA CONNEXIÓ
// ==========================================
if ($conn->connect_error) {
    // Si falla, retornem un JSON amb l'error
    $resposta = array(
        "status" => "error",
        "missatge" => "Connexió fallida: " . $conn->connect_error
    );
    echo json_encode($resposta);
    exit();
}

// Assegurem que els caràcters especials (accents, emojis) es vegin bé
$conn->set_charset("utf8mb4");

// ==========================================
// 3. FER UNA CONSULTA DE PROVA
// ==========================================
// Consultem la taula 'categories' perquè sabem que segur que té dades
$sql = "SELECT * FROM categories";
$result = $conn->query($sql);

$dades = array();

if ($result->num_rows > 0) {
    // Si hi ha dades, les posem en un array
    while($row = $result->fetch_assoc()) {
        $dades[] = $row;
    }
    
    // Construïm la resposta d'èxit
    $resposta = array(
        "status" => "success",
        "missatge" => "Connexió exitosa! S'han trobat " . count($dades) . " categories.",
        "dades" => $dades
    );
} else {
    $resposta = array(
        "status" => "success",
        "missatge" => "Connexió exitosa, però la taula 'categories' està buida.",
        "dades" => []
    );
}

// ==========================================
// 4. RETORNAR EL RESULTAT EN JSON
// ==========================================
echo json_encode($resposta, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);

// Tanquem la connexió
$conn->close();
?>