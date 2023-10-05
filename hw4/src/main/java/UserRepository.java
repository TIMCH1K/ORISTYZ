import java.util.List;

public interface UserRepository <T> {
    List<T> findAllByAge(Integer age);
}