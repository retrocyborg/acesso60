import os

root = "C:/Prog/acesso60/Acesso60/app/src/main/java/com/acesso60"
issues = []
for dirpath, _, files in os.walk(root):
    for f in files:
        if f.endswith('.kt'):
            path = os.path.join(dirpath, f)
            with open(path, encoding='utf-8') as fh:
                content = fh.read()
                # múltiplos companion objects
                if content.count('companion object') > 1:
                    issues.append(f"{path}: multiple companion objects")
                # JSON sem configuração segura
                if 'kotlinx.serialization' in content and 'Json {' not in content:
                    issues.append(f"{path}: Json without configuration")

if issues:
    print('\n'.join(issues))
else:
    print('No JSON-related issues found.')
