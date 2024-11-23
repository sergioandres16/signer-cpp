import asyncio
import psutil

log_mensajes = []
pausado = False
ejecutando = True

async def obtener_uso_cpu():
    return psutil.cpu_percent(interval=1)

async def obtener_uso_memoria():
    return psutil.virtual_memory().percent

async def obtener_uso_discos():
    uso_discos = []
    for particion in psutil.disk_partitions():
        try:
            uso = psutil.disk_usage(particion.mountpoint)
            uso_discos.append(f"{particion.mountpoint}: {uso.percent}%")
        except Exception:
            continue
    return ", ".join(uso_discos)

def guardar_log(mensaje):
    log_mensajes.append(mensaje)
    print(mensaje)

async def capturar_entrada():
    """Maneja la entrada del usuario de manera asíncrona"""
    global pausado, ejecutando
    
    while ejecutando:
        try:
            comando = await asyncio.get_event_loop().run_in_executor(None, input)
            comando = comando.lower()
            
            if comando == "salir":
                ejecutando = False
                guardar_log("Monitorización detenida")
            elif comando == 'p':
                pausado = True
                guardar_log("Monitorización pausada")
            elif comando == 'r':
                pausado = False
                guardar_log("Monitorización reanudada")
                
        except EOFError:
            continue

async def mostrar_recursos():
    """Muestra el uso de recursos del sistema"""
    global ejecutando
    
    while ejecutando:
        if not pausado:
            cpu = await obtener_uso_cpu()
            memoria = await obtener_uso_memoria()
            discos = await obtener_uso_discos()
            
            guardar_log(f"CPU: {cpu}%")
            guardar_log(f"Memoria: {memoria}%")
            guardar_log(f"Discos: {discos}")
            guardar_log("-" * 50)
        
        await asyncio.sleep(1)

async def main():
    global ejecutando
    
    try:
        # Ejecutar ambas tareas concurrentemente
        await asyncio.gather(
            mostrar_recursos(),
            capturar_entrada()
        )
    except KeyboardInterrupt:
        ejecutando = False
        guardar_log("Monitorización detenida de manera forzosa")
    finally:
        # Guardar log en archivo
        with open("log.txt", "w") as f:
            f.write("\n".join(log_mensajes))

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nMonitorización detenida de manera forzosa")
