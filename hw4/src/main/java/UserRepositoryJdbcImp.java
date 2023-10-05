
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryJdbcImp implements UserRepository {
    private Connection connection;

    private static final String SQL_SELECT_ALL_FROM_DRIVER = "select * from driver";

    public UserRepositoryJdbcImp(Connection connection) {
        this.connection = connection;
    }
    @Override
    public List<User> findAllByAge(Integer age) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL_FROM_DRIVER);
            List<User> result = new ArrayList<>();

            while (resultSet.next()) {
                User user = new User(
                        resultSet.getLong("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getInt("age")
                );
                if (user.getAge().equals(age)) {
                    result.add(user);
                }
                if (result.isEmpty()) {
                    System.out.println("По введенному возврасту ничего не найдено...");
                }
                for (int i = 0; i < result.size(); i++) {
                    System.out.println(user.getFirstName() + " " + user.getLastName());
                }
                return result;
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
