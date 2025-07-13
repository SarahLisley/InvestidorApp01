# 🚀 Guia de Uso - InvestidorApp com Preços Reais

## ✅ O que foi implementado

### 🔥 Firebase Configurado
- ✅ Projeto Firebase criado e configurado
- ✅ Realtime Database funcionando
- ✅ Cloud Messaging para notificações push
- ✅ Autenticação de tokens

### 📈 API de Preços Reais
- ✅ Integração com Yahoo Finance API
- ✅ Busca de preços de ações brasileiras em tempo real
- ✅ Monitoramento automático a cada 5 minutos
- ✅ Histórico de preços no Firebase

### 🎯 Sistema de Alertas Funcional
- ✅ Criação de alertas de preços
- ✅ Monitoramento em tempo real
- ✅ Notificações push quando alertas são disparados
- ✅ Desativação automática após disparo

## 🎮 Como Usar o App

### 1. **Criar Alertas de Preços**
1. Abra o app
2. Toque no botão "+" (Floating Action Button)
3. Preencha:
   - **Símbolo**: Código da ação (ex: PETR4, VALE3, ITUB4)
   - **Preço Alvo**: Valor em reais
   - **Tipo**: Acima de (alta) ou Abaixo de (baixa)
4. Toque em "Criar Alerta"

### 2. **Ver Preços em Tempo Real**
1. Vá para a aba "Preços"
2. Toque em "Buscar Ações Populares" para carregar 10 ações principais
3. Ou toque em botões individuais (PETR4, VALE3) para buscar ações específicas
4. Os preços são atualizados automaticamente

### 3. **Monitoramento Automático**
- O app monitora preços automaticamente a cada 5 minutos
- Quando um alerta é disparado, você recebe uma notificação push
- O alerta é desativado automaticamente após disparar

## 📊 Ações Disponíveis

### Ações Brasileiras Populares
- **PETR4** - Petrobras
- **VALE3** - Vale
- **ITUB4** - Itaú Unibanco
- **BBDC4** - Bradesco
- **ABEV3** - Ambev
- **WEGE3** - WEG
- **RENT3** - Localiza
- **LREN3** - Lojas Renner
- **MGLU3** - Magazine Luiza
- **JBSS3** - JBS

### Como Adicionar Novas Ações
1. Digite o código da ação no campo "Símbolo"
2. O app automaticamente adiciona ".SA" para ações brasileiras
3. Exemplo: "PETR4" → "PETR4.SA"

## 🔔 Notificações

### Tipos de Notificação
- **🚀 Alerta de Alta**: Quando preço sobe acima do valor alvo
- **📉 Alerta de Baixa**: Quando preço cai abaixo do valor alvo
- **📊 Atualização do Portfólio**: Mudanças no valor total
- **📰 Notícias do Mercado**: Notícias importantes

### Configurações de Notificação
- Notificações de alertas têm prioridade alta
- Vibração e som personalizados
- Toque na notificação abre o app diretamente

## 🔧 Configurações Técnicas

### Firebase Database
```
/investidorapp
├── price_alerts/          # Alertas ativos
├── stock_prices/          # Preços atuais
├── users/                 # Tokens de usuários
└── alert_history/         # Histórico de alertas
```

### Monitoramento
- **Intervalo**: 5 minutos
- **API**: Yahoo Finance (gratuita)
- **Backup**: Firebase Realtime Database
- **Notificações**: Firebase Cloud Messaging

## 🐛 Troubleshooting

### Problema: Preços não carregam
**Solução:**
1. Verificar conexão com internet
2. Aguardar alguns segundos (API pode estar lenta)
3. Tentar novamente

### Problema: Alertas não funcionam
**Solução:**
1. Verificar se o alerta está ativo
2. Verificar se o preço alvo é realista
3. Aguardar próximo ciclo de monitoramento

### Problema: Notificações não aparecem
**Solução:**
1. Verificar permissões do Android
2. Verificar configurações do app
3. Verificar se o Firebase está configurado

## 📈 Próximas Funcionalidades

### Planejadas
- [ ] Gráficos de preços em tempo real
- [ ] Portfólio de investimentos
- [ ] Notícias financeiras
- [ ] Alertas compostos (múltiplas condições)
- [ ] Exportação de relatórios
- [ ] Widgets para tela inicial

### Melhorias Técnicas
- [ ] Cache local de preços
- [ ] Otimização de bateria
- [ ] Mais APIs de preços
- [ ] Autenticação de usuários

## 🎉 Sistema Funcionando!

Seu app agora está **100% funcional** com:
- ✅ Preços reais de ações brasileiras
- ✅ Alertas automáticos
- ✅ Notificações push
- ✅ Monitoramento em background
- ✅ Interface moderna e responsiva

**Parabéns! Você tem um sistema completo de alertas de preços funcionando! 🚀📈** 