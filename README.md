# React IDE - Android

Uma IDE completa para desenvolvimento React + TypeScript no Android com Material Design 3 e Dynamic Color.

## Características

- ✨ **Material Design 3** com Dynamic Color (adapta-se às cores do sistema)
- 📁 **Gerenciamento de Projetos** - Crie ou importe projetos React + TypeScript
- 📝 **Editor de Código** - Edite código diretamente no dispositivo
- 🔨 **Sistema de Build** - Compile projetos com npm/yarn
- 🚀 **Servidor Local** - Execute e visualize projetos em localhost
- 📱 **Preview Integrado** - Visualize alterações em tempo real
- 💻 **Terminal Integrado** - Veja logs e saída dos comandos
- 🌳 **Navegador de Arquivos** - Explore a estrutura do projeto

## Requisitos

- Android 8.0 (API 26) ou superior
- Permissões de armazenamento
- Conexão com internet (para instalação de dependências)

## Como Usar

### Criar Novo Projeto

1. Abra o app
2. Toque no botão "+" flutuante
3. Digite o nome do projeto
4. Aguarde a criação do template React + TypeScript

### Importar Projeto

1. Na tela principal, toque em "Importar"
2. Selecione a pasta do projeto existente
3. O projeto deve conter um `package.json` válido

### Editar Código

1. Selecione um projeto
2. Use o navegador de arquivos à esquerda
3. Toque em um arquivo para abrir no editor
4. Edite e salve com o ícone de disquete

### Build e Execução

1. Toque em "Build" para compilar o projeto
2. Toque em "Executar" para iniciar o servidor de desenvolvimento
3. Acesse a aba "Preview" para visualizar
4. Use a aba "Terminal" para ver logs

## Estrutura do Projeto Android

```
app/
├── src/main/
│   ├── java/com/reactide/app/
│   │   ├── MainActivity.kt
│   │   ├── models/
│   │   │   ├── Project.kt
│   │   │   └── BuildStatus.kt
│   │   ├── viewmodels/
│   │   │   └── ProjectViewModel.kt
│   │   ├── ui/
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt
│   │   │   └── screens/
│   │   │       ├── ProjectsScreen.kt
│   │   │       └── EditorScreen.kt
│   │   └── utils/
│   │       └── ProjectManager.kt
│   └── res/
├── build.gradle.kts
└── AndroidManifest.xml
```

## Tecnologias Utilizadas

### Android
- **Jetpack Compose** - UI moderna e declarativa
- **Material 3** - Design system mais recente do Google
- **Dynamic Color** - Adapta cores ao tema do sistema
- **Kotlin Coroutines** - Programação assíncrona
- **ViewModel** - Gerenciamento de estado
- **Navigation Compose** - Navegação entre telas

### Web
- **React 18** - Biblioteca para construção de interfaces
- **TypeScript** - Superset tipado do JavaScript
- **Vite** - Build tool rápida e moderna

## Build

```bash
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

O projeto inclui GitHub Actions para build automático:
- Build em cada push/PR na branch main
- APK disponível como artifact após o build

## Licença

MIT License

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.
