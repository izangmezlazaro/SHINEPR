import os
import re
import psycopg2
from datetime import datetime

# ─── CONFIGURACIÓN ───────────────────────────────────────────
CARPETA_IMAGENES = r"C:/SHINE/SHINE/frontend/assets/img/perfumes"
RUTA_BASE        = "assets/img/perfumes"
IMAGEN_FALLBACK  = "assets/img/perfumes/FB.jpeg"

DB_CONFIG = {
    "host":     "shine.cgup9yonqtos.us-east-1.rds.amazonaws.com",
    "port":     5432,
    "database": "shine",
    "user":     "postgres",
    "password": "shine2025"
}

EXTENSIONES = (".jpg", ".jpeg", ".png", ".webp", ".gif")

# ─────────────────────────────────────────────────────────────────

try:
    conn   = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    print("✅ Conexión a BD establecida\n")
except Exception as e:
    print(f"❌ Error de conexión a BD: {e}")
    exit(1)

# ─── OBTENER LISTA DE ARCHIVOS ───────────────────────────────
try:
    archivos = [f for f in os.listdir(CARPETA_IMAGENES) 
                if f.lower().endswith(EXTENSIONES)]
    print(f"📁 {len(archivos)} imágenes encontradas\n")
except Exception as e:
    print(f"❌ Error al leer carpeta: {e}")
    exit(1)

# ─── AGRUPAR IMÁGENES POR SKU ────────────────────────────────
imagenes_por_sku = {}

for archivo in archivos:
    nombre = os.path.splitext(archivo)[0]
    match  = re.match(r'^(.+?)(?:_(\d+))?$', nombre)
    if match:
        sku   = match.group(1)
        orden = int(match.group(2)) if match.group(2) else 0
        imagenes_por_sku.setdefault(sku, []).append((archivo, orden))

for sku in imagenes_por_sku:
    imagenes_por_sku[sku].sort(key=lambda x: x[1])

# ─── OBTENER TODOS LOS PRODUCTOS ────────────────────────────
cursor.execute("SELECT id_producto, sku FROM producto")
productos = {row[1]: row[0] for row in cursor.fetchall()}
print(f"📦 {len(productos)} productos encontrados\n")

# ─── PROCESAR CADA PRODUCTO ──────────────────────────────────
insertadas   = []
actualizadas = []
fallback     = []
omitidas     = []
errores      = []

for sku_prod, id_producto in productos.items():

    imagenes = imagenes_por_sku.get(sku_prod)

    if not imagenes:
        # ── Sin imágenes → asignar FB.jpeg como fallback ──────
        try:
            cursor.execute("""
                SELECT id_imagen FROM imagen_producto WHERE id_producto = %s
            """, (id_producto,))
            existente = cursor.fetchone()

            if existente:
                cursor.execute("""
                    UPDATE imagen_producto SET url = %s, descripcion = %s
                    WHERE id_producto = %s AND id_imagen = %s
                """, (IMAGEN_FALLBACK, "sin-imagen", id_producto, existente[0]))
                actualizadas.append(f"{sku_prod} (FB)")
            else:
                cursor.execute("""
                    INSERT INTO imagen_producto (id_producto, url, descripcion)
                    VALUES (%s, %s, %s)
                """, (id_producto, IMAGEN_FALLBACK, "sin-imagen"))
                insertadas.append(f"{sku_prod} (FB)")

            fallback.append(sku_prod)
            print(f"🖼️  {sku_prod:15} → Sin foto, asignado FB.jpeg")
        except Exception as e:
            errores.append((sku_prod, str(e)))
            print(f"❌ {sku_prod:15} → Error: {e}")
        continue

    # ── Con imágenes → procesar SOLO FB.*, omitir otras ───────
    try:
        cursor.execute("""
            SELECT id_imagen, url FROM imagen_producto WHERE id_producto = %s
            ORDER BY id_imagen
        """, (id_producto,))
        existentes = cursor.fetchall()

        contador_imagen = 0
        
        for archivo, orden in imagenes:
            nombre_archivo = os.path.splitext(archivo)[0].upper()
            
            # ── FILTRO: Solo procesar FB ───────────────────
            if nombre_archivo == "FB":
                ruta_url    = f"{RUTA_BASE}/FB.jpeg"
                descripcion = "principal" if orden == 0 else f"secundaria-{orden}"

                if contador_imagen < len(existentes):
                    cursor.execute("""
                        UPDATE imagen_producto SET url = %s, descripcion = %s
                        WHERE id_imagen = %s
                    """, (ruta_url, descripcion, existentes[contador_imagen][0]))
                    actualizadas.append(archivo)
                    print(f"🔄 {archivo:20} → Actualizada a FB.jpeg")
                    contador_imagen += 1
                else:
                    cursor.execute("""
                        INSERT INTO imagen_producto (id_producto, url, descripcion)
                        VALUES (%s, %s, %s)
                    """, (id_producto, ruta_url, descripcion))
                    insertadas.append(archivo)
                    print(f"✅ {archivo:20} → Insertada como FB.jpeg")
                    contador_imagen += 1
            else:
                # ── OMITIR cualquier otra imagen ───────────
                omitidas.append(archivo)
                print(f"⏭️  {archivo:20} → Omitida (solo se procesan FB.*)")

    except Exception as e:
        errores.append((sku_prod, str(e)))
        print(f"❌ {sku_prod:15} → Error: {e}")

# ─── GUARDAR CAMBIOS ─────────────────────────────────────────
try:
    conn.commit()
    print("\n" + "─" * 70)
    print("✅ Cambios guardados en BD")
    print("─" * 70 + "\n")
except Exception as e:
    conn.rollback()
    print(f"\n❌ Error al guardar cambios: {e}")
    errores.append(("BD COMMIT", str(e)))

cursor.close()
conn.close()

# ─── RESUMEN FINAL ───────────────────────────────────────────
print("""
╔════════════════════════════════════════════════════════════╗
║                    RESUMEN DE OPERACIÓN                    ║
╚════════════════════════════════════════════════════════════╝
""")

print(f"✅ Insertadas:                {len(insertadas):4}")
print(f"🔄 Actualizadas:              {len(actualizadas):4}")
print(f"⏭️  Omitidas:                 {len(omitidas):4}")
print(f"🖼️  Con fallback FB.jpeg:    {len(fallback):4}")
print(f"❌ Errores:                   {len(errores):4}")

total_procesadas = len(insertadas) + len(actualizadas)
print(f"""
╔════════════════════════════════════════════════════════════╗
Total procesadas (FB):                        {total_procesadas:4}
Total omitidas (otras):                       {len(omitidas):4}
Porcentaje procesado:         {((total_procesadas) / (len(insertadas) + len(actualizadas) + len(omitidas)) * 100) if (len(insertadas) + len(actualizadas) + len(omitidas)) > 0 else 0:.1f}%
╚════════════════════════════════════════════════════════════╝
""")

if fallback:
    print(f"\n📌 SKUs sin imágenes (asignado FB.jpeg): {len(fallback)}")
    for i, sku in enumerate(fallback[:10]):
        print(f"   • {sku}")
    if len(fallback) > 10:
        print(f"   ... y {len(fallback) - 10} más")

if errores:
    print(f"\n❌ Errores encontrados: {len(errores)}")
    for sku, error in errores[:10]:
        print(f"   • {sku}: {error[:60]}")
    if len(errores) > 10:
        print(f"   ... y {len(errores) - 10} más")

print(f"\n⏱️  Generado: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}")