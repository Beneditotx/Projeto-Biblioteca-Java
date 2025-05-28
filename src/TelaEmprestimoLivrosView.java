import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelaEmprestimoLivrosView extends JDialog {

    private JTable tabelaLivros;
    private DefaultTableModel tableModel;
    private JButton btnEmprestar;
    private JButton btnVoltar;
    private JComboBox<String> cmbCategorias;
    private JLabel lblMensagem;
    private JTextField txtCampoBusca;
    private JButton btnBuscarTitulo;
    private JButton btnBuscarAutor;

    private BooksManager booksManager;
    private User usuarioLogado;

    private static final int ID_COLUMN_INDEX = 0;

    public TelaEmprestimoLivrosView(JFrame parent, BooksManager booksManager, User usuarioLogado) {
        super(parent, "Emprestar Livros", true);
        this.booksManager = booksManager;
        this.usuarioLogado = usuarioLogado;

        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 5));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel panelFiltrosCategoria = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltrosCategoria.add(new JLabel("Filtrar por Categoria:"));
        cmbCategorias = new JComboBox<>();
        panelFiltrosCategoria.add(cmbCategorias);

        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusca.add(new JLabel("Buscar por:"));
        txtCampoBusca = new JTextField(20);
        panelBusca.add(txtCampoBusca);
        btnBuscarTitulo = new JButton("Título");
        panelBusca.add(btnBuscarTitulo);
        btnBuscarAutor = new JButton("Autor");
        panelBusca.add(btnBuscarAutor);

        panelSuperior.add(panelFiltrosCategoria, BorderLayout.NORTH);
        panelSuperior.add(panelBusca, BorderLayout.CENTER);

        JPanel panelTabela = new JPanel(new BorderLayout());
        String[] colunasModelo = {"ID_Interno", "Título", "Autor", "Editora", "Categoria", "Disponíveis"};
        tableModel = new DefaultTableModel(colunasModelo, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaLivros = new JTable(tableModel);
        tabelaLivros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        TableColumn idColumn = tabelaLivros.getColumnModel().getColumn(ID_COLUMN_INDEX);
        idColumn.setMinWidth(0); idColumn.setMaxWidth(0); idColumn.setPreferredWidth(0); idColumn.setResizable(false);
        tabelaLivros.getColumnModel().getColumn(1).setPreferredWidth(250);
        tabelaLivros.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabelaLivros.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaLivros.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabelaLivros.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(tabelaLivros);
        panelTabela.add(scrollPane, BorderLayout.CENTER);
        panelTabela.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel panelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnEmprestar = new JButton("Emprestar Selecionado(s)");
        btnVoltar = new JButton("Voltar");
        lblMensagem = new JLabel(" ");
        lblMensagem.setPreferredSize(new Dimension(450, 25));

        panelAcoes.add(btnEmprestar);
        panelAcoes.add(btnVoltar);
        panelAcoes.add(lblMensagem);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabela, BorderLayout.CENTER);
        add(panelAcoes, BorderLayout.SOUTH);

        popularCategorias();

        cmbCategorias.addActionListener(e -> carregarEExibirLivros(txtCampoBusca.getText().trim(), (String) cmbCategorias.getSelectedItem()));
        btnBuscarTitulo.addActionListener(e -> carregarEExibirLivros(txtCampoBusca.getText().trim(), "titulo"));
        btnBuscarAutor.addActionListener(e -> carregarEExibirLivros(txtCampoBusca.getText().trim(), "autor"));
        btnVoltar.addActionListener(e -> dispose());

        btnEmprestar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = tabelaLivros.getSelectedRows();
                if (selectedRows.length == 0) {
                    lblMensagem.setForeground(Color.RED);
                    lblMensagem.setText("Por favor, selecione um ou mais livros para emprestar.");
                    return;
                }

                List<Integer> idsLivrosEscolhidos = new ArrayList<>();
                List<String> titulosLivrosEscolhidos = new ArrayList<>();
                boolean algumIndisponivelNaSelecao = false;

                for (int selectedRow : selectedRows) {
                    int idLivro = (int) tableModel.getValueAt(selectedRow, ID_COLUMN_INDEX);
                    String titulo = (String) tableModel.getValueAt(selectedRow, 1);
                    int qtdDisponivel = (int) tableModel.getValueAt(selectedRow, 5);

                    if (qtdDisponivel <= 0) {
                        algumIndisponivelNaSelecao = true;
                        break;
                    }
                    idsLivrosEscolhidos.add(idLivro);
                    titulosLivrosEscolhidos.add(titulo);
                }

                if (algumIndisponivelNaSelecao) {
                    lblMensagem.setForeground(Color.RED);
                    lblMensagem.setText("Um ou mais livros selecionados não estão disponíveis. Atualize a lista.");
                    carregarEExibirLivros(txtCampoBusca.getText().trim(), (String) cmbCategorias.getSelectedItem());
                    return;
                }

                String livrosParaConfirmacao = String.join(", ", titulosLivrosEscolhidos);
                int confirm = JOptionPane.showConfirmDialog(TelaEmprestimoLivrosView.this,
                        "Emprestar o(s) livro(s): " + livrosParaConfirmacao + "?",
                        "Confirmar Empréstimo", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean sucesso = booksManager.realizarMultiplosEmprestimosGUI(usuarioLogado, idsLivrosEscolhidos);
                    if (sucesso) {
                        lblMensagem.setForeground(Color.GREEN);
                        String dataDevolucaoPrevista = LocalDate.now().plusDays(14).format(BooksManager.DATE_FORMATTER);
                        lblMensagem.setText(idsLivrosEscolhidos.size() + " livro(s) emprestado(s)! Devolver até: " + dataDevolucaoPrevista);
                        carregarEExibirLivros(txtCampoBusca.getText().trim(), (String) cmbCategorias.getSelectedItem());
                    } else {
                        lblMensagem.setForeground(Color.RED);
                        lblMensagem.setText("Falha ao emprestar. Verifique disponibilidade ou logs.");
                        carregarEExibirLivros(txtCampoBusca.getText().trim(), (String) cmbCategorias.getSelectedItem());
                    }
                }
            }
        });
        carregarEExibirLivros(null, null); // Carga inicial
    }

    private void popularCategorias() {
        cmbCategorias.removeAllItems();
        cmbCategorias.addItem("Todas as Categorias");
        List<String> categoriasDoManager = booksManager.getTodasAsCategoriasDisponiveisDistintas();
        for (String categoria : categoriasDoManager) {
            cmbCategorias.addItem(categoria);
        }
    }

    private void carregarEExibirLivros(String termoBusca, String filtro) {
        tableModel.setRowCount(0);
        lblMensagem.setText(" ");

        List<BooksManager.Book> livrosFonte;

        if (termoBusca != null && !termoBusca.isEmpty()) {
            if ("titulo".equals(filtro)) {
                livrosFonte = booksManager.pesquisarLivrosPorTituloGUI(termoBusca);
            } else if ("autor".equals(filtro)) {
                livrosFonte = booksManager.pesquisarLivrosPorAutorGUI(termoBusca);
            } else {
                booksManager.carregarLivrosDoBanco();
                livrosFonte = booksManager.getTodosOsLivros();
            }
        } else {
            booksManager.carregarLivrosDoBanco();
            livrosFonte = booksManager.getTodosOsLivros();
        }

        String categoriaSelecionadaNoCombo = (String) cmbCategorias.getSelectedItem();
        if (termoBusca != null && !termoBusca.isEmpty() && ("titulo".equals(filtro) || "autor".equals(filtro))) {

        } else if (categoriaSelecionadaNoCombo != null && !"Todas as Categorias".equals(categoriaSelecionadaNoCombo)) {
            livrosFonte = livrosFonte.stream()
                    .filter(book -> categoriaSelecionadaNoCombo.equals(book.getCategory()))
                    .collect(Collectors.toList());
        }

        List<BooksManager.Book> livrosParaExibir = livrosFonte.stream()
                .filter(BooksManager.Book::isDisponivel)
                .collect(Collectors.toList());

        if (livrosParaExibir.isEmpty()) {
            lblMensagem.setForeground(Color.BLACK);
            if (termoBusca != null && !termoBusca.isEmpty()) {
                lblMensagem.setText("Nenhum livro encontrado para: '" + termoBusca + "'.");
            } else if (categoriaSelecionadaNoCombo != null && !"Todas as Categorias".equals(categoriaSelecionadaNoCombo)) {
                lblMensagem.setText("Nenhum livro disponível na categoria: " + categoriaSelecionadaNoCombo);
            } else {
                lblMensagem.setText("Nenhum livro disponível no momento.");
            }
        } else {
            for (BooksManager.Book livro : livrosParaExibir) {
                tableModel.addRow(new Object[]{
                        livro.getIdLivro(),
                        livro.getTitle(),
                        livro.getAuthor(),
                        livro.getEditora(),
                        livro.getCategory(),
                        livro.getQuantidadeDisponivel()
                });
            }
        }
    }
}