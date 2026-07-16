package moneymate.service;

import moneymate.exception.DatabaseException;
import moneymate.model.Category;
import moneymate.repository.CategoryRepository;
import java.util.List;

public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService() {
        this.repository = new CategoryRepository();
    }

    public List<Category> getAllCategories() throws DatabaseException {
        return repository.getAll();
    }

    public Category getCategoryById(int id) throws DatabaseException {
        return repository.getById(id);
    }

    public void addCategory(Category category) throws DatabaseException {
        repository.add(category);
    }

    public void updateCategory(Category category) throws DatabaseException {
        repository.update(category);
    }

    public void deleteCategory(int id) throws DatabaseException {
        repository.delete(id);
    }
}
