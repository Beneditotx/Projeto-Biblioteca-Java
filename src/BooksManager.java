import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class BooksManager {

    private List<Book> todosOsLivros;
    private Scanner scanner;
    private DatabaseManager dbManager;

    private static final int LOAN_DAYS = 14;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class Book {
        private int idLivro;
        private String title;
        private String category;
        private String author;
        private String editora;
        private int quantidadeTotal;
        private int quantidadeDisponivel;

        public Book(int idLivro, String title, String author, String editora, String category, int quantidadeTotal, int quantidadeDisponivel) {
            this.idLivro = idLivro;
            this.title = title;
            this.author = author;
            this.editora = editora;
            this.category = category;
            this.quantidadeTotal = quantidadeTotal;
            this.quantidadeDisponivel = quantidadeDisponivel;
        }
        public int getIdLivro() { return idLivro; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getAuthor() { return author; }
        public String getEditora() { return editora; }
        public int getQuantidadeTotal() { return quantidadeTotal; }
        public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
        public boolean isDisponivel() { return quantidadeDisponivel > 0; }
        public void emprestarCopia() { if (isDisponivel()) this.quantidadeDisponivel--; }
        public void devolverCopia() { if (this.quantidadeDisponivel < this.quantidadeTotal) this.quantidadeDisponivel++; }
        @Override
        public String toString() {
            return "ID: " + idLivro + " | Título: " + title + " | Autor: " + author +
                    " | Editora: " + editora + " | Categoria: " + category +
                    " | Disponíveis: " + quantidadeDisponivel + "/" + quantidadeTotal;
        }
    }

    public BooksManager() {
        this.todosOsLivros = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.dbManager = new DatabaseManager();
        carregarLivrosDoBanco();
    }

    public void carregarLivrosDoBanco() {
        this.todosOsLivros = dbManager.buscarTodosOsLivros();
    }

    public List<Book> getTodosOsLivros() {
        return new ArrayList<>(this.todosOsLivros);
    }

    public boolean realizarEmprestimoGUI(User user, int idLivro) {
        Book livroParaEmprestar = null;
        for (Book book : todosOsLivros) {
            if (book.getIdLivro() == idLivro && book.isDisponivel()) {
                livroParaEmprestar = book;
                break;
            }
        }

        if (livroParaEmprestar == null) {
            System.err.println("BooksManager: Tentativa de emprestar livro (ID: " + idLivro + ") não encontrado na lista local ou indisponível (antes de recarregar).");
            carregarLivrosDoBanco();
            for (Book book : todosOsLivros) {
                if (book.getIdLivro() == idLivro && book.isDisponivel()) {
                    livroParaEmprestar = book;
                    break;
                }
            }
            if (livroParaEmprestar == null) {
                System.err.println("BooksManager: Livro (ID: " + idLivro + ") não encontrado ou indisponível mesmo após recarregar.");
                return false;
            }
        }

        boolean sucessoDB = dbManager.registrarEmprestimo(user.getIdLeitor(), idLivro);
        if (sucessoDB) {
            livroParaEmprestar.emprestarCopia();
            return true;
        }
        return false;
    }

    public boolean realizarMultiplosEmprestimosGUI(User user, List<Integer> idsLivros) {
        if (idsLivros == null || idsLivros.isEmpty()) {
            return false;
        }
        for (int idLivro : idsLivros) {
            boolean encontradoEDisponivelLocalmente = false;
            for (Book book : todosOsLivros) {
                if (book.getIdLivro() == idLivro && book.isDisponivel()) {
                    encontradoEDisponivelLocalmente = true;
                    break;
                }
            }
            if (!encontradoEDisponivelLocalmente) {
                System.err.println("BooksManager: Livro ID " + idLivro + " selecionado para empréstimo múltiplo não está disponível na lista local ou não foi encontrado (antes de recarregar).");
                carregarLivrosDoBanco();
                encontradoEDisponivelLocalmente = false;
                for (Book book : todosOsLivros) {
                    if (book.getIdLivro() == idLivro && book.isDisponivel()) {
                        encontradoEDisponivelLocalmente = true;
                        break;
                    }
                }
                if(!encontradoEDisponivelLocalmente) {
                    System.err.println("BooksManager: Livro ID " + idLivro + " ainda indisponível/não encontrado após recarregar.");
                    return false;
                }
            }
        }

        boolean sucessoDB = dbManager.registrarMultiplosEmprestimos(user.getIdLeitor(), idsLivros);
        if (sucessoDB) {
            for (int idLivro : idsLivros) {
                for (Book book : todosOsLivros) {
                    if (book.getIdLivro() == idLivro) {
                        book.emprestarCopia();
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public List<EmprestimoDetalhe> getEmprestimosAtivosParaGUI(User user) {
        return dbManager.buscarEmprestimosAtivosPorUsuario(user.getIdLeitor());
    }

    public boolean realizarDevolucaoGUI(int idItemEmprestimo, int idLivro) {
        boolean sucessoDB = dbManager.registrarDevolucao(idItemEmprestimo, idLivro);
        if (sucessoDB) {
            for (Book book : todosOsLivros) {
                if (book.getIdLivro() == idLivro) {
                    book.devolverCopia();
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public void borrowBooksConsole(User user) {
        System.out.println("\n--- EMPRÉSTIMO DE LIVROS (Console) ---");
        listAvailableBooksConsole();
        boolean algumLivroDisponivelParaEmprestimo = false;
        for (Book book : todosOsLivros) {
            if (book.isDisponivel()) {
                algumLivroDisponivelParaEmprestimo = true;
                break;
            }
        }
        if (!algumLivroDisponivelParaEmprestimo) {
            System.out.println("Nenhum livro disponível no momento.");
            return;
        }

        System.out.print("\nDigite o ID do livro que deseja emprestar (ou 0 para voltar): ");
        String idInput = scanner.nextLine().trim();
        if (idInput.equals("0")) return;

        try {
            int idLivroEscolhido = Integer.parseInt(idInput);
            if (realizarEmprestimoGUI(user, idLivroEscolhido)) {
                System.out.println("Livro emprestado com sucesso via console!");
                System.out.println("Data de devolução prevista: " + LocalDate.now().plusDays(LOAN_DAYS).format(DATE_FORMATTER));
            } else {
                System.out.println("Falha ao emprestar o livro via console. Verifique a disponibilidade ou se o ID é válido.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido. Por favor, digite um número.");
        }
    }

    public void handleBookReturnConsole(User user) {
        System.out.println("\n--- DEVOLUÇÃO DE LIVROS (Console) ---");
        List<EmprestimoDetalhe> emprestimosAtivos = getEmprestimosAtivosParaGUI(user);

        if (emprestimosAtivos.isEmpty()) {
            System.out.println("Você não possui livros emprestados no momento.");
            return;
        }
        System.out.println("Seus livros emprestados (não devolvidos):");
        for (int i = 0; i < emprestimosAtivos.size(); i++) {
            System.out.println((i + 1) + " - " + emprestimosAtivos.get(i).toString());
        }
        System.out.println("0 - Voltar ao menu anterior");

        int escolhaItemLista = -1;
        while (true) {
            System.out.print("\nDigite o número do item da lista que deseja devolver (ou 0 para voltar): ");
            try {
                escolhaItemLista = scanner.nextInt();
                scanner.nextLine();
                if (escolhaItemLista >= 0 && escolhaItemLista <= emprestimosAtivos.size()) {
                    break;
                } else {
                    System.out.println("Opção inválida. Por favor, tente novamente.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, digite um número.");
                scanner.nextLine();
            }
        }

        if (escolhaItemLista == 0) {
            return;
        }

        EmprestimoDetalhe itemParaDevolver = emprestimosAtivos.get(escolhaItemLista - 1);
        System.out.print("Confirmar devolução de '" + itemParaDevolver.getTituloLivro() + "'? (S/N): ");
        String confirmacao = scanner.nextLine().trim().toUpperCase();

        if (confirmacao.equals("S")) {
            if (realizarDevolucaoGUI(itemParaDevolver.getIdItemEmprestimo(), itemParaDevolver.getIdLivro())) {
                System.out.println("Livro devolvido com sucesso via console!");
            } else {
                System.out.println("Falha ao devolver o livro via console.");
            }
        } else {
            System.out.println("Devolução cancelada.");
        }
    }

    public void listAvailableBooksConsole() {
        if (todosOsLivros.isEmpty()) {
            System.out.println("Não há livros carregados do banco de dados.");
            return;
        }
        System.out.println("\n=== TODOS OS LIVROS DISPONÍVEIS (Console) ===");
        Map<String, List<Book>> booksByCategory = new LinkedHashMap<>();
        for (Book book : todosOsLivros) {
            if (book.isDisponivel()) {
                String categoryKey = book.getCategory() != null ? book.getCategory() : "Sem Categoria";
                booksByCategory.computeIfAbsent(categoryKey, k -> new ArrayList<>()).add(book);
            }
        }
        if (booksByCategory.isEmpty()){
            System.out.println("Nenhum livro disponível no momento.");
            return;
        }
        for (Map.Entry<String, List<Book>> entry : booksByCategory.entrySet()) {
            System.out.println("\n--- " + entry.getKey().toUpperCase() + " ---");
            for (Book book : entry.getValue()) {
                System.out.println(book.toString());
            }
        }
    }

    public void showCategorySelection() {
        if (this.todosOsLivros.isEmpty()) {
            System.out.println("Nenhum livro carregado do banco de dados.");
            return;
        }
        Set<String> categories = new HashSet<>();
        for (Book book : todosOsLivros) {
            if (book.getCategory() != null && book.isDisponivel()) {
                categories.add(book.getCategory());
            }
        }
        if(categories.isEmpty()){
            System.out.println("Nenhuma categoria com livros disponíveis no momento.");
            return;
        }

        System.out.println("\n=== CATEGORIAS DISPONÍVEIS (com livros disponíveis) ===");
        int index = 1;
        List<String> categoryList = new ArrayList<>(categories);
        for (String category : categoryList) {
            System.out.println(index + " - " + category);
            index++;
        }
        System.out.println("0 - Voltar");
        System.out.println("99 - Mostrar todos os livros disponíveis");

        System.out.print("\nEscolha uma categoria: ");
        String choice = scanner.nextLine();

        if (choice.equals("0")) {
            return;
        } else if (choice.equals("99")) {
            listAvailableBooksConsole();
            return;
        }

        try {
            int categoryIndex = Integer.parseInt(choice);
            if (categoryIndex > 0 && categoryIndex <= categoryList.size()) {
                String selectedCategory = categoryList.get(categoryIndex - 1);
                listBooksByCategoryConsole(selectedCategory);
            } else {
                System.out.println("Opção inválida!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Por favor, digite um número válido.");
        }
    }

    private void listBooksByCategoryConsole(String category) {
        System.out.println("\n=== " + category.toUpperCase() + " (Disponíveis) ===");
        boolean found = false;
        for (Book book : todosOsLivros) {
            if (book.getCategory() != null && book.getCategory().equalsIgnoreCase(category) && book.isDisponivel()) {
                System.out.println(book.toString());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Nenhum livro disponível encontrado nesta categoria.");
        }
    }

    public List<String> getTodasAsCategoriasDisponiveisDistintas() {
        Set<String> categorias = new HashSet<>();
        if (this.todosOsLivros != null) {
            for (Book book : this.todosOsLivros) {
                if (book.isDisponivel() && book.getCategory() != null && !book.getCategory().isEmpty()) {
                    categorias.add(book.getCategory());
                }
            }
        }
        List<String> listaOrdenada = new ArrayList<>(categorias);
        Collections.sort(listaOrdenada);
        return listaOrdenada;
    }

    public boolean realizarMultiplasDevolucoesGUI(List<Integer> idsItensEmprestimo, List<Integer> idsLivros) {
        if (idsItensEmprestimo == null || idsLivros == null || idsItensEmprestimo.size() != idsLivros.size() || idsItensEmprestimo.isEmpty()) {
            return false;
        }

        boolean sucessoDB = dbManager.registrarMultiplasDevolucoes(idsItensEmprestimo, idsLivros);

        if (sucessoDB) {
            for (int idLivro : idsLivros) {
                for (Book book : todosOsLivros) {
                    if (book.getIdLivro() == idLivro) {
                        book.devolverCopia();
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public List<Book> pesquisarLivrosPorTituloGUI(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return getTodosOsLivros();
        }
        return dbManager.buscarLivrosPorTituloComLike(termoBusca.trim());
    }

    public List<Book> pesquisarLivrosPorAutorGUI(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return getTodosOsLivros();
        }
        return dbManager.buscarLivrosPorAutorComLike(termoBusca.trim());
    }
}