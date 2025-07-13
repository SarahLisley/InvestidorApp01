# 📱 MobileMessagin - App de Alertas de Preços de Ações

Um aplicativo Android desenvolvido em Kotlin para monitorar preços de ações brasileiras e enviar notificações quando os preços atingem valores específicos.

## 🚀 Funcionalidades

### 📊 Monitoramento de Preços
- **Preços em Tempo Real**: Acompanhe preços de ações populares da B3
- **Cache Inteligente**: Cache de 30 segundos para otimizar requisições à API
- **Atualização Manual**: Botão para atualizar preços quando necessário

### 🔔 Sistema de Alertas
- **Alertas Personalizados**: Configure alertas para preços acima ou abaixo de valores específicos
- **Notificações Push**: Receba notificações quando os preços atingem os valores configurados
- **Edição e Exclusão**: Gerencie seus alertas facilmente

### 🧪 Modo Teste (Test01)
- **Criação Rápida**: Crie alertas com preços adaptados automaticamente
- **Simulação de Preços**: Teste alertas com variações de +2% ou -2%
- **Feedback Visual**: Confirmação via Snackbar para todas as operações

### 🔧 Recursos Técnicos
- **Firebase Integration**: Armazenamento em tempo real dos alertas e preços
- **Yahoo Finance API**: Dados reais de preços de ações
- **Background Monitoring**: Monitoramento contínuo em segundo plano
- **Material Design 3**: Interface moderna e intuitiva

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **Jetpack Compose**: UI declarativa moderna
- **Firebase**: Backend e notificações
- **Yahoo Finance API**: Dados de preços
- **Coroutines**: Programação assíncrona
- **StateFlow**: Gerenciamento de estado reativo
- **Material Design 3**: Design system

## 📋 Pré-requisitos

- Android Studio Arctic Fox ou superior
- Android SDK 24+
- Kotlin 1.8+
- Conta Firebase (para backend)

## 🔧 Instalação

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/MobileMessagin.git
cd MobileMessagin
```

### 2. Configure o Firebase
1. Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
2. Adicione um app Android com o package `com.example.investidorapp`
3. Baixe o arquivo `google-services.json` e coloque em `app/`
4. Configure as regras do Realtime Database (veja `firebase_rules.json`)

### 3. Configure as APIs
- **Yahoo Finance**: Não requer chave de API (público)
- **Firebase**: Configurado via `google-services.json`

### 4. Build e Execute
```bash
./gradlew assembleDebug
```

## 📱 Como Usar

### Criando Alertas
1. Toque no botão **+** (FAB)
2. Digite o símbolo da ação (ex: PETR4, VALE3)
3. Defina o preço alvo
4. Escolha o tipo: "Acima de" ou "Abaixo de"
5. Confirme

### Modo Teste (Test01)
1. Use o botão **Test01** no diálogo de criação
2. O app criará automaticamente um alerta com preço adaptado
3. Use os botões **+2%** ou **-2%** na aba de preços para simular variações
4. O alerta será disparado instantaneamente

### Gerenciando Alertas
- **Editar**: Toque no ícone de editar no card do alerta
- **Excluir**: Toque no ícone de lixeira
- **Recarregar**: Use o botão "Recarregar Alertas"

## 🏗️ Estrutura do Projeto

```
app/src/main/java/com/example/investidorapp/
├── MainActivity.kt                 # Activity principal
├── model/
│   ├── PriceAlert.kt              # Modelo de alerta
│   └── StockPrice.kt              # Modelo de preço
├── service/
│   ├── FirebaseDatabaseService.kt # Serviço do Firebase
│   ├── StockPriceApiService.kt    # API de preços
│   ├── PriceAlertService.kt       # Lógica de alertas
│   ├── NotificationService.kt     # Notificações
│   └── AutoMonitoringService.kt   # Monitoramento automático
├── ui/
│   ├── screens/
│   │   └── HomeScreen.kt          # Tela principal
│   ├── components/
│   │   └── AddAlertDialog.kt      # Diálogo de criação
│   └── theme/                     # Temas e cores
└── viewmodel/
    └── PriceAlertViewModel.kt     # ViewModel principal
```

## 🔐 Configuração de Segurança

### Firebase Rules
```json
{
  "rules": {
    "price_alerts": {
      ".indexOn": "active",
      "$userId": {
        ".read": "$userId == auth.uid",
        ".write": "$userId == auth.uid"
      }
    },
    "stock_prices": {
      ".read": true,
      ".write": true
    }
  }
}
```

## 🧪 Testes

### Test01 - Teste Rápido
1. Crie um alerta usando o modo Test01
2. Vá para a aba de preços
3. Use os botões de simulação (+2% / -2%)
4. Verifique se a notificação é recebida

### Funcionalidades para Testar
- ✅ Criação de alertas
- ✅ Edição de alertas
- ✅ Exclusão de alertas
- ✅ Busca de preços
- ✅ Notificações
- ✅ Cache de preços
- ✅ Monitoramento em background

## 📝 Logs e Debug

O app inclui logs detalhados para debug:
- `PriceAlertViewModel`: Operações de alertas
- `StockPriceApiService`: Requisições à API
- `PriceAlertService`: Monitoramento de alertas
- `NotificationService`: Notificações

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Autor

**Seu Nome**
- GitHub: [@seu-usuario](https://github.com/seu-usuario)

## 🙏 Agradecimentos

- Yahoo Finance pela API de preços
- Firebase pelo backend
- Jetpack Compose pela UI moderna
- Comunidade Android/Kotlin

---

**⚠️ Disclaimer**: Este app é para fins educacionais e de teste. Não use para investimentos reais sem consultar um profissional financeiro. 