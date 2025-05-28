
public class LoginManager {

    private BooksManager booksManager;
    private DatabaseManager dbManager;

    public LoginManager(BooksManager booksManager) {
        this.booksManager = booksManager;
        this.dbManager = new DatabaseManager();
    }

    public boolean authenticateLogin(String username, String plainPasswordAttempt) {
        String storedHashedPassword = dbManager.buscarSenhaHashPorUsername(username);
        if (storedHashedPassword == null) {
            return false;
        }

        return PasswordManager.checkPassword(plainPasswordAttempt, storedHashedPassword);
    }

    public User getAuthenticatedUser(String username) {
        return dbManager.buscarUsuarioCompletoPorUsername(username);
    }

    public String processarRedefinicaoSenhaGUI(String username, String emailForVerification, String newPlainPassword) {
        User user = dbManager.buscarUsuarioCompletoPorUsername(username);

        if (user == null) {
            return "Usuário não encontrado!";
        }

        if (!user.getEmail().equals(emailForVerification)) {
            return "Email de verificação incorreto!";
        }

        if (newPlainPassword.length() < 6) {
            return "Nova senha muito curta (mínimo 6 caracteres).";
        }

        String newHashedPassword = PasswordManager.hashPassword(newPlainPassword);

        if (dbManager.atualizarSenha(username, newHashedPassword)) {
            return "SUCESSO";
        } else {
            return "Falha ao atualizar a senha no banco de dados.";
        }
    }
}