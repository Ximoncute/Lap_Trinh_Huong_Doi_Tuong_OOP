package moneymate.repository;

import moneymate.exception.DatabaseException;
import java.util.List;

/**
 * Interface Generic IRepository đại diện cho Repository Pattern.
 * @param <T> Kiểu của đối tượng thực thể
 */
public interface IRepository<T> {
    List<T> getAll() throws DatabaseException;
    T getById(int id) throws DatabaseException;
    void add(T entity) throws DatabaseException;
    void update(T entity) throws DatabaseException;
    void delete(int id) throws DatabaseException;
}
