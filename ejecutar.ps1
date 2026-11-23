# Compila y ejecuta el juego
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
if ($LASTEXITCODE -eq 0) { java -cp out serpiente.Main }
