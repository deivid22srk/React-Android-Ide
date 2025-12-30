# React IDE - Android

<div align="center">

![React IDE](https://img.shields.io/badge/React-IDE-61DAFB?style=for-the-badge&logo=react)
![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?style=for-the-badge&logo=android)
![Material Design 3](https://img.shields.io/badge/Material%20Design-3-6200EE?style=for-the-badge&logo=materialdesign)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-7F52FF?style=for-the-badge&logo=kotlin)

**Uma IDE completa para desenvolvimento React + TypeScript no Android**

[Download APK](../../releases) · [Reportar Bug](../../issues) · [Solicitar Feature](../../issues)

</div>

---

## ✨ Características

### 🎨 **Interface Moderna**
- **Material Design 3** com Dynamic Color
- **Navigation Drawer** lateral para arquivos
- **Toolbar** organizado com ações rápidas
- **Tabs** para Editor, Preview e Terminal
- **Theme** adaptativo (claro/escuro)

### 📁 **Gerenciamento de Projetos**
- ✅ Criar projetos React + TypeScript com template completo
- ✅ Importar projetos existentes
- ✅ Navegador de arquivos em árvore
- ✅ Criar novos arquivos
- ✅ Persistência de projetos

### 📝 **Editor de Código**
- ✅ Editor de texto otimizado
- ✅ Fonte monoespaçada
- ✅ Syntax highlighting por tipo de arquivo
- ✅ Salvar arquivos
- ✅ Indicador de arquivo atual
- ✅ Scroll horizontal e vertical

### 🔨 **Sistema de Build**
- ✅ Instalação automática de dependências (npm install)
- ✅ Build de projetos (npm run build)
- ✅ Servidor de desenvolvimento (npm start)
- ✅ Logs em tempo real no terminal
- ✅ Indicadores de status (Building/Running/Error)

### 💻 **Terminal Integrado**
- ✅ Logs de build e execução
- ✅ **Copiar logs** para clipboard
- ✅ **Exportar logs** para arquivo
- ✅ Limpar terminal
- ✅ Visual tipo VS Code (tema escuro)

### 📺 **Preview**
- ✅ WebView integrado
- ✅ Visualização em tempo real
- ✅ Suporte a localhost:3000

---

## 📱 Screenshots

<details>
<summary>Ver capturas de tela</summary>

### Tela de Projetos
Interface limpa com lista de projetos e botão FAB para criar novos.

### Editor com Drawer
Navigation drawer lateral mostrando árvore de arquivos, editor de código central com syntax highlighting.

### Terminal
Terminal integrado com logs coloridos, botões para copiar e limpar.

</details>

---

## 🚀 Como Usar

### Requisitos
- **Android 8.0 (API 26)** ou superior
- **Node.js e npm** instalados no dispositivo (via Termux)
- Permissões de armazenamento
- 100 MB de espaço livre

### Instalação

#### Opção 1: Download Direto
1. Acesse a aba [Releases](../../releases)
2. Baixe o APK mais recente
3. Instale no dispositivo
4. Permita instalação de fontes desconhecidas

#### Opção 2: Build do Código
```bash
git clone https://github.com/deivid22srk/React-Android-Ide.git
cd React-Android-Ide
./gradlew assembleDebug
```

### Configuração Node.js (Necessário para Build/Run)

Para que o sistema de build funcione, você precisa ter Node.js instalado. A melhor forma no Android é via **Termux**:

#### Instalar Termux + Node.js

1. **Baixe o Termux**
   - [F-Droid](https://f-droid.org/packages/com.termux/) (recomendado)
   - Não use a versão do Google Play (desatualizada)

2. **Configure o Termux**
   ```bash
   # Atualizar pacotes
   pkg update && pkg upgrade
   
   # Instalar Node.js e npm
   pkg install nodejs
   
   # Verificar instalação
   node --version
   npm --version
   ```

3. **Dar permissões de armazenamento**
   ```bash
   termux-setup-storage
   ```

4. **Acessar diretório da IDE**
   ```bash
   cd /storage/emulated/0/Android/data/com.reactide.app/files/projects
   ```

#### Usar a IDE

**Criar Projeto:**
1. Abra a IDE
2. Toque no botão **+**
3. Digite o nome do projeto
4. Aguarde a criação

**Editar Código:**
1. Toque no projeto
2. Use o ícone de **pasta** para abrir o drawer
3. Navegue pelos arquivos
4. Edite e salve com o ícone de **disquete**

**Build do Projeto:**
1. Toque em **Build**
2. Aguarde instalação de dependências
3. Veja logs no terminal
4. Aguarde mensagem de sucesso

**Executar Projeto:**
1. Toque em **Executar**
2. Aguarde servidor iniciar
3. Vá para aba **Preview**
4. Navegue no site

**Copiar Logs:**
1. Vá para aba **Terminal**
2. Toque no ícone de **copiar**
3. Cole onde quiser

---

## 🎯 Funcionalidades Implementadas

### ✅ Core
- [x] Material Design 3 com Dynamic Color
- [x] Navegação por drawer
- [x] Gerenciamento de estado com ViewModel
- [x] Coroutines para operações assíncronas
- [x] Persistência de dados

### ✅ Editor
- [x] Editor de código funcional
- [x] Árvore de arquivos
- [x] Criar/editar/salvar arquivos
- [x] Syntax highlighting visual
- [x] Scroll suave

### ✅ Build System
- [x] npm install automático
- [x] npm run build
- [x] npm start
- [x] Logs em tempo real
- [x] Controle de processo

### ✅ Terminal
- [x] Output colorido
- [x] Copiar logs
- [x] Exportar logs
- [x] Limpar terminal
- [x] Auto-scroll

### ✅ Preview
- [x] WebView integrado
- [x] Carregar localhost
- [x] JavaScript habilitado

---

## 🔧 Arquitetura

### Tecnologias
- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e declarativa
- **Material 3** - Design system
- **Coroutines** - Programação assíncrona
- **ViewModel** - Gerenciamento de estado
- **StateFlow** - State management reativo
- **Navigation Compose** - Navegação entre telas
- **Gson** - Serialização JSON

### Estrutura do Código
```
app/src/main/java/com/reactide/app/
├── MainActivity.kt                    # Activity principal
├── models/
│   ├── Project.kt                    # Modelo de projeto
│   ├── FileNode.kt                   # Modelo de arquivo
│   └── BuildStatus.kt                # Estados do build
├── viewmodels/
│   └── ProjectViewModel.kt           # Lógica de negócio
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt          # Navegação
│   └── screens/
│       ├── ProjectsScreen.kt         # Tela de projetos
│       └── EditorScreen.kt           # Tela do editor
└── utils/
    └── ProjectManager.kt             # Gerenciador de projetos
```

### Fluxo de Dados
```
User Interaction → ViewModel → ProjectManager → File System
                     ↓
                  StateFlow
                     ↓
                Compose UI (Recompõe)
```

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Siga estes passos:

1. Fork o projeto
2. Crie sua branch (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Add: Minha feature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Roadmap

#### 🎯 Próximas Features
- [ ] Syntax highlighting real (CodeMirror ou Monaco)
- [ ] Autocomplete de código
- [ ] Integração com nodejs-mobile
- [ ] Múltiplas abas de arquivos
- [ ] Buscar e substituir
- [ ] Git integration
- [ ] Temas de código personalizados
- [ ] Atalhos de teclado
- [ ] Snippets
- [ ] Linter integrado

#### 🐛 Melhorias
- [ ] Performance do editor em arquivos grandes
- [ ] Cache de builds
- [ ] Hot reload
- [ ] Otimizar uso de memória
- [ ] Suporte a tablets

---

## 📋 Requisitos do Sistema

### Mínimo
- Android 8.0 (API 26)
- 2 GB RAM
- 100 MB espaço livre

### Recomendado
- Android 12+ (Dynamic Color)
- 4 GB RAM
- 500 MB espaço livre
- Termux instalado

---

## ❓ FAQ

**P: Por que preciso do Termux?**  
R: O Android não permite executar Node.js nativamente. O Termux fornece um ambiente Linux completo onde Node.js funciona perfeitamente.

**P: Posso usar sem Termux?**  
R: Sim! Você pode editar código e visualizar arquivos. Mas build e execução requerem Node.js.

**P: Os projetos são salvos onde?**  
R: Em `/storage/emulated/0/Android/data/com.reactide.app/files/projects/`

**P: Funciona offline?**  
R: Sim, após instalar dependências do projeto.

**P: Suporta outros frameworks?**  
R: Atualmente só React, mas pode ser adaptado para Vue, Angular, etc.

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja [LICENSE](LICENSE) para mais detalhes.

---

## 🙏 Agradecimentos

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [React](https://react.dev/)
- [Node.js Mobile](https://nodejs-mobile.github.io/)
- [Termux](https://termux.dev/)

---

## 📞 Contato

**GitHub Issues:** [Abrir issue](../../issues)  
**Discussões:** [Discussions](../../discussions)

---

<div align="center">

**Feito com ❤️ para desenvolvedores mobile**

⭐ Deixe uma estrela se este projeto foi útil!

</div>
