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
        ┌─────────▼────┐   ┌────▼─────────┐
        │ images       │   │ css          │
        │ (nginx)      │   │ (nginx)      │
        │ photo.jpg    │   │ style.css    │
        │ avatar.svg   │   └──────────────┘
        │ docker.svg   │
        └──────────────┘
```

| Container | Imagine de bază | Ce face |
|---|---|---|
| web | nginx:alpine | servește CV-ul și trimite /img/ și /css/ la celelalte containere |
| images | nginx:alpine | servește imaginile |
| css | nginx:alpine | servește foaia de stil |

## Rulare

```bash
cd lab7
docker compose up --build -d        # construiește și pornește tot
```

- Site: http://localhost:8090
- Imaginea direct: http://localhost:8090/img/photo.jpg
- CSS direct: http://localhost:8090/css/style.css

Verificare:

```bash
docker compose ps
docker images | grep lab7
docker network ls | grep lab7
```

Oprire: `docker compose down`

## Optimizare

- `nginx:alpine` (~50 MB) în loc de `nginx` (~190 MB).
- Fiecare container are un singur rol; doar `web` are port deschis spre exterior.
