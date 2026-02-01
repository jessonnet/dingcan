import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ResetPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码: " + encodedPassword);

        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("验证结果: " + matches);

        // 测试现有的加密密码
        String existingPassword = "$2a$10$C3ToOUoMv/Ou2siuvQ/rFetTby.zfN3WA9tjwoCBE.S3M.o0/JBxW";
        boolean existingMatches = encoder.matches(password, existingPassword);
        System.out.println("现有密码验证结果: " + existingMatches);
    }
}
