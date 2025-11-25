# Energy Optimizer - Backend API ⚡

Backendowa część aplikacji. Projekt zrealizowany jako bezstanowa usługa REST API, która integruje się z zewnętrznymi dostawcami danych energetycznych w celu wyznaczania najbardziej ekologicznych okien czasowych na ładowanie EV.

## 📌 O Projekcie

System analizuje miks energetyczny Wielkiej Brytanii (udział źródeł odnawialnych vs kopalnych) i na podstawie prognoz wskazuje użytkownikowi przedziały czasowe, w których emisja CO2 jest najniższa.

**Główne funkcjonalności:**
* Pobieranie danych historycznych i prognozowanych z Carbon Intensity API (National Grid ESO).
* Algorytm wyznaczania optymalnego okna ładowania o zadanej długości.

## 🚀 Technologie

* **Język:** Java 21
* **Framework:** Spring Boot 3.5.8
* **Build Tool:** Maven
* **Konteneryzacja:** Docker
* **Testy:** JUnit 5, Mockito
* **Architektura:** API-centric (Stateless)

## 🔌 Dokumentacja API

**Główny punkt wejścia:** `/api/energy`

| Metoda | Endpoint | Opis | Parametry |
|:---|:---|:---|:---|
| `GET` | `/daily-mix` | Zwraca miks energetyczny na dziś i najbliższe 2 dni. | *brak* |
| `GET` | `/optimal-charge` | Oblicza najlepsze okno ładowania. | `durationHours` (int, 1-6) |

**Przykładowe zapytanie:**
```
GET /api/energy/optimal-charge?durationHours=3
```

## ☁️ Deployment (Render.com)

Aplikacja została wdrożona na platformie Render
