# 🍀 LuckyBlock – Paper 26.2 / 26.3

Lucky Blocki w **3 rodzajach** i **100 efektów**, do tego animacja otwierania, kolekcja efektów, receptury i event „deszcz lucky blocków”. Plugin nie ma zależności.

| Rodzaj | Blok | Szczęście / niespodzianka / pech | Siła nagród |
|---|---|---|---|
| 🟨 **Rzadki** | gąbka | 55% / 20% / 25% | ★ |
| 🟪 **Epicki** | blok ametystu | 60% / 15% / 25% | ★★ |
| 🟧 **Legendarny** | blok złota | 70% / 10% / 20% | ★★★ |

Zwykłe bloki gąbki, ametystu czy złota nie są lucky. Lucky blockiem jest tylko blok postawiony ze specjalnego przedmiotu. Pozycje lucky blocków są zapisywane w danych chunka, więc przetrwają restart serwera.

## Jak zdobyć

- **Crafting**:
  - Rzadki: 8× sztabka złota + dozownik.
  - Epicki: 4× rzadki + 4× diament + blok ametystu.
  - Legendarny: 4× epicki + 4× blok diamentów + gwiazda Netheru.
- **Z rud**: 1% szans przy wykopaniu każdej rudy. Z tego 25% to epicki, a 5% legendarny.
- **Deszcz lucky blocków**: `/lucky deszcz` albo automatycznie co X minut. Bloki spadają z nieba obok graczy.
- **Admin**: `/lucky daj <gracz> <rodzaj> [ilość]`.

## Funkcje

- Animacja otwierania: blok unosi się, kręci i świeci w kolorze rodzaju, a na końcu wybucha cząsteczkami.
- Mikstura Szczęścia (Luck) zwiększa szansę na dobry efekt.
- **Kolekcja**: `/lucky kolekcja` pokazuje 100 efektów, a nieodkryte widać jako „???”.
- Ranking graczy z największą liczbą otwartych bloków (`/lucky top`).
- Ogłoszenia na czacie przy legendarnych blokach.
- Poświata wokół postawionych lucky blocków.
- Ochrona: lucky blocków nie da się zniszczyć wybuchem ani przesunąć tłokiem.
- TNT z lucky blocków domyślnie nie niszczy terenu, a fajerwerki nie ranią graczy.
- Tymczasowe pułapki (klatki, pajęczyny) same znikają. Przy wyłączeniu serwera wszystko jest przywracane.
- Król Zombie to boss, z którego po pokonaniu wypadają diamenty i lucky block.
- **Admin**: `/lucky test <efekt>` uruchamia wybrany efekt, `/lucky efekty` pokazuje klikalną listę. W kolekcji admin testuje efekt przez Shift+klik.
- Niebezpieczne efekty (Wither, Warden) można wyłączyć w configu, podobnie jak dowolny inny efekt (`wylaczone-efekty`).

## 100 efektów

### ✅ Dobre (60)

