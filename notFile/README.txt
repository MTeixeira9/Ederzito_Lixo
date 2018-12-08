Grupo tm011

Como executar:
	1- Executar a classe Server, com os par�metros:	
		Program arguments: 23232
	
	2- Executar a classe Client, com os par�metros:	
		Program arguments: <localUserId> <password> <IP:23232>
		(Exemplo de Program arguments: Joao 123 127.10.129.49:23232)
		Tome 123 10.101.148.134:12345
		
//ALTERARRRRRRRRRRRRRRRRRRRRRRRRR

Estrutura:
A primeira vez que um Cliente se ligar ao Servidor ser�o criadas 2 pastas:
	- do lado do Servidor, a pasta PhotoShareRepositorio, que cont�m automaticamente um ficheiro
	  users.txt (para guardar informa��es dos Clientes) e uma pasta com nome do Cliente que se 
	  conectou (onde ir� ser guardada todas as fotos e outras informa��es deste Cliente),
	  juntamente com um ficheiro listaFotos.txt (para guardar nome e data das fotos do Cliente)
	  
	- do lado do Cliente, a pasta RepositorioLocal, que cont�m uma pasta com o nome do Cliente
	  que se conectou (onde ir� ser guardada todas as fotos que o Cliente desejar copiar para o
	  seu Reposit�rio Local atrav�s da opera��o '-g')
	  
Depois disso, todos os Clientes que se conectarem ao Servidor, apenas as subpastas com os nomes
respetivos dos Clientes ser�o criadas (no caso do Servidor o ficheiro listaFotos.txt tamb�m ser�
criado para cada Cliente).

NOTA: Ao utilizar a opera��o '-a', quando indicar o Path da foto, n�o utilizar pastas cujo nome
      tenha espa�os em branco. 
      Por exemplo: C://Users/Joao Francisco/Imagens/sol.jpg -> este caminho n�o funcionar�. 
      As barras tamb�m ter�o que ser orientadas neste sentido -> /