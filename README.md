# VozNote

Aplicativo Android para gravação de notas por voz com transcrição em tempo real.

Registro de atividades (log de progresso por etapas):

- Etapa 1: Scaffold do projeto criado (Gradle, Manifest, Activity e layouts iniciais).

Arquivos criados/atualizados nesta etapa:

- `settings.gradle` - nome do projeto e inclusão do módulo `:app`.
- `build.gradle` - configuração raiz do Gradle com plugins Android e Kotlin.
- `app/build.gradle` - configuração do módulo Android, dependências essenciais (Kotlin, Coroutines, Lifecycle, Room, Timber, Material).
- `app/src/main/AndroidManifest.xml` - permissões `RECORD_AUDIO`, `FOREGROUND_SERVICE` e registro de `BroadcastReceiver` para alarmes.
- `app/src/main/java/com/bsbchurch/voznote/VozNoteApp.kt` - `Application` para inicializar o Timber.
- `app/src/main/java/com/bsbchurch/voznote/GravacaoActivity.kt` - activity inicial (Tela de Gravação) com comentários PT-BR.
- `app/src/main/java/com/bsbchurch/voznote/NotasActivity.kt` - activity para lista de notas (esqueleto).
- `app/src/main/res/layout/activity_gravacao.xml` - layout da tela de gravação com transcrição e FABs.
- `app/src/main/res/layout/activity_notas.xml` - layout da tela de notas com RecyclerView.
- `app/src/main/res/layout/item_nota.xml` - layout do item de nota.
- `app/src/main/res/values/strings.xml` - strings em PT-BR.
- `app/src/main/res/values/colors.xml` - cores básicas.
- `app/src/main/res/values/themes.xml` - tema simples de Material.

Próximo passo: implementar gravação de áudio, permissões e transcrição em tempo real (Etapa 2).

Instruções rápidas:

1. Abra o projeto no Android Studio (minSdk 24).
2. Sincronize o Gradle.
3. Rode em um dispositivo ou emulador com microfone.

CI e GitHub Actions:

- Adicionado workflow `.github/workflows/ci.yml` que lista a árvore do projeto e tenta rodar `./gradlew assembleDebug` se o Gradle Wrapper existir.

Arquivos para abrir no Android Studio:

- Abra a pasta raiz do repositório no Android Studio. Se o `gradlew` não existir, gere o Gradle Wrapper via Android Studio.

Comentários e nomes estão em PT-BR para facilitar entendimento por leigos.