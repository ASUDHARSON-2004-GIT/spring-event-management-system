package com.EventManagementSystem.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EventManagementSystem.exception.ValidationException;
import com.EventManagementSystem.model.Category;
import com.EventManagementSystem.repository.CategoryRepository;
import com.EventManagementSystem.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }
        Category category = new Category(name, description);
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found with id " + id));
    }

    @Override
    public List<Category> listCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void updateCategory(long id, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found with id " + id));
        category.setName(name);
        category.setDescription(description);
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found with id " + id));
        categoryRepository.delete(category);
    }
}
