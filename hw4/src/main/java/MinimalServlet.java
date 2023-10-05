
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;
import java.util.UUID;

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
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("sessionUUID")) {
                    UUID sessionUUID = UUID.fromString(cookie.getValue());
                    String sqlCheckSession = "SELECT user_id FROM user_sessions WHERE session_uuid = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCheckSession)) {
                        preparedStatement.setObject(1, sessionUUID);
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                int userId = resultSet.getInt("user_id");

                                User user = findUserById(userId);
                                if (user != null) {
                                    HttpSession session = request.getSession();
                                    session.setAttribute("user", user);
                                    response.sendRedirect("/html/dashboard.html");
                                    return;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        request.getRequestDispatcher("/html/profile.html").forward(request, response);
    }
    public User findUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
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


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("name");
        String secondName = request.getParameter("surname");
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try {
            String sqlInsertUser = "INSERT INTO users (first_name, second_name, login, password) VALUES (?, ?, ?, ?) RETURNING id";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertUser);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, secondName);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");


                UUID sessionUUID = UUID.randomUUID();


                Cookie sessionCookie = new Cookie("sessionUUID", sessionUUID.toString());
                response.addCookie(sessionCookie);


                String sqlInsertSession = "INSERT INTO user_sessions (user_id, session_uuid) VALUES (?, ?)";
                PreparedStatement preparedStatementSession = connection.prepareStatement(sqlInsertSession);
                preparedStatementSession.setInt(1, userId);
                preparedStatementSession.setObject(2, sessionUUID);
                preparedStatementSession.executeUpdate();
                response.sendRedirect("/html/dashboard.html");
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

