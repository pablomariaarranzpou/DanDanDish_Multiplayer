[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-c66648af7eb3fe8bc4f294546bfd86ef473780cde1dea487d3c4ff354943c9ae.svg)](https://classroom.github.com/online_ide?assignment_repo_id=10150861&assignment_repo_type=AssignmentRepo)
# ReadMe Pràctica 1 Grup: A1

## Descàrrega del projecte

En fer els tests als ordinadors del laboratori a classe vam poder comprovar que abans d'executar el programa s'han de fer els següents passos pel seu correcte funcionament:

* Marcar el directori src com a Sources Root:

![image](figures/C1.png)

* Marcar el directori out com a Excluded:

![image](figures/C3.png)

* Configurar l'estructura del projecte (project structures) escollint les següents opcions:

![image](figures/C2.png)

## Executable

S'han creat dos ".jar" per a executar el projecte des de la terminal. Aquests es troben a la carpeta out/artifacts del projecte pujat al campus virtual. En la carpeta Server es troba l'executable del servidor i a la carpeta Client el del client. La comanda per a executar el servidor en mode Server VS Client és el següent:

* java -jar Server.jar  -p 9999 -m 0
  * -p: port a on establir-se
  * -m: 0 per mode Client vs Servidor i 1 mode Client vs Client

![image](figures/Server_0.png)

Per a executar el client en local:

* java -jar Client.jar -h localhost -p 9999
  * -h: IP o nom de la màquina a on connectar-se
  * -p: port on trobar el servidor

![image](figures/Client.png)

Per a executar el servidor en mode Client VS Client:

* java -jar Server.jar -p 9999 -m 1

![image](figures/Server_1.png)