# Lab 7 SO: containere Docker (opțiunea dificilă)

Cucerescu Liviu, IA-241

## Schema

```
            Browser  http://localhost:8090
                         │
              ┌──────────▼──────────┐
              │  web  (nginx)       │  index.html (CV)
              │  /img/ → images     │
              │  /css/ → css        │
              └───┬─────────────┬───┘
     rețeaua lab7net│             │
        ┌──────────▼───┐   ┌─────▼────────┐
        │ images       │   │ css          │
        │ (nginx)      │   │ (nginx)      │
        │ avatar.svg   │   │ style.css    │
        │ docker.svg   │   └──────────────┘
        └──────────────┘
        ┌──────────────────────────────────┐
        │ timer (Java 21): lab 1 refăcut   │
        │ interval · moment · periodic     │
        └──────────────────────────────────┘
```

| Container | Imagine de bază | Ce face |
|---|---|---|
| web | nginx:alpine | servește CV-ul și trimite /img/ și /css/ la celelalte containere |
| images | nginx:alpine | servește imaginile |
| css | nginx:alpine | servește foaia de stil |
| timer | eclipse-temurin:21 (multi-stage) | lab 1 în Java: `java.util.Timer` |

## Rulare

```bash
cd "lab 7"
docker compose up --build -d        # construiește și pornește tot
```

- Site: http://localhost:8090
- Imaginea direct: http://localhost:8090/img/avatar.svg
- CSS direct: http://localhost:8090/css/style.css
- Lab 1 (timer): `docker compose logs timer` sau rulat din nou: `docker compose run --rm timer`
- Cu alte valori: `docker compose run --rm -e DELAY_SEC=5 -e REPEAT=3 timer`

Verificare:

```bash
docker compose ps
docker images | grep lab7
docker network ls | grep lab7
```

Oprire: `docker compose down`

## Optimizare

- `nginx:alpine` (~50 MB) în loc de `nginx` (~190 MB).
- Timer-ul are build în 2 etape: compilează cu JDK, rulează doar cu JRE (imagine mai mică).
- Fiecare container are un singur rol; doar `web` are port deschis spre exterior.
