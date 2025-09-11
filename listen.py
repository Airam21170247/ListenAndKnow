from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import base64
import hashlib
from datetime import datetime
from datetime import time
import socket
import threading
import tkinter as tk
import requests

# Derivar clave AES de la frase proporcionada
frase = "(Password for encryption)"
clave = hashlib.sha256(frase.encode()).digest()  # 32 bytes para AES-256

# Crear root oculto para segundo plano
root = tk.Tk()
root.withdraw()  # Oculta completamente la ventana raíz, para siempre

# --- Configuración ---
GIST_ID = "(Your Gist ID)"  # ID del Gist
FILENAME = "(Your Gist file name with extension (Like, .txt))"        # nombre del archivo dentro del gist
TOKEN = "(Your Gist Token)"     # tu token personal con permiso gist

# --- Obtener IP privada ---
def get_ip():
    while True:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            if ip:  # si obtuvo una IP válida
                return ip
        except:
            pass
        finally:
            s.close()

        print("❌ No se pudo obtener la IP. Reintentando en 1 minuto...")
        time.sleep(60)  # esperar 60 segundos antes de reintentar

pc_ip = get_ip()
print("IP de la PC:", pc_ip)

# --- Actualizar el Gist ---
url = f"https://api.github.com/gists/{GIST_ID}"
headers = {"Authorization": f"token {TOKEN}"}
data = {
    "files": {
        FILENAME: {
            "content": pc_ip
        }
    }
}

response = requests.patch(url, json=data, headers=headers)

if response.status_code == 200:
    print("Gist actualizado correctamente.")
    print("URL raw para Android:")
    print(f"https://gist.githubusercontent.com/raw/{GIST_ID}/{FILENAME}")
else:
    print("Error al actualizar gist:", response.status_code, response.text)



# Mostrar notificación animada tipo push
def mostrar_notificacion(texto):
    def cerrar():
        ventana.destroy()

    ventana = tk.Toplevel(root)
    ventana.overrideredirect(True)
    ventana.attributes("-topmost", True)
    ventana.configure(bg="black")

    # Marco con bordes redondeados (solo estéticos en apariencia)
    frame = tk.Frame(ventana, bg="black", bd=0)
    frame.pack(expand=True, fill="both", padx=8, pady=8)

    label = tk.Label(frame, text=texto, bg="black", fg="white", font=("Segoe UI", 10), justify="left", wraplength=280)
    label.pack(padx=10, pady=10, side="left")

    btn_cerrar = tk.Button(frame, text="✘", command=cerrar, font=("Segoe UI", 18), bg="black", fg="white", borderwidth=0, relief="flat", cursor="hand2")
    btn_cerrar.pack(padx=10, pady=0, side="right")

    # Posición inicial fuera de la pantalla (arriba a la izquierda)
    ventana.geometry(f"370x100+10+-100")

    def animar():
        for y in range(-100, 20, 5):
            ventana.geometry(f"370x120+10+{y}")
            ventana.update()
            ventana.after(10)

    threading.Thread(target=animar, daemon=True).start()

# Hilo del servidor
def escuchar():
    servidor = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    servidor.bind(("0.0.0.0", 5000))
    servidor.listen(5)
    print("OK")
    threading.Thread(target=mostrar_notificacion, args=("OK",), daemon=True).start()

    while True:
        cliente, direccion = servidor.accept()
        datos = cliente.recv(1024)
        try:
            datos = base64.b64decode(datos)
            iv = datos[:16]
            cifrado = datos[16:]
            cipher = AES.new(clave, AES.MODE_CBC, iv)
            mensaje_descifrado = unpad(cipher.decrypt(cifrado), AES.block_size).decode()
        except Exception as e:
            error = f"❌ Mensaje inválido de {direccion[0]}: {e}"
            print(error)
            threading.Thread(target=mostrar_notificacion, args=(error,), daemon=True).start()
            cliente.close()
            continue

        fecha_hora = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        mensaje = f"{mensaje_descifrado}\n[{fecha_hora}] => {direccion}"
        print(mensaje)
        threading.Thread(target=mostrar_notificacion, args=(mensaje,), daemon=True).start()
        cliente.close()

#Hilo para cerrar el programa si son las 10:00pm o mas tarde
def cerrar_programa():
    while True:
        threading.Event().wait(1)
    # Verificar la hora actual cada segundo
        hora_actual = datetime.now().time()
        print("⏰ Hora actual:", hora_actual)

        if hora_actual >= time(22, 0):  # 22:00 equivale a 10:00 p.m.
            print(f"⏰ Son las {hora_actual.strftime('%H:%M')}, cerrando el programa.")
            root.quit()
            root.destroy()


# Iniciar servidor en segundo plano
threading.Thread(target=escuchar, daemon=True).start()
# Iniciar hilo para cerrar el programa
threading.Thread(target=cerrar_programa, daemon=True).start()

# Loop principal oculto para mantener el programa corriendo
root.mainloop()
