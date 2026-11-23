# Crea serpiente.jar: un solo archivo que se puede abrir con doble clic
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
if ($LASTEXITCODE -eq 0) {
    jar --create --file serpiente.jar --main-class serpiente.Main -C out .
    Write-Host "Listo: serpiente.jar creado. Ejecutalo con: java -jar serpiente.jar"
}
