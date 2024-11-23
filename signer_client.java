import asyncio
import math

async def main():
    try:
        # Conectar con el servidor
        reader, writer = await asyncio.open_connection('127.0.0.1', 5000)
        
        # Solicitar datos al usuario
        ciudad = input("Ingrese el nombre de la ciudad a consultar: ")
        n_ciudades = input("Ingrese la cantidad de ciudades cercanas a mostrar: ")
        
        # Normalizar el nombre de la ciudad (primera letra de cada palabra en mayúscula)
        ciudad = ' '.join(word.capitalize() for word in ciudad.strip().split())
        
        # Enviar datos al servidor
        mensaje = f"{ciudad},{n_ciudades}"
        writer.write(mensaje.encode())
        await writer.drain()
        
        # Recibir respuesta del servidor
        respuesta = await reader.read(4096)
        respuesta = respuesta.decode()
        
        # Guardar resultados en archivo CSV
        with open("lista_ciudades.csv", "w") as f:
            f.write("Ciudades,Distancia\n")  # Encabezado
            f.write(respuesta)
            
        print("Resultados guardados en lista_ciudades.csv")
        
        # Cerrar conexión
        writer.close()
        await writer.wait_closed()
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    asyncio.run(main())
