# TcpChat

Este projeto implementa um chat TCP com duas versões distintas:

---

## Estrutura

### `main/`
Contém a versão **baseada em terminal**, que permite a comunicação entre **dispositivos diferentes na mesma rede** (ou em redes roteadas entre si).

- Funciona via linha de comando (sem interface gráfica).
- Permite que clientes em diferentes máquinas se comuniquem via IP.

**Como executar:**

```bash
# Compilar
javac ChatServer.java ChatClient.java

# Iniciar o servidor (em uma máquina)
cd ..
java main/ChatServer <porta>

# Iniciar o cliente (em outra máquina)
cd ..
java main/ChatClient <ip_do_servidor> <porta> <nome>

```
**Para swing**
```bash
# Compilar
javac swing/ChatClient.java swing/ChatServer.java

# Iniciar o servidor
cd ..
java swing/ChatServer

# Iniciar o cliente (pode abrir várias instâncias)
cd ..
java swing/ChatClient

```
O projeto do swing é a representação visual em apenas uma máquina.
#Fotos (1 arquivos da pasta main em diferentes máquinas) (2 arquivos da pasta swing em uma máquina)
![Screenshot 2025-05-14 003237](https://github.com/user-attachments/assets/0cf965b9-1948-4eb6-a191-c4201ef2516e)
![WhatsApp Image 2025-05-13 at 19 29 23_728de672](https://github.com/user-attachments/assets/ac5e01a6-c908-4bf9-b21b-43bffa24796d)


