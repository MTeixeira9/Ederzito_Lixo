Grupo tm011

Como executar:
	1- Executar a classe Server, com os parâmetros:	
		Program arguments: 23232
	
	2- Executar a classe Client, com os parâmetros:	
		Program arguments: <localUserId> <password> <IP:23232>
		(Exemplo de Program arguments: Joao 123 127.10.129.49:23232)
		Tome 123 10.101.148.134:12345
		
//ALTERARRRRRRRRRRRRRRRRRRRRRRRRR

Estrutura:
A primeira vez que um Cliente se ligar ao Servidor serão criadas 2 pastas:
	- do lado do Servidor, a pasta PhotoShareRepositorio, que contém automaticamente um ficheiro
	  users.txt (para guardar informações dos Clientes) e uma pasta com nome do Cliente que se 
	  conectou (onde irá ser guardada todas as fotos e outras informações deste Cliente),
	  juntamente com um ficheiro listaFotos.txt (para guardar nome e data das fotos do Cliente)
	  
	- do lado do Cliente, a pasta RepositorioLocal, que contém uma pasta com o nome do Cliente
	  que se conectou (onde irá ser guardada todas as fotos que o Cliente desejar copiar para o
	  seu Repositório Local através da operação '-g')
	  
Depois disso, todos os Clientes que se conectarem ao Servidor, apenas as subpastas com os nomes
respetivos dos Clientes serão criadas (no caso do Servidor o ficheiro listaFotos.txt também será
criado para cada Cliente).

NOTA: Ao utilizar a operação '-a', quando indicar o Path da foto, não utilizar pastas cujo nome
      tenha espaços em branco. 
      Por exemplo: C://Users/Joao Francisco/Imagens/sol.jpg -> este caminho não funcionará. 
      As barras também terão que ser orientadas neste sentido -> /