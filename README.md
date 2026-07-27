# NAD

Aplicació Android que mostra el visor web de NAD dins d'un `WebView`.

URL inicial: `http://217.154.181.249/ws/pautestallers/visor2.php`

La primera cadena de redireccions del servidor es permet perquè el visor pugui carregar. Quan arriba a la pàgina final, la navegació principal queda bloquejada en aquell mateix document.

## PDFs

La versió 1.6 detecta enllaços i descàrregues PDF, inclosos els que s'intenten obrir en una finestra nova. El document es baixa temporalment a la memòria cau privada de l'app i es mostra en un visor intern amb controls de pàgina. En tancar-lo, el fitxer temporal s'elimina i es torna al visor NAD.

La versió continua sent provisional: tant el WebView com la descàrrega dels PDFs ignoren els errors del certificat TLS del servidor.
