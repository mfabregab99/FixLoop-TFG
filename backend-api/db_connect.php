<?php
// Dades de connexió
$servername = "localhost";
$username   = "mfabregab99"; 
$password   = "nHFOZ8RF";
$dbname     = "mfabregab99";

// Crear connexió
$conn = new mysqli($servername, $username, $password, $dbname);

// Configurar charset per a emojis i accents
$conn->set_charset("utf8mb4");

// Verificar si hi ha error
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>