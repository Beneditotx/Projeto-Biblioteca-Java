import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class TelaRecuperarSenhaView extends JDialog {

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarNovaSenha;
    private JButton btnRedefinir;
    private JLabel lblMensagem;

    private LoginManager loginManager;

    public TelaRecuperarSenhaView(JFrame parent, LoginManager loginManager) {
        super(parent, "Recuperar Senha", true);
        this.loginManager = loginManager;

        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        txtUsername = new JTextField(20);
        add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email cadastrado:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Nova Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        txtNovaSenha = new JPasswordField(20);
        add(txtNovaSenha, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirmar Nova Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        txtConfirmarNovaSenha = new JPasswordField(20);
        add(txtConfirmarNovaSenha, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        btnRedefinir = new JButton("Redefinir Senha");
        add(btnRedefinir, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        lblMensagem = new JLabel(" ");
        lblMensagem.setForeground(Color.RED);
        add(lblMensagem, gbc);

        btnRedefinir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String email = txtEmail.getText().trim();
                char[] novaSenhaChars = txtNovaSenha.getPassword();
                char[] confirmarSenhaChars = txtConfirmarNovaSenha.getPassword();

                String novaSenha = new String(novaSenhaChars);
                String confirmarSenha = new String(confirmarSenhaChars);

                Arrays.fill(novaSenhaChars, ' ');
                Arrays.fill(confirmarSenhaChars, ' ');
                txtNovaSenha.setText("");
                txtConfirmarNovaSenha.setText("");

                if (username.isEmpty() || email.isEmpty() || novaSenha.isEmpty()) {
                    lblMensagem.setText("Todos os campos são obrigatórios.");
                    return;
                }
                if (!novaSenha.equals(confirmarSenha)) {
                    lblMensagem.setText("As novas senhas não coincidem.");
                    return;
                }
                if (novaSenha.length() < 6) {
                    lblMensagem.setText("Nova senha muito curta (mínimo 6 caracteres).");
                    return;
                }

                String resultado = loginManager.processarRedefinicaoSenhaGUI(username, email, novaSenha);

                if (resultado.equals("SUCESSO")) {

                    JOptionPane.showMessageDialog(TelaRecuperarSenhaView.this,
                            "Senha redefinida com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    lblMensagem.setForeground(Color.RED);
                    lblMensagem.setText(resultado);
                }
            }
        });
    }
}