| Efekt | ID | Rodzaje |
|---|---|---|
| Garść diamentów | `diamenty` | 🟨 🟪 🟧 |
| Szmaragdowy skarb | `szmaragdy` | 🟨 🟪 🟧 |
| Złote sztabki | `zloto` | 🟨 🟪 |
| Stack żelaza | `zelazo` | 🟨 🟪 |
| Sztabka netherytu | `netherite` | 🟪 🟧 |
| Deszcz diamentów! | `deszcz_diamentow` | 🟪 🟧 |
| Złoty deszcz | `deszcz_zlota` | 🟨 🟪 |
| Bloki minerałów | `bloki_mineralow` | 🟪 🟧 |
| Złote jabłka | `zlote_jablka` | 🟨 🟪 🟧 |
| Zaklęte złote jabłko | `zaklete_jablko` | 🟧 |
| Uczta | `jedzenie` | 🟨 |
| Tort urodzinowy | `tort` | 🟨 |
| Paczka budowlańca | `bloki_budowlane` | 🟨 |
| Perły Endu | `perly` | 🟨 🟪 |
| Butelki doświadczenia | `butelki_xp` | 🟨 🟪 |
| Nagły przypływ wiedzy | `poziomy_xp` | 🟨 🟪 🟧 |
| Diamentowa zbroja | `zbroja_diament` | 🟪 🟧 |
| Zbroja Władcy | `zbroja_netherite` | 🟧 |
| Miecz Szczęściarza | `miecz` | 🟪 🟧 |
| Kilof Fortuny | `kilof` | 🟪 🟧 |
| Jedwabny kilof | `kilof_jedwab` | 🟨 🟪 |
| Łuk Burzy | `luk` | 🟪 |
| Trójząb Posejdona | `trojzab` | 🟪 🟧 |
| Skrzydła! | `elytra` | 🟧 |
| Totem nieśmiertelności | `totem` | 🟪 🟧 |
| Netherytowy komplet | `narzedzia_netherite` | 🟧 |
| Tarcza obrońcy | `tarcza` | 🟨 |
| Rakietowe buty | `rakietowe_buty` | 🟪 |
| Zestaw rybaka | `zestaw_rybaka` | 🟨 |
| Zestaw farmera | `zestaw_farmera` | 🟨 |
| Magiczna księga | `ksiega` | 🟨 🟪 |
| Tajna biblioteka | `biblioteka` | 🟧 |
| Księga Naprawy | `mending` | 🟪 🟧 |
| Latarnia | `beacon` | 🟧 |
| Gwiazda Netheru | `gwiazda` | 🟧 |
| SMOCZE JAJO | `smocze_jajo` | 🟧 |
| Serce morza | `serce_morza` | 🟪 🟧 |
| Shulker pełen skarbów | `shulker` | 🟪 🟧 |
| Skrzynia skarbów | `skrzynia` | 🟨 🟪 🟧 |
| Więcej szczęścia! | `wiecej_lucky` | 🟨 🟪 🟧 |
| Awans bloku | `ulepszenie` | 🟨 🟪 |
| Supermoc | `supermoc` | 🟨 🟪 🟧 |
| Dar latania | `latanie` | 🟧 |
| Chwilowa nieśmiertelność | `niesmiertelnosc` | 🟪 🟧 |
| Złote serca | `zlote_serca` | 🟨 🟪 |
| Oczy sowy i skrzela | `widzenie` | 🟨 |
| Pełny brzuszek | `najedzenie` | 🟨 |
| Czterolistna koniczyna | `szczescie` | 🟨 🟪 |
| Fontanna doświadczenia | `fontanna_xp` | 🟪 🟧 |
| Wierny piesek | `pies` | 🟨 🟪 |
| Wilcza wataha | `wataha` | 🟪 🟧 |
| Kotek na szczęście | `kot` | 🟨 |
| Gadająca papuga | `papuga` | 🟨 |
| Pomocny duszek | `allay` | 🟪 |
| Rumak z siodłem | `kon` | 🟨 🟪 |
| Żelazny obrońca | `golem` | 🟪 🟧 |
| Handlarz szczęścia | `handlarz` | 🟪 🟧 |
| Ukryta kopalnia | `kopalnia` | 🟪 🟧 |
| Drzewo pieniędzy | `drzewo_pieniedzy` | 🟧 |
| Twój pomnik | `pomnik` | 🟪 |

### 🎲 Neutralne / zabawne (16)

| Efekt | ID | Rodzaje |
|---|---|---|
| Pokaz fajerwerków | `fajerwerki` | 🟨 🟪 🟧 |
| Tęczowe owieczki | `teczowe_owce` | 🟨 🟪 |
| Armia kurczaków | `kurczaki` | 🟨 |
| Szafa grająca | `muzyka` | 🟨 🟪 |
| Twoja głowa! | `glowa` | 🟨 |
| Konfetti | `konfetti` | 🟨 |
| Wystrzał w niebo | `wystrzal` | 🟨 🟪 |
| Deszcz ryb | `deszcz_ryb` | 🟨 |
| Pogodynka | `pogoda` | 🟨 |
| Pan czasu | `czas` | 🟨 |
| Diament? | `falszywy_diament` | 🟨 |
| Nic | `nic` | 🟨 |
| Losowy teleport | `teleport` | 🟨 🟪 |
| Dyskoteka | `dyskoteka` | 🟨 🟪 |
| Tęczowy deszcz | `tecza` | 🟨 |
| Król Zombie | `krol_zombie` | 🟪 🟧 |

### 💀 Złe (24)

| Efekt | ID | Rodzaje |
|---|---|---|
| TNT! | `tnt` | 🟨 🟪 |
| Deszcz TNT | `deszcz_tnt` | 🟪 🟧 |
| Gniew bogów | `piorun` | 🟨 🟪 |
| Kowadła z nieba | `kowadla` | 🟨 🟪 |
| Syczące towarzystwo | `creepery` | 🟨 🟪 |
| Naładowany creeper | `naladowany` | 🟪 🟧 |
| Horda zombie | `zombie` | 🟨 🟪 🟧 |
| Snajperzy | `szkielety` | 🟨 🟪 |
| Pajęcze gniazdo | `pajaki` | 🟨 |
| Plaga rybików | `rybiki` | 🟨 |
| Sabat czarownic | `czarownice` | 🟪 |
| Niszczyciel | `ravager` | 🟪 🟧 |
| Strażnik głębin | `warden` | 🟧 |
| Wither! | `wither` | 🟧 |
| Duchy | `vexy` | 🟪 🟧 |
| Gigantyczny szlam | `slime` | 🟪 |
| Piekielny ogień | `blazy` | 🟪 🟧 |
| Rozzłoszczone endermany | `endermany` | 🟪 |
| Szklana klatka | `klatka` | 🟨 🟪 |
| Pajęcza sieć | `pajeczyny` | 🟨 |
| Klątwa | `debuffy` | 🟨 🟪 |
| Wielki głód | `glod` | 🟨 |
| Lodowy dotyk | `zamrozenie` | 🟨 🟪 |
| Lewitacja | `lewitacja` | 🟨 🟪 |

## Uprawnienia

- `lucky.admin`: dawanie, testowanie, deszcz, reload (OP)
