import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class TelaRegistroView extends JDialog {

    private JTextField txtFullName, txtPhone, txtEmail, txtAddress, txtBirthDate, txtCpf, txtUsername;
    private JPasswordField pwdPassword, pwdConfirmPassword;
    private JButton btnRegistrar, btnCancelar;
    private JLabel lblMensagem;

    private RegisterManager registerManager;

    public TelaRegistroView(JFrame parent) {
        super(parent, "Registrar Novo Usuário", true);
        this.registerManager = new RegisterManager();

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtFullName = new JTextField(25);
        add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtPhone = new JTextField(25);
        add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email (@gmail.com):"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtEmail = new JTextField(25);
        add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtAddress = new JTextField(25);
        add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Data Nasc. (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtBirthDate = new JTextField(25);
        add(txtBirthDate, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("CPF (só números):"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtCpf = new JTextField(25);
        add(txtCpf, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        txtUsername = new JTextField(25);
        add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Senha (mín. 6 caracteres):"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        pwdPassword = new JPasswordField(25);
        add(pwdPassword, gbc);

        gbc.gridx = 0; gbc.gridy = linha; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirmar Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = linha++; gbc.anchor = GridBagConstraints.WEST;
        pwdConfirmPassword = new JPasswordField(25);
        add(pwdConfirmPassword, gbc);

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRegistrar = new JButton("Registrar");
        btnCancelar = new JButton("Cancelar");
        panelBotoes.add(btnRegistrar);
        panelBotoes.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = linha++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(panelBotoes, gbc);

        lblMensagem = new JLabel(" ", SwingConstants.CENTER);
        lblMensagem.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = linha; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(lblMensagem, gbc);

        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fullName = txtFullName.getText().trim();
                String phone = txtPhone.getText().trim();
                String email = txtEmail.getText().trim();
                String address = txtAddress.getText().trim();
                String birthDateStr = txtBirthDate.getText().trim();
                String cpf = txtCpf.getText().trim();
                String username = txtUsername.getText().trim();
                char[] passChars = pwdPassword.getPassword();
                char[] confirmPassChars = pwdConfirmPassword.getPassword();

                String plainPassword = new String(passChars);
                String confirmPlainPassword = new String(confirmPassChars);

                Arrays.fill(passChars, ' ');
                Arrays.fill(confirmPassChars, ' ');
                pwdPassword.setText("");
                pwdConfirmPassword.setText("");

                if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty() ||
                        birthDateStr.isEmpty() || cpf.isEmpty() || username.isEmpty() || plainPassword.isEmpty()) {
                    lblMensagem.setText("Todos os campos são obrigatórios!");
                    return;
                }
                if (!plainPassword.equals(confirmPlainPassword)) {
                    lblMensagem.setText("As senhas não coincidem!");
                    return;
                }
                if (plainPassword.length() < 6) {
                    lblMensagem.setText("A senha deve ter no mínimo 6 caracteres.");
                    return;
                }

                String resultado = registerManager.processarRegistroGUI(fullName, phone, email, address,
                        birthDateStr, cpf, username, plainPassword);

                if (resultado.equals("SUCESSO")) {
                    JOptionPane.showMessageDialog(TelaRegistroView.this,
                            "Usuário registrado com sucesso!",
                            "Registro Bem-sucedido",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    lblMensagem.setText(resultado);
                }
            }
        });

        btnCancelar.addActionListener(e -> dispose());
    }
}