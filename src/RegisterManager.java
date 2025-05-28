import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class RegisterManager {
    private Scanner scanner;
    private DatabaseManager dbManager;

    public RegisterManager() {
        this.scanner = new Scanner(System.in);
        this.dbManager = new DatabaseManager();
    }

    private boolean isValidPhoneFormat(String phone) {
        return phone != null && phone.matches("^\\d{8,15}$");
    }

    private boolean isValidEmailFormat(String email) {
        return email != null && email.endsWith("@gmail.com");
    }

    private boolean isValidCpfFormat(String cpf) {
        return cpf != null && cpf.matches("^\\d{11}$");
    }

    public String processarRegistroGUI(String fullName, String phone, String email, String address,
                                       String birthDateStr, String cpf, String username, String plainPassword) {

        if (fullName == null || fullName.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                address == null || address.trim().isEmpty() ||
                birthDateStr == null || birthDateStr.trim().isEmpty() ||
                cpf == null || cpf.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                plainPassword == null || plainPassword.isEmpty()) {
            return "Todos os campos são obrigatórios.";
        }

        if (!isValidName(fullName)) {
            return "Nome inválido. Deve ter pelo menos 4 letras e não conter números ou caracteres especiais.";
        }
        if (!isValidPhoneFormat(phone)) {
            return "Telefone inválido. Deve conter apenas dígitos e ter entre 8 e 15 caracteres.";
        }
        if (!isValidEmailFormat(email)) {
            return "Email inválido. Apenas emails @gmail.com são aceitos.";
        }
        if (!isValidAddress(address)) {
            return "Endereço inválido. Deve ter pelo menos 5 caracteres e conter letras e números.";
        }
        if (!isValidCpfFormat(cpf)) {
            return "CPF inválido. Deve conter exatamente 11 dígitos.";
        }
        if (username.length() < 4) {
            return "Nome de usuário muito curto (mínimo 4 caracteres).";
        }
        if (plainPassword.length() < 6) {
            return "Senha muito curta. Deve ter pelo menos 6 caracteres.";
        }

        if (dbManager.usernameJaExiste(username)) {
            return "Nome de usuário já existe. Por favor, escolha outro.";
        }

        int age;
        try {
            LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 16 || age > 85) {
                return "Idade inválida. Usuário deve ter entre 16 e 85 anos.";
            }
        } catch (DateTimeParseException e) {
            return "Formato de data de nascimento inválido. Use dd/MM/yyyy.";
        }

        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String hashedPassword;

        try {
            hashedPassword = PasswordManager.hashPassword(plainPassword);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao tentar gerar o hash da senha: " + e.getMessage());
            return "Erro interno ao processar senha.";
        }

        User user = new User(username, hashedPassword, firstName, lastName, email, birthDateStr, phone, address, cpf, age);

        if (dbManager.registrarLeitor(user)) {
            return "SUCESSO";
        } else {
            return "Falha ao registrar usuário no banco de dados.";
        }
    }

    public void registerUser() {
        String fullName = getValidFullName();
        String phone = getValidPhone();
        String email = getValidEmail();
        String address = getValidAddress();
        String birthDateStr = getValidBirthDate();
        String cpf = getValidCPF();
        String username = getValidUsername();
        String plainPassword = getValidPassword();

        String resultado = processarRegistroGUI(fullName, phone, email, address, birthDateStr, cpf, username, plainPassword);

        if (resultado.equals("SUCESSO")) {
            System.out.println("\nUser successfully registered in the database!");
        } else {
            System.out.println("\n" + resultado);
        }
    }

    private String getValidFullName() {
        while (true) {
            System.out.print("Enter full name: ");
            String fullName = scanner.nextLine().trim();
            if (isValidName(fullName)) {
                return fullName;
            }
            System.out.println("Invalid name. It must have at least 4 letters and contain no numbers or special characters.");
        }
    }

    private String getValidPhone() {
        while (true) {
            System.out.print("\nEnter phone number: ");
            String phone = scanner.nextLine().trim();
            if (isValidPhoneFormat(phone)) {
                return phone;
            }
            System.out.println("Invalid phone number. It must contain only digits and be between 8 and 15 characters long.");
        }
    }

    private String getValidEmail() {
        while (true) {
            System.out.print("\nEnter email: ");
            String email = scanner.nextLine().trim();
            if (isValidEmailFormat(email)) {
                return email;
            }
            System.out.println("Only @gmail.com emails are accepted. Please try again.");
        }
    }

    private String getValidAddress() {
        while (true) {
            System.out.print("\nEnter address: ");
            String address = scanner.nextLine().trim();
            if (isValidAddress(address)) {
                return address;
            }
            System.out.println("Invalid address. It must be at least 5 characters long and contain both letters and numbers.");
        }
    }

    private String getValidBirthDate() {
        while (true) {
            System.out.print("\nEnter date of birth (dd/MM/yyyy): ");
            String birthDateStr = scanner.nextLine().trim();
            try {
                LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                int age = Period.between(birthDate, LocalDate.now()).getYears();
                if (age >= 16 && age <= 85) {
                    return birthDateStr;
                }
                System.out.println("Invalid age. User must be between 16 and 85 years old.");
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use dd/MM/yyyy.");
            }
        }
    }

    private String getValidCPF() {
        while (true) {
            System.out.print("\nEnter CPF (only numbers): ");
            String cpf = scanner.nextLine().trim();
            if (isValidCpfFormat(cpf)) {
                return cpf;
            }
            System.out.println("Invalid CPF. It must contain exactly 11 digits.");
        }
    }

    private String getValidUsername() {
        while (true) {
            System.out.print("\nChoose a username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please choose another.");
            } else if (dbManager.usernameJaExiste(username)) {
                System.out.println("Already existing username. Please choose another.");
            } else {
                return username;
            }
        }
    }

    private String getValidPassword() {
        while (true) {
            System.out.print("\nChoose a password: ");
            String password = this.scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.");
                continue;
            }
            if (password.length() >= 6) {
                return password;
            }
            System.out.println("Password too short. It must be at least 6 characters long.");
        }
    }

    private int calculateAge(String birthDateStr) {
        LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private boolean isValidName(String name) {
        return name != null && Pattern.matches("^[a-zA-ZÀ-ÿ\\s]{4,}$", name);
    }

    private boolean isValidAddress(String address) {
        return address != null && address.length() >= 5 &&
                address.matches(".*[a-zA-Z].*") &&
                address.matches(".*\\d.*");
    }
}