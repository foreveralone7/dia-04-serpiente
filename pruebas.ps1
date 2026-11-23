# Compila el juego junto con las pruebas y las ejecuta
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src, test).FullName
if ($LASTEXITCODE -eq 0) { java -cp out serpiente.modelo.PruebasJuego }
