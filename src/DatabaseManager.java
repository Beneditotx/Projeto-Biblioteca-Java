import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

public class DatabaseManager {

    public static Connection getConnection() throws SQLException {
        Map<String, String> env = EnvLoader.loadEnv(".env");

        String url = env.get("DB_URL");
        String usuario = env.get("DB_USER");
        String senha = env.get("DB_PASSWORD");

        if (url == null || url.isEmpty() ||
                usuario == null || usuario.isEmpty() ||
                senha == null) {
            throw new SQLException("Configurações do banco de dados (DB_URL, DB_USER, DB_PASSWORD) não encontradas ou incompletas no arquivo .env. Verifique o arquivo .env na raiz do projeto.");
        }
        return DriverManager.getConnection(url, usuario, senha);
    }

    public boolean registrarLeitor(User user) {

        String sql = "INSERT INTO Leitores (NomeCompleto, Username, Senha_Hash, E_mail, Telefone, Endereco, Data_Nascimento, CPF) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getNomeCompleto());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getAddress());
            if (user.getBirthDate() != null && !user.getBirthDate().isEmpty()) {
                try {
                    LocalDate localDate = LocalDate.parse(user.getBirthDate(),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    pstmt.setDate(7, Date.valueOf(localDate));
                } catch (java.time.format.DateTimeParseException e) {
                    System.err.println("Formato de data de nascimento inválido ao registrar: " + user.getBirthDate() + " - " + e.getMessage());
                    pstmt.setNull(7, java.sql.Types.DATE);
                }
            } else {
                pstmt.setNull(7, java.sql.Types.DATE);
            }
            pstmt.setString(8, user.getCpf());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar leitor: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("CPF")) {
                System.err.println("Detalhe: Tentativa de inserir CPF duplicado.");
            } else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("Username")) {
                System.err.println("Detalhe: Tentativa de inserir Username duplicado.");
            } else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("E_mail")) {
                System.err.println("Detalhe: Tentativa de inserir E_mail duplicado.");
            }
            return false;
        }
    }

    public String buscarSenhaHashPorUsername(String username) {
        String sql = "SELECT Senha_Hash FROM Leitores WHERE Username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Senha_Hash");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar senha do usuário '" + username + "': " + e.getMessage());
        }
        return null;
    }

    public boolean atualizarSenha(String username, String newHashedPassword) {
        String sql = "UPDATE Leitores SET Senha_Hash = ? WHERE Username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar senha para o usuário '" + username + "': " + e.getMessage());
            return false;
        }
    }

    public boolean usernameJaExiste(String username) {
        String sql = "SELECT COUNT(*) AS total FROM Leitores WHERE Username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar username '" + username + "': " + e.getMessage());
        }
        return false;
    }

    public User buscarUsuarioCompletoPorUsername(String username) {
        // CORREÇÃO: 'Email' mudado para 'E_mail' na query e no rs.getString()
        String sql = "SELECT ID_LEITOR, NomeCompleto, Username, Senha_Hash, E_mail, Telefone, Endereco, Data_Nascimento, CPF " +
                "FROM Leitores WHERE Username = ?";
        User user = null;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int idLeitor = rs.getInt("ID_LEITOR");
                    String hashedPassword = rs.getString("Senha_Hash");
                    String nomeCompleto = rs.getString("NomeCompleto");
                    String[] nameParts = nomeCompleto != null ? nomeCompleto.split(" ", 2) : new String[]{""};
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";

                    String email = rs.getString("E_mail"); // CORRIGIDO
                    Date sqlBirthDate = rs.getDate("Data_Nascimento");
                    String birthDateStr = "";
                    int age = 0;
                    if (sqlBirthDate != null) {
                        LocalDate localBirthDate = sqlBirthDate.toLocalDate();
                        birthDateStr = localBirthDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        age = Period.between(localBirthDate, LocalDate.now()).getYears();
                    }
                    String phone = rs.getString("Telefone");
                    String address = rs.getString("Endereco");
                    String cpf = rs.getString("CPF");

                    user = new User(idLeitor, username, hashedPassword, firstName, lastName, email, birthDateStr, phone, address, cpf, age);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário completo '" + username + "': " + e.getMessage());
        }
        return user;
    }

    public List<BooksManager.Book> buscarTodosOsLivros() {
        List<BooksManager.Book> livros = new ArrayList<>();

        String sql = "SELECT l.ID_LIVRO, l.Titulo, l.Autor, e.Nome AS Editora, c.Nome AS Categoria, " +
                "l.QuantidadeTotal, l.QuantidadeDisponivel " +
                "FROM Livros l " +
                "JOIN Editoras e ON l.ID_EDITORA = e.ID_EDITORA " +
                "JOIN Categorias c ON l.ID_CATEGORIA = c.ID_CATEGORIA";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int idLivro = rs.getInt("ID_LIVRO");
                String titulo = rs.getString("Titulo");
                String autor = rs.getString("Autor");
                String editora = rs.getString("Editora");
                String categoria = rs.getString("Categoria");
                int quantidadeTotal = rs.getInt("QuantidadeTotal");
                int quantidadeDisponivel = rs.getInt("QuantidadeDisponivel");
                BooksManager.Book livro = new BooksManager.Book(idLivro, titulo, autor, editora, categoria, quantidadeTotal, quantidadeDisponivel);
                livros.add(livro);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar livros do banco de dados: " + e.getMessage());
        }
        return livros;
    }

    public boolean registrarEmprestimo(int idLeitor, int idLivro) {
        String sqlCheckAvailability = "SELECT QuantidadeDisponivel FROM Livros WHERE ID_LIVRO = ? FOR UPDATE";
        String sqlInsertEmprestimo = "INSERT INTO Emprestimos (ID_LEITOR, Data_Devolucao) VALUES (?, ?)";
        String sqlInsertItemEmprestimo = "INSERT INTO Itens_Emprestimo (ID_EMPRESTIMO, ID_LIVRO) VALUES (?, ?)";
        String sqlUpdateLivro = "UPDATE Livros SET QuantidadeDisponivel = QuantidadeDisponivel - 1 WHERE ID_LIVRO = ? AND QuantidadeDisponivel > 0";
        Connection conn = null;
        long idEmprestimoGerado = -1;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckAvailability)) {
                pstmtCheck.setInt(1, idLivro);
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("QuantidadeDisponivel") <= 0) {
                            conn.rollback();
                            System.err.println("Livro com ID " + idLivro + " não está disponível para empréstimo.");
                            return false;
                        }
                    } else {
                        conn.rollback();
                        System.err.println("Livro com ID " + idLivro + " não encontrado.");
                        return false;
                    }
                }
            }
            try (PreparedStatement pstmtEmprestimo = conn.prepareStatement(sqlInsertEmprestimo, Statement.RETURN_GENERATED_KEYS)) {
                pstmtEmprestimo.setInt(1, idLeitor);
                LocalDate dataDevolucao = LocalDate.now().plusDays(14);
                pstmtEmprestimo.setDate(2, Date.valueOf(dataDevolucao));
                pstmtEmprestimo.executeUpdate();
                try (ResultSet generatedKeys = pstmtEmprestimo.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idEmprestimoGerado = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Falha ao obter o ID do empréstimo gerado.");
                    }
                }
            }
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlInsertItemEmprestimo)) {
                pstmtItem.setLong(1, idEmprestimoGerado);
                pstmtItem.setInt(2, idLivro);
                pstmtItem.executeUpdate();
            }
            try (PreparedStatement pstmtUpdateLivro = conn.prepareStatement(sqlUpdateLivro)) {
                pstmtUpdateLivro.setInt(1, idLivro);
                int rowsAffected = pstmtUpdateLivro.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    System.err.println("Não foi possível decrementar a quantidade do livro ID " + idLivro + ". A quantidade pode ter sido alterada por outra transação ou o livro não tem estoque.");
                    return false;
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar empréstimo: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }

    public List<EmprestimoDetalhe> buscarEmprestimosAtivosPorUsuario(int idLeitor) {
        List<EmprestimoDetalhe> emprestimosAtivos = new ArrayList<>();

        String sql = "SELECT ie.ID_ITEM, ie.ID_LIVRO, l.Titulo, e.Data_Devolucao " +
                "FROM Itens_Emprestimo ie " +
                "JOIN Emprestimos e ON ie.ID_EMPRESTIMO = e.ID_EMPRESTIMO " +
                "JOIN Livros l ON ie.ID_LIVRO = l.ID_LIVRO " +
                "WHERE e.ID_LEITOR = ? AND ie.Devolvido = FALSE";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLeitor);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idItem = rs.getInt("ID_ITEM");
                    int idLivro = rs.getInt("ID_LIVRO");
                    String titulo = rs.getString("Titulo");
                    LocalDate dataDevolucao = rs.getDate("Data_Devolucao").toLocalDate();
                    emprestimosAtivos.add(new EmprestimoDetalhe(idItem, idLivro, titulo, dataDevolucao));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar empréstimos ativos para o leitor ID " + idLeitor + ": " + e.getMessage());
        }
        return emprestimosAtivos;
    }

    public boolean registrarDevolucao(int idItemEmprestimo, int idLivro) {
        String sqlUpdateItem = "UPDATE Itens_Emprestimo SET Devolvido = TRUE, Data_Devolucao = CURRENT_TIMESTAMP WHERE ID_ITEM = ? AND Devolvido = FALSE";
        String sqlUpdateLivro = "UPDATE Livros SET QuantidadeDisponivel = QuantidadeDisponivel + 1 WHERE ID_LIVRO = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlUpdateItem)) {
                pstmtItem.setInt(1, idItemEmprestimo);
                int itemRowsAffected = pstmtItem.executeUpdate();
                if (itemRowsAffected == 0) {
                    conn.rollback();
                    System.err.println("Item de empréstimo com ID " + idItemEmprestimo + " não encontrado ou já devolvido.");
                    return false;
                }
            }
            try (PreparedStatement pstmtLivro = conn.prepareStatement(sqlUpdateLivro)) {
                pstmtLivro.setInt(1, idLivro);

                int livroRowsAffected = pstmtLivro.executeUpdate();
                if (livroRowsAffected == 0) {
                    conn.rollback();
                    System.err.println("Não foi possível atualizar a quantidade do livro ID " + idLivro + ". Livro não encontrado?");
                    return false;
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar devolução para o item ID " + idItemEmprestimo + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter devolução: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após devolução: " + e.getMessage());
                }
            }
        }
    }
    public boolean registrarMultiplosEmprestimos(int idLeitor, List<Integer> idsLivros) {
        if (idsLivros == null || idsLivros.isEmpty()) {
            System.err.println("Nenhum livro selecionado para empréstimo.");
            return false;
        }

        String sqlCheckAvailability = "SELECT QuantidadeDisponivel FROM Livros WHERE ID_LIVRO = ? FOR UPDATE";
        String sqlInsertEmprestimo = "INSERT INTO Emprestimos (ID_LEITOR, Data_Devolucao) VALUES (?, ?)";
        String sqlInsertItemEmprestimo = "INSERT INTO Itens_Emprestimo (ID_EMPRESTIMO, ID_LIVRO) VALUES (?, ?)";
        String sqlUpdateLivro = "UPDATE Livros SET QuantidadeDisponivel = QuantidadeDisponivel - 1 WHERE ID_LIVRO = ? AND QuantidadeDisponivel > 0";
        Connection conn = null;
        long idEmprestimoGerado = -1;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtEmprestimo = conn.prepareStatement(sqlInsertEmprestimo, Statement.RETURN_GENERATED_KEYS)) {
                pstmtEmprestimo.setInt(1, idLeitor);
                LocalDate dataDevolucao = LocalDate.now().plusDays(14); // CORRIGIDO
                pstmtEmprestimo.setDate(2, Date.valueOf(dataDevolucao));
                pstmtEmprestimo.executeUpdate();

                try (ResultSet generatedKeys = pstmtEmprestimo.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idEmprestimoGerado = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Falha ao obter o ID do empréstimo gerado.");
                    }
                }
            }

            for (int idLivro : idsLivros) {
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckAvailability)) {
                    pstmtCheck.setInt(1, idLivro);
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt("QuantidadeDisponivel") <= 0) {
                                conn.rollback();
                                System.err.println("Livro com ID " + idLivro + " não está disponível (quantidade esgotada durante a transação).");
                                return false;
                            }
                        } else {
                            conn.rollback();
                            System.err.println("Livro com ID " + idLivro + " não encontrado durante a transação.");
                            return false;
                        }
                    }
                }

                try (PreparedStatement pstmtItem = conn.prepareStatement(sqlInsertItemEmprestimo)) {
                    pstmtItem.setLong(1, idEmprestimoGerado);
                    pstmtItem.setInt(2, idLivro);
                    pstmtItem.executeUpdate();
                }

                try (PreparedStatement pstmtUpdateLivro = conn.prepareStatement(sqlUpdateLivro)) {
                    pstmtUpdateLivro.setInt(1, idLivro);
                    int rowsAffected = pstmtUpdateLivro.executeUpdate();
                    if (rowsAffected == 0) {
                        conn.rollback();
                        System.err.println("Não foi possível decrementar a quantidade do livro ID " + idLivro + " durante múltiplos empréstimos. Verifique a disponibilidade.");
                        return false;
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar múltiplos empréstimos: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro crítico ao reverter transação de múltiplos empréstimos: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após múltiplos empréstimos: " + e.getMessage());
                }
            }
        }
    }

    public boolean registrarMultiplasDevolucoes(List<Integer> idsItensEmprestimo, List<Integer> idsLivros) {
        if (idsItensEmprestimo == null || idsLivros == null || idsItensEmprestimo.size() != idsLivros.size() || idsItensEmprestimo.isEmpty()) {
            System.err.println("Listas de IDs inválidas ou incompatíveis para múltiplas devoluções.");
            return false;
        }

        String sqlUpdateItem = "UPDATE Itens_Emprestimo SET Devolvido = TRUE, Data_Devolucao = CURRENT_TIMESTAMP WHERE ID_ITEM = ? AND Devolvido = FALSE";
        String sqlUpdateLivro = "UPDATE Livros SET QuantidadeDisponivel = QuantidadeDisponivel + 1 WHERE ID_LIVRO = ?";
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (int i = 0; i < idsItensEmprestimo.size(); i++) {
                int idItem = idsItensEmprestimo.get(i);
                int idLivro = idsLivros.get(i);

                try (PreparedStatement pstmtItem = conn.prepareStatement(sqlUpdateItem)) {
                    pstmtItem.setInt(1, idItem);
                    int itemRowsAffected = pstmtItem.executeUpdate();
                    if (itemRowsAffected == 0) {
                        conn.rollback();
                        System.err.println("Nenhum item de empréstimo encontrado ou já devolvido para ID_ITEM: " + idItem + ". Transação revertida.");
                        return false;
                    }
                }

                try (PreparedStatement pstmtLivro = conn.prepareStatement(sqlUpdateLivro)) {
                    pstmtLivro.setInt(1, idLivro);
                    int livroRowsAffected = pstmtLivro.executeUpdate();
                    if (livroRowsAffected == 0) {
                        conn.rollback();
                        System.err.println("Não foi possível atualizar a quantidade do livro ID " + idLivro + " durante múltiplas devoluções. Livro não encontrado?");
                        return false;
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar múltiplas devoluções no banco de dados: " + e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Tentando reverter transação de múltiplas devoluções...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro crítico ao tentar reverter transação de múltiplas devoluções: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar auto-commit ou fechar conexão após múltiplas devoluções: " + e.getMessage());
                }
            }
        }
    }

    public List<BooksManager.Book> buscarLivrosPorTituloComLike(String termoBusca) {
        List<BooksManager.Book> livrosEncontrados = new ArrayList<>();

        String sql = "SELECT l.ID_LIVRO, l.Titulo, l.Autor, e.Nome AS Editora, c.Nome AS Categoria, " +
                "l.QuantidadeTotal, l.QuantidadeDisponivel " +
                "FROM Livros l " +
                "JOIN Editoras e ON l.ID_EDITORA = e.ID_EDITORA " +
                "JOIN Categorias c ON l.ID_CATEGORIA = c.ID_CATEGORIA " +
                "WHERE l.Titulo LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + termoBusca + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idLivro = rs.getInt("ID_LIVRO");
                String titulo = rs.getString("Titulo");
                String autor = rs.getString("Autor");
                String editora = rs.getString("Editora");
                String categoria = rs.getString("Categoria");
                int quantidadeTotal = rs.getInt("QuantidadeTotal");
                int quantidadeDisponivel = rs.getInt("QuantidadeDisponivel");

                livrosEncontrados.add(new BooksManager.Book(idLivro, titulo, autor, editora, categoria, quantidadeTotal, quantidadeDisponivel));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar livros por título com LIKE: " + e.getMessage());
        }
        return livrosEncontrados;
    }

    public List<BooksManager.Book> buscarLivrosPorAutorComLike(String termoBusca) {
        List<BooksManager.Book> livrosEncontrados = new ArrayList<>();

        String sql = "SELECT l.ID_LIVRO, l.Titulo, l.Autor, e.Nome AS Editora, c.Nome AS Categoria, " +
                "l.QuantidadeTotal, l.QuantidadeDisponivel " +
                "FROM Livros l " +
                "JOIN Editoras e ON l.ID_EDITORA = e.ID_EDITORA " +
                "JOIN Categorias c ON l.ID_CATEGORIA = c.ID_CATEGORIA " +
                "WHERE l.Autor LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + termoBusca + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idLivro = rs.getInt("ID_LIVRO");
                String titulo = rs.getString("Titulo");
                String autor = rs.getString("Autor");
                String editora = rs.getString("Editora");
                String categoria = rs.getString("Categoria");
                int quantidadeTotal = rs.getInt("QuantidadeTotal");
                int quantidadeDisponivel = rs.getInt("QuantidadeDisponivel");
                livrosEncontrados.add(new BooksManager.Book(idLivro, titulo, autor, editora, categoria, quantidadeTotal, quantidadeDisponivel));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar livros por autor com LIKE: " + e.getMessage());
        }
        return livrosEncontrados;
    }
}