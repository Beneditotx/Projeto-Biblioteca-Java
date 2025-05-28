import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class TelaLoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistrar;
    private JButton btnRecuperarSenha;
    private JButton btnSair;
    private JLabel lblMensagem;

    private LoginManager loginManager;
    private BooksManager booksManager;

    public TelaLoginView() {
        this.booksManager = new BooksManager();
        this.loginManager = new LoginManager(this.booksManager);

        setTitle("Login - Sistema de Biblioteca");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Usuário:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        txtUsername = new JTextField(15);
        add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtPassword = new JPasswordField(15);
        add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnLogin = new JButton("Login");
        add(btnLogin, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        btnRegistrar = new JButton("Registrar Novo Usuário");
        add(btnRegistrar, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        btnRecuperarSenha = new JButton("Recuperar Senha");
        add(btnRecuperarSenha, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        btnSair = new JButton("Sair do Programa");
        add(btnSair, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        lblMensagem = new JLabel(" ", SwingConstants.CENTER);
        lblMensagem.setForeground(Color.RED);
        add(lblMensagem, gbc);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                char[] passwordChars = txtPassword.getPassword();
                String password = new String(passwordChars);
                Arrays.fill(passwordChars, ' ');
                txtPassword.setText("");

                if (username.isEmpty() || password.isEmpty()){
                    lblMensagem.setText("Usuário e senha são obrigatórios.");
                    lblMensagem.setForeground(Color.RED);
                    return;
                }

                if (loginManager.authenticateLogin(username, password)) {
                    User usuarioLogado = loginManager.getAuthenticatedUser(username);
                    if (usuarioLogado != null) {
                        dispose();
                        new TelaPrincipalUsuarioView(usuarioLogado, booksManager, loginManager).setVisible(true);
                    } else {
                        lblMensagem.setText("Erro ao carregar dados do usuário.");
                        lblMensagem.setForeground(Color.RED);
                    }
                } else {
                    lblMensagem.setText("Usuário ou senha inválidos.");
                    lblMensagem.setForeground(Color.RED);
                }
            }
        });

        btnRegistrar.addActionListener(e -> abrirTelaRegistro());

        btnRecuperarSenha.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaRecuperarSenhaView(TelaLoginView.this, loginManager).setVisible(true);
            }
        });

        btnSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(TelaLoginView.this,
                        "Tem certeza que deseja sair do programa?",
                        "Confirmar Saída",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private void abrirTelaRegistro() {

        TelaRegistroView telaRegistro = new TelaRegistroView(this);
        telaRegistro.setVisible(true);
    }
}