import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TelaDevolucaoLivrosView extends JDialog {

    private JTable tabelaEmprestimos;
    private DefaultTableModel tableModel;
    private JButton btnDevolverSelecionados;
    private JButton btnAtualizarLista;
    private JButton btnVoltar;
    private JLabel lblMensagem;

    private BooksManager booksManager;
    private User usuarioLogado;

    private static final int ID_ITEM_EMPRESTIMO_COL_INDEX = 0;
    private static final int ID_LIVRO_COL_INDEX = 1;

    public TelaDevolucaoLivrosView(JFrame parent, BooksManager booksManager, User usuarioLogado) {
        super(parent, "Devolver Livros", true);
        this.booksManager = booksManager;
        this.usuarioLogado = usuarioLogado;

        setSize(750, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTabela = new JPanel(new BorderLayout());
        String[] colunasModelo = {"ID_Item_Interno", "ID_Livro_Interno", "Título do Livro", "Devolver Até"};
        tableModel = new DefaultTableModel(colunasModelo, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaEmprestimos = new JTable(tableModel);

        tabelaEmprestimos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        TableColumn idItemColumn = tabelaEmprestimos.getColumnModel().getColumn(ID_ITEM_EMPRESTIMO_COL_INDEX);
        idItemColumn.setMinWidth(0); idItemColumn.setMaxWidth(0); idItemColumn.setPreferredWidth(0); idItemColumn.setResizable(false);
        TableColumn idLivroColumn = tabelaEmprestimos.getColumnModel().getColumn(ID_LIVRO_COL_INDEX);
        idLivroColumn.setMinWidth(0); idLivroColumn.setMaxWidth(0); idLivroColumn.setPreferredWidth(0); idLivroColumn.setResizable(false);
        tabelaEmprestimos.getColumnModel().getColumn(2).setPreferredWidth(350);
        tabelaEmprestimos.getColumnModel().getColumn(3).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tabelaEmprestimos);
        panelTabela.add(scrollPane, BorderLayout.CENTER);
        panelTabela.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JPanel panelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAtualizarLista = new JButton("Atualizar Lista");
        btnDevolverSelecionados = new JButton("Devolver Selecionado(s)");
        btnVoltar = new JButton("Voltar");
        lblMensagem = new JLabel(" ");
        lblMensagem.setPreferredSize(new Dimension(400, 25));

        panelAcoes.add(btnAtualizarLista);
        panelAcoes.add(btnDevolverSelecionados);
        panelAcoes.add(btnVoltar);
        panelAcoes.add(lblMensagem);

        add(panelTabela, BorderLayout.CENTER);
        add(panelAcoes, BorderLayout.SOUTH);

        btnAtualizarLista.addActionListener(e -> carregarEExibirEmprestimos());
        btnVoltar.addActionListener(e -> dispose());

        btnDevolverSelecionados.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = tabelaEmprestimos.getSelectedRows();
                if (selectedRows.length == 0) {
                    lblMensagem.setForeground(Color.RED);
                    lblMensagem.setText("Selecione um ou mais itens da lista para devolver.");
                    return;
                }

                List<Integer> idsItensParaDevolver = new ArrayList<>();
                List<Integer> idsLivrosParaAtualizar = new ArrayList<>();
                StringBuilder titulosParaConfirmacao = new StringBuilder("Devolver o(s) livro(s):\n");

                for (int selectedRow : selectedRows) {
                    idsItensParaDevolver.add((int) tableModel.getValueAt(selectedRow, ID_ITEM_EMPRESTIMO_COL_INDEX));
                    idsLivrosParaAtualizar.add((int) tableModel.getValueAt(selectedRow, ID_LIVRO_COL_INDEX));
                    titulosParaConfirmacao.append("- ").append(tableModel.getValueAt(selectedRow, 2)).append("\n");
                }
                titulosParaConfirmacao.append("Confirmar devolução?");

                int confirm = JOptionPane.showConfirmDialog(TelaDevolucaoLivrosView.this,
                        titulosParaConfirmacao.toString(),
                        "Confirmar Devolução Múltipla", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean sucesso = booksManager.realizarMultiplasDevolucoesGUI(idsItensParaDevolver, idsLivrosParaAtualizar);
                    if (sucesso) {
                        lblMensagem.setForeground(Color.GREEN);
                        lblMensagem.setText(idsItensParaDevolver.size() + " livro(s) devolvido(s) com sucesso!");
                        carregarEExibirEmprestimos();
                        booksManager.carregarLivrosDoBanco();
                    } else {
                        lblMensagem.setForeground(Color.RED);
                        lblMensagem.setText("Falha ao devolver um ou mais livros. Verifique os logs.");
                        carregarEExibirEmprestimos();
                        booksManager.carregarLivrosDoBanco();
                    }
                }
            }
        });
        carregarEExibirEmprestimos();
    }

    private void carregarEExibirEmprestimos() {
        tableModel.setRowCount(0);
        lblMensagem.setText(" ");
        List<EmprestimoDetalhe> emprestimos = booksManager.getEmprestimosAtivosParaGUI(usuarioLogado);

        if (emprestimos.isEmpty()) {
            lblMensagem.setForeground(Color.BLACK);
            lblMensagem.setText("Você não possui livros emprestados no momento.");
        } else {
            for (EmprestimoDetalhe emprestimo : emprestimos) {
                tableModel.addRow(new Object[]{
                        emprestimo.getIdItemEmprestimo(),
                        emprestimo.getIdLivro(),
                        emprestimo.getTituloLivro(),
                        emprestimo.getDataDevolucaoPrevista().format(BooksManager.DATE_FORMATTER)
                });
            }
        }
    }
}