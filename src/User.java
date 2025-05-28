import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {
    private int idLeitor;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String phone;
    private String address;
    private String cpf;
    private int age;
    private Map<String, BookLoanInfo> borrowedBooks;

    public User(int idLeitor, String username, String password, String firstName, String lastName,
                String email, String birthDate, String phone, String address,
                String cpf, int age) {
        this.idLeitor = idLeitor;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.cpf = cpf;
        this.age = age;
        this.borrowedBooks = new HashMap<>();
    }
    public User(String username, String password, String firstName, String lastName,
                String email, String birthDate, String phone, String address,
                String cpf, int age) {
        this.idLeitor = 0;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.cpf = cpf;
        this.age = age;
        this.borrowedBooks = new HashMap<>();
    }

    public int getIdLeitor() {
        return idLeitor;
    }

    public static class BookLoanInfo {
        private String title;
        private LocalDate checkoutDate;
        private LocalDate dueDate;

        public BookLoanInfo(String title, LocalDate checkoutDate, LocalDate dueDate) {
            this.title = title;
            this.checkoutDate = checkoutDate;
            this.dueDate = dueDate;
        }

        public String getTitle() {
            return title;
        }

        public LocalDate getCheckoutDate() {
            return checkoutDate;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return "Título: '" + title + '\'' +
                    ", Retirada: " + (checkoutDate != null ? checkoutDate.format(formatter) : "N/A") +
                    ", Devolução Prevista: " + (dueDate != null ? dueDate.format(formatter) : "N/A");
        }
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCpf() { return cpf; }
    public int getAge() { return age; }

    public String getNomeCompleto() {
        return firstName + " " + lastName;
    }

    public void setPassword(String password) { this.password = password; }

    public void borrowBook(String code, String title, LocalDate checkoutDate, LocalDate dueDate) {
        borrowedBooks.put(code, new BookLoanInfo(title, checkoutDate, dueDate));
    }

    public boolean returnBook(String code) {
        return borrowedBooks.remove(code) != null;
    }

    public Map<String, BookLoanInfo> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void listBorrowedBooks() {
        if (borrowedBooks.isEmpty()) {
            System.out.println("Você não tem livros emprestados (segundo o registro em memória).");
            return;
        }

        System.out.println("\n--- Seus Livros Emprestados (Registro em Memória) ---");
        borrowedBooks.forEach((code, loanInfo) -> {
            System.out.println("Código/ID do Livro: " + code + " | " + loanInfo.toString());
        });
        System.out.println("----------------------------------------------------");
    }
}