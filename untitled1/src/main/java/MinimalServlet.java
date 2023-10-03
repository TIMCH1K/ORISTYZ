
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

@WebServlet("/reg")
public class MinimalServlet extends HttpServlet {
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pKMFr7CW";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/db1";
    private Connection connection;

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String login = request.getParameter("login");
        if (login != null) {
            try {
                User user = findByLogin(login);
                if (user != null) {
                    System.out.println("User found: " + user.getFirstName() + " " + user.getLastName());
                } else {
                    System.out.println("No user found with login: " + login);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            request.getRequestDispatcher("/html/profile.html").forward(request, response);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("name");
        String secondName = request.getParameter("surname");
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try {
            String sqlInsertUser = "INSERT INTO users (first_name, second_name, login, password) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertUser);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, secondName);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(firstName + " " + secondName + " " + login + " " + password);
    }

    public User findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getLong("id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("second_name"),
                            resultSet.getString("login"),
                            resultSet.getString("password")
                    );
                    return user;
                }
            }
        }
        return null;
    }
}

