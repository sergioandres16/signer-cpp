import asyncio
import math

def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Calcula la distancia entre dos puntos usando la fórmula de Haversine
    """
    RADIO_TIERRA = 6371
    
    # convetrir grados a radianes
    lat1 = math.radians(lat1)
    lon1 = math.radians(lon1)
    lat2 = math.radians(lat2)
    lon2 = math.radians(lon2)
    
    # diferencias en coordenadas
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    
    # fórmula de Haversine
    a = math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    
    return RADIO_TIERRA * c

def leer_coordenadas():
    """Lee el archivo de coordenadas y retorna un diccionario con la información"""
    ciudades = {}
    try:
        with open("coordenadas.csv", "r") as f:
            lineas = f.readlines()
            
        for linea in lineas[1:]:
            ciudad, lat, lon = linea.strip().split(',')
            ciudades[ciudad.title()] = (float(lat), float(lon))
            
    except Exception as e:
        print(f"Error leyendo el archivo: {e}")
    return ciudades

def encontrar_ciudades_cercanas(ciudad_origen, n, ciudades):
    """Encuentra las N ciudades más cercanas a la ciudad origen"""
    ciudad_origen = ciudad_origen.title()
    
    if ciudad_origen not in ciudades:
        return f"Error: La ciudad '{ciudad_origen}' no fue encontrada"
    
    lat1, lon1 = ciudades[ciudad_origen]
    distancias = []
    
    for ciudad, (lat2, lon2) in ciudades.items():
        if ciudad != ciudad_origen:
            distancias.append((ciudad, haversine_distance(lat1, lon1, lat2, lon2)))
    
    distancias.sort(key=lambda x: x[1])
    return distancias[:n]

async def handle_client(reader, writer):
    """Maneja la conexión con un cliente"""
    try:
        data = await reader.read(1024)
        ciudad, n = data.decode().split(',')
        n = int(n)
        
        if n <= 0:
            raise ValueError("El número de ciudades debe ser positivo")
            
        # Calcular ciudades cercanas
        ciudades = leer_coordenadas()
        resultado = encontrar_ciudades_cercanas(ciudad, n, ciudades)
        
        # Preparar y enviar respuesta
        if isinstance(resultado, str):
            respuesta = resultado
        else:
            respuesta = "\n".join(f"{ciudad},{dist:.2f}" for ciudad, dist in resultado)
            
        writer.write(respuesta.encode())
        await writer.drain()
        
    except Exception as e:
        writer.write(f"Error: {str(e)}".encode())
        await writer.drain()
    finally:
        writer.close()

async def main():
    server = await asyncio.start_server(handle_client, '127.0.0.1', 5000)
    addr = server.sockets[0].getsockname()
    print(f'Servidor iniciado en {addr}')
    async with server:
        await server.serve_forever()

if __name__ == "__main__":
    asyncio.run(main())
