import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmprestimoDetalhe {
    private int idItemEmprestimo;
    private int idLivro;
    private String tituloLivro;
    private LocalDate dataDevolucaoPrevista;

    public EmprestimoDetalhe(int idItemEmprestimo, int idLivro, String tituloLivro, LocalDate dataDevolucaoPrevista) {
        this.idItemEmprestimo = idItemEmprestimo;
        this.idLivro = idLivro;
        this.tituloLivro = tituloLivro;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }

    public int getIdItemEmprestimo() {
        return idItemEmprestimo;
    }

    public int getIdLivro() {
        return idLivro;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "Item ID: " + idItemEmprestimo + " | Livro ID: " + idLivro +
                " | Título: '" + tituloLivro + '\'' +
                " | Devolver até: " + dataDevolucaoPrevista.format(formatter);
    }
}