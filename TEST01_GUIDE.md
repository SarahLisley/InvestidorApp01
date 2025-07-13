# 🧪 Guia Test01 - Sistema de Testes de Alertas

## O que é o Test01?

O **Test01** é uma funcionalidade especial que adapta automaticamente o preço alvo dos alertas para facilitar os testes. Em vez de você definir um preço alvo manualmente, o sistema busca o preço atual da ação e define um preço alvo próximo (±1%) para que o alerta seja disparado rapidamente.

## 🚀 Como Usar o Test01

### 1. Criando um Alerta Test01

1. **Abra o app** e toque no botão **+** para criar um novo alerta
2. **Digite o símbolo** da ação (ex: PETR4, VALE3)
3. **Digite qualquer preço** no campo "Preço Alvo" (será adaptado automaticamente)
4. **Selecione o tipo** de alerta (Acima de / Abaixo de)
5. **Marque a caixa** "🧪 Modo Teste (Test01)"
6. **Toque em "Criar Alerta Test01"**

### 2. Como Funciona a Adaptação

- **Para alertas "Acima de"**: O sistema define o preço alvo como **preço atual + 1%**
- **Para alertas "Abaixo de"**: O sistema define o preço alvo como **preço atual - 1%**

### 3. Testando os Alertas

Na aba **"Preços"**, você encontrará uma seção **"🧪 Testes Test01"** com botões para simular mudanças de preço:

- **Botão +2%**: Simula um aumento de 2% no preço
- **Botão -2%**: Simula uma queda de 2% no preço

## 📊 Exemplo Prático

### Cenário 1: Alerta "Acima de"
1. PETR4 está cotado a R$ 32,63
2. Você cria um alerta Test01 "Acima de" R$ 50,00
3. O sistema adapta para R$ 32,95 (32,63 + 1%)
4. Você clica em "+2%" para simular
5. O preço vai para R$ 33,28
6. **Alerta disparado!** ✅

### Cenário 2: Alerta "Abaixo de"
1. VALE3 está cotado a R$ 56,00
2. Você cria um alerta Test01 "Abaixo de" R$ 40,00
3. O sistema adapta para R$ 55,44 (56,00 - 1%)
4. Você clica em "-2%" para simular
5. O preço vai para R$ 54,88
6. **Alerta disparado!** ✅

## 🔧 Funcionalidades Técnicas

### Funções Implementadas

1. **`createTest01PriceAlert()`**: Cria alertas com preços adaptados
2. **`simulatePriceChangeForTest01()`**: Simula mudanças de preço
3. **Interface adaptada**: Checkbox para ativar o modo teste
4. **Botões de simulação**: +2% e -2% para testes rápidos

### Vantagens do Test01

- ✅ **Testes rápidos**: Não precisa esperar mudanças reais de preço
- ✅ **Preços realistas**: Baseado no preço atual real da ação
- ✅ **Feedback imediato**: Mensagens informativas sobre adaptações
- ✅ **Simulação controlada**: Você controla as mudanças de preço
- ✅ **Seguro**: Não afeta dados reais, apenas para testes

## 🎯 Casos de Uso

### Para Desenvolvedores
- Testar a lógica de disparo de alertas
- Verificar notificações push
- Validar cálculos de preços
- Debug de funcionalidades

### Para Usuários
- Entender como funcionam os alertas
- Testar diferentes cenários
- Familiarizar-se com a interface
- Verificar se as notificações chegam

## 📱 Interface do Usuário

### Dialog de Criação
- Checkbox "🧪 Modo Teste (Test01)"
- Descrição explicativa
- Botão adaptado "Criar Alerta Test01"
- Feedback visual diferenciado

### Aba de Preços
- Seção destacada "🧪 Testes Test01"
- Botões +2% e -2%
- Mensagens informativas
- Indicadores visuais

## 🔍 Monitoramento

### Logs de Teste
- Preço atual vs preço adaptado
- Mudanças simuladas
- Status dos alertas
- Erros e sucessos

### Feedback ao Usuário
- Mensagens claras sobre adaptações
- Confirmação de criação
- Status de simulações
- Alertas de erro

## ⚠️ Importante

- O Test01 é **apenas para testes**
- Não use para alertas reais de investimento
- Os preços simulados não refletem o mercado real
- Use apenas para validar funcionalidades

## 🎉 Resultado Esperado

Com o Test01, você pode:
1. **Criar alertas** com preços adaptados automaticamente
2. **Simular cenários** de alta e baixa
3. **Testar notificações** rapidamente
4. **Validar funcionalidades** sem esperar mudanças reais
5. **Entender o sistema** de forma prática

---

**Test01: Tornando os testes de alertas mais rápidos e eficientes! 🚀** 