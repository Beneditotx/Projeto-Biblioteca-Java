import javax.swing.*;
import java.awt.*;

public class TelaPrincipalUsuarioView extends JFrame {

    private User usuarioLogado;
    private BooksManager booksManager;
    private LoginManager loginManager;

    public TelaPrincipalUsuarioView(User user, BooksManager booksManager, LoginManager loginManager) {
        this.usuarioLogado = user;
        this.booksManager = booksManager;
        this.loginManager = loginManager;

        setTitle("Biblioteca - Bem-vindo " + usuarioLogado.getNomeCompleto());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel lblBemVindo = new JLabel("Bem-vindo, " + usuarioLogado.getNomeCompleto() + "!", SwingConstants.CENTER);
        lblBemVindo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblBemVindo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblBemVindo, BorderLayout.NORTH);

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnVerLivrosEmprestar = new JButton("Ver Livros / Emprestar");
        JButton btnDevolverLivros = new JButton("Devolver Livros");
        JButton btnPerfil = new JButton("Meu Perfil");
        JButton btnSair = new JButton("Sair (Logout)");

        Dimension buttonSize = new Dimension(200, 40);
        btnVerLivrosEmprestar.setPreferredSize(buttonSize);
        btnDevolverLivros.setPreferredSize(buttonSize);
        btnPerfil.setPreferredSize(buttonSize);
        btnSair.setPreferredSize(buttonSize);

        panelBotoes.add(btnVerLivrosEmprestar);
        panelBotoes.add(btnDevolverLivros);
        panelBotoes.add(btnPerfil);
        panelBotoes.add(btnSair);

        add(panelBotoes, BorderLayout.CENTER);

        btnVerLivrosEmprestar.addActionListener(e -> {

            TelaEmprestimoLivrosView telaEmprestimo = new TelaEmprestimoLivrosView(
                    this,
                    booksManager,
                    usuarioLogado
            );
            telaEmprestimo.setVisible(true);
        });


        btnDevolverLivros.addActionListener(e -> {

            TelaDevolucaoLivrosView telaDevolucao = new TelaDevolucaoLivrosView(
                    this,
                    booksManager,
                    usuarioLogado
            );
            telaDevolucao.setVisible(true);
        });

        btnPerfil.addActionListener(e -> {
            StringBuilder perfilInfo = new StringBuilder();
            perfilInfo.append("----- Seus Dados Cadastrais -----\n\n");
            perfilInfo.append("Nome Completo: ").append(usuarioLogado.getNomeCompleto()).append("\n");
            perfilInfo.append("Email: ").append(usuarioLogado.getEmail()).append("\n");
            perfilInfo.append("Data de Nascimento: ").append(usuarioLogado.getBirthDate()).append("\n");
            perfilInfo.append("Idade: ").append(usuarioLogado.getAge()).append(" anos\n");
            perfilInfo.append("CPF: ").append(usuarioLogado.getCpf()).append("\n");
            perfilInfo.append("Endereço: ").append(usuarioLogado.getAddress()).append("\n");
            perfilInfo.append("Telefone: ").append(usuarioLogado.getPhone()).append("\n");
            JOptionPane.showMessageDialog(this,
                    perfilInfo.toString(),
                    "Meu Perfil",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnSair.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja sair da sua conta?",
                    "Confirmar Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLoginView().setVisible(true);
                System.out.println("Usuário deslogado.");
            }
        });
    }
}