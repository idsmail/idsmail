## Instrukcja konfiguracji outlook365Send.exe
### plik cfg
Plik _app.cfg_ zawiera infromacje potrzebne do połączenia z kontem _printerwarsaw@bacardi.com_. Uwaga aplikacja outlook365Send.exe domyślnie poszukuje pliku w katalogu,
z którego została uruchomiona, a nie w katalogu w którym została zainstalowana.
* _refreshToken_ zawiera token o długiej ważności, który może wymienić na access token umożliwiający wykonanie połączenia do usługi outlook365 w chmurze Azure.
* _clientId_ zawiera identyfikator klienta, który będzie użyty do wymiany refresh token na access token
* _clientSecret_ pozwala przekazać informacje uwierzytelniającą dla klienta wskazanego w _clientId_


### przed uruchomieniem
przed pierwszym uruchomieniem _outlook365Send.exe_ należy przygotować konfigurację korzystając z _outlook365Fetch.exe, 
aby wygenerować refresh token

### uruchomienie
Typowe wywołanie aplikacji wygląda następująco:
outlook365Send.exe <nazwa-pliku> <adres-email-odbiorcy> 

Aplikacja wyśle plik o nazwie _nazwa-pliku_ na adres _adres-email-odbiorcy_.

Dodatkowo można użyc opcji _-c_ aby wskazać plik konfiguracyjny, który aplikacja ma użyć. Domyślnie użyje _app.cfg_.





