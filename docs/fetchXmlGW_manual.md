## Instrukcja konfiguracji fetchXmlGW.exe
### plik cfg
Plik _app.cfg_ zawiera infromacje potrzebne do połączenia z kontem _emcs@carrefour.com_. Uwaga aplikacja fetchXmlGW domyślnie poszukuje pliku w katalogu,
z którego została uruchomiona, a nie w katalogu w którym została zainstalowana.
* _oauth.refresh_token_ zawiera token o długiej ważności, który może wymienić na access token umożliwiający wykonanie połączenia do usługi IMAP/SMTP w chmurze GCP.
* _oauth.client_id_ zawiera identyfikator klienta, który będzie użyty do wymiany refresh token na access token
* _oauth.client_secret_ pozwala przekazać informacje uwierzytelniającą dla klienta wskazanego w _oauth.client_id_
* _email_address_ - address email skrzynki użytej do wysłania/odebrania wiadomości przez _fetchXmlGW.exe_

### wiersz poleceń
Aplikacja fetchXMLGW ma tylko jeden paramter obowiązkowy. Najprostsze wywołanie czyli

_fetchXMLGW temp_

spowoduje zapisanie wszystkich załączników do wiadomości ze skrzynki wskazanej w pliku _app.cfg_ do katalogu _temp_.
Domyślnie aplikacja nie usunie żadnych danych z serwera i przetworzy wszystkie wiadomości na serwerze.
Tym zachowaniem można sterować przy użyciu przełączników:

__-d__     kasuje mail zawierający plik w załączniku po pobraniu go z serwera

__-o__     nadpisuje plik na dysku, np jeśli w mailu jest załącznik o nazwie dane.xml
i w katalogu gdzie zapisywane są te pliki również znajduje się taki plik to
zawartość pliku na dysku zostanie zastąpiona treścią pobraną z załącznika

__-f \<data\>__     włącza pobieranie plików z maili, które zostałe wysłane po dacie podanej
jako paramter przełącznika, pozwala ograniczyć ilość przetwarzanych maili,
np -f 11-11-2020 pobierze tylko maile wysłane po 11 listopada 2020

__-t \<data\>__    włącza pobieranie plików z maili, które zostałe wysłane przed datą podaną
jako paramter przełącznika, pozwala ograniczyć ilość przetwarzanych maili,
np -t 11-11-2021 pobierze tylko maile wysłane przed 11 listopada 2021
__-s__ podanie tego przełącznika spowoduje, że aplikacja zakończy swoje działanie zaraz po pobraniu
pierwszego załącznika, w przypadku gdy oczekuje w skrzynce 5 maili do przetworzenia to należy
aplikacje wywołać 5 razy z przełącznikiem -s w celu pobrania ich wszystkich,

__-v__            wlącz tryb gadatliwy, w którym na ekranie prezentowanych jest wiele
informacji pomocnych w sprawdzeniu poprawności działania programu bądź zrozumieniu przyczyn
błędów w działaniu programu

__-c__ <plik>    pozwala wskazać plik z którego mają być wczytane poświadczenia dla konta 
dla konta gmail, np. _-c app-fetch.cfg_ spowoduje wczytanie z pliku o tej nazwie, dzięki temu
przełącznikowi możliwe jest trzymanie fetch i send korzystających z dwóch różnych
kont w jednym katalogu 

