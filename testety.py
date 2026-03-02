import subprocess
from pathlib import Path

# Caminho base onde estão as pastas simple, function e full
base_dir = Path("testes/semantica/certo")
pastas = ["simple", "function", "full"]

# Caminho do compilador
java_cmd_base = ["java", "-cp", ".:tools/java-cup-11b-runtime.jar", "Lang2Compiler", "-t"]

for pasta in pastas:
    pasta_path = base_dir / pasta
    erros = []

    # Percorre todos os arquivos .lan na pasta
    for arquivo in pasta_path.glob("*.lan"):
        cmd = java_cmd_base + [str(arquivo)]
        try:
            resultado = subprocess.run(cmd, capture_output=True, text=True)
            saida = resultado.stdout.strip()
            if saida != "well-typed":
                erros.append(arquivo.name)
        except Exception as e:
            erros.append(arquivo.name)

    # Printando resumo da pasta
    print(f"{pasta} - {len(erros)} erros")
    for e in erros:
        print(e)