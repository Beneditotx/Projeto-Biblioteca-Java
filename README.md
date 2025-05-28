Documentação Técnica - Projeto Sistema de Biblioteca 
Introdução
Este documento descreve as funcionalidades e estruturas das principais classes que compõem o projeto do Sistema de Biblioteca. Desenvolvido em Java, o sistema tem como objetivo gerenciar o login e registro de usuários, além do catálogo, empréstimo e devolução de livros. Todas as informações persistentes são armazenadas e gerenciadas através de um banco de dados MySQL, e a interação com o usuário ocorre por meio de um menu de terminal.

Classe Main 
A classe Main é o ponto de entrada da aplicação. Ela apresenta um menu principal com opções para o usuário realizar login, registrar-se no sistema ou sair. Utiliza a classe Scanner para capturar a entrada do usuário e interage com as classes LoginManager e RegisterManager para executar as ações correspondentes à escolha do usuário.

Classe User 
Representa o modelo de dados de um usuário (leitor). Contém atributos como idLeitor (identificador único do banco de dados), username (para login), password (que armazena o hash seguro da senha), firstName, lastName, email, birthDate (data de nascimento), phone (telefone), address (endereço), cpf e age (idade calculada). Possui os respectivos métodos getters e setters, sendo a estrutura base para criar e manipular dados de usuários no sistema.

Classe PasswordManager 
Esta classe é um componente crucial responsável por garantir a segurança das senhas dos usuários. Ela utiliza o algoritmo BCrypt para gerar um "hash" seguro das senhas antes de serem armazenadas no banco de dados e para verificar as senhas durante o processo de login.

Classe DatabaseManager 
Esta classe é central para a persistência de dados. Ela encapsula toda a lógica de conexão com o banco de dados MySQL utilizando JDBC. Fornece métodos para realizar operações CRUD (Create, Read, Update, Delete) nas tabelas do banco, como registrar novos leitores, buscar informações de usuários, atualizar senhas, verificar a existência de usernames, carregar livros, registrar empréstimos e devoluções, e atualizar quantidades de livros. Utiliza PreparedStatement para prevenir SQL Injection e gerencia transações para garantir a atomicidade de operações críticas (como empréstimos e devoluções).

Classe RegisterManager 
Responsável por registrar novos usuários no sistema. Seu método registerUser() solicita dados como nome completo, telefone, e-mail, endereço, data de nascimento, CPF, nome de usuário (username) e senha. Antes de salvar, a senha é transformada em um hash seguro pela classe PasswordManager. O RegisterManager utiliza o DatabaseManager para verificar se o username (ou outros campos únicos como CPF/email) já existe no banco e para inserir o novo usuário na tabela Leitores.

Classe LoginManager 
Gerencia o processo de login e redefinição de senha dos usuários.
O método performLogin() solicita o username e a senha. Ele utiliza o DatabaseManager para buscar o hash da senha armazenado no banco para o username fornecido e, em seguida, usa o PasswordManager para comparar o hash da senha digitada com o hash armazenado. Se corresponderem, o login é bem-sucedido e o usuário acessa o menu de suas funcionalidades.
O método handlePasswordReset() permite que um usuário redefina sua senha após verificar seu username e e-mail (buscando dados via DatabaseManager). A nova senha é hasheada pelo PasswordManager e atualizada no banco de dados através do DatabaseManager.

Classe BooksManager 
Gerencia o catálogo de livros, empréstimos e devoluções, interagindo com o DatabaseManager para todas as operações persistentes.
Os livros são carregados da tabela Livros do banco de dados, onde cada título possui informações sobre QuantidadeTotal e QuantidadeDisponivel. Fornece funcionalidades para o usuário visualizar todos os livros disponíveis ou filtrar por categoria. A disponibilidade é determinada pela QuantidadeDisponivel > 0.
O método borrowBooks() permite ao usuário selecionar um livro disponível. Se o empréstimo for confirmado, ele chama o DatabaseManager para registrar o empréstimo nas tabelas Emprestimos e Itens_Emprestimo, e para decrementar a QuantidadeDisponivel do livro na tabela Livros (tudo dentro de uma transação).
O método handleBookReturn() permite ao usuário visualizar seus empréstimos ativos (via DatabaseManager) e selecionar um item para devolução. A devolução é registrada no DatabaseManager, que atualiza a tabela Itens_Emprestimo e incrementa a QuantidadeDisponivel do livro na tabela Livros (também em uma transação). A classe interna Book representa os dados de um livro, incluindo suas quantidades.

Classe EmprestimoDetalhe ℹ
Classe auxiliar utilizada para transportar e exibir informações detalhadas sobre os empréstimos ativos de um usuário, como ID do item, ID do livro, título e data de devolução prevista.

Conclusão 
O projeto Sistema de Biblioteca é uma aplicação Java de console robusta que gerencia usuários e um catálogo de livros com funcionalidades de empréstimo e devolução, utilizando um banco de dados MySQL para persistência de dados. Ele implementa práticas importantes como hashing seguro de senhas (BCrypt) para proteger os dados dos usuários e transações de banco de dados para garantir a integridade dos dados em operações críticas. O projeto demonstra conceitos de programação orientada a objetos, interação com banco de dados (JDBC), e gerenciamento de dependências. Pode ser expandido com mais funcionalidades e, futuramente, uma interface gráfica.
