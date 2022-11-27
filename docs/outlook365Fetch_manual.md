## Instrukcja konfiguracji outlook365Fetch.exe
### plik cfg
Plik _app.cfg_ zawiera infromacje potrzebne do połączenia z kontem _printerwarsaw@bacardi.com_. Uwaga aplikacja outlook365Fetch.exe domyślnie poszukuje pliku w katalogu,
z którego została uruchomiona, a nie w katalogu w którym została zainstalowana.
* _refreshToken_ zawiera token o długiej ważności, który może wymienić na access token umożliwiający wykonanie połączenia do usługi outlook365 w chmurze Azure.
* _clientId_ zawiera identyfikator klienta, który będzie użyty do wymiany refresh token na access token
* _clientSecret_ pozwala przekazać informacje uwierzytelniającą dla klienta wskazanego w _clientId_


### przed uruchomieniem
należy stworzyć plik app.cfg z wypelnionymi _clientId_ oraz _clientSecret_, zawartość pliku będzie następująca:
```
clientId=4e33db79-7e64-4bc7-a482-ad24de40913d
clientSecret=secret
```
gdzie secret należy zamienić na wartość przekazaną w sposób bezpieczny przez Karola


### pierwsze uruchomienie
Na ekranie pojawi się link który należy otworzyć w przeglądarce (testowane w Chrome), link ma postać,
(łamania linii wprowadziłem dla lepszej czytelności, powinna to być jedna linia)
```html
https://login.microsoftonline.com/common/oauth2/v2.0/authorize?
client_id=4e33db79-7e64-4bc7-a482-ad24de40913d&response_type=code
&redirect_uri=http://localhost:3000/auth/callback&response_mode=query
&scope=offline_access%20user.read%20mail.read%20mail.send&state=12345
```

Wyświetli się monit o login i hasło. Po udanym logowaniu zostaniemy przeniesieni na stronę o adresie:
(łamania linii wprowadziłem dla lepszej czytelności, powinna to być jedna linia)
```html
http://localhost:3000/auth/callback?code=0.AQ0ADngWnp1xa0mogCSsI
MMKYHnbNADKeIAA&state=12345&session_state=2b01cba8-c0ed-4da2-b677-12b5108dcb84#

```

Należy skopiować adres z przeglądarki i wyciągnąć z niego wartość dla klucza _code_, czyli wszystkie znaki jakie pojawiają się
pomiędzy _code=_, a _&state=12345_. Te znaki należ wkleić w oknie programu _outlook365Fetch.exe_ i nacisnąć enter.

Zostanie dodany nowy wpis do plku _app.cfg_ z kluczem _refreshToken_
Aplikacja zacznie pracować dalej ściągając załączniki z maili na serwerze i zapisze je w folderze, z którego ją uruchomiono.


### drugie uruchomienie
Aplikacja zapisze załączniki z maili na serwerze w folderze, z którego ją uruchomiono.

## Kody błędów zwracane przez outlook365Fetch
__3__ - nie udało się wczytać pliku z konfiguracją, np _app.cfg_

__4__ - nie udało się zapisać załącznika, powodów może być wiele

__16__ - użyto nieprawidłowy refresh token

__17__ - refresh token wygasł, trzeba usunąc z app.cfg i uruchomić fetch jeszcze raz


