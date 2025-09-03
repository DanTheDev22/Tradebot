# TradeBot - Financial Assistant 🤖📊

**TradeBot - Financial Assistant** is a Telegram bot designed to provide **real-time financial data** and **personalized tools** for traders and investors.  

### 🔧 Key Capabilities:

- 📈 **Real-Time Market Data:** Get up-to-date prices for cryptocurrencies, stocks, and forex pairs.
- 🔔 **Price Alerts:** Set custom alerts for crypto prices and get notified instantly when the target is reached.
- 🔍 **Smart Search:** Find cryptocurrencies and stocks using symbol or name.
- 💱 **Currency Exchange Rates:** Retrieve real-time exchange rates between physical and digital currencies.
- 🏛️ **Market Status:** Check whether the US stock market is open or closed.
- 📂 **Supported Currency Lists:** Downloadable lists for all supported physical and digital currencies.

> TradeBot is built with a **modular** and **scalable** structure, making it easy to extend functionality and support additional markets or features.

---

## 📽️ Demo

Check out the demo of TradeBot in action:  
👉 **[Demo Link – COMING SOON]**  
<!-- Replace '#' with the actual link when available -->

---

## 📖 General Information

TradeBot is a **Java-based Telegram bot** developed to simplify financial monitoring and decision-making. It serves as a **personal assistant** for anyone needing quick, real-time financial data, including:

- 📊 Crypto, stock, and forex prices
- 🚨 Alerts for price thresholds
- 🔄 Exchange rates (digital and physical)
- 🧠 Smart symbol search

> 🧭 Purpose: To help users make better financial decisions by providing instant data access through a Telegram bot — no extra apps or platforms needed.

---

## 💻 Technologies Used

- ☕ **Java**
- 🌱 **Spring Boot**
- 🐳 **Docker**
- 📡 **Telegram Bot API**

---

## 🌟 Features Overview

- ✅ **Market Status** – Get US market open/close status
- ✅ **Cryptocurrency Info** – Full and simple price/market data
- ✅ **Stock Info** – Retrieve current data using symbols or names
- ✅ **Exchange Rates** – Get live forex + crypto conversion rates
- ✅ **Search Tools** – Find symbols easily by name or ticker
- ✅ **Price Alerts** – Create, show, and delete alerts
- ✅ **Currency Lists** – Download supported currency files

---

## 💬 Usage (Bot Commands)

To interact with TradeBot, simply start a chat with the bot in Telegram and use the following commands:

### 🟢 General Commands
- `/start` – Displays the welcome message and full list of available commands.
- `/status` – Shows the current status of the US stock market (open/closed).

### 💰 Cryptocurrency
- `/getsimplecrypto` – Get the latest price for a cryptocurrency.  
  → _You’ll be prompted to enter a symbol, e.g.,_ `BTC-USDT`.

- `/getfullcrypto` – Get full details for a cryptocurrency including volume, change %, etc.  
  → _Example:_ `TON-USDT`

- `/findcrypto` – Search for a crypto by symbol or name (e.g., `Bitcoin`, `DOGE`).

- `/suppcrypto` – Get a downloadable list of supported digital currencies.

### 📈 Stocks
- `/getstock` – Get detailed information about a stock using its ticker symbol.  
  → _Example:_ `AAPL` or `TSLA`

- `/findstock` – Search for a stock by symbol or name (e.g., `Tesla`, `GOOG`).

### 💱 Exchange Rates
- `/exchange` – Convert between physical or digital currencies.  
  → _You’ll be guided to enter the source (e.g., `USD`) and target (e.g., `BTC`) currencies._

- `/suppcurrency` – Download a list of supported physical currencies (e.g., `USD`, `EUR`).

### 🚨 Alerts
- `/createalert` – Set a custom price alert for a cryptocurrency.  
  → _You’ll be asked for the symbol and target price._

- `/showalerts` – Display all your active price alerts.

- `/deletealert` – Remove a specific alert by providing the crypto symbol (e.g., `BTC-USDT`).

> 🔔 After typing a command, follow the prompts sent by the bot. Most commands are interactive and guide you step-by-step.

---

## 📈 Project Status

🟢 **Active and in development**

---

## 📸 Screenshots / GIFs
---
Below are some screenshots and a short demo GIF of TradeBot in action.

![tradebot](https://github.com/user-attachments/assets/314d5a4e-f01b-4cd4-9ad0-017af2fd5f60)


### 🧵 Command Flow Examples
- `/start` command to display available commands.
- Using `/getfullcrypto` with `TON-USDT` to get crypto details.
- Setting a price alert with `/createalert`.

### 📷 Screenshots
> _You can add screenshots of the bot running in Telegram UI here._

```md
![Start Command](assets/start-command.png)
![Crypto Full Info](assets/crypto-full-info.png)
![Price Alert Example](assets/price-alert.png)
