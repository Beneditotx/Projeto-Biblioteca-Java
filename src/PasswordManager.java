import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {

    public static String hashPassword(String plainTextPassword){
        if(plainTextPassword == null || plainTextPassword.isEmpty()){
            throw  new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }else{
            return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        }
    }

    public static boolean checkPassword(String plaintextPassoword, String hashedPassword){
        if(plaintextPassoword == null || plaintextPassoword.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()){
            return false;
        }
        try{
            return BCrypt.checkpw(plaintextPassoword, hashedPassword);
        }catch (IllegalArgumentException e){
            System.err.println("Erro  ao verificar a senha (formato de hash inválido?): " + e.getMessage());
            return false;
        }
    }

}

