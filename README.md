# NAD

Aplicació Android que mostra el visor web de NAD dins d'un `WebView`.

URL inicial: `https://217.154.181.249/ws/pautestallers/visor2.php`

La navegació principal queda limitada al visor NAD. Els documents PDF es detecten, es baixen temporalment a la memòria cau privada i s'obren en un visor intern amb controls de pàgina.

## NAD 1.7

- Nou logotip SHAD NAD a la icona i a la pantalla d'error.
- Elimina l'avís visible de «mode provisional».
- Deixa d'acceptar qualsevol certificat sense comprovar-lo.
- Accepta certificats normals validats per Android o, transitòriament, el certificat exacte que presenta actualment el servidor `217.154.181.249`, comprovat per la seva empremta SHA-256.
- Si el certificat canvia, caduca o es presenta des d'un altre host, la connexió s'atura.

La solució definitiva continua sent instal·lar al servidor un certificat públic vàlid associat a un nom de domini.
