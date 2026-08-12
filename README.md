# Calendário Mágico 🗓️✨

Mini calendário partilhado para família/amigos — eventos, tarefas e lembretes
editáveis, sincronizados entre vários telemóveis Android via Internet.
Interface em tons pastel, pensada para ser simples de usar por toda a família.

## Como funciona

- Cada pessoa instala a app e entra com o **nome** dela.
- Quem cria um calendário recebe um **código de convite** de 6 caracteres
  (ex. `G7K2QM`) para partilhar com o resto da família.
- Quem recebe o código escolhe "Já tenho código" e entra no mesmo calendário —
  sem necessidade de conta, password ou email.
- Eventos, tarefas (com caixa de marcar) e lembretes ficam sincronizados em
  tempo real entre todos os dispositivos, através do Firebase (Firestore).
- Os lembretes disparam como notificações locais em cada telemóvel, mesmo
  sem internet no momento exato (assim que os dados chegaram uma vez ao
  dispositivo, o alarme fica agendado localmente).

## ⚠️ Antes de instalar: configurar o Firebase (obrigatório para a sincronização)

Este repositório inclui um `app/google-services.json` **de exemplo** (falso),
só para o projeto compilar. Para a sincronização entre dispositivos
funcionar de verdade, precisas de um projeto Firebase teu (gratuito):

1. Vai a [console.firebase.google.com](https://console.firebase.google.com) e
   cria um projeto novo (~2 minutos, só precisa de conta Google).
2. Dentro do projeto, adiciona uma app Android com o package name
   `com.calendariomagico.app`.
3. Descarrega o ficheiro `google-services.json` que a consola gera e substitui
   o ficheiro `app/google-services.json` deste repositório por ele.
4. No painel do projeto Firebase, ativa:
   - **Authentication** → método "Anónimo" (Anonymous).
   - **Firestore Database** → cria a base de dados (modo produção).
5. Nas regras do Firestore, usa algo como:

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /calendars/{calendarId} {
         allow read, create: if request.auth != null;
         allow update, delete: if request.auth != null;
         match /items/{itemId} {
           allow read, write: if request.auth != null;
         }
         match /members/{memberId} {
           allow read, write: if request.auth != null;
         }
       }
     }
   }
   ```

6. Faz commit do novo `google-services.json` e volta a compilar (ou faz
   `./gradlew assembleDebug` localmente no Android Studio).

Sem este passo, a app abre e a interface funciona, mas criar/entrar num
calendário vai falhar (sem ligação a um projeto Firebase real).

## Compilar

O projeto é um projeto Android normal (Gradle + Kotlin + Jetpack Compose).
Podes abri-lo diretamente no **Android Studio** (Hedgehog ou mais recente) e
carregar em "Run", ou por linha de comandos:

```
./gradlew assembleDebug
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

Este ambiente de desenvolvimento (sandbox usado pelo Claude) não tem acesso
ao `dl.google.com`, pelo que a compilação real acontece através do GitHub
Actions (`.github/workflows/android-build.yml`), que corre em cada push e
publica o APK de debug como artefacto e como asset de uma release fixa
(`latest-debug-apk`).

## Testes

```
./gradlew testDebugUnitTest   # testes unitários (lógica, repositório, ViewModels)
./gradlew lintDebug           # análise estática
```

## Estrutura

- `data/model` — modelos de domínio (`CalendarItem`, `CalendarGroup`, `Member`).
- `data/remote` — acesso ao Firebase (Auth anónima + Firestore em tempo real).
- `data/local` — estado local do dispositivo (calendário ativo, nome).
- `data/repository` — `CalendarRepository`, fonte única de verdade para a UI.
- `notifications` — agendamento de lembretes locais (AlarmManager).
- `ui` — ecrãs em Jetpack Compose (onboarding, calendário mensal, editor de
  item, definições/membros) com tema pastel em `ui/theme`.
