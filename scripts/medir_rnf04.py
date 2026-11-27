import json, time, urllib.request, urllib.error, random, statistics
from concurrent.futures import ThreadPoolExecutor

BASE = "http://127.0.0.1:8081"


def peticion(metodo, ruta, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + ruta, data=data, method=metodo)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    inicio = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=10) as r:
            r.read()
            codigo = r.status
    except urllib.error.HTTPError as e:
        e.read()
        codigo = e.code
    return codigo, (time.perf_counter() - inicio) * 1000


def percentil(valores, p):
    valores = sorted(valores)
    k = max(0, min(len(valores) - 1, int(round((p / 100) * len(valores) + 0.5)) - 1))
    return valores[k]


def medir(nombre, metodo, ruta, body, token, total, workers):
    def una(_):
        return peticion(metodo, ruta, body, token)

    with ThreadPoolExecutor(max_workers=workers) as pool:
        resultados = list(pool.map(una, range(total)))

    codigos = {c: sum(1 for x, _ in resultados if x == c) for c in set(x for x, _ in resultados)}
    tiempos = [t for _, t in resultados]
    print(f"\n{nombre}: {total} peticiones, {workers} concurrentes")
    print(f"  codigos: {codigos}")
    print(f"  p50={percentil(tiempos,50):.0f} ms  p95={percentil(tiempos,95):.0f} ms  "
          f"p99={percentil(tiempos,99):.0f} ms  max={max(tiempos):.0f} ms  media={statistics.mean(tiempos):.0f} ms")
    return tiempos


sufijo = random.randint(100000, 999999)
codigo, _ = peticion("POST", "/api/v1/auth/registro", {
    "nombreCompleto": "Prueba Carga",
    "correo": f"carga{sufijo}@test.co",
    "password": "Carga2026*",
    "intereses": [1],
    "aceptaTratamientoDatos": True,
})
print("registro turista:", codigo)
codigo, _ = peticion("POST", "/api/v1/auth/login", {
    "correo": f"carga{sufijo}@test.co",
    "password": "Carga2026*",
})
print("login turista:", codigo)
with urllib.request.urlopen(urllib.request.Request(
        BASE + "/api/v1/auth/login",
        data=json.dumps({"correo": f"carga{sufijo}@test.co", "password": "Carga2026*"}).encode(),
        headers={"Content-Type": "application/json"}, method="POST")) as r:
    _d = json.load(r)
    token = _d.get("token") or _d.get("tokenSesion")
print("token:", token[:20] + "...")

medir("GET /api/v1/lugares", "GET", "/api/v1/lugares?tamano=20", None, None, 300, 20)

cuerpo = {"municipio": "Salento", "intereses": ["ecológico"],
          "tiempoDisponibleMin": 240, "origen": {"lat": 4.637, "lng": -75.571}}
medir("POST /api/v1/rutas/propuesta", "POST", "/api/v1/rutas/propuesta", cuerpo, token, 120, 10)
